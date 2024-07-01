---
caip: 25
title: JSON-RPC Provider Authorization
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/25
status: Review
type: Standard
created: 2020-10-14
updated: 2022-10-26
requires: 2, 10, 171, 217
---

## Simple Summary

CAIP-25 defines an authorization procedure for a chain agnostic provider to interface with a wallet as part of their initialization and/or "handshake" protocol.

## Abstract

This proposal has the goal to define a standard procedure for decentralized applications to interface with chain agnostic cryptocurrency wallets and other user agents which govern identities (including accounts) in multiple cryptographic systems.
It defines a lightweight protocol for negotiating and persisting authorizations during a session managed by a provider construct.

## Motivation

The motivation comes from the lack of standardization across blockchains to expose accounts and define the expected JSON-RPC methods to be used by an application through a provider connecting to a signer or other user agent.

## Specification

The session is proposed by a caller and the response by the respondent is used as the baseline for an ongoing session that both parties will persist.
The properties and authorization scopes that make up the session are expected to be persisted and tracked over time by both parties in a discrete data store, identified by an entropic [identifier][CAIP-171] assigned in the initial response.
This object gets updated, extended, closed, etc. by successive calls and notifications, each tagged by this identifier.

If a respondent (e.g. a wallet) needs to initiate a new session, whether due to user input, security policy, or session expiry reasons, it can simply generate a new session identifier to signal this notification to the calling provider; if a caller needs to initiate a new session, it can do so by sending a new request without a `sessionIdentifier`. In such cases, a respondent (e.g. wallet) may choose to explicitly close all sessions upon generation of a new one from the same origin or identity, or leave it to time-out; maintaining concurrent sessions is discouraged (see Security Considerations).

Initial and ongoing authorization requests are grouped into two top-level arrays of [scopeObjects][CAIP-217], named `requiredScopes` and `optionalScopes`
respectively.
Each `scopeObject` in either array MUST be keyed uniquely within its parent, but these keys CAN appear in both
(i.e., additional properties of an authorization target in `requiredScopes` may be requested in a separate `scopeObject` with the same key in the `optionalScopes` array).

Each `scopeObject` in these arrays can be keyed to a specific [CAIP-2][] network identifier, or to an entire [CAIP-104][] namespace.
`scopeObjects` keyed to an entire [CAIP-104][] namespace SHOULD contain a non-empty `scopes` array to be actionable, making them functionally equivalent to a series of identical `scopeObjects`, each keyed to one of the members of `scopes` expressed as a [CAIP-2][] scope.
An empty or absent `scopes` array SHOULD NOT be interpreted as a namespace-wide authorization (i.e. authorization for ANY network therein), but rather as a null authorization of 0 specified `chainId`s within that namespace.
(See [CAIP-217][] for more details on the structure of the typed objects included in these arrays.)

