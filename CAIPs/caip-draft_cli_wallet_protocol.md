---
caip: CAIP-X
title: CLI Wallet Protocol
author: Derek Rein (@arein)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/396
status: Draft
type: Standard
created: 2026-02-23
updated: 2026-02-23
requires: [2, 10]
---

## Simple Summary

A standard protocol for CLI applications to discover and interact with wallet providers through executable plugins on PATH, inspired by git credential helpers and [EIP-6963][] browser wallet discovery.

## Abstract

CLI Wallet Protocol (CWP) defines a convention for wallet providers to expose signing and account capabilities to command-line tools. Wallet providers ship executables named `wallet-<name>` that implement a small set of JSON-based operations (info, accounts, sign-message, sign-typed-data, sign-transaction, send-transaction). A central `wallet` orchestrator discovers providers on PATH and delegates operations to them. This decouples CLI tools that need wallet functionality from specific wallet implementations, enabling hardware wallets, browser extensions, cloud signers, and local keystores to participate equally.

## Motivation

CLI-based blockchain tools (API clients, deployment scripts, AI agents) increasingly need wallet interaction — signing transactions, authorizing payments, proving identity. Today, each tool hardcodes support for a specific wallet provider (e.g., WalletConnect, Ledger, local keystore), creating tight coupling that limits user choice and increases integration burden.

Browser-based ecosystems solved this with [EIP-6963][] (Multi Injected Provider Discovery), allowing dApps to discover all available wallets without hardcoding. No equivalent exists for the CLI environment.

The git ecosystem provides a compelling model: `git credential-<name>` helpers allow any credential storage backend to participate in authentication flows without git itself knowing the details. CWP applies this pattern to wallet operations.

Without a standard:
- Each CLI tool must independently integrate each wallet provider
- Users cannot choose their preferred wallet for CLI operations
- New wallet providers must convince each CLI tool to add support
- Hardware wallet users are often excluded from CLI workflows entirely

## Specification

### Binary Naming Convention

Wallet providers MUST ship an executable named `wallet-<name>` where `<name>` is a lowercase identifier using only `[a-z0-9-]` characters. The executable MUST be placed on the user's PATH.

Examples: `wallet-walletconnect`, `wallet-ledger`, `wallet-cast`, `wallet-1password`

### Communication Pattern

All operations follow the same pattern:

```
wallet-<name> <operation>
```

