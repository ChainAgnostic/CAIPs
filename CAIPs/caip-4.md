---
caip: 4
title: Blockchain Reference for Bitcoin
author: Simon Warta (@webmaster128), ligi <ligi@ligi.de>, Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/4, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2019-12-12
requires: 2
---

## Simple Summary

This document is about the details of the Bitcoin namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Bitcoin.

## Motivation

See CAIP-2.

## Specification

### Bitcoin Namespace

The namespace "bitcoin" refers to the wider Bitcoin ecosystem, including multiple forks of the chain and the code.

#### Reference Definition

The definition is delegated to [BIP122](https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki#definition-of-chain-id). The format is `bip122-%s`, where `%s` is a 32 character prefix of the block hash from BIP122 (lower case hex).

## Rationale

We delegate the identification of Bitcoin-like chains to BIP122, as this is the best Bitcoin chain identification standard known to the authors of this CAIP.

In order to prepare for other network identification standards within the Bitcoin namespace, we keep the "bip122-" prefix, which seems unneeded at first glance.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Bitcoin mainnet (see https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki#definition-of-chain-id)
bitcoin:bip122-000000000019d6689c085ae165831e93

# Litecoin
bitcoin:bip122-12a765e31ffd4059bada1e25190f6e98

# Feathercoin (Litecoin fork)
bitcoin:bip122-fdbe99b90c90bae7505796461471d89a
```

## Links

- [BIP122](https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
