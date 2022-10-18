---
caip: 152
title: Account ID Specification Superset
author: Pedro Gomes (@pedrouid), Juan Caballero (@bumblefudge)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/151
status: Draft
type: Standard
created: 2020-03-13
updated: 2021-08-11
requires: 2
replaces: 10
---

## Simple Summary

A variation of CAIP-10 that allows more addressing systems to be transposed directly into the `account address` value of a CAIP-10 address, including ones that use characters `.` and `%` (and by extension, URL-encoded strings).

## Abstract

CAIP-10 defined a way to identify prefix an account address from any [CASA namespace][namespaces] addressable on a given, valid [CAIP-2 `Chain ID`][caip-2] with the aforementioned information for off-chain or cross-chain use cases. All valid CAIP-10 addresses are valid CAIP-151 addresses, but not vice versa. See [CAIP10#abstract](https://chainagnostic.org/CAIPs/caip-10#abstract).

## Motivation

 The permitted character set of its account address syntax (see regular expressions in [CAIP10#syntax](https://chainagnostic.org/CAIPs/caip-10#syntax) ) prevented its straight-forward application in namespaces where addressing systems use characters like `.` and `%`.  Rather than create translation issues (and break "human readability", i.e. recognizability to developers from that namespace) in shoehorning those addressing syntaxes into the constraints of CAIP-10, a new CAIP would allow implementers to choose and schedule an upgrade of functionality to include a less constrained character set.

## Motivation

The motivation for proposal stem from designing a chain-agnostic protocol for communication between dapps and wallets that was independent of any blockchain but provide the flexibility to be backwards compatible with existing applications.

## Specification

The account id specification will be prefixed with the CAIP-2 blockchain ID and delimited with a colon sign (`:`)

### Syntax

The `account_id` is a case-sensitive string in the form

```
account_id:        chain_id + ":" + account_address
chain_id:          [-a-z0-9]{3,8}:[a-zA-Z0-9]{1,32}
account_address:   [-.%a-zA-Z0-9]{1,64}
```

### Semantics

The `chain_id` is specified by the [CAIP-2](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md) which describes the blockchain id.
The `account_address` is a case sensitive string which its format is specific to the blockchain that is referred to by the `chain_id`

## Rationale

The goals of the general account ID format is:

- Uniqueness between chains regardless if they are mainnet or testnet
- Readibility using the prefix of a chainId to quickly identify before parsing the address
- Restricted to constrained set of characters and length for parsing

## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet
eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb

# Bitcoin mainnet
bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6

# Cosmos Hub
cosmos:cosmoshub-3:cosmos1t2uflqwqe0fsj0shcfkrvpukewcw40yjj6hdc0

# Kusama network
polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy

# Dummy max length (64+1+8+1+32 = 106 chars/bytes)
chainstd:8c3444cf8970a9e41a706fab93e7a6c4:6d9b0b4b9994e8a6afbd3dc3ed983cd51c755afb27cd1dc7825ef59c134a39f7

# Hedera address (includes `.`s)
hedera:mainnet/nft:0.0.55492
```

## Backwards Compatibility

See [CAIP-10#backwards-compatibility](https://chainagnostic.org/CAIPs/caip-10#backwards-compatibility).

## Links

n/a

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

[namespaces]: https://github.com/ChainAgnostic/namespaces
[caip-2]: https://chainagnostic.org/CAIPs/caip-2