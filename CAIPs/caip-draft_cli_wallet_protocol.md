---
caip: CAIP-X
title: CLI Wallet Protocol
author: Derek Rein (@arein)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/396
status: Draft
type: Standard
created: 2026-02-23
updated: 2026-02-23
requires: [2, 10, 171]
---

## Simple Summary

A standard protocol for CLI applications to discover and interact with wallet providers through executable plugins on PATH, inspired by git credential helpers and [EIP-6963][] browser wallet discovery.

## Abstract

CLI Wallet Protocol (CWP) defines a convention for wallet providers to expose signing and account capabilities to command-line tools. Wallet providers ship executables named `wallet-<name>` that implement a small set of JSON-based operations (info, accounts, sign-message, sign-typed-data, sign-transaction, send-transaction). A central `wallet` orchestrator discovers providers on PATH and delegates operations to them. This decouples CLI tools that need wallet functionality from specific wallet implementations, enabling hardware wallets, browser extensions, cloud signers, and local keystores to participate equally.

CWP also defines a session mechanism for autonomous agent use cases. A human approves a scoped permission envelope once via `grant-session`, then subsequent operations within those bounds execute without human approval. Session identifiers follow [CAIP-171][], and the permission model draws from [EIP-7715][] (Grant Permissions from Wallets).

## Motivation

CLI-based blockchain tools (API clients, deployment scripts, AI agents) increasingly need wallet interaction — signing transactions, authorizing payments, proving identity. Today, each tool hardcodes support for a specific wallet provider (e.g., WalletConnect, Ledger, local keystore), creating tight coupling that limits user choice and increases integration burden.

Browser-based ecosystems solved this with [EIP-6963][] (Multi Injected Provider Discovery), allowing dApps to discover all available wallets without hardcoding. No equivalent exists for the CLI environment.

The git ecosystem provides a compelling model: `git credential-<name>` helpers allow any credential storage backend to participate in authentication flows without git itself knowing the details. CWP applies this pattern to wallet operations.

Without a standard:
- Each CLI tool must independently integrate each wallet provider
- Users cannot choose their preferred wallet for CLI operations
- New wallet providers must convince each CLI tool to add support
- Hardware wallet users are often excluded from CLI workflows entirely

AI agents represent a particularly acute need. An autonomous agent managing funds or signing transactions cannot block on human approval for every operation — the 120-second hardware wallet timeouts assume a human is present. Yet unrestricted auto-approval (`--yes` flags) offers no guardrails. Sessions bridge this gap: a human pre-authorizes a scoped set of operations (e.g., "spend up to 0.1 ETH on this contract for the next hour"), and the agent operates autonomously within those bounds. This follows the principle of least privilege while enabling practical autonomy, similar to [EIP-7715][]'s approach for browser wallets.

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
| `sessionId` | string | No | Session identifier. When present, operation executes without human approval if within session bounds. |

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
| `sessionId` | string | No | Session identifier. When present, operation executes without human approval if within session bounds. |

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
| `sessionId` | string | No | Session identifier. When present, operation executes without human approval if within session bounds. |

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
| `sessionId` | string | No | Session identifier. When present, operation executes without human approval if within session bounds. |

**Output**:
```json
{
  "transactionHash": "0x..."
}
```

#### `grant-session`

Creates a scoped permission session for autonomous operation. MUST complete within 120 seconds (requires human approval to authorize the session).

