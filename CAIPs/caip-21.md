---
caip: 21
title: Asset Reference for the ERC20 Asset Namespace
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/20
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 10
---

## Simple Summary

This document is about the details of the ERC20 asset namespace and reference for CAIP-19.

## Abstract

In CAIP-19 a general asset identification scheme is defined. This is the
implementation of CAIP-19 for ERC20 (referencing erc20 tokens).

## Motivation

See CAIP-19.

## Specification

### ERC20 Asset Namespace

The asset namespace is called "erc20" as in [ERC20](https://eips.ethereum.org/EIPS/eip-20). It reference erc20 tokens in the eip155 namespace (cf. CAIP3).

#### Asset Reference Definition

The Asset Reference format is the smart contract address of the erc20 token in the current chain_id.

## Rationale

The smart contract address strives for uniqueness for any erc20 tokens in the chain_id scope.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# DAI Token
eip155:1/erc20:0x6b175474e89094c44da98b954eedeac495271d0f

# REQ Token
eip155:1/erc20:0x8f8221afbb33998d8584a2b05749ba73c37a938a
```

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).