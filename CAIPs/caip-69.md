---
caip: 69
title: EOA extension to CAIP-10
author: Griff Green (@griffgreen), Krati Jain (@jainkrati), Greg-the-Greek (Please edit Greg!)
status: Draft
type: Standard
created: 2024-03-07
---


## Simple Summary

CAIP-10 defines a way to identify an address in our multichain world using
CAIP-2 blockchain id. CAIP-69 extends it to include a special case for EOAs
using the chain ID of 0.


## Abstract

This proposal aims to facilitate specifying EOAs as multichain accounts
extending CAIP-10 chain id specification. This is useful for both
decentralized applications to communicate that EOAs can be used for multiple 
chains using a chainID of 0 to specify that the account is an EOA. Currently, 
addresses are usually assigned to only one chain using CAIP-10 and an EOA
would be required to be assigned multiple times for each chain. This proposal 
aims to extend the CAIP-10 standard so EOAs can be designated as an EOA once,
and be treated as an EOA on every applicable chain.


## Motivation

The motivation for proposal stems from the redundancy of assigning an EOA
as living on every chain that exists. As new chains continue to be launched
it becomes more and more obvious that assigning an EOA its own specific
designation will greatly simplify their use in cross chain applications.


## Specification

The account id specification will follow CAIP-10, but for EOAs use the 
prefix `eip155:10:` before teh EOA account address


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


## Security Concerns

We should have users sign a message to prove they have an EOA (Greg to fill out more completely)


## Links

- CAIP-10: https://ChainAgnostic.org/CAIPs/caip-10


## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
