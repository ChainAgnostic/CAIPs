---
caip: 285
title: JSON-RPC Method for Revoking Session Authorizations
author: [Alex Donesky] (@adonesky1)
discussions-to: TBD
status: Draft
type: Standard
created: 2024-07-12
requires: 25, 217
---

## Simple Summary

CAIP-285 introduces the `wallet_revokeSession` method for revoking an active [CAIP-25][] session.

## Abstract

This proposal aims to enhance session management for  [CAIP-25][] sessions by defining a new JSON-RPC method for revoking sessions. This method provides an explicit protocol for revoking sessions with or without `sessionId`s.

## Motivation

The motivation behind this proposal is to enhance the flexibility of [CAIP-25][] by enabling the revocation of session authorizations without `sessionId`s, which don't map well to extension-based wallet's dapp connections and could add constraints and burdens to existing flows. The proposed method provides an intuitive way to revoke authorizations within an existing session, simplifying the management of session lifecycles.

## Specification

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in
uppercase in this document are to be interpreted as described in [RFC
2119][]


### Definition

The `wallet_revokeSession` method revokes the entire active session. If a `sessionId` is provided, it revokes that specific session; otherwise, it revokes the single active session between the wallet and the caller

**Parameters:**

- `sessionId` (string, optional): The session identifier.

### Request
The caller would interface with a wallet via the same provider by which it generated the session to revoke a session by calling the following JSON-RPC request:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_revokeSession",
  "params": {}
}
```
### Validation
TODO

### Response

The wallet can respond to this method with either a success result or an error message.

### Success

An example of a successful response follows:
```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "success": true
  }
}
```

The `wallet_revokeSession` method revokes the entire active session. If a `sessionId` is provided, it revokes that specific session; otherwise, it revokes the single active session between the wallet and the caller.

### Failure

## Security Considerations

The introduction of this lifecycle method must ensure that only authorized parties can revoke the authorizations of a session. Proper authentication and authorization mechanisms must be in place to prevent unauthorized access or modifications.

## Privacy Considerations

Revoking authorizations within an existing session reduces the need to create multiple session identifiers, which can help minimize the exposure of session-related metadata.

## Links

- [CAIP-25] - JSON-RPC Handshake Protocol Specification. i.e `wallet_createSession`
- [CAIP-217]- Authorization Scopes, i.e. syntax for `scopeObject`s

- CAIP-25: https://chainagnostic.org/CAIPs/caip-25
- CAIP-217: https://chainagnostic.org/CAIPs/caip-217

## Copyright

Copyright and related rights waived via
[CC0](https://creativecommons.org/publicdomain/zero/1.0/).
