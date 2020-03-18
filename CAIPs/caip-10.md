---
caip: 10
title: Account ID Specification
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/10
status: Draft
type: Standard
created: 2020-03-13
updated: 2020-03-18
requires: 2
---

## Simple Summary

CAIP-10 defines a way to identify an account in any blockchain specified by CAIP-2 blockchain id.

## Abstract

This proposal aims to facilitate specifying accounts for any blockchain extending CAIP-2 blockchain id specification. This is useful for both decentralized applications and wallets to communicate user accounts for multiple chains using string identifiers specific to each chain. Currently wallets are usually designed for each chain and multi-chain wallets use proprietray data structures to differentiate accounts. This proposal aims to standardize these identifiers for accounts to allow interoperability.

## Motivation

The motivation for proposal stem from designing a chain-agnostic protocol for communication between dapps and wallets that was independent of any blockchain but provide the flexibility to be backwards compatible with existing applications.

## Specification

The account id specification will be prefixed with the CAIP-2 blockchain ID and delimited with an at sign (`@`) commonly used to define addresses.

### Syntax

The `account_id` is a case-sensitive string in the form

```
account_id:        account_address + "@" + chain_id
account_address:   [a-zA-Z0-9]{1,63}
chain_id:          [:-a-zA-Z0-9]{5,64}
```

### Semantics

The `account_address` is a case sensitive string which its format is specific to the blockchain that is referred to by the `chain_id`
The `chain_id` is specified by the [CAIP-2](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md) which describes the blockchain id.

## Rationale

The goals of the general account ID format is:
 - Uniqueness between chains regardless if they are mainnet or testnet
 - Readibility using `@` to easily identify the account address
 - Restricted to constrained set of characters and length for parsing
 
## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet
0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb@eip155:1

# Bitcoin mainnet 
128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6@bip122:000000000019d6689c085ae165831e93

# Cosmos Hub (Tendermint + Cosmos SDK)
cosmos1t2uflqwqe0fsj0shcfkrvpukewcw40yjj6hdc0@cosmos:cosmoshub-3

# Dummy max length (16+1+47 = 64 chars/bytes)
bd57219062044ed77c7e5b865339a6d727309c548763141f11e26e9242bbd34@max-namespace-16:xip3343-8c3444cf8970a9e41a706fab93e7a6c4-xxxyyy
```

## Links

n/a

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
