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

CAIP-285 introduces the `wallet_revokeSession` method for revoking an entire existing CAIP-25 session. This method provides an alternative to session management via `sessionId`s, allowing `sessionId`s to be optional for CAIP-25.

## Abstract

This proposal aims to extend the CAIP-25 standard by defining a new JSON-RPC method for revoking authorizations within a session. This method allows dapps and wallets to dynamically revoke authorizations, providing more granular control and better user experience.

## Motivation

The motivation behind this proposal is to enhance the flexibility of CAIP-25 by enabling the revocation of session authorizations without sessionIds, which don't map well to extension-based wallet's dapp connections and could add constraints and burdens to existing flows. The proposed method provides an intuitive way to revoke authorizations within an existing session, simplifying the management of session lifecycles.

## Specification

### `wallet_revokeSession`

Revokes authorizations for an active session.

**Parameters:**

- `sessionId` (string, optional): The session identifier.

**Initial Session Scopes:**

```json
{
  "eip155:1": {
    "methods": ["eth_signTransaction", "eth_sendTransaction"],
    "notifications": ["accountsChanged", "chainChanged"],
    "accounts": ["eip155:1:0xabc123", "eip155:1:0xdef456"]
  },
  "eip155:137": {
    "methods": ["eth_sendTransaction"],
    "notifications": ["chainChanged"],
    "accounts": ["eip155:137:0xdef456"]
  }
}
```

**Example Request:**

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_revokeSession",
  "params": {}
}
```

**Example Response:**

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "success": true
  }
}
```

**Explanation:**

- The `wallet_revokeSession` method revokes the entire active session. If a `sessionId` is provided, it revokes that specific session; otherwise, it revokes the single active session between the wallet and the caller.

## Security Considerations

The introduction of this lifecycle method must ensure that only authorized parties can revoke the authorizations of a session. Proper authentication and authorization mechanisms must be in place to prevent unauthorized access or modifications.

## Privacy Considerations

Revoking authorizations within an existing session reduces the need to create multiple session identifiers, which can help minimize the exposure of session-related metadata.

## Links

- [CAIP-25](https://chainagnostic.org/CAIPs/caip-25)
- [CAIP-217](https://chainagnostic.org/CAIPs/caip-217)
