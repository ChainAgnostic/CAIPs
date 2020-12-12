---
caip: 26
title: Blockchain Reference for the Tezos Namespace
author: Stanly Johnson (@stanly-johnson)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/36
status: Draft
type: Standard
created: 2020-12-12
updated: 2020-12-12
requires: 2
---

## Simple Summary

This document is about the details of the Tezos namespaces and references for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Tezos.

## Motivation

See CAIP-2.

## Specification

### Tezos Namespace

The namespace "tezos" refers to the Tezos open-source blockchain platform.

#### Reference Definition

The definition for this namespace will use the `genesis-block-hash` as an indentifier for different Tezos chains. The method for calculating the chain Id is as follows 
```
tezosB58CheckEncode('Net',
  firstFourBytes(
    blake2b(msg = tezosB58CheckDecode('B', genesisBlockHash),
            size = 32)))
```

## Rationale

Blockchains in the "tezos" namespace are identified by their Chain ID as mentioned in the Reference Definition Section.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Tezos Mainnet
tezos:NetXdQprcVkpaWU

# Tezos DelphiNet (Current active testnet)
tezos:NetXm8tYqnMWky1
```

## Links

- [Implementation](https://gitlab.com/tezos/tezos/blob/e7612c5ffa46570cdcc612f7bcead771edc24283/src/lib_crypto/chain_id.ml)


## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
