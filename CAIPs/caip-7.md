---
caip: 7
title: Blockchain Reference for the EOSIO Namespace
author: Sebastian Montero (@sebastianmontero)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/32
status: Draft
type: Standard
created: 2020-11-30
updated: 2020-11-30
requires: 2
---

## Simple Summary

This document is about the details of the EOSIO namespaces and references for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for EOSIO.

## Motivation

See CAIP-2.

## Specification

### EOSIO Namespace

The namespace "eosio" refers to the EOSIO open-source blockchain platform.

#### Reference Definition

The definition is delegated to the [EOSIO Transactions Protocol Documentation](https://developers.eos.io/welcome/v2.0/protocol/transactions_protocol/#32-sign-transaction), the [Chain API Plugin Documentation of the EOSIO Developers Manual](https://developers.eos.io/manuals/eos/latest/nodeos/plugins/chain_api_plugin/api-reference/index?query=chain%20id&page=1#operation/get_info) and the pull request that implemented its generation [Chain ID generation implementation pull request](https://github.com/EOSIO/eos/pull/3425). 
The Chain ID is the SHA256 hash of the genesis state of the chain, and its represented as hexadecimal number of 64 digits

## Rationale

Blockchains in the "eosio" namespace are identified by their Chain ID as mentioned in the Reference Definition Section.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# EOS Mainnet
eosio:aca376f206b8fc25a6ed44dbdc66547c36c6c33e3a119ffbeaef943642f0e906

# Jungle Testnet
eosio:e70aaab8997e1dfce58fbfac80cbbb8fecec7b99cf982a9444273cbc64c41473

# Telos Mainnet
eosio:4667b205c6838ef70ff7988f6e8257e8be0e1284a2f59699054a018f743b1d11

# Telos Testnet
eosio:1eaa0824707c8c16bd25145493bf062aecddfeb56c736f6ba6397f3195f33c9f
```

## Links

- [Chain API Plugin Documentation of the EOSIO Developers Manual](https://developers.eos.io/manuals/eos/latest/nodeos/plugins/chain_api_plugin/api-reference/index?query=chain%20id&page=1#operation/get_info)
- [Chain ID generation implementation pull request](https://github.com/EOSIO/eos/pull/3425)
- [EOSIO Transactions Protocol Documentation](https://developers.eos.io/welcome/v2.0/protocol/transactions_protocol/#32-sign-transaction)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
