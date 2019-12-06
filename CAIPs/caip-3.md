---
caip: 3
title: The Ethereum interface for CAIP-2
author: Simon Warta (@webmaster128), ligi <ligi@ligi.de>, Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/3, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
requires: 2
---

## Simple Summary

This document is about the details of the Ethereum interface for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Ethereum.

## Motivation

See CAIP-2.

## Specification

### Interface name

The name of this interface is "ethereum", referring to the wider Ethereum ecosystem.

### Reference definition

The definition is delegated to [EIP155](https://eips.ethereum.org/EIPS/eip-155). The format is `eip155-%d`, where `%d` is an unsigned integer in decimal represenation and corresponds to `CHAIN_ID` of EIP155.

Note: due to length restrictions of the reference field (47 characters), the largest supported `CHAIN_ID` is 9999999999999999999999999999999999999999.

## Rationale

The chain ID defined in EIP155 is the most widely used chain identifier in the Ethereum ecosystem known to the authors. It strives for uniqueness and the fact that the standard is used for replay protection ensure that creators of a new Ethereum network have an incentive to use an ID that is not used elsewhere.

In order to prepare for other network identification standards within the Ethereum interface, we keep the "eip155-" prefix, which seems unneeded at first glance.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet
ethereum:eip155-1

# Görli
ethereum:eip155-5

# Auxilium Network Mainnet
ethereum:eip155-28945486
```

## Links

- [EIP155](https://eips.ethereum.org/EIPS/eip-155)
- [chainid.network](https://chainid.network/)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
