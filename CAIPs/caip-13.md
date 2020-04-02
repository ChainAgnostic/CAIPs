---
caip: 13
title: Blockchain Reference for the Polkadot Namespace
author: Pedro Gomes (@pedrouid), Joshua Mir (@joshua-mir), Shawn Tabrizi (@shawntabrizi)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/13
status: Draft
type: Standard
created: 2020-04-01
updated: 2020-04-02
requires: 2
---

## Simple Summary

This document is about the details of the Polkadot namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Polkadot chains.

## Motivation

See CAIP-2.

## Specification

### Polkadot Namespace

The namespace is called "polkadot" to refer to Polkadot-like chains.

#### Reference Definition

The definition for this namespace will use the `genesis-hash` as an indentifier for different Polkadot chains. The format is a 32 character prefix of the block hash (lower case hex).

## Rationale

The rationale behind the use of block hash from the genesis block stems from its usage in the Polkadot architecture in network and consensus.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Kusama
polkadot:b0a8d493285c2df73290dfb7e61f870f

# Edgeware
polkadot:742a2ca70c2fda6cee4f8df98d64c4c6

# Kulupu
polkadot:37e1f8125397a98630013a4dff89b54c
```

## Links

Not applicable

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
