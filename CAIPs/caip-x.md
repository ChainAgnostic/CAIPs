---
caip: XX
title: JSON-RPC Provider Lifecycle Methods for Session Management
author: [Alex Donesky] (@adonesky1)
discussions-to: TBD
status: Draft
type: Standard
created: 2024-06-07
requires: 25, 217
---

## Simple Summary

CAIP-XX introduces new "lifecycle" methods and a new event for managing permissions within an existing CAIP-25 session, allowing for addition, revocation, and retrieval of permissions. These methods provide an alternative to session management via `sessionId`s, allowing `sessionId`s to be optional for CAIP-25.

## Abstract

This proposal aims to extend the CAIP-25 standard by defining new JSON-RPC methods for managing the lifecycle of permissions within a session. These methods allow dapps and wallets to dynamically adjust permissions, providing more granular control and better user experience. Additionally, it allows for session management without mandatory sessionIds, offering more flexibility in handling sessions.

## Motivation

The motivation behind this proposal is to enhance the flexibility of CAIP-25 by enabling management of session permissions without sessionIds which don't map well to extension based wallet's dapp connections and could therefore add constraints and burdens to existing flows. The proposed methods provide an intuitive way to add, revoke, and retrieve permissions within an existing session, simplifying the management of session lifecycles.

### Use Case Scenarios

1. **Dapp Initiated Adding Permissions To an Existing Session:**

   - **Current Method:** Call `provider_authorize` again with an existing session identifier and new scopes/methods to add. This is actually somewhat ambiguous in CAIP-25 where it’s unclear if incremental permissions adds should include the full object or just scopes to add: "This object gets updated, extended, closed, etc. by successive calls and notifications, each tagged by this identifier."
   - **Proposed Method:** Use `provider_augment` to add new permissions to the existing session. Alternatively, this could be achieved with more specific language indicating that a subsequent `provider_authorize` call with new scopes/permissions could simply add to an existing session.

2. **Wallet Initiated Adding Permissions To an Existing Session:**

   - **Current Method:** CAIP-25 does not make it very clear how a respondent (wallet), can modify the permissions of an existing session. The following excerpt is the closest we get: "The properties and authorization scopes that make up the session are expected to be persisted and tracked over time by both parties in a discrete data store, identified by an entropic identifier assigned in the initial response. This object gets updated, extended, closed, etc. by successive calls and notifications, each tagged by this identifier."
   - **Proposed Method:** Wallet publishes and caller/dapp listens for an event `providerAuthorizationChanged` with the new full sessionScope.

3. **Wallet Initiated Permissions Revocation:**

   - **Current Method:** "If a respondent (e.g. a wallet) needs to initiate a new session, whether due to user input, security policy, or session expiry reasons, it can simply generate a new session identifier to signal this notification to the calling provider."
   - Given this language it is unclear if a wallet can revoke permissions without creating a new session. It is also therefore unclear if a wallet can revoke some subset of permissions without creating a new session.
   - **Proposed Method:** Wallet publishes and caller/dapp listens for an event `providerAuthorizationChanged` with the new full sessionScope.

4. **Dapp Initiated Permissions Revocation:**

   - **Current Method:** "if a caller [dapp] needs to initiate a new session, it can do so by sending a new request without a sessionIdentifier."
   - **Proposed Method:** Use `provider_revoke` to revoke specific permissions or the entire existing session.

5. **Retrieving Current Permissions:**

   - **Current Method:** Track permissions externally or infer from session creation.
   - **Proposed Method:** Use `provider_getSession` to retrieve the current permissions of the session.

## Specification

### New Methods

#### `provider_augment` // open to a different name here!

Adds new permissions to an existing session.

**Parameters:**

- `sessionId` (string, optional): The session identifier.
- `scopes` (object, required): An object containing the new scopes to be added, formatted according to CAIP-217.

**Initial Session Scopes:**

```json
{
  "eip155:1": {
    "methods": ["eth_signTransaction"],
    "notifications": ["accountsChanged"],
    "accounts": ["eip155:1:0xabc123"]
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
  "method": "provider_augment",
  "params": {
    "scopes": {
      "eip155:1": {
        "methods": ["eth_sendTransaction"],
        "notifications": ["chainChanged"]
      }
    }
  }
}
```