- **Input**: JSON on stdin (avoids shell escaping issues with complex data)
- **Output**: JSON on stdout
- **Status/Progress**: stderr only (MUST NOT write non-JSON to stdout)
- **Exit codes**: Semantic (see [Exit Codes](#exit-codes))

Providers MUST be stateless between invocations. Session state (if needed) MUST be persisted to the filesystem.

### Operations

#### `info`

Returns provider metadata and capabilities. MUST complete within 3 seconds.

**Input**: None (stdin is empty)

**Output**:
```json
{
  "name": "walletconnect",
  "version": "1.0.0",
  "rdns": "com.walletconnect.cli",
  "capabilities": ["accounts", "sign-typed-data"],
  "chains": ["eip155"]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | string | Yes | Human-readable provider name |
| `version` | string | Yes | Provider version (semver) |
| `rdns` | string | No | Reverse domain identifier per [EIP-6963][] |
| `capabilities` | string[] | Yes | Supported operations (see [Capabilities](#capabilities)) |
| `chains` | string[] | Yes | Supported chain namespaces per [CAIP-2][] (e.g., `eip155`, `solana`, `cosmos`) |

#### `accounts`

Returns available accounts. MUST complete within 10 seconds.

**Input** (stdin):
```json
{
  "chain": "eip155"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `chain` | string | No | Filter accounts by chain namespace. If omitted, return all accounts. |

**Output**:
```json
{
  "accounts": [
    {
      "address": "0x1234...abcd",
      "chain": "eip155:1",
      "name": "My Wallet"
    }
  ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `accounts[].address` | string | Yes | Account address |
| `accounts[].chain` | string | Yes | [CAIP-2][] chain identifier |
| `accounts[].name` | string | No | Human-readable account name |

#### `sign-message`

Signs a plaintext message. MUST complete within 120 seconds (allows for hardware wallet interaction).

**Input** (stdin):
```json
{
  "account": "0x1234...abcd",
  "message": "Hello, world!",
  "chain": "eip155:1"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account` | string | Yes | Signing account address |
| `message` | string | Yes | Message to sign |
| `chain` | string | Yes | [CAIP-2][] chain identifier |

**Output**:
```json
{
  "signature": "0x..."
}
```

#### `sign-typed-data`

Signs [EIP-712][] typed structured data. MUST complete within 120 seconds.

**Input** (stdin):
```json
{
  "account": "0x1234...abcd",
  "typedData": {
    "types": { ... },
    "primaryType": "...",
    "domain": { ... },
    "message": { ... }
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account` | string | Yes | Signing account address |
| `typedData` | object | Yes | [EIP-712][] typed data object |

**Output**:
```json
{
  "signature": "0x..."
}
```

#### `sign-transaction`

Signs a transaction without broadcasting. MUST complete within 120 seconds.

**Input** (stdin):
```json
{
  "account": "0x1234...abcd",
  "transaction": {
    "to": "0x...",
    "value": "0x0",
    "data": "0x..."
  },
  "chain": "eip155:1"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account` | string | Yes | Signing account address |
| `transaction` | object | Yes | Transaction object (chain-specific format) |
| `chain` | string | Yes | [CAIP-2][] chain identifier |

**Output**:
```json
{
  "signedTransaction": "0x..."
}
```

#### `send-transaction`

Signs and broadcasts a transaction. MUST complete within 180 seconds.

**Input** (stdin):
```json
{
  "account": "0x1234...abcd",
  "transaction": {
    "to": "0x...",
    "value": "0x0",
    "data": "0x..."
  },
  "chain": "eip155:1"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account` | string | Yes | Signing account address |
| `transaction` | object | Yes | Transaction object (chain-specific format) |
| `chain` | string | Yes | [CAIP-2][] chain identifier |

**Output**:
```json
{
  "transactionHash": "0x..."
}
```

### Capabilities

Providers declare supported operations in the `info` response. Valid capability values:

| Capability | Operation |
|------------|-----------|
| `accounts` | `accounts` |
| `sign-message` | `sign-message` |
| `sign-typed-data` | `sign-typed-data` |
| `sign-transaction` | `sign-transaction` |
| `send-transaction` | `send-transaction` |

Orchestrators SHOULD check capabilities before dispatching operations.

### Exit Codes

| Code | Constant | Description |
|------|----------|-------------|
| 0 | `SUCCESS` | Operation completed successfully |
| 1 | `GENERAL_ERROR` | Unspecified error |
| 2 | `UNSUPPORTED` | Operation not supported by this provider |
| 3 | `REJECTED` | User rejected the operation |
| 4 | `TIMEOUT` | Operation timed out |
| 5 | `NOT_CONNECTED` | No wallet session/connection active |

### Error Output

On non-zero exit, providers MUST write a JSON error object to stdout:

```json
{
  "error": "User rejected the signing request",
  "code": "USER_REJECTED"
}
```

Standard error codes:

| Code | Description |
|------|-------------|
| `UNSUPPORTED_OPERATION` | Operation not supported |
| `USER_REJECTED` | User declined the request |
| `TIMEOUT` | Operation exceeded time limit |
| `NOT_CONNECTED` | No active wallet connection |
| `ACCOUNT_NOT_FOUND` | Requested account not available |
| `INVALID_INPUT` | Malformed input JSON |
| `INTERNAL_ERROR` | Provider internal error |

### Discovery

An orchestrator (`wallet` CLI) discovers providers by:

1. Scanning PATH for executables matching `wallet-*`
2. Calling `wallet-<name> info` on each discovered binary (3 second timeout, in parallel)
3. Deduplicating by name (first match on PATH wins)
4. Optionally reading `~/.config/wallet/config.json` for user preferences:

```json
{
  "default": "walletconnect",
  "disabled": ["cast"],
  "priority": ["ledger", "walletconnect"]
}
```

### Orchestrator CLI

The `wallet` CLI is the user-facing orchestrator. It discovers providers and delegates:

```bash
wallet list                              # Show all discovered providers
wallet accounts [--wallet <name>]        # List accounts
wallet sign-message [--wallet <name>]    # Sign message (JSON on stdin)
wallet sign-typed-data [--wallet <name>] # Sign EIP-712 typed data (JSON on stdin)
wallet sign-transaction [--wallet <name>]
wallet send-transaction [--wallet <name>]
```

When `--wallet` is not specified, the orchestrator SHOULD use the default provider from config, or the first available provider.

## Rationale

### Why PATH-based discovery?

Following git's credential helper pattern, PATH-based discovery is the simplest mechanism that works across all operating systems and shells. It requires no registry, no daemon process, and no configuration file. Users can install providers with their package manager of choice.

### Why stdin/stdout JSON?

Command-line arguments have length limits and shell escaping complexity, especially for structured data like EIP-712 typed data. JSON on stdin/stdout avoids these issues while remaining easy to implement in any language. stderr is reserved for human-readable progress/status messages, keeping stdout clean for machine consumption.

### Why semantic exit codes?

Different error conditions require different handling. A timeout (code 4) might warrant a retry, while an unsupported operation (code 2) should fall back to a different provider. Binary success/failure is insufficient for orchestration.

### Why not a daemon/socket protocol?

A daemon adds complexity (lifecycle management, port conflicts, authentication) that most CLI wallet interactions don't need. Operations are infrequent and short-lived — spawning a process per operation is acceptable. Providers that need persistent connections (e.g., WalletConnect) manage their own connection state internally.

### Relation to EIP-6963

[EIP-6963][] defines wallet discovery for browser environments. CWP is the CLI analog — same goal (pluggable wallet discovery), different mechanism (PATH scanning vs window events). CWP reuses `rdns` identifiers from EIP-6963 for cross-environment wallet identity.

## Test Cases

### Provider Discovery

Given `wallet-foo` and `wallet-bar` on PATH:
1. `wallet list` returns both providers with their `info` output
2. If `wallet-foo info` times out (>3s), it is excluded from results
3. If `wallet-foo` is in `disabled` config, it is excluded

### Operation Delegation

Given a provider `wallet-test` that supports `accounts` and `sign-typed-data`:
1. `echo '{}' | wallet accounts --wallet test` returns accounts
2. `echo '{"account":"0x...","typedData":{...}}' | wallet sign-typed-data --wallet test` returns signature
3. `echo '{}' | wallet sign-message --wallet test` returns exit code 2 (unsupported)

### Error Handling

1. Provider exits with code 3 → orchestrator reports "user rejected"
2. Provider exits with code 5 → orchestrator reports "not connected"
3. Provider writes invalid JSON to stdout → orchestrator reports internal error

## Security Considerations

- **PATH injection**: Malicious binaries named `wallet-*` on PATH could intercept signing requests. Users SHOULD audit their PATH and verify provider authenticity. Package managers provide the primary trust anchor.
- **Stdin/stdout interception**: On multi-user systems, process stdin/stdout may be observable. Providers SHOULD avoid passing raw private keys through the protocol. The protocol is designed for signing delegation, not key export.
- **Timeout enforcement**: Orchestrators MUST enforce timeouts to prevent providers from hanging indefinitely, which could be used for denial-of-service.
- **Input validation**: Providers MUST validate all JSON input. Orchestrators MUST validate all JSON output. Neither should trust the other's output format without verification.

## Privacy Considerations

- **Account enumeration**: The `accounts` operation exposes all available accounts. Providers MAY require user confirmation before returning account lists.
- **Provider discovery**: `wallet list` reveals which wallet software a user has installed. On shared systems, this could be sensitive information.
- **Transaction data**: Signing operations pass full transaction data through stdin. This data is not encrypted in transit between orchestrator and provider processes.

## Backwards Compatibility

This is a new protocol with no prior standard to break compatibility with. Tools that currently hardcode specific wallet providers can adopt CWP incrementally by:

1. Creating a `wallet-<provider>` adapter for their existing integration
2. Updating their tool to prefer the `wallet` orchestrator when available
3. Falling back to the direct integration when `wallet` is not installed

## References

- [EIP-6963][] — Multi Injected Provider Discovery
- [EIP-712][] — Typed structured data hashing and signing
- [CAIP-2][] — Blockchain ID Specification
- [CAIP-10][] — Account ID Specification
- [git-credential][] — Git credential helper protocol

[EIP-6963]: https://eips.ethereum.org/EIPS/eip-6963
[EIP-712]: https://eips.ethereum.org/EIPS/eip-712
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[git-credential]: https://git-scm.com/docs/gitcredentials

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
