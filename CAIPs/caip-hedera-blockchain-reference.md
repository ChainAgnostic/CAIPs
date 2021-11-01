---
caip: <to be assigned>
title: Blockchain Reference for the Hedera namespace
author: Danno Ferrin (@shemnon)
discussions-to: <URL>
status: Draft
type: Standard
created: 2021-11-01
updated: 2021-11-01
requires: 2
---

## Simple Summary

This document is about the details of the Hedera namespace and reference for
CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for `hedera` (Hedera Hashgraph).

## Motivation

See CAIP-2.

### Hedera Namespace

The namespace `hedera` refers to the Hedera Hashgraph network.

#### Reference Definition

The reference relies on Hedera's current network topology addresses being a
single production network (mainnet), two persistent testing networks (testnet
and previewnet) and one refers to local development work (devnet).

Reference should only be populated with `mainnet`, `testnet`, `previewnet`,
and `devnet` symbols.

### Resolution Method

There is no in-protocol resolution method at present. The particular network
configurations defined in the various SDKs set the networks up for mainnet,
testnet, and previewnet. Account checksums
([HIP-15](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-15.md)),
however, are sensitive to the specific network.

## Rationale

Blockchains in the 'hedera' namespace are referenced by name as the number of
total hedera blockchains should be very small: one mainnet and two testnets at
present. Various governance mechanisms in the Hedera governance structure work
to ensure that small number.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Hedera mainnet
hedera:mainnet

# Hedera testnet
hedera:testnet

# Hedera previewnet
hedera:previewnet
```

## Links

- [CAIP-2](./caip-2.md) Blockchain ID Specification
- [HIP-15](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-15.md)
  Address Checksum
- [HIP-30](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-30.md)
  CAIP Identifiers for the Hedera Network
- [Hedera Developer Documentation](https://docs.hedera.com/guides/)

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
