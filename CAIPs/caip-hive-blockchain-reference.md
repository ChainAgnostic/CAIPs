---
caip: <to be assigned>
title: Blockchain Reference for the Hive Namespace
author: stoodkev (@stoodkev)
discussions-to: https://peakd.com/hive/@stoodkev/hive-caip-2
status: Draft
type: Standard
created: 2022-01-27
updated: 2022-01-27
requires: 2
---

## Simple Summary

This document is about the details of the Hive namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the implementation of CAIP-2 for Hive.

## Motivation

See CAIP-2.

## Specification

### Hive Namespace

The namespace "hive" refers to the Hive ecosystem.

#### Reference Definition

The definition for this namespace will use the `chain_id` as an indentifier for different Hive chains.
The method for calculating the chain ID is as follows with pseudo-code:

```
truncate(chain_id, 32)
```

### Resolution Method

To resolve a blockchain reference for the Hive namespace, make a JSON-RPC request to the blockchain node with method `condenser_api.get_version`, for example:

```jsonc
// Request
{
    "id": 0,
    "jsonrpc": "2.0",
    "method": "condenser_api.get_version",
    "params": []
}

// Response
{
    "id":0,
    "jsonrpc":"2.0",
    "result":
    {
       // ... ,
        "chain_id":"beeab0de00000000000000000000000000000000000000000000000000000000"
    }
}
```

The response will contain a `chain_id` value that should be sliced to its first 32 characters to be CAIP-<to be assigned> compatible.

## Rationale

Blockchains in the "hive" namespace are identified by their chain ID as mentioned in the Reference Definition Section.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Hive mainnet
hive:beeab0de000000000000000000000000

# Hive testnet
hive:18dcf0a285365fc58b71f18b3d3fec95
```

## Links

- [CAIP 2: Blockchain ID Specification](https://github.com/ChainAgnostic/CAIPs/blob/dbaa80c465d5c6cea5c65d95f14223b44f806f69/CAIPs/caip-2.md)
- [Hive Documentation : condenser_api.get_version](https://developers.hive.io/apidefinitions/#condenser_api.get_version)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
