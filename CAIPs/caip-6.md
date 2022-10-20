---
caip: 6
title: Blockchain Reference for the LIP9 Namespace
author: Simon Warta (@webmaster128)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/7, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2020-01-16
requires: 2
---

## Simple Summary

This document is about the details of the LIP9 namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for LIP9 (Lisk).

## Motivation

See CAIP-2.

## Specification

### LIP9 Namespace

The namespace is called "lip9" as in [LIP9](https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md).

#### Reference Definition

The definition is delegated to LIP9.
The reference format is a 16 character prefix of the network identifier from LIP9 (lower case hex).

## Rationale

The LIP9 namespace should cover Lisk Mainnet and Testnet, forks and side chains.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Lisk Mainnet (https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md#appendix-example)
lip9:9ee11e9df416b18b

# Lisk Testnet (echo -n "da3ed6a45429278bac2666961289ca17ad86595d33b31037615d4b8e8f158bbaLisk" | sha256sum | head -c 16)
lip9:e48feb88db5b5cf5
```

## Links

- [LIP9](https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
