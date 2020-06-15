---
caip: 19
title: Asset ID Specification
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/19
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 10
---

## Simple Summary

CAIP-19 defines a way to identify an asset (e.g. Bitcoin, Ether, ATOM)
in a human readably, developer friendly and transaction-friendly way.

## Abstract

Often you need to reference an asset. For example when you want to do an atomic swap between two assets.

## Motivation

Currently, each wallet or each exchange needs to create their own registry of assets and their associated metadata for example like [Trust-Wallet](https://github.com/trustwallet/assets/tree/master/blockchains) or [CoinMarketCap](https://coinmarketcap.com/). Providing a unique Asset ID for each asset for developers can reduce the risk of confusion betweens different assets. 

## Specification

The Asset ID is a string designed to uniquely identify assets in a developer-friendly fashion.

### Syntax

The `asset_id` is a case-sensitive string in the form

```
asset_id:    chain_id + "/" + asset_namespace + ":" + asset_reference
chain_id:          Blockchain ID Specification cf. CAIP2 
asset_namespace:   [-a-z0-9]{3,16}
asset_reference:   [-a-zA-Z0-9]{1,47}
```

### Semantics

Each `asset_namespace` covers a class of similar assets.
Usually it describes an ecosystem or standard, such as e.g. `slip44` or `erc20`.
One asset_namespace should include as many assets as possible.
`asset_reference` is a way to identify a asset within a given asset_namespace.
The semantics as well as the more granular syntax are of the reference are delegated to ecosystem specific documents, to be expected as separate CAIPs.

## Rationale

The goals of the general asset ID format is:

- Uniqueness within the entire asset ecosystem
- To some degree human readable and helps for basic debugging
- Restricted in a way that it can be stored on chain
- Character set basic enough to display in hardware wallets as part of a transaction content

The following secondary goals can easily be archived:

- Can be used unescaped in URL paths
- Can be used as filename in a case-sensitive UNIX file system (Linux/git).

Those secondary goals have been given up along the way:

- Can be used as filename in a case-insensitive UNIX file system (macOS).
- Can be used as filename in a Windows file system.

## Test Cases

This is a list of manually composed examples

```
# Ether Token
eip155:1/slip44:60

# Bitcoin Token
bip122:000000000019d6689c085ae165831e93/slip44:0

# ATOM Token
cosmos:cosmoshub-3/slip44:118

# Litecoin Token
bip122:12a765e31ffd4059bada1e25190f6e98/slip44:2

# Binance Token
cosmos:Binance-Chain-Tigris/slip44:714

# IOV Token
cosmos:iov-mainnet/slip44:234

# Lisk Token
lip9:9ee11e9df416b18b/slip44:134
```

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).