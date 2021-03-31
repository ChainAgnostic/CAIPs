---
caip: 25
title: JSON-RPC Provider Handshake
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/25
status: Draft
type: Standard
created: 2020-10-14
updated: 2020-10-14
requires: 2, 10
---

## Simple Summary

CAIP-25 defines an handshake procedure for a chain agnostic provider to interface with a wallet

## Abstract

This proposal has the goal to define a standard procedure for decentralized application to interface with cryptocurrency wallets with multiple chains and defining a set of rules to be followed during a session managed by a provider.

## Motivation

The motivation comes from the lack of standardization across blockchains to expose accounts and define the expected JSON-RPC methods to be used by an application through a provider connecting to a wallet.

## Specification

The provider is defined within a session once the handshake procedure is successfully approved by a wallet and is considered to be over once the wallet chooses to terminate the session by sending a corresponding event labelled as `disconnect`

### Request

The application would interface with a provider to initiate a session by calling the following JSON-RPC request

```
{
    "id": 1,
    "jsonrpc": "2.0",
    "method": "caip_handshake",
    "params": {
        "chains": ["eip155:1"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "eth_sign", "personal_sign"]
    }
}
```

The JSON-RPC method is labelled as `caip_handshake` and expects two parameters:

* chains - array of CAIP-2 complaint chainId's
* methods - array of JSON-RPC methods expected to be used during the session

### Response

The wallet can respond to this method with either a success result or an error message.

#### Success

The response MUST be a success result when the user approved accounts matching the requested chains to be exposed and the requested methods to be used.

An example of a successful response should match the following format:

```jsonc
{
    "id": 1,
    "jsonrpc": "2.0",
    "result": {
        "accounts": ["0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb@eip155:1"],
    }
}
```

The accounts returned as a result should match the requested chainId's and should be an array of CAIP-10 complaint accountId's

#### Error


The response MUST NOT be a success result when the user disapproves the accounts matching the requested chains to be exposed or the requested methods are not approved or the requested chains are not supported by the wallet or the requested methods are not supported

An example of an error response should match the following format:

```jsonc
{
    "id": 1,
    "jsonrpc": "2.0",
    "error": {
        "code": 5000,
        "message": "User disapproved requested chains",
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
* When wallet evaluates requested chains to not be supported
    * code = 5100
    * message = "Requested chains are not supported"
* When wallet evaluates requested methods to not be supported
    * code = 5101
    * message = "Requested methods are not supported"

## Links

n/a

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
