---
caip: 5
title: Blockchain Reference for Cosmos
author: Simon Warta (@webmaster128)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/5, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Draft
type: Standard
created: 2019-12-05
updated: 2019-12-12
requires: 2, 6
---

## Simple Summary

This document is about the details of the Cosmos interface for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Cosmos.

## Motivation

See CAIP-2.

## Specification

### Interface name

The name of this interface is "cosmos", referring to the wider Cosmos ecosystem.

### Reference definition

The reference uses the Tendermint `chain_id` from the genesis file directly (a JSON-compatible unicode string), assuming it matches the case-sensitive pattern `[-a-zA-Z0-9]{3,47}`. Otherwise the Cosmos Hash interface (CAIP-6) must be used.

## Rationale

Blockchains in this interface are [Cosmos SDK](https://github.com/cosmos/cosmos-sdk) blockchains (e.g. Cosmoshub, Binance, Cosmos Testnets) and [Weave](https://github.com/iov-one/weave) based blockchains (e.g. IOV) with a chain ID matching `[-a-zA-Z0-9]{3,47}`.

While there is no enforced restriction on chain_id, the author of this document did not find a
non-conforming chain ID in the wild. There is [a discussion about documenting a best practice chain ID pattern](https://github.com/cosmos/cosmos-sdk/issues/5363).

During the development of this chain ID definition, we came across changing chain IDs for Cosmos Hub (`cosmoshub-1`, `cosmoshub-2`, `cosmoshub-3`). A new chain ID is assigned every time Cosmos Hub dumps the current blockchain state and creates a new genesis from the old state. Technically this leads to different blockchains and can (and maybe should) treated as such. For this specification, we treat them as different blockchains. It is the responsibility of a higher level application to interpret some chains as sequels of each other or create equality sets.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Cosmos Hub (Tendermint + Cosmos SDK)
cosmos:cosmoshub-2
cosmos:cosmoshub-3

# Binance chain (Tendermint + Cosmos SDK; see https://dataseed5.defibit.io/genesis)
cosmos:Binance-Chain-Tigris

# IOV Mainnet (Tendermint + Weave)
cosmos:iov-mainnet
```

## Links

- [Cosmos chain ID best practice](https://github.com/cosmos/cosmos-sdk/issues/5363)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
