---
caip: 312
title: JSON-RPC Method for Retrieving Session Authorizations
author: [Alex Donesky] (@adonesky1)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/285/files
status: Draft
type: Standard
created: 2024-07-13
requires: 25, 217
---

## Simple Summary

CAIP-307 introduces the `wallet_getSession` method for retrieving authorizations from an active [CAIP-25][] initiated session.

## Abstract

This proposal aims to extend the [CAIP-25][] standard by defining a new JSON-RPC method for retrieving authorizations within a session. This method allows callers to dynamically retrieve authorizations and properties without necessarily having to persist and track it throughout the session's life.

## Motivation

The motivation behind this proposal is to enhance the flexibility of [CAIP-25][] by enabling the retrieval of session authorizations at any time. The proposed method provides an intuitive way to retrieve authorizations for an active session, allowing callers to access session data without having to persist and track it over the full life of the method.

## Specification

### Definition

The `wallet_getSession` method returns an active session.
If a `sessionId` is provided, it returns the authorizations for that specific session;
If no `sessionId` parameter is provided - and there is a single active session with no `sessionId` assigned - it returns the session authorizations and properties for that session;
otherwise, an appropriate error message;

**Parameters:**

- `sessionId` (string, optional): The session identifier.

### Request

The caller would interface with a wallet via the same provider by which it called `wallet_createSession` to retrieve a session by calling the following JSON-RPC request:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_getSession",
  "params": {}
}
```

### Response

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionScopes": {
      "eip155:1": {
        "methods": ["eth_signTransaction"],
        "notifications": ["accountsChanged"],
        "accounts": ["eip155:1:0xabc123"]
      },
      "eip155:137": {
        "methods": ["eth_sendTransaction"],
        "notifications": ["chainChanged"],
        "accounts": ["eip155:137:0xdef456"]
      },
      "solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ": {
        "methods": ["getBalance", "getAccountInfo", "sendTransaction", "getBlock"],
        "notifications": [],
        "accounts": ["solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ:4Nd1mS8AUwK3kU3gdiAM6QCvqhA7Do8rKtMXsGyqrJxy"]
      }
  }
}
```

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

- a `sessionId` is passed but not recognized,
- no `sessionId` is passed and only active session(s) have `sessionId`s, or
- there are no active sessions

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
