---
caip: 78
title: Asset Reference for the Hedera nft namespace
author: Danno Ferrin (@shemnon)
discussions-to: https://github.com/hashgraph/hedera-improvement-proposal/discussions/169
status: Draft
type: Standard
created: 2021-11-01
updated: 2021-11-01
requires: 19, 75
---

## Simple Summary

This document is about the details of the Hedera Token Service 'nft' asset
namespace and reference for CAIP-19.

## Abstract

In CAIP-19 a general asset identification scheme is defined. This is the
implementation of CAIP-19 for `nft` in the
[Hedera Token Service](https://docs.hedera.com/guides/docs/integrating-a-hedera-token-service-token#hedera-token-service)
representing non-fungible tokens.

## Motivation

See CAIP-19.

## Specification

### Token Asset Namespace

The asset namespace is called `nft` hosted in the Hedera Token Service (HTS). It
references HTS non-fungible tokens in the `hedera` namespace (see CAIP-75).

#### Asset Reference Definition

The Asset reference format is the tokenID in the specific hashgraph.

#### Token ID Definition

The Token ID reference format is the serial number of the token.

## Rationale

The tokenID is the canonical reference to the token within hedera. The serial
number uniquely identifies each NFT in the token namespace.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Edition 12 of 50: First-Generation Hedera Robot VENOM EDITION
hedera:mainnet/nft:0.0.55492/12
```

## Links

- [CAIP-19](./caip-19.md) Asset Type and Asset ID Specification
- [CAIP-75](./caip-75.md) Blockchain Reference for the Hedera namespace
- [HIP-15](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-15.md)
  Address Checksum
- [HIP-30](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-30.md)
  CAIP Identifiers for the Hedera Network

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
