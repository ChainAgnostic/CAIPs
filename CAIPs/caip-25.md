---
caip: 25
title: Wallet Create Session JSON-RPC Method
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/25
status: Review
type: Standard
created: 2020-10-14
updated: 2024-07-02
requires: 2, 10, 171, 217, 285, 311, 312
---

## Simple Summary

CAIP-25 defines an authorization procedure for a chain agnostic provider to interface with a wallet as part of their initialization and/or "handshake" protocol.

## Abstract

This proposal has the goal to define a standard procedure for decentralized applications to interface with chain agnostic cryptocurrency wallets and other user agents which govern identities (including accounts) in multiple cryptographic systems.
It defines a lightweight protocol for negotiating and persisting authorizations during a session managed by a provider construct.

## Motivation

The motivation comes from the lack of standardization across blockchains to expose accounts and define the expected JSON-RPC methods to be used by an application through a provider connecting to a signer or other user agent.

## Specification

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in
uppercase in this document are to be interpreted as described in [RFC
2119][]

### Definition

#### Session Lifecycle

The session is proposed by a caller and the response by the respondent is used as the baseline for an ongoing session between the two parties.
- When a wallet responds with a success response containing a `sessionId` (an entropic [identifier][CAIP-171]), the properties and authorization scopes that make up the session should be persisted and tracked over the life of the session by both parties in a discrete data store.
- When the wallet does not provide a `sessionId` in its initial response, the wallet MUST persist and track the properties and authorization scopes that make up the session.
The caller is not expected to persist session data or even a `sessionId`.
Note that wallets NOT returning `sessionId`s MUST implement additional methods and notifications to handle the full lifecycle of the session:
    * [`wallet_getSession`][CAIP-312] to enable the caller to query for the current status of the session at any time.
    * [`wallet_revokeSession`][CAIP-285] to explicitly end the session
    * [`wallet_sessionChanged`][CAIP-311] to notify caller of updated session authorizations.

After a session is established between wallet and caller, subsequent `wallet_createSession` calls can be used to update the properties and authorization scopes of the session.
- When a `sessionId` is returned in the initial `wallet_createSession` response, subsequent `wallet_createSession` calls either:
  - include a previously used `sessionId` on the root of the request meaning this request is intended to modify that session, or
  - do not include a `sessionId`, in which case a new session is created - the respondent generates a new `sessionId` and sends it with the success response - and the previous session dangles in parallel (until its expiration, if applicable), though maintaining concurrent sessions is discouraged (see Security Considerations).
- When the wallet does not provide a `sessionId` in its initial response, subsequent `wallet_createSession` calls target the previous singular session between caller and wallet.
- `wallet_createSession` calls either increase scope, decrease scope, or both.

When a user wishes to update the authorizations of an active session from within the wallet, the wallet should notify the caller of the changes with a [`wallet_sessionChanged`][CAIP-311] notification.

If a connection is initially established without a `sessionId` and the wallet later implements `sessionId` support, the wallet can revoke the single session and notify the caller via `wallet_sessionChanged`. When the caller seeks to re-establish the session via `wallet_createSession`, the wallet should return a `sessionId` in the response.

When a caller wishes revoke an active session, it can do so by calling [`wallet_revokeSession`][CAIP-285].
- When a `sessionId` is returned in the initial `wallet_createSession` response, the caller MUST call `wallet_revokeSession` with the supplied `sessionId` to revoke that session.
- When the wallet does not provide a `sessionId` in its initial response, a call to `wallet_revokeSession` revokes the single active session between caller and wallet.

For more detail on the lifecycle and management of sessions with and without `sessionId`s, see the informational [CAIP-316][].

#### Session Data and Metadata

Initial and ongoing authorization requests are grouped into two top-level objects containing keyed [scopeObjects][CAIP-217], named `requiredScopes` and `optionalScopes`
respectively.
Each `scopeObject` in either parent object MUST be keyed uniquely within its parent, but these keys CAN appear in both
(i.e., additional properties of an authorization target in `requiredScopes` may be requested in a separate `scopeObject` with the same key in the `optionalScopes` array).

Each `scopeObject` in these parent `...Scopes` objects can be keyed to a specific [CAIP-2][] network identifier, or to an entire [CAIP-104][] namespace.
`scopeObjects` keyed to an entire [CAIP-104][] namespace SHOULD contain a non-empty `references` array to be actionable, making them functionally equivalent to a series of identical `scopeObjects`, each keyed to one of the members of `references` expressed as a [CAIP-2][] scope.
An empty or absent `references` array SHOULD NOT be interpreted as a namespace-wide authorization (i.e. authorization for ANY network therein), but rather as a null authorization of 0 specified `references`s within that namespace.
(See [CAIP-217][] for more details on the structure of the typed objects included in these `...Scopes` objects.)

