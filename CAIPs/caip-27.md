---
caip: 27
title: JSON-RPC Provider Request
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/27
status: Draft
type: Standard
created: 2020-12-12
updated: 2022-11-16
requires: ["2", "25", "171"]
---

## Simple Summary

CAIP-27 defines a standard JSON-RPC method for requesting methods mapped to a
target chain.

## Abstract

This proposal has the goal to define a standard method for decentralization
applications to request JSON-RPC methods from cryptocurrency wallets directed to
a given target chain.

## Motivation

The motivation comes from the ambiguity that comes from interfacing with
multi-chain cryptocurrency wallets which may support the same methods for
different chains and there is no indication of the chain that is being targeted
by the decentralized application.

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
    "chainId": "eip155:1",
    "session": "0xdeadbeef",
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

The JSON-RPC method is labelled as `caip_request` and expects three parameters:

- chainId - [CAIP-2][]-defined `chainId` to identify both a namespace and a
  specific chain or network within it
- session - [CAIP-171][] `SessionToken` to identify the session opened or
  updated by a [CAIP-25][] interaction.
- request - an object containing the fields:
  - method - JSON-RPC method to request
  - params - JSON-RPC parameters to request

### Response

The wallet will respond to the requested with the targeted chain connection and
it will return a response with a success result or error message.

## Links

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
