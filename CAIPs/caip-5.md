---
caip: 5
title: Blockchain Reference for the Cosmos Namespace
author: Simon Warta (@webmaster128)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/5, https://github.com/ChainAgnostic/CAIPs/issues/6, https://github.com/ChainAgnostic/CAIPs/pull/1
status: Superseded
type: Standard
created: 2019-12-05
updated: 2020-01-17
requires: 2
superseded-by: https://github.com/ChainAgnostic/namespaces/tree/main/cosmos
---

## Simple Summary

This document is about the details of the Cosmos namespaces and references for CAIP-2.

## Abstract

In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for Cosmos.

## Motivation

See CAIP-2.

## Specification

### Cosmos Namespace

The namespace "cosmos" refers to the wider Cosmos ecosystem.

#### Reference Definition

The reference relies on Tendermint's `chain_id` from the genesis file (a JSON-compatible unicode string).
In some cases we use it directly, otherwise it is hashed.
An empty `chain_id` must be treated as an error.

##### Direct

If the `chain_id` matches the case-sensitive pattern `[-a-zA-Z0-9]{1,32}` and does not start with "hashed-",
it is used the `reference` directly.

##### Hashed

Otherwise the `reference` is defined as `first_16_chars(hex(sha256(utf8(chain_id))))`, with

- the Tendermint `chain_id` from the genesis file (a JSON-compatible unicode string)
- `utf8` being the UTF-8 encoding
- `sha256` being the SHA256 hash function
- `hex` being a lowercase hex encoder
- `first_16_chars` being the first 16 characters

### Resolution Method

To resolve a blockchain reference for the Cosmos namespace, make a REST GET request to the blockchain node with endpoint `/node_info`, for example:

```jsonc
// Request
curl -X GET "https://stargate.cosmos.network/node_info" -H "accept: application/json"

// Response
{
  "application_version": {
    "build_tags": "string",
    "client_name": "string",
    "commit": "string",
    "go": "string",
    "name": "string",
    "server_name": "string",
    "version": "string"
  },
  "node_info": {
    "id": "string",
    "moniker": "validator-name",
    "protocol_version": {
      "p2p": 7,
      "block": 10,
      "app": 0
    },
    "network": "gaia-2",
    "channels": "string",
    "listen_addr": "192.168.56.1:26656",
    "version": "0.15.0",
    "other": {
      "tx_index": "on",
      "rpc_address": "tcp://0.0.0.0:26657"
    }
  }
}
```
The response will return a JSON object which will include node information and the blockchain reference can be retrieved from `node_info.network` to be CAIP-5 compatible.

## Rationale

Blockchains in the "cosmos" namespace are [Cosmos SDK](https://github.com/cosmos/cosmos-sdk) blockchains (e.g. Cosmoshub, Binance, Cosmos Testnets) and [Weave](https://github.com/iov-one/weave) based blockchains (e.g. IOV).

While there is no enforced restriction on `chain_id`, the author of this document did not find a chain ID in the wild that does not conform to the restrictions of the direct reference definition.
There is [a discussion about documenting a best practice chain ID pattern](https://github.com/cosmos/cosmos-sdk/issues/5363).

Cosmos blockchains with a chain ID not matching `[-a-zA-Z0-9]{1,32}` or prefixed with "hashed-" need to be hashed in order to comply with CAIP-2.
No real world example is known to the author yet.

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

# chain_ids "x", "hash-", "hashed" (are direct)
cosmos:x
cosmos:hash-
cosmos:hashed

# chain_ids "hashed-", "hashed-123" (invalid prefix for the direct definition)
cosmos:hashed-c904589232422def
cosmos:hashed-99df5cd68192b33e

# chain_id "123456789012345678901234567890123456789012345678" (too long for the direct definition)
cosmos:hashed-0204c92a0388779d

# chain_ids " ", "wonderland🧝‍♂️" (invalid character for the direct definition)
cosmos:hashed-36a9e7f1c95b82ff
cosmos:hashed-843d2fc87f40eeb9
```

## Links

- [Cosmos chain ID best practice](https://github.com/cosmos/cosmos-sdk/issues/5363)
- [TypeScript implementation in IOV Core](https://github.com/iov-one/iov-core/blob/1cd39e708b/packages/iov-cosmos/src/caip5.ts)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
