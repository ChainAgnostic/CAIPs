---
caip: 25
title: JSON-RPC Provider Authorization
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/25
status: Draft
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

The provider is defined within a session once the handshake procedure is
successfully approved by a wallet, and updated, extended, closed, etc by events.
These are out of scope of this CAIP interface and will be specified in a
forthcoming one.

Within that session model, this interface outlines the authorization of an
injected provider per namespace.

The application interfaces with a provider to populate a session with a base
state describing authorized chains, methods, event, and accounts.  This
negotation takes place by sending the application's REQUIRED and REQUESTED
properties of the session. If any requirements are not met, a failure response
expressive of one or more specific failure states will be sent (see below).
Conversely, a succesful response will contain all the required properties *and
the provider's choice of the optional properties* expressed as a unified set of
parameters.

### Request

Example:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "provider_authorization",
  "params": {
    "required": {
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
    "optional":{
      "eip155:42161": {
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "personal_sign"],
        "events": ["accountsChanged", "chainChanged"]
      },
      "sessionProperties": {
        "expiry": "2022-12-24T17:07:31+00:00",
        "auto-refresh": "true"
      }         
    }
  }
}
```

The JSON-RPC method is labelled as `provider_authorization` and both the
"required" and "optional" arrays are populated with objects each
named after the scope of authorization:
1. EITHER an entire ChainAgnostic [namespace][] 
2. OR a specific [CAIP-2][] in that namespace.

Each object contains the following parameters:
- chains - array of [CAIP-2][]-compliant `chainId`'s. This parameter MAY be
  omitted if the chain scope is already given by the name of the object.
- methods - array of JSON-RPC methods expected to be used during the session
- events - array of JSON-RPC message/events expected to be emitted during the
  session

The `required` array MUST contain 1 or more of these objects; the `optional`
array MAY contain 0 or more of them, but MUST NOT be present if empty of any
objects.

A special case is the `sessionProperties` object, which MUST be in the
`optional` array if present, as applications cannot mandate session variables to
providers. Because they are optional, providers MAY respond with all of the
requested properties, or a subset of the session properties, or no
`sessionProperties` object at all; they MAY even replace the values of the
optional session properties with their own values.  Applications are expected to
track all of these returned properties in the session object identified by the
`sessionIdentifier`. All properties and their values MUST conform to definitions
in [CAIP-170][].

### Response

The wallet can respond to this method with either a success result or an error message.

#### Success

The response MUST be structured as a session object containing all required
parameters and all, none, or some of the optional objections.  Optionally, a
`sessionProperties` object may also be present, but its contents MUST conform to
[CAIP-170][] and MAY share all, none, or some of the recommended properties, in
addition to those provided by the provider.

All namespace objects MUST contain an `accounts` array, and at least one of them
must contain an account authorized for use in the constructed session.

The response MUST also include `sessionIdentifier` which is a `sessionIdentifier` as
defined in [caip-171](./caip-171) before the `session` object it identifies.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionIdentifier": "0xdeadbeef",
    "session": {
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
        },      
        "sessionProperties": {
          "expiry": "2022-11-31T17:07:31+00:00"          
        }
    }
  }
}
```

The accounts returned as a result should match the requested `chainId`s and
should be an array of [CAIP-10][] compliant `accountId`s.

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

[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[CAIP-170]: https://chainagnostic.org/CAIPs/caip-170

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
