---
caip: 30
title: Blockchain Reference for the Solana Namespace
author: Antoine Herzog (@antoineherzog), Josh Hundley (@oJshua)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/60
status: Superseded
type: Standard
created: 2021-08-03
updated: 2021-08-03
requires: 2
superseded-by: https://github.com/ChainAgnostic/namespaces/tree/main/solana
---

## Simple Summary

This document is about the details of the Solana namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Solana.

## Motivation

See CAIP-2.

## Specification

### Solana Namespace

The namespace "solana" refers to the Solana open-source blockchain platform.

#### Reference Definition

The definition for this namespace will use the `genesisHash` as an indentifier for different Solana chains.
The method for calculating the chain ID is as follows with pseudo-code:

```
truncate(genesisHash, 32)
```

### Resolution Method

To resolve a blockchain reference for the Solana namespace, make a JSON-RPC request to the blockchain node with method `getGenesisHash`, for example:

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "getGenesisHash"
}

// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": "4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZAMdL4VZHirAn"
}
```

The response will return as a value for the result a hash for the block with height 0 that should be truncated to its first 32 characters to be CAIP-30 compatible.

## Rationale

Blockchains in the "solana" namespace are identified by their chain ID as mentioned in the Reference Definition Section.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Solana Mainnet
solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ

# Solana Devnet
solana:8E9rvCKLFQia2Y35HXjjpWzj8weVo44K
```

## Links

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
