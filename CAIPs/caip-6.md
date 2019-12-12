---
caip: 6
title: Blockchain Reference for Cosmos (Hashed Variant)
author: Simon Warta (@webmaster128)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/6, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2019-12-12
requires: 2, 5
---

## Simple Summary

This document is about the details of the Cosmos Hash namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Cosmos blockchains that cannot be covered by CAIP-5
due to a chain ID that does not match the pattern.

## Motivation

See CAIP-2.

## Specification

### Cosmos Hash Namespace

In the namespace "cosmos-hash" the term "cosmos" refers to the wider Cosmos ecosystem and "hash" to the fact that native chain IDs are hashed in order to fit in the general format of CAIP-2.

#### Reference Definition

The `reference` is defined as `first_16_chars(hex(sha256(utf8(chain_id))))`, with

- the Tendermint `chain_id` from the genesis file (a JSON-compatible unicode string)
- `utf8` being the UTF-8 encoding
- `sha256` being the SHA256 hash function
- `hex` being a lowercase hex encoder
- `first_16_chars` being the first 16 characters

## Rationale

Cosmos blockchains with a chain ID matching not matching `[-a-zA-Z0-9]{3,47}` fall into the "cosmos-hash" namespace.
No real world example is known to the author yet.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# chain_id "x" (too short for the "cosmos" namespace)
cosmos-hash:2d711642b726b044

# chain_id "123456789012345678901234567890123456789012345678" (too long for the "cosmos" namespace)
cosmos-hash:0204c92a0388779d

# chain_id "wonderland🧝" (invalid character for the "cosmos" namespace)
cosmos-hash:843d2fc87f40eeb9
```

## Links

- [Cosmos chain ID best practice](https://github.com/cosmos/cosmos-sdk/issues/5363)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
