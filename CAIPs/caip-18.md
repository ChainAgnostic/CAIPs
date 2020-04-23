---
caip: 18
title: NFT ID Specification
author: Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/18
status: Draft
type: Standard
created: 2020-04-23
updated: 2020-04-23
requires: 2
---

## Simple Summary

CAIP-18 defines a way to identify a specific NFT token on any blockchain specified by CAIP-2 blockchain id.

## Abstract

This proposal aims to facilitate specifying NFT tokens on any blockchain extending CAIP-2 blockchain id specification. This is useful for both decentralized applications and wallets to specify NFT tokens on multiple chains using string identifiers specific to each chain. Currently wallets are usually designed for each chain and multi-chain wallets use proprietray data structures to differentiate NFTs. This proposal aims to standardize these identifiers for NFTs to allow interoperability.

## Motivation

The motivation for proposal stem from designing a chain-agnostic protocol for communication between dapps and wallets that was independent of any blockchain but provide the flexibility to be backwards compatible with existing applications.

## Specification

The NFT id specification will be prefixed with the NFT token ID and delimited with an at sign (`@`) followed by a CAIP-2 blockchain ID.

### Syntax

The `account_id` is a case-sensitive string in the form

```
account_id:        nft_id + "@" + chain_id
nft_id:            contract_address + ":" + token_id
contract_address:  [a-zA-Z0-9]{1,63}
token_id:          [0-9]{1,78}
chain_id:          [:-a-zA-Z0-9]{5,64}
```

### Semantics

The `contract_address` is a case sensitive string which its format is specific to the blockchain that is referred to by the `chain_id`. It represents the address of the token contract of the given NFT type. This is very similar to `account_address` in [CAIP-10](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md)
The `token_id` represents an integer (`uint256` on ethereum) that specifies the specific NFT token in the token contract.
The `chain_id` is specified by the [CAIP-2](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md) which describes the blockchain id.

## Rationale

The goals of the general account ID format is:
 - Uniqueness between chains regardless if they are mainnet or testnet
 - Readibility using `@` to easily identify the NFT identifier
 - Restricted to constrained set of characters and length for parsing
 
## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet
0xfac7bea255a6990f749363002136af6556b31e04:43628454634268938339309640558483935353102684380863414629699234195440168867178@eip155:1
```

## Links

n/a

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
