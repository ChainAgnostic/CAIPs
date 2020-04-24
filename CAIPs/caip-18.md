---
caip: 18
title: NFT ID Specification
author: Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/18
status: Draft
type: Standard
created: 2020-04-23
updated: 2020-04-23
requires: 10
---

## Simple Summary

CAIP-18 defines a way to identify a specific NFT token on any blockchain specified by CAIP-2 blockchain id.

## Abstract

This proposal aims to facilitate specifying NFT tokens on any blockchain extending CAIP-2 blockchain id specification. This is useful for both decentralized applications and wallets to specify NFT tokens on multiple chains using string identifiers specific to each chain. Currently wallets are usually designed for each chain and multi-chain wallets use proprietary data structures to differentiate NFTs. This proposal aims to standardize these identifiers for NFTs to allow interoperability.

## Motivation

The motivation for proposal stem from designing a chain-agnostic protocol for communication between dapps and wallets that was independent of any blockchain but provide the flexibility to be backwards compatible with existing applications.

## Specification

The NFT id specification will be prefixed with the NFT token ID and the NFT contract address and delimited with an at sign (`@`) followed by a CAIP-2 blockchain ID. Since [CAIP-10](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md) can be used to reference a specific account (which includes contract addresses) we can represent the NFT id as the token specific id concatinated with the CAIP-10 account id.

### Syntax

The `nft_id` is a case-sensitive string in the form

```
nft_id:          token_id + ":" + account_id
token_id:        [0-9]{1,78}
account_id:      [@:-a-zA-Z0-9]{6,128}
```

### Semantics

The `account_id` is represented by a  [CAIP-10](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md) account identifier. In this case the `account_address` is an NFT contract address.

The `token_id` represents an integer (`uint256` on ethereum) that specifies the specific NFT token in the token contract.

## Rationale

The goals of the general NFT ID format is:
 - Uniqueness between chains regardless if they are mainnet or testnet (though CAIP-10)
 - Readability using `:` and`@` to easily identify the NFT identifier and token id
 - Restricted to constrained set of characters and length for parsing

## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet
43628454634268938339309640558483935353102684380863414629699234195440168867178:0xfac7bea255a6990f749363002136af6556b31e04@eip155:1
```

## Links

n/a

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
