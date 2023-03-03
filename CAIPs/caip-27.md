---
caip: 27
title: JSON-RPC Provider Request
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/27
status: Draft
type: Standard
created: 2020-12-12
updated: 2023-03-02
requires: ["2", "25", "171"]
---

## Simple Summary

CAIP-27 defines a standard JSON-RPC method for routing method calls through a
CAIP-25 session object.

## Abstract

This proposal has the goal to define a standard method for decentralization
applications to request JSON-RPC methods from user agents (such as
cryptocurrency wallets) directed to a given previously-authorized target network
(such as a specific blockchain).

## Motivation

The motivation comes from the ambiguity that comes from interfacing with a
multi-network agent (e.g. a cryptocurrency wallets which supports the same
method on multiple chains in a namespace, or supports methods with the same name
on multiple namespaces).

## Specification

The JSON-RPC provider is able to make one or more JSON-RPC requests accompanied
by a [CAIP-2][] compatible `chainId` and a keyed to a specific [CAIP-171][]
session. 

### Request

The application would interface with a provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "caip_request",
  "params": {
    "session": "0xdeadbeef",
    "scope": "eip155",
    "chainId": "eip155:1",
    "request": {
      "method": "personal_sign",
      "params": [
        "0x68656c6c6f20776f726c642c207369676e2074657374206d65737361676521",
        "0xa89Df33a6f26c29ea23A9Ff582E865C03132b140"
      ]
    }
  }
}
```

The JSON-RPC method is labeled as `caip_request` and expects 
three **required parameters** and 
one *optional parameter*:

- **session** - [CAIP-171][] `SessionToken` to identify the session opened or
  updated by a [CAIP-25][] interaction.
- **scope** - a `scopeObject` authorized by a [CAIP-25][] response and persisted in
  the session by both caller and respondent
- *chainId* - [CAIP-2][]-defined `chainId` including both namespace and a
  specific chain or network within it, if `scope` is an entire namespace
- **request** - an object containing the fields:
  - **method** - JSON-RPC method to request
  - **params** - JSON-RPC parameters to request (may be empty but must be set)

NOTE: a respondent MUST check the scope, chainId (if application), and method
against their [CAIP-25][] session object before executing or responding to such
a request.

### Response

The wallet will respond to the requested with the targeted chain connection and
it will return a response with a success result or error message.

## Links

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
