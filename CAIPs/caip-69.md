---
caip: 69
title: EOA extension to CAIP-10
author: Griff Green (@griffgreen), Krati Jain (@jainkrati), Gregory Markou (@GregTheGreek)
status: Draft
type: Standard
created: 2024-03-07
---


## Simple Summary

CAIP-69 extends the CAIP-10 address format to include an identifier if the given address is an externally owned account (EOA).


## Abstract

This proposal aims to facilitate specifying EOAs as multichain accounts extending CAIP-10's chainID specification. This will enable decentralized applications to specify EOAs as an address with chainID 0 signifying that it can live on multiple networks. Currently, addresses are usually assigned to only one chain using CAIP-10 and an EOA would be required to be assigned multiple times for each chain. This proposal aims to extend the CAIP-10 standard so EOAs can be designated as an EOA once, and be treated as an EOA on every applicable chain.


## Motivation

The motivation for proposal stems from the redundancy of assigning an EOA as living on every chain that exists. As new chains continue to be launched it becomes more and more obvious that assigning an EOA its own specific designation will greatly simplify their use in cross chain applications.


## Specification

The blockchain ID will be ammended to contain a new parameter, `eoa_flag`, that will be a boolean (denoted by the integer 0), such that 0 denotes an EOA, and 1 denotes an address that only exists on Ethereum.

### Syntax

The `chain_id` string will be ammended as follows:

```
chain_id:    namespace + ":" + eoa_flag + ":" + reference
eoa_flag:    [0-9]{1,1}
namespace:   [-a-z0-9]{3,8}
reference:   [-_a-zA-Z0-9]{1,32}
```

## Rationale

The goals of the EOA designation is:
- Simplify the registration of EOAs in applications
- Avoid changing the standards that exist in any meaningful way.
- Full backwards compatibility for the current CAIP-10 standard users.


## Usage

```
# EOA
eip155:0:0x839395e20bbB182fa440d08F850E6c7A8f6F0780
```
```
# Ethereum Mainnet
eip155:1:0x6B175474E89094C44Da98b954EedeAC495271d0F
```

## Security Concerns

We should have users sign a message to prove they have an EOA (Greg to fill out more completely)


## Links

- CAIP-10: https://ChainAgnostic.org/CAIPs/caip-10


## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