The distinction between `requiredScopes` and `optionalScopes` is ultimately semantic, since a wallet may still choose to establish a connection authorizing a subset of requested networks or requested capabilities from each; the primary function of the distinction is to offer callers a mechanism for signaling which scopes they consider primary and which they consider secondary to their request, in order to better inform the authorization logic of the respondent.

If a connection is being rejected, whether on the basis of end-user input or on the basis of evaluating `requiredScopes` against available capabilities, the respondent SHOULD choose its response based on trust:
e.g., one or more specific failure states MAY be sent (see [#### failure states](#failure-states) below) for trusted counterparties, but an `undefined` response (or no response, depending on implementation) MAY also be sent to prevent incentivizing unwanted requests and to minimize the surface for fingerprinting of public web traffic (See Privacy Considerations below).

After parsing and authorizing separately all the networks and capabilities within each, a respondent establishes a connection by returning a success response that organizes all authorized features of each authorized scope in a single unified object of `scopeObject`s called `sessionScopes`.
In the case of identically-keyed `scopeObject`s appearing in both top-level objects in the request (`requestedScopes` and `optionalScopes`), the identically-scoped objects MUST be merged in the response, since `sessionScopes` MUST NOT contain redundant keys (see examples below).
However, respondents MUST NOT restructure scopes (e.g., by folding properties from a [CAIP-2][]-keyed, chain-specific scope object into a [CAIP-104][]-keyed, namespace-wide scope object) as this may introduce ambiguities (See Security Considerations below).

### Request

The application would interface with a provider to authorize that provider with a given set of parameters by calling the following JSON-RPC request

Example:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_createSession",
  "params": {
    "requiredScopes": {
      "eip155": {
        "references": ["1", "137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "eth_sign", "get_balance", "personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:0": {
        "methods": ["wallet_getPermissions", "wallet_creds_store", "wallet_creds_verify", "wallet_creds_issue", "wallet_creds_present"],
        "notifications": []
      },
      "cosmos": {
        ...
      }
    },
    "optionalScopes":{
      "eip155:42161": {
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"]
    },
    "scopedProperties": {
      "eip155:42161": {
        "extension_foo": "bar"
      }
    },
    "sessionProperties": {
      "expiry": "2022-12-24T17:07:31+00:00",
      "caip154-mandatory": "true"
    }
  }
}
```

The JSON-RPC method is labeled as `wallet_createSession` and its `params` object contains "requiredScopes" and/or "optionalScopes" objects populated with [CAIP-217][] "scope objects" keyed to [CAIP-217][] scope strings.

- The `requiredScopes` object MUST contain 1 or more `scopeObjects`, if present.
- The `optionalScopes` object MUST contain 1 or more `scopeObjects`, if present.

A third object is the `scopedProperties` object, which also MUST contain 1 or more objects if present.
Each object should be keyed to the scope of a `sessionScopes` member to which it corresponds.
All properties of each object in `scopedProperties` MUST be interpreted by the respondent as proposals or declarations rather than as requirements.
In addition to making additional properties of or metadata about the corresponding `sessionScopes` member explicit, they can also annotate, support, or extend the negotiation of scope proposals (e.g., providing connection information about unfamiliar scopes, or which accounts to expose to each).

A fourth object, `sessionProperties`, is optional and its shape undefined.
It is intended for metadata or additional information not bound to any specific authorization scope, but made "global" to the connection.

The respondent SHOULD ignore and drop from its response any properties not defined in this document or in another CAIP document extending this protocol which the respondent has implemented in its entirety;
similarly, the `requiredScopes`, `optionalScopes`, and `sessionScopes` objects returned by the respondent SHOULD contain only valid [CAIP-217][] objects, and properties not defined in [CAIP-217][] SHOULD also be dropped from each of those objects.
The same absolute security posture is not expected for the metadata objects `scopedProperties` and `sessionProperties`, but caution is still recommended for such extensions:
callers and respondents alike SHOULD allow for their counterparties dropping or ignoring unfamiliar members from either.

When a `sessionId` is returned with the initial success response, requesting applications and respondents alike are expected to manage state for the connection, including `scopedProperties` and `sessionProperties`, and that same `sessionId` should be added to the `wallet_createSession` request to update the associated session.
When no `sessionId` is included in an initial success response, a caller does not need to maintain `sessionId` state and can assume the extension methods defined in [CAIP-311][] and [CAIP-312][] are available for refreshing and updating the session directly.
See [CAIP-316][] for more on lifecycle management.
if multiple concurrent connections are allowed, callers are expected to track, persist and identify them separately by the unique `sessionId` returned initially.

### Response

The wallet can respond to this method with either a success result or an error message.

#### Success

The successful result MAY contain a string (keyed as `sessionId` with a value conformant to [CAIP-171][]). As described above, if a `sessionId` is returned in the response, the caller should persist and track the properties and authorization scopes associated with this `sessionid`. If the wallet does not return a `sessionId` in the response, the connection will only consist of one session at a time, the contents of which are always retrievable for the caller via [`wallet_getSession`][CAIP-312].

The successful result MUST contain an object called `sessionScopes` which contains 1 or more `scopeObjects`.
- All required `scopeObjects` and all, none, or some of the optional `scopeObject`s (at the discretion of the provider) MUST be included if successful.
- Unlike the request, each scope object MUST also contain an `accounts` array,
containing 0 or more [CAIP-10][]-conformant accounts authorized for the session
and valid in that scope. Additional constraints on the accounts authorized for a given session MUST be applied conformant to the namespace's [CAIP-10][] profile, if one has been specified.

A `scopedProperties` object MAY also be present, each member of which corresponds to exactly 1 `sessionScope`.
This is intended for expressing connection-specific or non-standardized extensions to `sessionScope`.
Each object in `scopedProperties` MUST be keyed to a `scopeString`, and SHOULD correspond to a `sessionScopes` entry with the same key.
There are no type, depth, or shape constraints on the contents of each property in `scopedProperties`.
If an object in `scopedProperties` is keyed to a `scopeString` not currently authorized for the session, it SHOULD be ignored.

A `sessionProperties` object MAY also be present, with no protocol-wide shape constraints or semantics assumed.
This is intended for expressing metadata about the CAIP-25 connection and accessible to ALL `sessionScopes` equally.
There are no type, depth, or shape constraints on the contents of `sessionProperties`.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "sessionScopes": {
      "eip155": {
        "references": ["1", "137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "eth_sign", "personal_sign"]
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts": ["eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb", "eip155:137:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts": []
      },
      "eip155:42161": {
        "methods": ["personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts":["eip155:42161:0x0910e12C68d02B561a34569E1367c9AAb42bd810"],
        "rpcDocuments": "https://example.com/wallet_extension.json"
      },
      "eip155:0": {
        "methods": ["wallet_getPermissions", "wallet_creds_store", "wallet_creds_verify", "wallet_creds_issue", "wallet_creds_present"],
        "notifications": []
      },
      "cosmos": {
        ...
      }
    },
    "scopedProperties": {
      "eip155:42161": {
        "walletExtensionConfig": {
          "foo": "bar"
        }
      }
    },
    "sessionProperties": {
      "expiry": "2022-11-31T17:07:31+00:00",
      "globalConfig": {
          "foo": "bar"
      }
    }
  }
}
```

#### Failure States

The response MUST NOT be a JSON-RPC success result in any of the following failure states.

##### Generic Failure Code

Unless the dapp is known to the wallet and trusted, the generic/undefined error response,

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

- the user denies consent for exposing accounts that match the requested and
  approved networks/"chains",
- the user denies consent for requested methods,
- the user denies all requested or any required scope objects,
- the wallet cannot support all requested or any required scope objects,
- the requested networks/"chains" are not supported by the wallet, or
- the requested methods are not supported by the wallet

##### Trusted Failure Codes

More informative error messages MAY be sent in trusted-counterparty circumstances, although extending this trust too widely may contribute to widespread fingerprinting and analytics which corrode herd privacy (see [Privacy Considerations](#privacy-considerations) below).
The core error messages over trusted connections are as follows:

The valid error messages codes are the following:

- Unknown error OR no scopes were authorized
  - code = 5000
  - message = "Unknown error with request"
- When user disapproves accepting calls with the request methods
  - code = 5001
  - message = "User disapproved requested methods"
- When user disapproves accepting calls with the request notifications
  - code = 5002
  - message = "User disapproved requested notifications"
- When provider evaluates networks requested by chainId as not supported
  - code = 5100
  - message = "Requested networks are not supported"
- When provider evaluates requested methods to not be supported
  - code = 5101
  - message = "Requested methods are not supported"
- When provider evaluates requested notifications to not be supported
  - code = 5102
  - message = "Requested notifications are not supported"


##### Trust-Agnostic Malformed Request Failure Codes

Regardless of caller trust level, the following error responses can reduce friction and user experience problems in the case of malformed requests.

- When provider does not recognize one or more requested method(s)
  - code = 5201
  - message = "Unknown method(s) requested"
- When provider does not recognize one or more requested notification(s)
  - code = 5202
  - message = "Unknown notification(s) requested"
- When a badly-formed request defines one `chainId` two ways
  - code = 5204
  - message = "ChainId defined in two different scopes"
- Invalid scopedProperties Object
  - code = 5300
  - message = "Invalid scopedProperties requested"
- scopedProperties requested outside of scopedProperties Object
  - code = 5301
  - message = "scopedProperties can only be outside of sessionScopes"
- Invalid sessionProperties Object
  - code = 5302
  - message = "Invalid sessionProperties requested"

Note: respondents SHOULD to implement support for core RPC Documents per each
supported namespace to avoid sending error messages 5201 and 5202 in cases where
0, 5101 or 5102 would be more appropriate. Failure to do so may leak versioning
or feature-completeness information to a malicious or fingerprinting caller.

## Security Considerations

The crucial security function of a shared session negotiated and maintained by a
series of CAIP-25 calls is to reduce ambiguity in authorization. This requires
a potentially counterintuitive structuring of the building-blocks of a
Chain-Agnostic session into scopes at the "namespace-wide" ([CAIP-104][]) or at
the "chain-specific" ([CAIP-2][]) level; for this reason, requests and responses
are structures as objects full of objects keyed to these scopes, formatted either as a
[CAIP-104][] scheme OR as a full [CAIP-2][]. While internal systems are free to
translate this object into other structures, preserving it in the CAIP-25
interface is crucial to the unambiguous communication between caller and
respondent about what exact authorization is granted.

## Privacy Considerations

One major risk in browser-based or HTTP-based communications is "fingerprinting
risk", i.e. the risk that public or intercepted traffic can be used to
deanonymize browsers and/or wallets deductively based on response times, error
codes, etc. To minimize this risk, and to minimize the data (including
behavioral data) leaked by responses to potentially malicious CAIP-25 calls,
respondents are recommended to ignore calls

1. which the respondent explicitly does not authorize,
2. which are rejected automatically or by policy, or
3. which are rejected for unknown reasons.

"Ignoring" these calls means responding to all three in a way that is
*indistinguishable* to a malicious caller or observer which might deduce
information from differences in those responses (including the time taken to
provide them). Effectively, this means allowing requests in all three cases to
time out even if the end-user experience might be better served by
differentiating them, particularly in complex multi-party architectures where
parties on one side of this interface need to have a shared understanding of why
a request did not receive a response. At scale, however, better user experiences
in a single architecture or context can contribute to a systemic erosion of
anonymity.

Given this "silent time out" behavior, the best strategy to ensure good user
experience is not to request too many properties in the initial establishment of
a session and to iteratively and incrementally expand session authorization over
time. This also contributes to a more consentful experience overall and
encourages progressive trust establishment across complex architectures with
many distinct actors and agents.

Another design pattern that accomodates the "silent time out" behavior is minor
updates to the session. For example, a caller sending a request identical to a
previous request (or a previous response) except for a new session expiry
further in the future could expect one of exactly three responses:

1. An identical response to the previous request (meaning the session extension was denied);
2. A response identical expect that it includes the new, extended session expiry; or,
3. A silent time out (meaning the calling behavior was malformed in ways the
respondent cannot understand, or the respondent choses not to make explicit how
the request was malformed, or the end-user rejected them, or the request itself
was in violation of policy).

## Changelog

-- 2024-07-29: added lifecycle management methods and notification for single session connections, see [CAIP-316][] for equivalence chart and diagrams
- 2024-07-16: redefined requiredScopes to be functionally equivalent to optionalScopes, but semantically different; previously, authorizing less than 100% of reqScopes required rejecting the connection
- 2023-03-29: refactored out scopeObject syntax as separate CAIP-217, simplified
- 2022-11-26: add mandatory indexing by session identifier (i.e. CAIP-171 requirement)
- 2022-10-26: Addressed Berlin Gathering semantics issues and params syntax; consolidated variants across issues and forks post-Amsterdam Gathering

## Links

- [CAIP-2][] - Chain ID Specification
- [CAIP-10][] - Account ID Specification
- [CAIP-104][] - Definition of Chain Agnostic Namespaces or CANs
- [CAIP-171][] - Session Identifier, i.e. syntax and usage of `sessionId`s
- [CAIP-217][] - Authorization Scopes, i.e. syntax for `scopeObject`s
- [CAIP-285][] - `wallet_revokeSession` Specification
- [CAIP-312][] - `wallet_getSession` Specification
- [CAIP-311][] - `wallet_sessionChanged` Specification
- [CAIP-316][] -  Session Lifecycle Management equivalence chart and diagrams

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[CAIP-285]: https://chainagnostic.org/CAIPs/caip-285
[CAIP-312]: https://chainagnostic.org/CAIPs/CAIP-312
[CAIP-311]: https://chainagnostic.org/CAIPs/CAIP-311
[CAIP-316]: https://chainagnostic.org/CAIPs/caip-316
[namespaces]: https://namespaces.chainagnostic.org
[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