**Example Response:**

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "success": true,
    "updatedScopes": {
      "eip155:1": {
        "methods": ["eth_signTransaction", "eth_sendTransaction"],
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts": ["eip155:1:0xabc123"]
      },
      "eip155:137": {
        "methods": ["eth_sendTransaction"],
        "notifications": ["chainChanged"],
        "accounts": ["eip155:137:0xdef456"]
      }
    }
  }
}
```

**Explanation:**

- The `provider_augment` method adds the specified scopes to the current session's permissions. If the scope already exists, the new methods and notifications are merged with the existing ones.

#### `provider_revoke`

Revokes permissions from an existing session.

**Parameters:**

- `sessionId` (string, optional): The session identifier.
- `scopes` (object, required): An object containing the scopes to be revoked, formatted according to CAIP-217.

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
  "method": "provider_revoke",
  "params": {
    "scopes": {
      "eip155:1": {
        "methods": ["eth_sendTransaction"],
        "notifications": ["chainChanged"],
        "accounts": ["eip155:1:0xabc123"]
      }
    }
  }
}
```

**Example Response:**

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "success": true,
    "updatedScopes": {
      "eip155:1": {
        "methods": ["eth_signTransaction"],
        "notifications": ["accountsChanged"],
        "accounts": ["eip155:1:0xdef456"]
      },
      "eip155:137": {
        "methods": ["eth_sendTransaction"],
        "notifications": ["chainChanged"],
        "accounts": ["eip155:137:0xdef456"]
      }
    }
  }
}
```

**Explanation:**

- The `provider_revoke` method removes the specified methods, notifications, and accounts from the current session's permissions. If the scope no longer has any methods, notifications, or accounts after revocation, it is removed from the session. If no scopes remain in the session, the session is considered closed. If no accounts remain in a scope, and only write methods the entire scope is removed.

#### `provider_getSession` // open to a different name here!

Retrieves the current permissions of an existing session.

**Parameters:**

- `sessionId` (string, optional): The session identifier.

**Initial Session Scopes:**

```json
{
  "eip155:1": {
    "methods": ["eth_signTransaction"],
    "notifications": ["accountsChanged"],
    "accounts": ["eip155:1:0xabc123"]
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
  "method": "provider_getSession",
  "params": {}
}
```

**Example Response:**

```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "scopes": {
      "eip155:1": {
        "methods": ["eth_signTransaction"],
        "notifications": ["accountsChanged"],
        "accounts": ["eip155:1:0xabc123"]
      },
      "eip155:137": {
        "methods": ["eth_sendTransaction"],
        "notifications": ["chainChanged"],
        "accounts": ["eip155:137:0xdef456"]
      }
    }
  }
}
```

**Explanation:**

- The `provider_getSession` method returns the current permissions for the session. It lists all scopes along with their methods, notifications, and accounts.

### Events

#### `providerAuthorizationChanged`

This event is published by the wallet to notify the caller/dapp of updates to the session authorization scopes. The event payload indicates how the scopes have changed, showing additions and removals in the permissions.

**Event Payload:**

- `sessionId` (string, optional): The session identifier.
- `scopes` (object, required): An object containing the updated session scopes, formatted according to CAIP-217.

**Initial Session Scopes:**

```json
{
  "eip155:1": {
    "methods": ["eth_signTransaction"],
    "notifications": ["accountsChanged"],
    "accounts": ["eip155:1:0xabc123"]
  },
  "eip155:137": {
    "methods": ["eth_sendTransaction"],
    "notifications": ["chainChanged"],
    "accounts": ["eip155:137:0xdef456"]
  }
}
```

**Example Event:**

```json
{
  "event": "providerAuthorizationChanged",
  "data": {
    "scopes": {
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

This event indicates how the scopes have changed by comparing the updated scopes with the initial session scopes. In the example, the method `eth_sendTransaction` was added to `eip155:1`, and the notification `chainChanged` was removed from `eip155:137`.

### Optional SessionIds

The `sessionId` parameter in the new lifecycle methods is optional. When not provided, the methods will operate on the current active session. This approach allows for more flexible session management without the overhead of tracking session identifiers.

## Security Considerations

The introduction of these lifecycle methods must ensure that only authorized parties can modify the permissions of a session. Proper authentication and authorization mechanisms must be in place to prevent unauthorized access or modifications.

## Privacy Considerations

Managing permissions within an existing session reduces the need to create multiple session identifiers, which can help minimize the exposure of session-related metadata. However, care must be taken to handle these methods in a way that does not inadvertently leak sensitive information.

## Changelog

- 2024-06-07: Initial draft of CAIP-XX.

## Links

- [CAIP-25](https://chainagnostic.org/CAIPs/caip-25)
- [CAIP-217](https://chainagnostic.org/CAIPs/caip-217)

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