**Input** (stdin):
```json
{
  "account": "0x1234...abcd",
  "chain": "eip155:1",
  "permissions": [
    {
      "type": "native-token-transfer",
      "data": {},
      "policies": [
        { "type": "value-limit", "limit": "100000000000000000" },
        { "type": "rate-limit", "count": 10, "interval": 3600 }
      ]
    }
  ],
  "expiry": 1700000000,
  "metadata": {
    "agent": "deployment-bot",
    "description": "Automated gas payments for contract deployments"
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `account` | string | Yes | Account to authorize |
| `chain` | string | Yes | [CAIP-2][] chain identifier |
| `permissions` | object[] | Yes | Requested permissions (see [Permission Types](#permission-types)) |
| `permissions[].type` | string | Yes | Permission type identifier |
| `permissions[].data` | object | Yes | Permission-specific parameters |
| `permissions[].policies` | object[] | No | Constraints on this permission (see [Policy Types](#policy-types)) |
| `expiry` | number | Yes | Unix timestamp for session expiration |
| `metadata` | object | No | Optional context for the wallet approval UI |
| `metadata.agent` | string | No | Name of the requesting agent |
| `metadata.description` | string | No | Human-readable description of intended use |

**Output**:
```json
{
  "sessionId": "cwp_s_a1b2c3d4e5f6...",
  "permissions": [ ... ],
  "expiry": 1700000000
}
```

| Field | Type | Description |
|-------|------|-------------|
| `sessionId` | string | [CAIP-171][] compliant session identifier (minimum 96 bits entropy) |
| `permissions` | object[] | Granted permissions (wallet MAY attenuate downward, MUST NOT escalate beyond requested) |
| `expiry` | number | Granted expiry (wallet MAY shorten, MUST NOT extend beyond requested) |

#### `revoke-session`

Revokes an active session. MUST complete within 10 seconds. Does not require human approval.

**Input** (stdin):
```json
{
  "sessionId": "cwp_s_a1b2c3d4e5f6..."
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | string | Yes | Session to revoke |

**Output**:
```json
{
  "revoked": true
}
```

#### `get-session`

Queries the current state of a session. MUST complete within 5 seconds. Does not require human approval.

**Input** (stdin):
```json
{
  "sessionId": "cwp_s_a1b2c3d4e5f6..."
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `sessionId` | string | Yes | Session to query |

**Output**:
```json
{
  "sessionId": "cwp_s_a1b2c3d4e5f6...",
  "account": "0x1234...abcd",
  "chain": "eip155:1",
  "status": "active",
  "permissions": [
    {
      "type": "native-token-transfer",
      "data": {},
      "policies": [
        { "type": "value-limit", "limit": "100000000000000000" },
        { "type": "rate-limit", "count": 10, "interval": 3600, "remaining": 7 }
      ]
    }
  ],
  "expiry": 1700000000
}
```

| Field | Type | Description |
|-------|------|-------------|
| `sessionId` | string | Session identifier |
| `account` | string | Authorized account |
| `chain` | string | [CAIP-2][] chain identifier |
| `status` | string | One of: `active`, `expired`, `revoked` |
| `permissions` | object[] | Granted permissions with current state |
| `permissions[].policies[].remaining` | number | Remaining quota for count-based policies (present only for `rate-limit` and `call-limit`) |
| `expiry` | number | Session expiry timestamp |

### Permission Types

Each permission in a session grant specifies a `type` and type-specific `data`:

| Type | Description | `data` Fields |
|------|-------------|---------------|
| `native-token-transfer` | Transfer native token (e.g., ETH) | `allowance` (total wei, optional) |
| `token-transfer` | Transfer fungible token | `contract` (token address), `allowance` (total units, optional) |
| `sign-message` | Sign arbitrary messages | — |
| `sign-typed-data` | Sign [EIP-712][] typed data | — |
| `sign-transaction` | Sign transactions without broadcast | — |
| `send-transaction` | Sign and broadcast transactions | — |
| `contract-call` | Call specific contract methods | `contract` (address), `methods` (string[], optional), `allowance` (value cap in wei, optional) |

Custom permission types use reverse-domain notation (e.g., `com.example.custom-permission`). Providers MUST reject unknown permission types rather than silently ignoring them.

### Policy Types

Policies constrain how a permission may be used. Multiple policies on the same permission AND-combine (all must be satisfied):

| Type | Description | Fields |
|------|-------------|--------|
| `rate-limit` | Maximum operations per time window | `count` (number), `interval` (seconds) |
| `call-limit` | Maximum total operations for session lifetime | `count` (number) |
| `value-limit` | Maximum value per individual operation | `limit` (string, wei or smallest unit) |
| `recipient-allowlist` | Restrict destination addresses | `addresses` (string[]) |

Custom policy types use reverse-domain notation (e.g., `com.example.custom-policy`). Providers MUST reject unknown policy types rather than silently ignoring them.

### Capabilities

Providers declare supported operations in the `info` response. Valid capability values:

| Capability | Operation |
|------------|-----------|
| `accounts` | `accounts` |
| `sign-message` | `sign-message` |
| `sign-typed-data` | `sign-typed-data` |
| `sign-transaction` | `sign-transaction` |
| `send-transaction` | `send-transaction` |
| `grant-session` | `grant-session` |
| `revoke-session` | `revoke-session` |
| `get-session` | `get-session` |

Providers that declare `grant-session` MUST also declare `revoke-session` and `get-session`.

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
| 6 | `SESSION_ERROR` | Session-related error (see error code in JSON body for specifics) |

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
| `SESSION_NOT_FOUND` | Session ID not recognized or already revoked |
| `SESSION_EXPIRED` | Session has passed its expiry timestamp |
| `PERMISSION_DENIED` | Operation not covered by session permissions |
| `ALLOWANCE_EXCEEDED` | Operation would exceed session spending allowance |
| `RATE_LIMIT_EXCEEDED` | Operation would exceed rate or call limit policy |

All session-related error codes (`SESSION_*`, `PERMISSION_DENIED`, `ALLOWANCE_EXCEEDED`, `RATE_LIMIT_EXCEEDED`) use exit code 6 (`SESSION_ERROR`).

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
wallet grant-session [--wallet <name>]   # Create scoped session (JSON on stdin)
wallet revoke-session [--wallet <name>]  # Revoke session (JSON on stdin)
wallet get-session [--wallet <name>]     # Query session state (JSON on stdin)
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

### Why sessions instead of auto-approve flags?

A `--yes` or `--auto-approve` flag is binary: either everything requires human approval, or nothing does. Sessions provide scoped, auditable autonomy — a human sees exactly what permissions an agent will have, for how long, with what spending limits. The session grant creates an explicit authorization record, and `get-session` provides an audit trail of remaining capacity. This is strictly superior to silent auto-approval for security, compliance, and debuggability.

### Why off-chain session enforcement?

Session permissions are enforced by the wallet provider process, not by on-chain smart contracts. This is intentional: CWP is chain-agnostic, and not all chains support on-chain permission systems like [EIP-7710][]. The provider is already the trust boundary in CWP (it holds or mediates access to keys), so enforcing session bounds at the provider is consistent with the protocol's trust model. On-chain enforcement (e.g., via smart account session keys) can be layered on top by providers that support it.

### Why one session per account per chain?

Allowing unbounded concurrent sessions per account increases blast radius. If an agent is compromised, limiting it to one session means only that session's permissions are at risk. Providers that need multiple concurrent sessions (e.g., multiple agents operating on the same account) MAY support this, but SHOULD warn the user. The default of one-session-per-pair keeps the common case simple and safe.

### Why does get-session exist?

Without `get-session`, agents must maintain their own shadow accounting of remaining allowances and rate limits. Shadow accounting inevitably drifts from provider state (e.g., after a crash, or if another process uses the session). `get-session` provides a canonical view of session state, eliminating an entire class of accounting bugs. It also enables monitoring tools to display active sessions without accessing provider internals.

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

### Session Lifecycle

Given a provider `wallet-test` that supports `grant-session`:

1. Grant a session with `native-token-transfer` permission, `value-limit` of 0.1 ETH, `rate-limit` of 5 per hour, expiry in 1 hour → returns `sessionId`, permissions (possibly attenuated), expiry (possibly shortened)
2. `send-transaction` with `sessionId` for 0.05 ETH → succeeds without human approval
3. `get-session` → shows `remaining: 4` on rate-limit policy
4. 5 more `send-transaction` calls in quick succession → 5th returns exit code 6 with `RATE_LIMIT_EXCEEDED`
5. `send-transaction` for 0.2 ETH (exceeds value-limit) → returns exit code 6 with `ALLOWANCE_EXCEEDED`
6. Wait until session expires → `send-transaction` with `sessionId` returns exit code 6 with `SESSION_EXPIRED`
7. `revoke-session` on an active session → returns `{ "revoked": true }`; subsequent operations with that `sessionId` return exit code 6 with `SESSION_NOT_FOUND`

### Session Unsupported Provider

Given a provider `wallet-basic` that does NOT support `grant-session`:
1. `wallet grant-session --wallet basic` returns exit code 2 (`UNSUPPORTED`)
2. All signing operations without `sessionId` continue to work normally with human approval

### Permission Attenuation

1. Request `native-token-transfer` with `value-limit` of 10 ETH → wallet MAY grant with `value-limit` of 1 ETH (attenuated downward)
2. Request `expiry` of 1 week → wallet MAY grant with `expiry` of 24 hours (shortened)
3. Request `contract-call` permission → wallet MUST NOT add `sign-message` permission that was not requested

## Security Considerations

- **PATH injection**: Malicious binaries named `wallet-*` on PATH could intercept signing requests. Users SHOULD audit their PATH and verify provider authenticity. Package managers provide the primary trust anchor.
- **Stdin/stdout interception**: On multi-user systems, process stdin/stdout may be observable. Providers SHOULD avoid passing raw private keys through the protocol. The protocol is designed for signing delegation, not key export.
- **Timeout enforcement**: Orchestrators MUST enforce timeouts to prevent providers from hanging indefinitely, which could be used for denial-of-service.
- **Input validation**: Providers MUST validate all JSON input. Orchestrators MUST validate all JSON output. Neither should trust the other's output format without verification.

### Session Security

- **Session tokens as bearer tokens**: A `sessionId` grants the holder the ability to execute operations without human approval. Session tokens MUST be treated with the same care as API keys: stored with restrictive file permissions (0600), never passed as command-line arguments (visible in `ps` output), and never logged. Orchestrators SHOULD store session state in `~/.config/wallet/sessions/` with appropriate permissions.
- **Minimum-viable permissions**: Callers SHOULD request the narrowest permissions and shortest expiry sufficient for their task. Wallets SHOULD encourage this by displaying the requested scope prominently during human approval.
- **One session per (account, chain) pair**: Providers SHOULD enforce at most one active session per account per chain. If a new `grant-session` is requested for an account/chain pair with an existing session, the provider SHOULD warn the user and revoke the existing session upon approval of the new one. This limits blast radius.
- **Expiry enforcement**: Providers MUST check session expiry against the local system clock before every operation. Providers SHOULD default to a maximum session lifetime of 24 hours even if the caller requests longer. Clock skew between orchestrator and provider processes is negligible (same machine).
- **Immediate revocation**: `revoke-session` MUST take effect immediately. Any in-flight operations that began before revocation MAY complete, but no new operations may begin.
- **Failed transaction accounting**: Allowances MUST NOT be decremented for operations that fail (e.g., reverted transactions). Only successful operations count against session limits.
- **Concurrent session warnings**: If a provider supports multiple concurrent sessions for the same account, it MUST warn the user during `grant-session` approval that multiple active sessions exist.
- **Atomic persistence**: Session state MUST be persisted atomically to the filesystem (write to temp file, then rename) with file locking to prevent corruption from concurrent access.

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
- [CAIP-171][] — Session Identifiers
- [EIP-7715][] — Grant Permissions from Wallets
- [EIP-7710][] — Smart Contract Delegation
- [git-credential][] — Git credential helper protocol

[EIP-6963]: https://eips.ethereum.org/EIPS/eip-6963
[EIP-712]: https://eips.ethereum.org/EIPS/eip-712
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[CAIP-171]: https://ChainAgnostic.org/CAIPs/caip-171
[EIP-7715]: https://eips.ethereum.org/EIPS/eip-7715
[EIP-7710]: https://eips.ethereum.org/EIPS/eip-7710
[git-credential]: https://git-scm.com/docs/gitcredentials

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
