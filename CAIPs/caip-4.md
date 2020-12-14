---
caip: 4
title: Blockchain Reference for the BIP122 Namespace
author: Simon Warta (@webmaster128), ligi <ligi@ligi.de>, Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/4, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2020-01-16
requires: 2
---

## Simple Summary

This document is about the details of the BIP122 namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for BIP122 (Bitcoin).

## Motivation

See CAIP-2.

## Specification

### BIP122 Namespace

The namespace is called "bip122" as in [BIP122](https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki).

#### Reference Definition

The definition is delegated to [BIP122's chain ID definition](https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki#definition-of-chain-id).
The format is a 32 character prefix of the block hash from BIP122 (lower case hex).

### Resolution Method

To resolve a blockchain reference for the BIP122 namespace, make a JSON-RPC request to the blockchain node with method `getblockhash`, for example:

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "getblockhash",
  "params": [0]
}

// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f"
}
```
The response will return as a value for the result a hash for the block with height 0 that should be sliced to its first 16 bytes (32 characters for base 16) to be CAIP-4 compatible.

## Rationale

We delegate the identification of Bitcoin-like chains to BIP122, as this is the best Bitcoin chain identification standard known to the authors of this CAIP.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Bitcoin mainnet (see https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki#definition-of-chain-id)
bip122:000000000019d6689c085ae165831e93

# Litecoin
bip122:12a765e31ffd4059bada1e25190f6e98

# Feathercoin (Litecoin fork)
bip122:fdbe99b90c90bae7505796461471d89a
```

## Links

- [BIP122](https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
