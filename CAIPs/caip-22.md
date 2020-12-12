---
caip: 22
title: Asset Reference for the ERC721 Asset Namespace
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid), Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/19
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 19
---

## Simple Summary

This document is about the details of the ERC721 asset namespace and reference for CAIP-19.

## Abstract

In CAIP-19 a general asset identification scheme is defined. This is the
implementation of CAIP-19 for ERC721 (referencing NFT tokens).

## Motivation

See CAIP-19.

## Specification

### ERC721 Asset Namespace

The asset namespace is called "erc721" as in [ERC721](https://eips.ethereum.org/EIPS/eip-721). It reference erc721 tokens (NFT) in the eip155 namespace (cf. CAIP3).

#### Asset Reference Definition

The Asset Reference format is the smart contract address of the erc721 token in the current chain_id.

#### Token ID Definition

The Token Id format is the NFT Identifier of the erc721 specification in the current chain_id.

## Rationale

The smart contract address strives for uniqueness for any erc721 tokens in the chain_id scope.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# CryptoKitties Collectible
eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d

# CryptoKitties Collectible ID
eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d/771769
```

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
