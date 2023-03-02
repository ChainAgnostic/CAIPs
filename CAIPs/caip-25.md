---
caip: 25
title: JSON-RPC Provider Authorization
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/25
status: Review
type: Standard
created: 2020-10-14
updated: 2022-10-26
requires: [2, 10, 171]
---

## Simple Summary

CAIP-25 defines an authorization procedure for a chain agnostic provider to
interface with a wallet as part of their initialization and/or "handshake"
protocol.

## Abstract

This proposal has the goal to define a standard procedure for decentralized
applications to interface with cryptocurrency wallets which govern accounts on
multiple chains and defining a set of rules to be followed during a session
managed by a provider construct.

## Motivation

The motivation comes from the lack of standardization across blockchains to
expose accounts and define the expected JSON-RPC methods to be used by an
application through a provider connecting to a wallet.

## Specification

The session is defined by a wallet's response to a provider's request, and
updated, extended, closed, etc by successive calls and events. The exact
parameters and assumptions of that session abstraction are defined in
[CAIP-171][], but note that a string identifier referring to it is absent from
the initial call (if authorization is granted) and present in both the initial
response and all future responses.

Given the session model of [CAIP-171][], this interface outlines the
authorization of a provider to handle a set of interfaces grouped into
namespaces, as well as to interact with a session abstraction used by both
caller and respondent to manage the authorization over time. The
`sessionIdentifier` defined in [CAIP-171][] enables this mutual management and
alignment across calls that are idempotent if identical. If a respondent (e.g. a
wallet) needs to initiate a new session, whether due to user input, security
policy, or session expiry reasons, it can simply generate a new session
identifier to signal this event to the calling provider; if a caller needs to
initiate a new session, it can do so by sending a new request without
`sessionIdentifier`. In such cases, a respondent (e.g. wallet) may choose to
explicitly close all sessions upon generation of a new one from the same origin,
or leave it to time-out; maintaining concurrent sessions is discouraged (see
Security Considerations).

In the initial call, the application interfaces with a provider to populate a
session with a base state describing authorized chains, methods, event, and
accounts.  This negotation takes place by sending the application's REQUIRED and
REQUESTED authorizations of the session, grouped into objects scoping those
authorizations which in turn are grouped into two top-level arrays (named
`requiredScopes` and `optionalScopes` respectively).  These two arrays are not
mutually exclusive (i.e., additional properties of a required scope may be
requested under the same keyed scope object key in the requested array). Note
that scopes can be keyed to an entire [CAIP-104][] "namespace", meaning
applicable to *any* current or future [CAIP-2][] chainID within that namespace,
or keyed to a specific [CAIP-2][] within that namespace.

