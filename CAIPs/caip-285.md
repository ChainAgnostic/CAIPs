---
caip: 285
title: JSON-RPC Method for Revoking Session Authorizations
author: Alex Donesky (@adonesky1), João Carlos (@ffmcgee725)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/285/files
status: Draft
type: Standard
created: 2024-07-12
requires: 25, 217
---

## Simple Summary

CAIP-285 introduces the `wallet_revokeSession` method for fully revoking the authorizations and properties of an active [CAIP-25][] session.

## Abstract

This proposal aims to enhance session management for [CAIP-25][] initiated sessions by defining a new JSON-RPC method for revoking sessions. This method provides an explicit protocol for revoking sessions with or without `sessionId`s.

## Motivation

The motivation behind this proposal is to enhance the flexibility of [CAIP-25][] initated sessions by enabling the revocation of session authorizations without `sessionId`s, which don't map well to extension-based wallet's dapp connections and could add unnecessary constraints and burdens to existing flows. The proposed method provides an intuitive way to revoke authorizations of an active session, simplifying the management of session lifecycles.

## Specification

### Definition

The `wallet_revokeSession` method revokes the entire active session.
If a `sessionId` parameter is provided, it revokes that specific session only;
if no `sessionId` parameter is provided and there is an active session without a `sessionId` this session gets revoked and a success result is returned;
otherwise, an appropriate error message is sent.

**Parameters:**

- `sessionId` (string, optional): The session identifier.
- `scopes` (string[], optional): Scopes to be revoked if a partial revoke is intended instead of a full permission revoke.

### Request

The caller would interface with a wallet via the same channel by which it called `wallet_createSession` to revoke a session by calling the following JSON-RPC request:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_revokeSession",
  "params": {}
}
```

### Response

The wallet can respond to this method with either a success result or an error message.

### Success

Upon a successful `wallet_revokeSession` call a wallet should remove authorizations and session properties associated with the revoked session.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": true
}
```

### Extended Usage: partial revokes via `wallet_revokeSession`

When the optional `scopes` parameter is passed, the `wallet_revokeSession` method MUST partially revoke the active session.

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_revokeSession",
  "params": {
    "scopes": ["eip155:1", "eip155:10"]
  }
}
```

The wallet SHOULD ignore any scopes that do not exist in the active session.
This parameter is optional, so if it is not present in the request, the wallet MUST allow the request to follow through as usual, fully revoking the active session.

If after a partial revoke, no scopes exist in the active session, the wallet SHOULD proceed with a full session revoke.

### Failure States

The response MUST NOT be a JSON-RPC success result in any of the following failure states.

#### Generic Failure Code

Unless the dapp is known to the wallet and trusted, the generic/undefined error response:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": 0,
    "message": "Unknown error"
  }
}
```

is RECOMMENDED for any of the following cases:

- a sessionId is passed but not recognized,
- no sessionId is passed and only active session(s) have sessionIds, or
- there are no active sessions

#### Trusted Failure Codes

More informative error messages MAY be sent in trusted-counterparty circumstances, although extending this trust too widely may contribute to widespread fingerprinting and analytics which corrode herd privacy (see Privacy Considerations below). The core error messages over trusted connections are as follows:

The valid error message codes are the following:

- When a sessionId is passed but not recognized:

  - code = 5500
  - message = "SessionId not recognized"

- When there are no active sessions:

  - code = 5501
  - message = "No active sessions"

- When no sessionId is passed and only active session(s) have sessionIds:

  - code = 5502
  - message = "All active sessions have sessionIds"

## Security Considerations

The introduction of this lifecycle method must ensure that only authorized parties can retrieve the authorizations of a session. Proper authentication and authorization mechanisms must be in place to prevent unauthorized access or modifications.

To achieve this, it is recommended to establish a connection over domain-bound or other 1:1 transports. Where applicable, additional binding to a `sessionId` is recommended to ensure secure session management. This approach helps to create a secure communication channel that can effectively authenticate and authorize session-related requests, minimizing the risk of unauthorized access or session hijacking.

## Links

- [CAIP-25] - JSON-RPC Handshake Protocol Specification. i.e `wallet_createSession`
- [CAIP-217]- Authorization Scopes, i.e. syntax for `scopeObject`s

[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217

## Copyright

Copyright and related rights waived via
[CC0](https://creativecommons.org/publicdomain/zero/1.0/).
