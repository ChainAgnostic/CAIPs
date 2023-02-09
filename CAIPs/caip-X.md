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

For the Ethereum ecosystem Pedro Gomez already created EIP-3091 and it already helped getting explorers to use the same routes. Ligi was verifying compatibility in ethereum-lists/chains often. And noticed that only addresses and transactions are really meaningfully used - and tokens/blocks where causing work verifying and failing verifications while not really being used. Also some L2s go away from the concept of blocks.
This is a CAIP with minimal routes that are used most of the time. If other routes are used in the future other CAIPs can inherit from this one and extend it. This way we can also differentiate between CAIPs with different feature-sets.

## Specification

Block explorers will route their webpages accordingly for the following data:

### Blocks

`<BLOCK_EXPORER_URL>/block/<BLOCK_HASH_OR_HEIGHT>`

### Transactions

`<BLOCK_EXPORER_URL>/tx/<TX_HASH>`

### Addresses

`<BLOCK_EXPORER_URL>/address/<ACCOUNT_ADDRESS>`

## Backwards Compatibility

This EIP was designed with existing API routes in mind to reduce disruption. Incompatible block explorers should include either 301 redirects to their existing API routes to match this EIP.
Explorers compatible with EIP-3091 are automatically compatible with this CAIP.

## Links

- [EIP-3091](https://eips.ethereum.org/EIPS/eip-3091)

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).