If any properties in the required scope(s) are not authorized by the
respondent (e.g. wallet), a failure response expressive of one or more specific
failure states will be sent (see [#### failure states](#failure-states) below),
with the exception of user denying consent. For privacy reasons, an `undefined`
response (or no response, depending on implementation) should be sent to prevent
incentivizing unwanted requests and to minimize the surface for fingerprinting
of public web traffic (See Privacy Considerations below).

Conversely, a succesful response will contain all the required properties *and
the provider's choice of the optional properties* expressed as a unified set of
parameters. In the case of identically-keyed scopes appearing in both arrays in
the request where properties from both are returned as authorized, the two
scopes MUST be merged in the response (see examples below). However, respondents
MUST NOT restructure scopes (e.g., by folding properties from a [CAIP2][]-keyed,
chain-specific scope object into a [CAIP-104][]-keyed, namespace-wide scope
object) as this may introduce ambiguities (See Security Considerations below).

### Request

The application would interface with a provider to authorize that provider with a
given set of parameters by calling the following JSON-RPC request

Example:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "provider_authorize",
  "params": {
    "requiredScopes": {
      "eip155": {
        "chains": ["eip155:1", "eip155:137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "eth_sign", "get_balance", "personal_sign"],
        "events": ["accountsChanged", "chainChanged"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "events": ["accountsChanged", "chainChanged"]
      },
      "cosmos": {
        ...
      }
    },
    "optionalScopes":{
      "eip155:42161": {
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "personal_sign"],
        "events": ["accountsChanged", "chainChanged"]
    },
    "sessionProperties": {
      "expiry": "2022-12-24T17:07:31+00:00",
      "caip154-mandatory": "true"
    }         
  }
}
```

The JSON-RPC method is labelled as `provider_authorize` and both the
"requiredScopes" and "optionalScopes" arrays are populated with 
"scope objects" each named after the scope of authorization requested:
1. EITHER an entire [CAIP-104][] [namespace][]
2. OR a specific [CAIP-2][]-identified chain in a specific namespace.

Each scope object contains the following parameters:
- chains - array of [CAIP-2][]-compliant `chainId`'s. This parameter MAY be
  omitted if a single-chain scope is already declared in the index of the object.
- methods - array of JSON-RPC methods expected to be used during the session
- events - array of JSON-RPC message/events expected to be emitted during the
  session

The `requiredScopes` array MUST contain 1 or more of these objects, if present;
the `optionalScopes` array MUST contain 1 or more of them, if present.

A third object is the `sessionProperties` object, all of whose properties MUST 
be in the interpreted as optional, since requesting applications cannot mandate
session variables to providers. Because they are optional, providers MAY respond
with all of the requested properties, or a subset of the session properties, or no
`sessionProperties` object at all; they MAY even replace the values of the
optional session properties with their own values.  The `sessionProperties` 
object MUST contain 1 or more properties if present.

Requesting applications are expected to track all of these returned properties in
the session object identified by the `sessionId`. All properties and their values
MUST conform to definitions in [CAIP-170][], and MUST be ignored (rather than 
tracked) if they do not.

### Response

The wallet can respond to this method with either a success result or an error message.

#### Success

The successful result contains one mandatory string (keyed as `sessionId` with a value 
conformant to [CAIP-171][]) and two session objects, both mandatory and non-empty. 

The first is called `sessionScopes` and contains 1 or more scope objects.
* All required scope objects and all, none, or some of the optional scope object
(at the discretion of the provider) MUST be included if successful.  
* As in the request, each scope object object MUST contain `methods` and
`events` objects, and a `chains` object if a specific chain is not specified in
the object's index.
* Unlike the request, each scope object MUST also contain an `accounts` array,
containing 0 or more [CAIP-10][] conformant accounts authorized for the session
and valid in the namespace and chain(s) authorized by the scope object they are
in. Additional constraints on the accounts authorized for a given session MAY be
specified in the corresponding [CAIP-104][] namespaces specification.

A `sessionProperties` object MAY also be present, and its contents MAY
correspond to the properties requested in the response or not (at the discretion
of the provider) but MUST conform to the property names and value constraints
described in [CAIP-170][]; any other MUST be dropped by the requester.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "sessionScopes": {
      "eip155": {
        "chains": ["eip155:1", "eip155:137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "eth_sign", "personal_sign"]
        "events": ["accountsChanged", "chainChanged"],
        "accounts": ["eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb", "eip155:137:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "events": ["accountsChanged", "chainChanged"],
        "accounts:" []
      },
      "eip155:42161": {
        "methods": ["personal_sign"],
        "events": ["accountsChanged", "chainChanged"],
        "accounts":["eip155:42161:0x0910e12C68d02B561a34569E1367c9AAb42bd810"]
      "cosmos": {
        ...
      }
    },      
    "sessionProperties": {
      "expiry": "2022-11-31T17:07:31+00:00"          
    }
  }
}
```

#### Failure States

The response MUST NOT be a JSON-RPC success result in any of the following
failure states.

##### Generic Failure Code

Unless the dapp is known to the wallet and trusted, the generic/undefined error
response,

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
  approved chains,
- the user denies consent for requested methods,
- the user denies all requested or any required scope objects,
- the wallet cannot support all requested or any required scope objects,
- the requested chains are not supported by the wallet, or 
- the requested methods are not supported by the wallet

##### Trusted Failure Codes

Send more informative error messages MAY be used in trusted-counterparty
circumstances, although extending this trust too widely may contribute to
widespread fingerprinting and analytics which corrode herd privacy (see 
[Privacy Considerations](#privacy-considerations) below). The core error 
messages over trusted connections are as follows:

The valid error messages codes are the following:
* Unknown error OR no scopes were authorized
    * code = 5000
    * message = "Unknown error with request"
* When user disapproves accepting calls with the request methods
    * code = 5001
    * message = "User disapproved requested methods"
* When user disapproves accepting calls with the request events
    * code = 5002
    * message = "User disapproved requested events"
* When provider evaluates requested chains to not be supported
    * code = 5100
    * message = "Requested chains are not supported"
* When provider evaluates requested methods to not be supported
    * code = 5101
    * message = "Requested methods are not supported"
* When provider evaluates requested events to not be supported
    * code = 5102
    * message = "Requested events are not supported"

##### Trust-Agnostic Malformed Request Failure Codes

Regardless of caller trust level, the following error responses can reduce
friction and user experience problems in the case of malformed requests. 

* When provider does not recognize one or more requested method(s)
    * code = 5201
    * message = "Unknown method(s) requested"
* When provider does not recognize one or more requested event(s)
    * code = 5202
    * message = "Unknown event(s) requested"
* When a badly-formed request includes a `chainId` mismatched to scope
    * code = 5203
    * message = "Scope/chain mismatch"
* When a badly-formed request defines one `chainId` two ways
    * code = 5204
    * message = "ChainId defined in two different scopes"  
* Invalid Session Properties Object
    * code = 5300
    * message = "Invalid Session Properties requested"
* Session Properties requested outside of Session Properties Object 
    * code = 5301
    * message = "Session Properties can only be optional and global"

Note: respondents are RECOMMENDED to implement support for core RPC Documents
per each supported namespace to avoid sending error messages 5201 and 5202 in
cases where 0, 5101 or 5102 would be more appropriate.

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
1. which the respondent does not authorize, 
2. which are rejected by policy, or 
3. requests which are rejected for unknown reasons. 
 
"Ignoring" these calls means responding to all three in a way that is
*indistinguishable* to a malicious caller or observer which might deduce
information from differences in those responses (including the time taken to
provide them). Effectively, this means allowing requests in all three cases to
time out even if the end-user experience might be better served by
differentiating them, particularly in complex multi-party architectures where
parties on one side of this interface need to have a shared understanding of why
a request did not receive a response. At scale, however, better user experiences in a single architecture or context can contribute to a systemic erosion of anonymity.

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

- 2022-11-26: add mandatory indexing by session identifier (i.e. CAIP-171 requirement) 
- 2022-10-26: Addressed Berlin Gathering semantics issues and params syntax;
  consolidated variants across issues and forks post-Amsterdam Gathering

## Links

- [CAIP-2][] - Chain ID Specification
- [CAIP-10][] - Account ID Specification
- [CAIP-25][] - JSON-RPC Provider Request
- [CAIP-75][] - Blockchain Reference for the Hedera namespace
- [CAIP-171][] - Session Identifier Specification

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-75]: https://chainagnostic.org/CAIPs/caip-75
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[namespaces]: https://namespaces.chainagnostic.org
[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[CAIP-170]: https://chainagnostic.org/CAIPs/caip-170

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
