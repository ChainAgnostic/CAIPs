---
caip: 84
title: Blockchain Reference for the Aleo Namespace
author: Charles E. Lehner (@clehner)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/84
status: Draft
type: Standard
created: 2021-11-19
updated: 2021-11-19
requires: 2
---

## Simple Summary

This document defines a CAIP-2 namespace for Aleo.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Aleo.

## Motivation

See CAIP-2.

## Specification

### Aleo Namespace

The namespace "aleo" refers to the Aleo open-source blockchain platform.

#### Reference Definition

The reference is an Aleo Network ID, that is a decimal-encoded unsigned integer.

## Rationale

Blockchains in the "aleo" namespace are identified by their network ID as mentioned in the Reference Definition Section.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Aleo Mainnet
aleo:0

# Aleo Testnet 1
aleo:1

# Aleo Testnet 2
aleo:2
```

## Links

- [CAIP 2: Blockchain ID Specification](https://github.com/ChainAgnostic/CAIPs/blob/dbaa80c465d5c6cea5c65d95f14223b44f806f69/CAIPs/caip-2.md)
- [Aleo Documentation: Transactions: Network ID](https://developer.aleo.org/aleo/concepts/transactions#network-id)
- [Implementation of Network ID in the snarkVM library](https://github.com/AleoHQ/snarkVM/blob/0178f34c73d0c740199d247df3067727c38809c3/dpc/src/traits/network.rs#L43)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
