---
caip: 308
title: JSON-RPC Event for Session Authorization Updates
author: [Alex Donesky] (@adonesky1)
discussions-to: TBD
status: Draft
type: Standard
created: 2024-07-12
requires: 25, 217
---

## Simple Summary

CAIP-308 introduces the `wallet_sessionChanged` event for notifying callers party to an active [CAIP-25][] session of updates to session authorizations made by users directly in the wallet.

## Abstract

This proposal aims to extend the [CAIP-25][] standard by defining a new JSON-RPC event for notifying the caller of updates to session authorizations. This event allows wallets to dynamically inform callers of changes to authorizations made by users on the wallet side, without having to initiate a new session each time.

## Motivation

The motivation behind this proposal is to provide bidirectional management of [CAIP-25][] session authorizations. The proposed event provides an intuitive way to notify dapps of changes to authorizations within an active session, simplifying the management of session lifecycles.

## Definition

## Specification

This event is published by the wallet to notify the callers of updates to a shared session's authorization scopes. The event payload contains the new `sessionScopes`. If a connection between the wallet and the caller is severed and the possibility of missed events arises, the caller should immediately call `wallet_getSession` to retrieve the current session scopes.

**Notification Parameters:**

- `sessionId` (string, optional): The session identifier.
- `sessionScopes` (object of `scopeObject` objects, required): An object containing the full updated session scopes, each formatted according to [CAIP-217][].

**Notification:**

```jsonc
{
  "method": "wallet_sessionChanged",
  "params": {
    "sessionScopes": {
      "eip155:1": {
        "methods": ["eth_signTransaction", "eth_sendTransaction"],
        "notifications": ["accountsChanged"],
        "accounts": ["eip155:1:0xabc123"]
      },
      "eip155:137": {
        "methods": ["eth_sendTransaction"],
        "notifications": [],
        "accounts": ["eip155:137:0xdef456"]
      }
    }
  }
}
```

## Security Considerations

The introduction of this lifecycle method must ensure that only authorized parties can retrieve the authorizations of a session. Proper authentication and authorization mechanisms must be in place to prevent unauthorized access or modifications.

To achieve this, it is recommended to establish a connection over domain-bound or other 1:1 transports. Where applicable additional binding to a `sessionId` is recommended to ensure secure session management. This approach helps to create a secure communication channel that can effectively authenticate and authorize session-related requests, minimizing the risk of unauthorized access or session hijacking.

## Links

- [CAIP-25] - JSON-RPC Handshake Protocol Specification. i.e `wallet_createSession`
- [CAIP-217]- Authorization Scopes, i.e. syntax for `scopeObject`s

[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217

## Copyright

Copyright and related rights waived via
[CC0](https://creativecommons.org/publicdomain/zero/1.0/).
