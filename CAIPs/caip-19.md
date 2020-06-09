---
caip: 19
title: Native Coin Types Specification
author: Antoine Herzog (@antoineherzog)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/19
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 10
---

## Simple Summary

CAIP-19 defines a way to identify Native Coin Types on any blockchain specified by CAIP-2 blockchain id based on the SLIP44 directory.

## Abstract

This proposal aims to facilitate specifying native coin types id on any blockchain extending CAIP-2 blockchain id specification. This is useful for both decentralized applications and wallets to specify native coin types on multiple chains using string identifiers specific to each chain.

## Motivation

The motivation for proposal stem from designing a chain-agnostic protocol for communication between dapps and wallets that was independent of any blockchain.

## Specification

The Native Coin Types id specification will be prefixed by the CAIP-2 blockchain ID of the native coin type delimited with an at sign (`/`)  followed the index number of the coin types registered in SLIPP44 (https://github.com/satoshilabs/slips/blob/master/slip-0044.md)

### Semantics

## Rationale

The goals of the general Native Coin Types id format is:
 - Uniqueness of coin types between chains
 - Always attach the chainID to the Native Coin Types format because a coin can move to another chain.

## Test Cases

This is a list of manually composed examples

```
# Ether of Ethereum mainnet
eip155:1/60

# Bitcoin of Bitcoin mainnet
bip122:000000000019d6689c085ae165831e93/0

# ATOM of Cosmos Hub
cosmos:cosmoshub-3/118
```

## Links

https://github.com/satoshilabs/slips/blob/master/slip-0044.md

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).