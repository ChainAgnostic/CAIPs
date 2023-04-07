---
caip: 20
title: Asset Reference for the SLIP44 Asset Namespace
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid), Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/19
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 19
---

## Simple Summary

This document is about the details of the SLIP44 asset namespace and reference for CAIP-19.

## Abstract

In CAIP-19 a general asset identification scheme is defined. This is the
implementation of CAIP-19 for SLIP44 (referencing native fungible coins).

## Motivation

See CAIP-19.

## Specification

### SLIP44 Asset Namespace

The asset namespace is called "slip44" as in [SLIP44](https://github.com/satoshilabs/slips/blob/master/slip-0044.md). It reference native fungible coins of most of the existing blockchains.

#### Asset Reference Definition

The definition is delegated to SLIP44. The format is an unsigned integer in decimal representation and corresponds to `index` of SLIP44.

Note: due to length restrictions of the reference field (64 characters), the largest supported `index` is 9999999999999999999999999999999999999999999999999999999999999999.

## Rationale

The `index` defined in SLIP44 is the most widely used coin identifier to the authors. It strives for uniqueness for native coins.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Ether Token
eip155:1/slip44:60

# Bitcoin Token
bip122:000000000019d6689c085ae165831e93/slip44:0

# ATOM Token
cosmos:cosmoshub-3/slip44:118

# Litecoin Token
bip122:12a765e31ffd4059bada1e25190f6e98/slip44:2

# Binance Token
cosmos:Binance-Chain-Tigris/slip44:714

# IOV Token
cosmos:iov-mainnet/slip44:234
```

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
