---
caip: 13
title: Blockchain Reference for the Polkadot Namespace
author: Pedro Gomes (@pedrouid), Joshua Mir (@joshua-mir), Shawn Tabrizi (@shawntabrizi)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/13
status: Superseded
type: Standard
created: 2020-04-01
updated: 2020-04-02
requires: 2
superseded-by: https://github.com/ChainAgnostic/namespaces/pull/6
---

## Simple Summary

This document is about the details of the Polkadot namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Polkadot chains.

## Motivation

See CAIP-2.

## Specification

### Polkadot Namespace

The namespace is called "polkadot" to refer to Polkadot-like chains.

#### Reference Definition

The definition for this namespace will use the `genesis-hash` as an identifier for different Polkadot chains. The format is a 32 character prefix of the block hash (lower case hex).

### Resolution Method

To resolve a blockchain reference for the Polkadot namespace, make a JSON-RPC request to the blockchain node with method `chain_getBlockHash`, for example:

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "chain_getBlockHash",
  "params": [0]
}

// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": "0x91b171bb158e2d3848fa23a9f1c25182fb8e20313b2c1eb49219da7a70ce90c3"
}
```

The response will return as a value for the result a hash for the block with height 0 that should be sliced to its first 16 bytes (32 characters for base 16) to be CAIP-13 compatible.

## Rationale

The rationale behind the use of block hash from the genesis block stems from its usage in the Polkadot architecture in network and consensus.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Kusama
polkadot:b0a8d493285c2df73290dfb7e61f870f

# Edgeware
polkadot:742a2ca70c2fda6cee4f8df98d64c4c6

# Kulupu
polkadot:37e1f8125397a98630013a4dff89b54c
```

## Links

Not applicable

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
