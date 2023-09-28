---
caip: 76
title: Account Address for the Hedera namespace
author: Danno Ferrin (@shemnon)
discussions-to: https://github.com/hashgraph/hedera-improvement-proposal/discussions/169
status: Superseded
type: Standard
created: 2021-11-01
updated: 2021-11-01
requires: 10, 75
superseded-by: https://github.com/ChainAgnostic/namespaces/pull/16
---

## Simple Summary

This document is about the details of the Hedera account address for CAIP-10.

## Abstract

In CAIP-10 a general account address scheme is defined. The definitions of
Hedera Address do not fit neatly into that specification so an alternate address
format is defined for Hedera

## Motivation

See CAIP-10.

## Specification

The account_id and chain_id from CAIP-10 will remain unchanged. The address will
be the Hedera account ID as a string.

### Syntax

The `account_id` is a case-sensitive string in the form

```
account_id:        chain_id + ":" + account_address + checksum{0,1}
chain_id:          [:-a-zA-Z0-9]{5,41}
account_address:   [0-9]{1,19} + "." + [0-9]{1,19} + "." + [0-9]{1,19}
checksum:          "-" + [a-z]{5}
```

### Semantics

The `chain_id` is specified by
the [CAIP-2](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md)
which describes the blockchain id. The `account_address` is the realm, shard,
and account id, where each is separated by a dot (`.`) and each number is a
non-negative signed 64-bit integer.

The optional checksum is described in
[HIP-15](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-15.md).
Addresses with or without checksum are valid. Intermediaries have no duty to
validate the validity of the checksum.

## Rationale

The account address maps directly to common Hedera usage.

## Test Cases

This is a list of manually composed examples

```
# Devnet funding account
hedera:devnet:0.0.98

# Mainnet treasury
hedera:mainnet:0.0.2

# Previewnet app propreties account
hedera:previewnet:0.0.121

# Mainnet account with checksum
hedera:mainnet:0.0.123-vfmkw

# Largest possible testnet account
hedera:testnet:9223372036854775807.9223372036854775807.9223372036854775807
```

## Backwards Compatibility

CAIP currently limits `account_address` to alphanumerics in the regular
expression definition. If that definition were to include the dot (`.`) and
dash('-') then this definition would be a subset of that definition, including
length restrictions in most reasonable cases. Since the checksum is optional in
pathological account numbering scenarios it may need to be dropped. It is not
expected that we will see this event in normal usage.

## Links

- [CAIP-10](./caip-10.md) Account ID Specification
- [CAIP-75](./caip-75.md) Blockchain Reference for the Hedera namespace
- [HIP-15](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-15.md)
  Address Checksum
- [HIP-30](https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-30.md)
  CAIP Identifiers for the Hedera Network

## Copyright

Copyright and related rights waived
via [CC0](../LICENSE).
