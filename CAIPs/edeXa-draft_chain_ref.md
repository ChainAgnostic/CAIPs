---
namespace-identifier: <to be assigned>
title: Blockchain Reference for the edeXa Namespace
author: Shubham Koli (@shubham-edx), Ranjith Kumar (@ranjith-edx)
discussions-to: []
status: Draft
type: Standard
created: 2023-01-23
requires: 2
---

## Simple Summary

This document is about the details of the edeXa namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for edeXa.

## Motivation

See CAIP-2.

## Specification

The JSON-RPC provider is able to make one or more JSON-RPC requests accompanied
by a [CAIP-2][] compatible `chainId`

### Resolution Method

To resolve a blockchain reference for the EIP155 namespace, make a JSON-RPC
request to a blockchain node with method `eth_chainId`, for example:

```
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "eth_chainId",
  "params": []
}

// Response
{
    "jsonrpc": "2.0",
    "id": 1,
    "result": "0x7cb"
}
```

The response will return a base-16-encoded integer that should be converted to
base 10 to format an EIP155-compatible blockchain reference.

## Test Cases

This is a list of manually composed examples

```
# edeXa testnet
eip155:1995

```

## References

- [EIP155][]: Ethereum Improvement Proposal specifying generation and validation of ChainIDs


## Rights

Copyright and related rights waived via [CC0](../LICENSE).
