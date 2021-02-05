---
caip: 28
title: Blockchain Reference for the NEAR Protocol Namespace
author: Aaron Luhning (@ALuhning), Trevor Clarke (@TrevorJTClarke)
discussions-to: https://github.com/ceramicnetwork/js-ceramic/issues/880, https://github.com/ceramicnetwork/js-ceramic/pull/881
status: Draft
type: Standard
created: 2021-02-04
updated: 2021-02-04
requires: 2
---

## Simple Summary

Defines the NEAR Protocol namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined.  This is the implementation of CAIP-2 for NEAR Protocol.

# Motivation

See CAIP-2.

## Specification

### NEAR Protocol Namespace

The namespace "near" refers to the wider [NEAR Protocol](https://near.org) ecosystem.

#### Reference Definition

The definition for this namespace will use the `chain_id` as an identifier for each of the three NEAR Protocol chains. The format is a string equal to either `mainnet`, `testnet` or `betanet`.  The chain identifier is appended by a [network id](https://chainid.network/).

## Rationale

Blockchains in the "near" namespace are [NEAR Core](https://github.com/near/nearcore) blockchains. NEAR Protocol is made up sharded nearcore blockchains, where shards can have features like Ethereum EVM.

The chain ID defined in EIP155 is the most widely used chain identifier in the Ethereum ecosystem known to the authors. It strives for uniqueness and the fact that the standard is used for replay protection ensure that creators of a new Ethereum network have an incentive to use an ID that is not used elsewhere.

Since there is EVM compatible shards, EIP155 is specified in addition to the root shard definitions. As more shards enable different capabilities, additional CAIPs will need specification.

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
# NEAR Protocol Root Shards
near:mainnet
near:testnet
near:betanet
near:guildnet

# NEAR EVM Shard: Mainnet
eip155:1313161554

# NEAR EVM Shard: Testnet
eip155:1313161555

# NEAR EVM Shard: Betanet
eip155:1313161556
```

## Links

- [Nomicon Accounts](https://nomicon.io/DataStructures/Account.html)
- [EIP155](https://eips.ethereum.org/EIPS/eip-155)
- [chainid.network](https://chainid.network/)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