If any properties in the required scope(s) are not authorized by the respondent, a failure response expressive of one or more specific failure states will be sent (see [#### failure states](#failure-states) below), with the exception of user denying consent.
For privacy reasons, an `undefined` response (or no response, depending on implementation) should be sent to prevent incentivizing unwanted requests and to minimize the surface for fingerprinting of public web traffic (See Privacy Considerations below).

Conversely, a succesful response will contain all the required properties *and the provider's choice of the optional properties* expressed in a single unified array of `scopeObject`s called `sessionScopes`.
In the case of identically-keyed `scopeObject`s appearing in both arrays in the request (`requestedScopes` and `optionalScopes`), the identically-scoped objects MUST be merged in the response such that no redundant keys appear in `sessionScopes`, (see examples below).
However, respondents MUST NOT restructure scopes (e.g., by folding properties from a [CAIP-2][]-keyed, chain-specific scope object into a [CAIP-104][]-keyed, namespace-wide scope object) as this may introduce ambiguities (See Security Considerations below).

### Request

The application would interface with a provider to authorize that provider with a given set of parameters by calling the following JSON-RPC request

Example:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "provider_authorize",
  "params": {
    "requiredScopes": {
      "eip155": {
        "scopes": ["eip155:1", "eip155:137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "eth_sign", "get_balance", "personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "wallet": {
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
        "expiry": "2022-12-24T17:07:31+00:00",
        "caip154-mandatory": "true"
      }
    }         
  }
}
```

The JSON-RPC method is labeled as `provider_authorize` and its `params` object contains "requiredScopes" and/or "optionalScopes" objects populated with [CAIP-217][] "scope objects" keyed to [CAIP-217][] scope strings.

- The `requiredScopes` array MUST contain 1 or more `scopeObjects`, if present.
- The `optionalScopes` array MUST contain 1 or more `scopeObjects`, if present.

A third object is the `scopedProperties` object, which also MUST contain 1 or more objects if present.
Each object should be keyed to the scope of a `sessionScopes` member to which it corresponds.
All properties of each object in `scopedProperties` MUST be interpreted by the respondent as proposals or declarations rather than as requirements.
In addition to making additional properties of or metadata about the corresponding `sessionScopes` member explicit, they can also annotate, support, or extend the negotiation of scope proposals (e.g., providing connection information about unfamiliar scopes or which accounts to expose to each).

Respondent SHOULD ignore and drop from its response any properties not defined in this document or in another CAIP document extending this protocol which the respondent has implemented in its entirety;
similarly, the `requiredScopes`, `optionalScopes`, and `sessionScopes` arrays returned by the respondent SHOULD contain only valid [CAIP-217][] objects, and properties not defined in [CAIP-217][] SHOULD also be dropped from each of those objects.
An exception can safely be made for `scopedProperties`, but caution is recommended for such extensions.

Requesting applications are expected to persist all of these returned properties in the session object identified by the `sessionId`.

### Response

The wallet can respond to this method with either a success result or an error message.

#### Success

The successful result contains one mandatory string (keyed as `sessionId` with a value conformant to [CAIP-171][]) and two session objects, both mandatory and non-empty.

The first is called `sessionScopes` and contains 1 or more `scopeObjects`.

- All required `scopeObjects` and all, none, or some of the optional `scopeObject`s (at the discretion of the provider) MUST be included if successful.
- Unlike the request, each scope object MUST also contain an `accounts` array,
containing 0 or more [CAIP-10][]-conformant accounts authorized for the session
and valid in that scope. Additional constraints on the accounts authorized for a given session MUST be applied conformant to the namespace's [CAIP-10][] profile, if one has been specified.

A `scopedProperties` object MAY also be present, each member of which corresponds to exactly 1 `sessionScope`.
This is intended for expressing connection-specific or non-standardized extensions to `sessionScope`.
Each object in `scopedProperties` MUST be keyed to a `scopeString`, and SHOULD correspond to a `sessionScopes` entry with the same key.
If an object in `scopedProperties` is keyed to a `scopeString` not currently authorized for the session, it SHOULD be ignored.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "sessionScopes": {
      "eip155": {
        "scopes": ["eip155:1", "eip155:137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "eth_sign", "personal_sign"]
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts": ["eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb", "eip155:137:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts:" []
      },
      "eip155:42161": {
        "methods": ["personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"],
        "accounts":["eip155:42161:0x0910e12C68d02B561a34569E1367c9AAb42bd810"]
      },
      "wallet": {
        "methods": ["wallet_getPermissions", "wallet_creds_store", "wallet_creds_verify", "wallet_creds_issue", "wallet_creds_present"],
        "notifications": []
      },
      "cosmos": {
        ...
      }
    },      
    "scopedProperties": {
      "eip155:42161": {
        "expiry": "2022-11-31T17:07:31+00:00"          
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

Regardless of caller trust level, the following error responses can reduce
friction and user experience problems in the case of malformed requests.

- When provider does not recognize one or more requested method(s)
  - code = 5201
  - message = "Unknown method(s) requested"
- When provider does not recognize one or more requested notification(s)
  - code = 5202
  - message = "Unknown notification(s) requested"
- When a badly-formed request includes a `chainId` mismatched to scope
  - code = 5203
  - message = "Scope/chain mismatch"
- When a badly-formed request defines one `chainId` two ways
  - code = 5204
  - message = "ChainId defined in two different scopes"  
- Invalid scopedProperties Object
  - code = 5300
  - message = "Invalid scopedProperties requested"
- scopedProperties requested outside of scopedProperties Object
  - code = 5301
  - message = "scopedProperties can only be outside of sessionScopes"

Note: respondents SHOULD to implement support for core RPC Documents per each
supported namespace to avoid sending error messages 5201 and 5202 in cases where
0, 5101 or 5102 would be more appropriate. Failure to do so may leak versioning
or feature-completeness information to a malicious or fingerprinting caller.

## Security Considerations

The crucial security function of a shared session negotiated and maintained by a
series of CAIP-25 calls is to reduce ambiguity in authorization.  This requires
a potentially counterintuitive structuring of the building-blocks of a
Chain-Agnostic session into scopes at the "namespace-wide" ([CAIP-104][]) or at
the "chain-specific" ([CAIP-2][]) level; for this reason, requests and responses
are structures as arrays of objects keyed to these scopes, formatted either as a
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

- 2023-03-29: refactored out scopeObject syntax as separate CAIP-217, simplified
- 2022-11-26: add mandatory indexing by session identifier (i.e. CAIP-171 requirement)
- 2022-10-26: Addressed Berlin Gathering semantics issues and params syntax;
  consolidated variants across issues and forks post-Amsterdam Gathering

## Links

- [CAIP-2][] - Chain ID Specification
- [CAIP-10][] - Account ID Specification
- [CAIP-25][] - JSON-RPC Provider Request
- [CAIP-104][] - Definition of Chain Agnostic Namespaces or CANs
- [CAIP-171][] - Session Identifier, i.e. syntax and usage of `sessionId`s
- [CAIP-217][] - Authorization Scopes, i.e. syntax for `scopeObject`s

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[namespaces]: https://namespaces.chainagnostic.org
[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
