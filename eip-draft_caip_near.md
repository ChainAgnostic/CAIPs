---
caip: 28
title: Blockchain Reference for the NEAR Protocol Namespace
author: Aaron Luhning (@ALuhning), Trevor Clarke (@TrevorJTClarke)
discussions-to: https://github.com/ceramicnetwork/js-ceramic/issues/880, https://github.com/ceramicnetwork/js-ceramic/pull/881
status: Draft
type: Standard
created: 2021-02-04
updated: 2021-02-04
requires 2
---

## Simple Summary

Provides details of the NEAR Protocol namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined.  This is the implementation of CAIP-2 for NEAR Protocol.

# Motivation

See CAIP-2.

## Specification

### NEAR Protocol Namespace

The namespace "near" refers to the wider NEAR Protocol ecosystem.

#### Reference Definition

The definition for this namespace will use the `chain_id` as an identifier for each of the three NEAR Protocol chains. The format is a string equal to either `mainnet`, `testnet` 
or `betanet`.  The chain identifier is appended by a [network id](https://https://chainid.network/).

## Rationale

NEAR Protocol chains are identified by their chain ID as mentioned in the Reference Definition Section.

## Resolution Method

To obtain the chain_id for the NEAR Protocol namespace, make a JSON-RPC request to the appropriate rpc endpoint with method `EXPERIMENTAL_genesis_config`.
RPC endpoints are: `https://rpc.mainnet.near.org`, `https://rpc.testnet.near.org`, or `https://rpc.betanet.near.org`.  For example (testnet):

```jsonc
// Request
{
  "jsonrpc": "2.0",
  "id": "dontcare",
  "method": "EXPERIMENTAL_genesis_config"
}

// Response
{
  "jsonrpc": "2.0",
  "result": {
    "protocol_version": 29,
    "genesis_time": "2020-07-31T03:39:42.911378Z",
    "chain_id": "testnet",
    "genesis_height": 10885359,
    "num_block_producer_seats": 100,
    ...
}
```

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# NEAR MainNet

near:mainnet:1313161554

# NEAR TestNet
near:testnet:1313161555

# NEAR BetaNet
near:betanet:1313161556
```

## Links

Not applicable

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
