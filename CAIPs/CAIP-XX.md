---
caip: <to be assigned>
title: Asset Reference for the ERC1155 Asset Namespace
author: Matt Condon (@shrugs)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/44
status: Draft
type: Standard
created: 2021-03-22
updated: 2021-03-22
requires: 19
---

## Simple Summary

This document is about the details of the ERC1155 asset namespace and reference for CAIP-19.

This specification is derived from the work done for [CAIP-19](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-19.md) by Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid), Joel Thorstensson (@oed).

## Abstract

In CAIP-19 a general asset identification scheme is defined. This is the implementation of CAIP-19 for ERC1155 (referencing non-fungible, semi-fungible, and fungible tokens).

## Motivation

See CAIP-19.

## Specification

### ERC1155 Asset Namespace

The asset namespace is called "erc1155" as in [ERC1155](https://eips.ethereum.org/EIPS/eip-1155). It references ERC1155 assets in the eip155 namespace (cf. CAIP-3).

#### Asset Reference Definition

The Asset Reference format is the smart contract address of the erc1155 token in the current `chain_id`.

#### Token ID Definition

The Token Id format is the `tokenId` of the erc1155 specification in the current `chain_id`.

## Rationale

ERC1155 smart contracts represent non-fungible, semi-fungible (i.e. fractional, or editional), and fungible assets. See also CAIP-22.

## Backwards Compatibility

Not applicable.

## Test Cases

This is a list of manually composed examples:

```
# the themanymatts stickers contract
eip155:1/erc1155:0x28959Cf125ccB051E70711D0924a62FB28EAF186

# a specific sticker asset from themanymatts contract
eip155:1/erc1155:0x28959Cf125ccB051E70711D0924a62FB28EAF186/0
```

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
