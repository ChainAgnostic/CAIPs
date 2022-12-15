---
caip: 25
title: JSON-RPC Provider Authorization
author: Pedro Gomes (@pedrouid)
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
updated, extended, closed, etc by successive calls and events. These are out of
scope of this CAIP interface and will be specified in a forthcoming one.

Within that session model, this interface outlines the authorization of an
injected provider per namespace. These authorization call/responses should be
idempotent, assuming the provider is tracking a session property, referred to by
a `sessionIdentifier` as defined in [CAIP-171][]. If a wallet needs to initiate
a new session, whether due to user input, security policy, or session expiry
reasons, it can simply generate a new session identifier to signal this event to
the calling provider.

The application interfaces with a provider to populate a session with a base
state describing authorized chains, methods, event, and accounts.  This
negotation takes place by sending the application's REQUIRED and REQUESTED
properties of the session. If any requirements are not met, a failure response
expressive of one or more specific failure states will be sent (see below).
Conversely, a succesful response will contain all the required properties *and
the provider's choice of the optional properties* expressed as a unified set of
parameters.

### Request

The application would interface with a provider to authorize that provider with a
given set of parameters by calling the following JSON-RPC request

Example:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "provider_authorization",
  "params": {
    "requiredNamespaces": {
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
    "optionalNamespaces":{
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

The JSON-RPC method is labelled as `provider_authorization` and both the
"requiredNamespaces" and "optionalNamespaces" arrays are populated with 
`namespace` objects each named after the scope of authorization:
1. EITHER an entire ChainAgnostic [namespace][] 
2. OR a specific [CAIP-2][] in that namespace.

Each `namespace` object contains the following parameters:
- chains - array of [CAIP-2][]-compliant `chainId`'s. This parameter MAY be
  omitted if a single-chain scope is already declared in the index of the object.
- methods - array of JSON-RPC methods expected to be used during the session
- events - array of JSON-RPC message/events expected to be emitted during the
  session

The `requiredNamespaces` array MUST contain 1 or more of these objects, if present; the `optionalNamespaces` array MUST contain 1 or more of them, if 
present.

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

The succesfull reslt contains one mandatory string (keyed as `sessionId` with a value 
conformant to [CAIP-171][]) and two session objects, both mandatory and non-empty. 

The first is called `sessionNamespaces` and contains 1 or more namespace objects.
* All required namespaces and all, none, or some of the optional namespaces (at the 
discretion of the provider) MUST be included if successful.  
* As in the request, each namespace object MUST contain `methods` and `events` objects, 
and a `chains` object if a specific chain is not specified in the object's index.
* Unlike the request, each namespace object MUST also contain an `accounts` array, 
containing 0 or more [CAIP-10][] conformant accounts authorized for the session and valid
in the namespace and chain(s) authorized by the object they are in. Additional constraints
on the accounts authorized for a given session MAY be specified in the corresponding 
[namespaces][] specification.

A `sessionProperties` object MAY also be present, and its contents MAY correspond to the
properties requested in the response or not (at the discretion of the provider) but MUST
conform to the properties names and value constraints described in [CAIP-170][]; any other 
MUST be dropped by the requester.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "sessionNamespaces": {
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

The response MUST NOT be a success result when the user disapproves the accounts
matching the requested chains to be exposed or the requested methods are not
approved or the requested chains are not supported by the wallet or the
requested methods are not supported.

An example of an error response should match the following format:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": 5000,
    "message": "User disapproved requested chains"
  }
}
```

The valid error messages codes are the following:
* When user disapproves exposing accounts to requested chains
    * code = 5000
    * message = "User disapproved requested chains"
* When user disapproves accepting calls with the request methods
    * code = 5001
    * message = "User disapproved requested methods"
* When user disapproves accepting calls with the request events
    * code = 5002
    * message = "User disapproved requested events"
* When wallet evaluates requested chains to not be supported
    * code = 5100
    * message = "Requested chains are not supported"
* When wallet evaluates requested methods to not be supported
    * code = 5101
    * message = "Requested methods are not supported"
* When wallet evaluates requested events to not be supported
    * code = 5102
    * message = "Requested events are not supported"
* When a badly-formed request includes a `chainId` mismatched to scope
    * code = 5103
    * message = "Scope/chain mismatch"
* When a badly-formed request defines one `chainId` two ways
    * code = 5104
    * message = "ChainId defined in two different scopes"  
* Invalid Session Properties Object
    * code = 5200
    * message = "Invalid Session Properties requested"
    * Required Session Properties 
    * code = 5201
    * message = "Session Properties can only be optional"

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
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[namespaces]: https://namespaces.chainagnostic.org
[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[CAIP-170]: https://chainagnostic.org/CAIPs/caip-170

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
