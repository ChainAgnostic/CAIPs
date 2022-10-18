---
caip: 151
title: Asset Type and Asset ID Specification Superset
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid), Joel Thorstensson (@oed), Juan Caballero (@bumblefudge)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/151
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 2
replaces: 19
---

## Simple Summary

A variation of CAIP-19 that allows more addressing systems to be transposed directly into the `Asset ID` value of a CAIP-19 address, including ones that use characters `.` and `%` (and by extension, URL-encoded strings).

## Abstract

CAIP-19 defined a way to identify a type of asset (e.g. Bitcoin, Ether, ATOM) and an asset ID (for a non-fungible token) in a human-readable, developer and transaction friendly way. CAIP-151 mirrors exactly its structure and usage, but allows for a broader range of characters in `Asset ID`.  All valid CAIP-19 addresses are valid CAIP-151 addresses, but not vice versa.

## Motivation

 The permitted character set of its Asset ID syntax (see regular expressions in [CAIP19#syntax](https://chainagnostic.org/CAIPs/caip-19#syntax-1) ) prevented its straight-forward application in namespaces where addressing systems use characters like `.` and `%`.  Rather than create translation issues (and break "human readability", i.e. recognizability to developers from that namespace) in shoehorning those addressing syntaxes into the constraints of CAIP-19, a new CAIP would allow implementers to choose and schedule an upgrade of functionality to include a less constrained character set.

## Specification of Asset Type

The Asset Type is a string designed to uniquely identify the types of assets in a developer-friendly fashion.

### Syntax

The `asset_type` is a case-sensitive string in the form

```
asset_type:    chain_id + "/" + asset_namespace + ":" + asset_reference
chain_id:          Blockchain ID Specification cf. CAIP2
asset_namespace:   [-a-z0-9]{3,8}
asset_reference:   [-.%a-zA-Z0-9]{1,64}
```

## Specification of Asset ID

The Asset ID is a string designed to uniquely identify a non-fungible asset in a developer-friendly fashion.

### Syntax

The `asset_id` is a case-sensitive string in the form

```
asset_id:    asset_type + "/" + token_id
token_id:   [-.%a-zA-Z0-9]{1,78}
```

### Semantics

Each `asset_namespace` covers a class of similar assets. Since the adoption of CAIP-19, these have been broken out into loosely and descriptively specified ["CASA namespaces"](https://github.com/chainAgnostic/namespaces), which include, among other profiles, a CAIP-19 and/or CAIP-151 profile explaining addressing system assumptions or security considerations specific to each namespace.  In many cases, a regular expression is provided for validating addresses, or additional resources for handling them.  See also [CAIP19#semantics](https://chainagnostic.org/CAIPs/caip-19#semantics)

## Rationale

(Inherited verbatim from [CAIP19#rationale](https://chainagnostic.org/CAIPs/caip-19#semantics))

The goals of the general asset type and asset ID format is:

- Uniqueness within the entire asset ecosystem
- To some degree human-readable and helps for basic debugging
- Restricted in a way that it can be stored on-chain
- Character set basic enough to display in hardware wallets as part of a transaction content

The following secondary goals can easily be achieved:

- Can be used unescaped in URL paths
- Can be used as a filename in a case-sensitive UNIX file system (Linux/git).

Those secondary goals have been given up along the way:

- Can be used as a filename in a case-insensitive UNIX file system (macOS).
- Can be used as a filename in a Windows file system.

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

# DAI Token
eip155:1/erc20:0x6b175474e89094c44da98b954eedeac495271d0f

# CryptoKitties Collectible
eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d

# CryptoKitties Collectible ID
eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d/771769

# Edition 12 of 50: First-Generation Hedera Robot VENOM EDITION
hedera:mainnet/nft:0.0.55492/12

```

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
