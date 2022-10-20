---
caip: 3
title: Blockchain Reference for the EIP155 Namespace
author: Simon Warta (@webmaster128), ligi <ligi@ligi.de>, Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/3, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2020-01-16
requires: 2
superseded-by: https://github.com/ChainAgnostic/namespaces/tree/main/eip155
---

## Simple Summary

This document is about the details of the EIP155 namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for EIP155 (Ethereum).

## Motivation

See CAIP-2.

## Specification

### EIP155 Namespace

The namespace is called "eip155" as in [EIP155](https://eips.ethereum.org/EIPS/eip-155).

#### Reference Definition

The definition is delegated to EIP155. The format is an unsigned integer in decimal representation and corresponds to `CHAIN_ID` of EIP155.

Note: due to length restrictions of the reference field (32 characters), the largest supported `CHAIN_ID` is 99999999999999999999999999999999.

### Resolution Method

To resolve a blockchain reference for the EIP155 namespace, make a JSON-RPC request to the blockchain node with method `eth_chainId`, for example:

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "eth_chainId",
  "params": []
}

// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": "0x1"
}
```

The response will return as a value for the result a base 16 encoded integer that should be converted to base 10 to format a CAIP-3 compatible blockchain reference.

## Rationale

The chain ID defined in EIP155 is the most widely used chain identifier in the Ethereum ecosystem known to the authors. It strives for uniqueness and the fact that the standard is used for replay protection ensure that creators of a new Ethereum network have an incentive to use an ID that is not used elsewhere.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet
eip155:1

# Görli
eip155:5

# Auxilium Network Mainnet
eip155:28945486
```

## Links

- [EIP155](https://eips.ethereum.org/EIPS/eip-155)
- [chainid.network](https://chainid.network/)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
