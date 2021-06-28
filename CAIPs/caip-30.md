---
caip: 30
title: Blockchain Reference for the Solana Namespace
author: Antoine Herzog (@antoineherzog)
discussions-to: []
status: Draft
type: Standard
created: 2021-06-28
updated: 2021-06-28
requires: 2
---

## Simple Summary

This document is about the details of the Solana namespaces and references for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Solana.

## Motivation

See CAIP-2.

## Specification

### Solana Namespace

The namespace "solana" refers to the Solana open-source blockchain platform.

#### Reference Definition

The definition for this namespace will use the `blockhash` as an indentifier for different Solana chains.
The method for calculating the chain ID is as follows with pseudo-code:

```
firstSixteenBytes((SHA256(blockhash))
```

### Resolution Method

To resolve a blockchain reference for the Solana namespace, make a JSON-RPC request to the blockchain node with method `getBlock`, for example:

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "getBlock",
  "params":[0]
}

// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
      "blockHeight": 0,
      "blockhash": "4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZAMdL4VZHirAn",
  }
}
```

The response will return as a value for the result a hash for the block with height 0 that should be sliced to its first 16 bytes (32 characters for base 16) to be CAIP-4 compatible.


## Rationale

Blockchains in the "solana" namespace are identified by their chain ID as mentioned in the Reference Definition Section.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Solana Mainnet
solana:db2ede332dd35306fd5a0b879194549b

# Solana Devnet
solana:55d16975dbaf9e1a68e017c171209415
```

## Links


## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).