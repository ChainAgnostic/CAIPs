---
caip: 25
title: Wallet Create Session JSON-RPC Method
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88), Alex Donesky (@adonesky1), Jiexi Luan (@jiexi)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/25
status: Review
type: Standard
created: 2020-10-14
updated: 2025-08-04
requires: 2, 10, 171, 217, 285, 311, 312
---

## Simple Summary

CAIP-25 defines an authorization procedure for a chain agnostic provider to interface with a wallet as part of their initialization and/or "handshake" protocol.

## Abstract

This proposal defines a standard procedure for decentralized applications to interface with chain agnostic cryptocurrency wallets and other user agents that govern identities (including accounts) across multiple cryptographic systems. It specifies a lightweight protocol for negotiating and persisting authorizations during a session managed either by an in-DOM provider construct, by a securely-addressed browser extensions, or by a distinct user-agent.

## Motivation

The absence of standardized interfaces and abstractions for reading from and writing to blockchains such as consistent account models and JSON-RPC method specifications has fragmented application and wallet interactions. CAIP-25 resolves this by defining a unified, session-based interface that standardizes communication between applications and wallets.

## Specification

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" are interpreted as described in [RFC-2119][].

### Definition

#### Session Lifecycle

The session is proposed by a caller and accepted by the respondent. The respondent may return a `sessionId` which both parties then persist along with session properties and authorization scopes. See [CAIP-316][] for guidance on session lifecycles with and without `sessionId`s.

If a wallet does not return a `sessionId`, it MUST track session data internally. The caller is not required to persist any session state but may query or revoke sessions via [`wallet_getSession`][CAIP-312], [`wallet_revokeSession`][CAIP-285], or receive updates via [`wallet_sessionChanged`][CAIP-311].

Subsequent `wallet_createSession` calls may:

- Update an existing session by including the same `sessionId`
- Create a new session if no `sessionId` is provided (discouraged)

Session updates initiated by the wallet must notify the caller using `wallet_sessionChanged`.

Callers may revoke sessions using `wallet_revokeSession`, passing the `sessionId` parameter if it was returned by the initial response.

#### Session Data and Metadata

Authorization requests are expressed as a top-level object `scopes` containing keyed [scopeObjects][CAIP-217].

Each `scopeObject` is keyed by a [CAIP-2][] chain ID.

Wallets MAY authorize a subset of scopes. This enables granular control and flexibility.

Upon successful negotiation, the response includes a unified `scopes` object containing all granted scopes. Identically-keyed `scopeObjects` from multiple requests MUST be merged. No duplicate keys are allowed.

Respondents MUST NOT restructure scope formats (e.g., converting chain-specific keys into namespace-wide keys).

If a connection is rejected, the wallet MAY respond with a generic error or silently ignore the request to minimize fingerprinting risk (see Privacy Considerations).

