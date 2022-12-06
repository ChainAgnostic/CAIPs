---
caip: <to be assigned>
title: Blockchain Reference for the Sui namespace
author: zoz (@0xzoz)
discussions-to: https://github.com/MystenLabs/sui/issues/6624
status: Draft
type: Standard
created: 2022-12-06
updated: 2021-11-01
requires: 2
---

## Simple Summary

This document is about the details of the Sui namespace and reference for
CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for `sui` (sui network).

## Motivation

See CAIP-2.

### Sui Namespace

The namespace `sui` refers to the Sui network.

#### Reference Definition

The reference relies on Sui's current network topology addresses being a single production network (mainnet), two persistent testing networks (testnet and devnet) and one refers to local development work (local).
> Sui is in the stages of launching and this is to preempt mainnet when it lauches

Reference should only be populated with `mainnet`, `testnet`, `devnet` and `local` symbols.

### Resolution Method

TBD

## Rationale

TBD 

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Sui mainnet
sui:mainnet
# Sui testnet
sui:testnet
# sui devnet
sui:devnet
```

## Links

- [CAIP-2](./caip-2.md) Blockchain ID Specification
- [Sui Developer Documentation](https://docs.sui.io/)

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).