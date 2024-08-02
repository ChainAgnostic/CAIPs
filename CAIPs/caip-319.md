---
caip: 319
title: Wallet Notification JSON-RPC Method
author: Alex Donesky (@adonesky1)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/X
status: Draft
type: Standard
created: 2024-08-01
requires: 2, 25, 171, 217
---

## Simple Summary

CAIP-319 defines a JSON-RPC method for a wallet to send notifications to a
caller in a specified context defined by a valid [scopeObject][CAIP-217] and,
optionally, tagged with a [sessionId][CAIP-171] for maintaining session
continuity.

## Abstract

This proposal aims to define a standard method for wallets to send notifications
to callers regarding events or state changes related to a specific,
previously-authorized target chain (such as nodes of a specific blockchain or
consensus community within a protocol). It requires a valid
[scopeObject][CAIP-217]. It MAY be tagged with a [sessionId][CAIP-171] if the
[CAIP-25][] session in which it is authorized is keyed by a sessionId (see
[CAIP-316][] for more details). These two properties MAY be inherited from a
persistent session created by [CAIP-25][], but could also be used as part of
other session management mechanisms.

## Motivation

The motivation for this proposal comes from the need for standardized,
chain-specific notifications from wallets to applications in a concurrent
multi-chain connection where methods and notifications with the same name may
exist across chains or namespaces.

## Specification

### Definition

The wallet is able to send a single JSON-RPC notification accompanied by a
[CAIP-2][] compatible `chainId`, and optionally scoped by the
[sessionId][CAIP-171] of a pre-existing session if applicable.

### Notification

The wallet or user agent would send a notification to the application as
follows:

```jsonc
{
  "jsonrpc": "2.0",
  "method": "wallet_notify",
  "params": {
    "sessionId": "0xdeadbeef",
    "scope": "eip155:1",
    "notification": {
      "method": "eth_subscription",
      "params": {
        "subscription": "0x12345678",
        "result": {
          "blockNumber": "0x1234",
          "transactionHash": "0x5678",
          "logIndex": "0x9abc"
        }
      }
    }
  }
}
```

The JSON-RPC method is labeled as `wallet_notify` and expects two required
parameters:

- **scope** - a valid `scopeObject` previously authorized to the caller and
  persisted in the session identified by `sessionId`
- **request** - an object containing the fields:
  - **method** - JSON-RPC notification method name
  - **params** - JSON-RPC notification parameters

Additionally, it MAY include an **optional parameter**:

- **sessionId** - [CAIP-171][] `sessionId` referencing a known, open session

### Validation

1. The application MUST check the scope against the identified session object
   before processing the notification.
2. The application SHOULD verify that the notification.method is one it expects
   to receive for that specific scope.
3. The application MAY apply other logic or validation to the notification data.
4. The application MAY choose to ignore notifications it doesn't recognize or
   considers invalid.

### Response
~~~~
As this is a notification, no response is expected from the application. The
wallet or user agent SHOULD NOT wait for a response before continuing its
operations.

## Links

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[CAIP-316]: https://chainagnostic.org/CAIPs/caip-316

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
