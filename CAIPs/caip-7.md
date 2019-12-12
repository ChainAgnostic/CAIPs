---
caip: 7
title: Blockchain Reference for Lisk
author: Simon Warta (@webmaster128)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/7, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2019-12-12
requires: 2
---

## Simple Summary

This document is about the details of the Lisk interface for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Lisk.

## Motivation

See CAIP-2.

## Specification

### Interface name

The name of this interface should be "lisk", referring to the wider Lisk ecosystem.

### Reference definition

The definition is delegated to [LIP9](https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md). The reference format is `lip9-%s`, where `%s` is a 16 character prefix of the hash from LIP9 (lower case hex).

## Rationale

This interface should cover Lisk Mainnet and Testnet, Forks, Side chains.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Lisk Mainnet (https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md#appendix-example)
lisk:lip9-9ee11e9df416b18b

# Lisk Testnet (echo -n "da3ed6a45429278bac2666961289ca17ad86595d33b31037615d4b8e8f158bbaLisk" | sha256sum | head -c 16)
lisk:lip9-e48feb88db5b5cf5
```

## Links

- [LIP9](https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