#### Request

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_createSession",
  "params": {
    "scopes": {
      "eip155:1": {
        "methods": ["eth_sendTransaction", "personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:8453": {
        "methods": ["eth_sendTransaction", "personal_sign", "wallet_sendCalls"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:42161": {
        "methods": ["eth_sendTransaction", "personal_sign", "wallet_sendCalls"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:0": {
        "methods": ["wallet_grantPermissions"],
        "notifications": []
      },
      "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp": {
        "methods": [
          "solana_signMessage",
          "solana_signTransaction",
          "solana_signAndSendTransaction"
        ],
        "notifications": []
      },
      "solana:4uhcVJyU9pJkvQyS88uRDiswHXSCkY3z": {
        "methods": [
          "solana_signMessage",
          "solana_signTransaction",
          "solana_signAndSendTransaction"
        ],
        "notifications": []
      }
    },
    "properties": {
      "expiry": "2022-12-24T17:07:31+00:00"
    }
  }
}
```

The `scopes` object MUST contain one or more scopeObjects.

The `properties` object MAY be included for global session metadata.

### Response

#### Success

A successful response includes:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "scopes": {
      "eip155:1": {
        "accounts": ["eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"],
        "methods": ["eth_sendTransaction", "personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"],
        "capabilities": {}
      },
      "eip155:8453": {
        "accounts": ["eip155:8453:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"],
        "methods": ["eth_sendTransaction", "personal_sign", "wallet_sendCalls"],
        "notifications": ["accountsChanged", "chainChanged"],
        "capabilities": {
          "atomicBatch": "true"
        }
      },
      "eip155:42161": {
        "accounts": ["eip155:42161:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"],
        "methods": ["eth_sendTransaction", "personal_sign", "wallet_sendCalls"],
        "notifications": ["accountsChanged", "chainChanged"],
        "capabilities": {
          "atomicBatch": "true",
          "paymasterService": {
            "url": "https://...",
            "optional": true
          }
        }
      },
      "eip155:0": {
        "methods": ["wallet_grantPermissions"],
        "notifications": [],
        "capabilities": {}
      },
      "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp": {
        "accounts": [
          "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp:7S3P4HxJpyyigGzodYwHtCxZyUQe9JiBMHyRWXArAaKv"
        ],
        "methods": [
          "solana_signMessage",
          "solana_signTransaction",
          "solana_signAndSendTransaction"
        ],
        "notifications": [],
        "capabilities": {
          "supportedTransactionVersions": ["legacy", "0"]
        }
      },
      "solana:4uhcVJyU9pJkvQyS88uRDiswHXSCkY3z": {
        "accounts": [
          "solana:4uhcVJyU9pJkvQyS88uRDiswHXSCkY3z:6LmSRCiu3z6NCSpF19oz1pHXkYkN4jWbj9K1nVELpDkT"
        ],
        "methods": [
          "solana_signMessage",
          "solana_signTransaction",
          "solana_signAndSendTransaction"
        ],
        "notifications": [],
        "capabilities": {
          "supportedTransactionVersions": ["legacy"]
        }
      }
    },
    "properties": {
      "expiry": "2022-12-24T17:07:31+00:00"
    }
  }
}
```

The `scopes` object MAY contain `accounts` as part of its `scopeObject` for success response.

#### Error

The wallet MAY return generic or specific error messages depending on trust. Trusted responses may include codes like:

- `5000`: Unknown error
- `5001`: User disapproved requested methods
- `5002`: User disapproved requested notifications
- `5100-5102`: Unsupported chains, methods, or notifications
- `5201-5302`: Malformed requests

## Security Considerations

To avoid ambiguity in authorizations, `scopes` MUST retain their original keyed structure using [CAIP-2][] or [CAIP-104][] identifiers. This ensures clarity in what is authorized and prevents accidental scope merging or misinterpretation.

## Privacy Considerations

To mitigate fingerprinting risks, wallets should prefer uniform or silent failure responses. Avoid leaking timing or error detail that may help malicious actors identify users or wallets. Progressive, minimal scope requests and updates are encouraged.

## Changelog

- 2025-08-04: Merged `capabilities` (fka `scopedProperties`) into `scopeObjects`.
- 2025-08-03: Removed Namespace-scoped `scopeObjects` and retained only Chain-scoped `scopeObjects`
- 2025-07-31: Removed `requiredScopes` and retained only `scopes` (fka `optionalScopes`).
- 2025-07-30: Renamed `optionalScopes` to `scopes`, `scopedProperties` to `capabilities` and `sessionProperties` to `properties`
- 2024-07-29: Added lifecycle management methods and notification for single session connections
- 2024-07-16: Redefined scope negotiation behavior
- 2023-03-29: Refactored `scopeObject` syntax to CAIP-217
- 2022-11-26: Introduced mandatory `sessionId` usage (CAIP-171)
- 2022-10-26: Updated session param syntax post community gathering

## Links

- [CAIP-2][] - Chain ID Specification
- [CAIP-10][] - Account ID Specification
- [CAIP-104][] - Definition of Chain Agnostic Namespaces or CANs
- [CAIP-171][] - Session Identifier, i.e. syntax and usage of `sessionId`s
- [CAIP-217][] - Authorization Scopes, i.e. syntax for `scopeObject`s
- [CAIP-285][] - `wallet_revokeSession` Specification
- [CAIP-312][] - `wallet_getSession` Specification
- [CAIP-311][] - `wallet_sessionChanged` Specification
- [CAIP-316][] - Session Lifecycle Management equivalence chart and diagrams
- [RFC-2119][] - Key words for use in RFCs to Indicate Requirement Levels

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[CAIP-285]: https://chainagnostic.org/CAIPs/caip-285
[CAIP-312]: https://chainagnostic.org/CAIPs/CAIP-312
[CAIP-311]: https://chainagnostic.org/CAIPs/CAIP-311
[CAIP-316]: https://chainagnostic.org/CAIPs/caip-316
[RFC-2119]: https://datatracker.ietf.org/doc/html/rfc2119

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
