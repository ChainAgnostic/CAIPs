--
caip: 200
title: BlockExplorer API Routes
author: Pedro Gomes (@pedrouid), ligi (@ligi)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/199
status: Draft
type: Standard
created: 2023-1-11
---

## Simple Summary

Standard API Routes for Blockchain explorers

## Abstract

This CAIP standardizes the routes for addesses and transactions between BlockExplorers.

## Motivation

For the Ethereum ecosystem, Pedro Gomez already created [EIP-3091][] and it has
already helped harmonize block explorers' usage of uniform routes. In the
process of verifying compatibility and conformance for new entries in
[ethereum-lists/chains][], Ligi noticed that only addresses and transactions are
really meaningfully used. Also, the evolution of L2s has seen many drift away from the
concept of blocks, making uniform syntax increasingly difficult. 

For these reasons, this CAIP requires minimal routes that are used most of the
time with the least divergence across L1s and L2s' data structures. Other
routes, or alternate syntaxes that can redirect to these, can be specified in
separate future CAIPs extending this one. This creates a more composable and
piecemeal conformance since many use cases need only these routes.

## Specification

Block explorers will route their webpages accordingly for the following syntax:

### Transactions

`<BLOCK_EXPORER_URL>/tx/<TX_HASH>`

Note: transactions should be addressed here natively, i.e. in the native,
internal syntax of the relevant namespace, rather than universally.

### Addresses

`<BLOCK_EXPLORER_URL>/address/<ACTOR_ADDRESS>`

Note: actor addresses should be addressed here natively, i.e. in the native,
internal syntax of the relevant namespace, rather than in CAIP-10 URN syntax or
any other multi-chain standard.

## Backwards Compatibility

This CAIP was designed with existing API routes in mind to reduce disruption.
Incompatible block explorers can come into conformance by programming redirects
from their existing API routes to the syntax outlined in this EIP, or supporting
both; they are not considered conformant if the syntax above redirects to any
other route. Explorers that conform to [EIP-3091][] are automatically conformant
with this CAIP.

## Links

- [EIP-3091][] - EVM-wide standard for block explorer addressing syntax

[EIP-3091]: https://eips.ethereum.org/EIPS/eip-3091
[ethereum-lists/chains]: https://github.com/ethereum-lists/chains

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
