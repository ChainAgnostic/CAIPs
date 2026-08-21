---
caip: 95
title: Account Address for the Hive namespace
author: stoodkev (@stoodkev)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/95 https://peakd.com/hive/@stoodkev/hive-caip-10#@josediccus/r72ky5
status: Draft
type: Standard
created: 2022-02-07
updated: 2022-02-10
requires: 10, 94
---

## Simple Summary

This document is about the details of the Hive account address for CAIP-10.

## Abstract

In CAIP-10 a general account address scheme is defined. The definition of
Hive username does not fit neatly into that specification so an alternate account address
format is defined for the Hive blockchain.

## Motivation

See CAIP-10.

## Specification

The `account_id` and `chain_id` from CAIP-10 will remain unchanged. The `account_address` will
be the Hive username as a string.

### Syntax

The `account_id` is a case-sensitive string in the form

```
account_id:        chain_id + ":" + account_address
chain_id:          [:-a-zA-Z0-9]{5,41}
account_address:   ^(?=.{3,16}$)[a-z]([0-9a-z]|[0-9a-z\-](?=[0-9a-z])){2,}([\.](?=[a-z][0-9a-z\-][0-9a-z\-])[a-z]([0-9a-z]|[0-9a-z\-](?=[0-9a-z])){1,}){0,}$
```

### Semantics

The `chain_id` is specified by
the [CAIP-2](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md)
which describes the blockchain id.

The `account_address` corresponds to the unique Hive username.

## Rationale

The account address maps directly to the corresponding Hive username.

## Test Cases

This is a list of manually composed examples

```
# @hiveio account on mainnet
hive:beeab0de000000000000000000000000:hiveio

## Accounts with special characters (`-`,`.`)

# @hive.fund account on mainnet
hive:beeab0de000000000000000000000000:hive.fund

# @hive-blockchain account on testnet
hive:18dcf0a285365fc58b71f18b3d3fec95:hive-blockchain

```

## Backwards Compatibility

CAIP currently limits `account_address` to alphanumerics in the regular
expression definition. If that definition were to include the dot (`.`) and
dash(`-`) then this definition would be a subset of that definition, including
length restrictions.

## Links

- [Hive blockchain](https://hive.io/)
- [CAIP-10](./caip-10.md) Account ID Specification
- [CAIP-94](./caip-94.md) Blockchain Reference for the Hive namespace
- [Hive account regex](https://regex101.com/r/yADKgU/1) For testing Hive accounts regex

## Copyright

Copyright and related rights waived
via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
