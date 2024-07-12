---
caip: 288
title: JSON-RPC Event for Session Authorization Updates
author: [Alex Donesky] (@adonesky1)
discussions-to: TBD
status: Draft
type: Standard
created: 2024-07-12
requires: 25, 217
---

## Simple Summary

CAIP-288 introduces the `wallet_sessionChanged` event for notifying the caller/dapp of updates to the session authorization scopes within an existing CAIP-25 session.

## Abstract

This proposal aims to extend the CAIP-25 standard by defining a new JSON-RPC event for notifying the caller/dapp of updates to the session authorization scopes. This event allows wallets to dynamically inform dapps of changes to authorizations, providing more granular control and better user experience.

## Motivation

The motivation behind this proposal is to enhance the flexibility of CAIP-25 by enabling dynamic updates to session authorizations without sessionIds, which don't map well to extension-based wallet's dapp connections and could add constraints and burdens to existing flows. The proposed event provides an intuitive way to notify dapps of changes to authorizations within an existing session, simplifying the management of session lifecycles.

## Use Case Scenarios

1. **Wallet Initiated Adding Authorizations To an Existing Session:**

   - **Current Method:** CAIP-25 does not make it very clear how a respondent (wallet), can modify the authorizations of an existing session. The following excerpt is the closest we get: "The properties and authorization scopes that make up the session are expected to be persisted and tracked over time by both parties in a discrete data store, identified by an entropic identifier assigned in the initial response. This object gets updated, extended, closed, etc. by successive calls and notifications, each tagged by this identifier."
   - **Proposed Method:** Wallet publishes and caller/dapp listens for an event `wallet_sessionChanged` with the new full sessionScope.

2. **Wallet Initiated Authorizations Revocation:**

   - **Current Method:** "If a respondent (e.g. a wallet) needs to initiate a new session, whether due to user input, security policy, or session expiry reasons, it can simply generate a new session identifier to signal this notification to the calling provider."
   - Given this language it is unclear if a wallet can revoke authorizations without creating a new session. It is also therefore unclear if a wallet can revoke some subset of authorizations without creating a new session.
   - **Proposed Method:** Wallet publishes and caller/dapp listens for an event `wallet_sessionChanged` with the new full sessionScope.

## Specification

### `wallet_sessionChanged`

This event is published by the wallet to notify the caller/dapp of updates to the session authorization scopes. The event payload contains the new `sessionScopes` objects. If a connection between the wallet and the caller/dapp is severed and the possibility of missed events arises, the caller/dapp should immediately call `wallet_getSession` to retrieve the current session scopes.

**Event Payload:**

- `sessionId` (string, optional): The session identifier.
- `sessionScopes` (object of `scopeObject` objects, required): An object containing the updated session scopes, formatted according to CAIP-217.

**Initial Session Scopes:**

```json
{
  "sessionScopes": {
    "eip155:1": {
      "methods": ["eth_signTransaction"],
      "notifications": ["accountsChanged"],
      "accounts": ["eip155:1:0xabc123"]
    },
    "eip155:137": {
      "methods": ["eth_sendTransaction"],
      "notifications": [],
      "accounts": ["eip155:1:0xabc123", "eip155:137:0xdef456"]
    }
  }
}
```

**Example Event:**

```json
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

**Explanation:**

This event indicates how the scopes have changed by comparing the updated scopes with the initial session scopes. In the example, the method `eth_sendTransaction` was added to `eip155:1`, and the account `eip155:1:0xabc123` was removed from `eip155:137`.

## Security Considerations

The introduction of this event must ensure that only authorized parties can publish updates to the session authorization scopes. Proper authentication and authorization mechanisms must be in place to prevent unauthorized access or modifications.

## Privacy Considerations

Managing authorizations within an existing session reduces the need to create multiple session identifiers, which can help minimize the exposure of session-related metadata.

## Links

- [CAIP-25](https://chainagnostic.org/CAIPs/caip-25)
- [CAIP-217](https://chainagnostic.org/CAIPs/caip-217)

## Copyright

Copyright and related rights waived via
[CC0](https://creativecommons.org/publicdomain/zero/1.0/).
