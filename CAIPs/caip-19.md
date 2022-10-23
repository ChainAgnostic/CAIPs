---
caip: 19
title: Asset Type and Asset ID Specification
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid), Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/19
status: Draft
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 2
---

## Simple Summary

CAIP-19 defines a way to identify a type of asset (e.g. Bitcoin, Ether, ATOM)
and an asset ID (for a non-fungible token) in a human-readable, developer- and
transaction-friendly way.

## Abstract

Often you need to reference the asset type or the asset ID. For example when you
want to do an atomic swap between a fungible asset and a non-fungible asset.

## Motivation

Currently, each wallet or each exchange needs to create their own registry of
type of assets and their associated metadata for example like
[Trust-Wallet](https://github.com/trustwallet/assets/tree/master/blockchains) or
[CoinMarketCap](https://coinmarketcap.com/). Providing a unique type of Asset
and an Asset ID for each asset for developers can reduce the risk of confusion
between different assets.

## Specification of Asset Type

The Asset Type is a string designed to uniquely identify the types of assets in
a developer-friendly fashion.

### Syntax

The `asset_type` is a case-sensitive string in the form

```
asset_type:    chain_id + "/" + asset_namespace + ":" + asset_reference
chain_id:          Blockchain ID Specification cf. CAIP2
asset_namespace:   [-a-z0-9]{3,8}
asset_reference:   [-.%a-zA-Z0-9]{1,64}
```

Note that `-`, `%` and `.` characters are allowed, but no other
non-alphanumerics such as `:`, `/` or `\`.  Implementers are recommended to use
"URL encoding" (% + 2-character codes, canonically capitalized) as per [Section
2][rfc3986sec2.1] of [RFC 3986][rfc3986] to escape any further non-alphanumeric
characters, and to consider [homograph attack surfaces][homograph] in the handling
of any non-alphanumerics.

## Specification of Asset ID

The Asset ID is a string designed to uniquely identify a non-fungible asset in a
developer-friendly fashion.

### Syntax

The `asset_id` is a case-sensitive string in the form

```
asset_id:    asset_type + "/" + token_id [+ "/" + serial_number]
token_id:   [-.%a-zA-Z0-9]{1,32}
```

In the case of collections, a second "/" divides the collection address from the
serial number of the specific item within it.

Note that `-`, `%` and `.` characters are allowed, but no other
non-alphanumerics such as `:`, `/` or `\`.  Implementers are recommended to use
"URL encoding" (% + 2-character codes, canonically capitalized) as per [Section
2][rfc3986sec2.1] of [RFC 3986][rfc3986] to escape any further non-alphanumeric
characters, and to consider [homograph attack surfaces][homograph] in the handling
of any non-alphanumerics.

### Semantics

Each `asset_namespace` covers a class of similar assets. Usually, it describes
an ecosystem or standard, such as e.g. `slip44` or `erc20`. One
`asset_namespace` should include as many assets as possible. `asset_reference`
is a way to identify an asset within a given `asset_namespace`.

To date, the only cross-chain/multi-namespace standard incorporated into CAIP
system is SLIP-44, described in [CAIP-20][]; the former offers a registry for
native fungible tokens across namespaces. Namespace-specific standards are
profiled in CAIP-19 profiles in the CASA [namespaces][] registry; the erc20
addressing on EVM chains, for example, is defined in
[namespaces/eip155/caip19](https://namespaces.chainagnostic.org/eip155/caip19). 

## Rationale

The goals of the general asset type and asset ID format is:

- Uniqueness within the entire asset ecosystem
- To some degree human-readable and helps for basic debugging
- Restricted in a way that it can be stored on-chain
- Character set basic enough to display in hardware wallets as part of a
  transaction content

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

# CryptoKitties Collection
eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d

# CryptoKitties Collectible #771769
eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d/771769

# Edition 12 of 50: First-Generation Hedera Robot VENOM EDITION
hedera:mainnet/nft:0.0.55492/12
```

## Links

- [IETF RFC 3986][rfc3986] - the IETF standard for URL, URI and URN syntax
- [CAIP-2][] - CASA Chain ID specification
- [EIP-55][] - Ethereum Improvement Proposal for canonicalizing ethereum addresses to by deterministic capitalization of a-f characters
- [HIP-15][] - Hedera Improvement Proposal defining a checksum suffix for addresses

[namespaces]: https://namespaces.chainagnostic.org/
[EIP-55]: https://github.com/ethereum/EIPs/blob/master/EIPS/eip-55.md
[HIP-15]: https://github.com/hashgraph/hedera-improvement-proposal/blob/main/HIP/hip-15.md
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[rfc3986]: https://www.rfc-editor.org/rfc/rfc3986
[rfc3986sec2.1]: https://www.rfc-editor.org/rfc/rfc3986#section-2.1
[homograph]: https://en.wikipedia.org/wiki/IDN_homograph_attack

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
