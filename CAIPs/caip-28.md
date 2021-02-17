---
caip: 28
title: Blockchain Reference for Stellar Namespace
author: Gleb Pitsevich (@pitsevich)
discussions-to: TBD
status: Draft
type: Standard
created: 2021-02-17
requires: 2
---

## Simple Summary

This document is about the details of the Stellar network namespace and reference for CAIP-2.

## Abstract
In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for the Stellar network.

## Motivation
See CAIP-2.

## Specification

### Filecoin Namespace

The namespace "stellar" refers to the wider Stellar ecosystem.

#### Reference Definition

The reference relies on Stellar's current designation of addresses belonging to test or main networks by prefixing them
with `testnet` or `pubnet` correspondingly.

Reference could only be populated with `testnet` or `pubnet` symbols.

## Rationale

Blockchains in the "stellar" namespace are [two Stellar public networks](https://developers.stellar.org/docs/glossary/network-passphrase/) - pubnet and testnet.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Testnet
stellar:testnet

# Mainnet
stellar:pubnet
```

## Links

- [Filecoin Specification](https://developers.stellar.org/docs)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
