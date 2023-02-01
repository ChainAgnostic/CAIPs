---
caip: 19
title: Asset Type and Asset ID Specification
author: Antoine Herzog (@antoineherzog), Pedro Gomes (@pedrouid), Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/19
status: Review
type: Standard
created: 2020-06-23
updated: 2020-06-23
requires: 2
---

## Simple Summary

CAIP-19 defines a way to identify a type of asset (e.g. Bitcoin, Ether, ATOM)
with an optional asset identifier suffix (for individually-addressable tokens
like NFTs) in a human-readable, developer- and transaction-friendly way.

## Abstract

Often you need to reference an asset type, or an asset type + asset identifier
to identify a specific token from that set (if non-fungible). For example,
precise specifications of assets exchanged as an atomic swap (within or between
blockchains) require this kind of unambiguous addressing, as do dashboards for
tracking assets held by a given address or in a given collection.

## Motivation

Currently, each wallet or each exchange needs to create their own registry of
types of assets and their associated metadata for example like
[Trust-Wallet](https://github.com/trustwallet/assets/tree/master/blockchains) or
[CoinMarketCap](https://coinmarketcap.com/). Providing a unique `Asset Type`
and, where applicable, a type-specific `Asset ID` for each asset for developers
can reduce the risk of confusion between different assets.

## Specification of Asset Type

The Asset Type is a string designed to uniquely identify the types of assets in
a developer-friendly fashion.

### Syntax

The `asset_type` is a case-sensitive string in the form

```
asset_type:        chain_id + "/" + asset_namespace + ":" + asset_reference
chain_id:          Namespace+Blockchain ID as per [CAIP-2][]
asset_namespace:   [-a-z0-9]{3,8}
asset_reference:   [-.%a-zA-Z0-9]{1,128}
```

Note that `-`, `%` and `.` characters are allowed in `asset_references`, which
include on-chain addresses like those specified in [CAIP-10][], but no other
non-alphanumerics such as `:`, `/` or `\`.  Implementers are recommended to use
"URL encoding" (% + 2-character codes, canonically capitalized) as per [Section
2][rfc3986sec2.1] of [RFC 3986][rfc3986] to escape any further non-alphanumeric
characters, and to consider [homograph attack surfaces][homograph] in the
handling of any non-alphanumerics.

## Specification of Asset ID

The optional addition of an `asset ID` suffix separated by `/` uniquely
identifies an addressible asset of a given type in a developer-friendly fashion.
In the case of non-fungible tokens or other collections, this address is called
a `token_id` (commonly referred to as a "serial number" since they are often
sequentially numbered). Note: [ERC721][] defines identifiers for specific tokens
as `uint256` values (i.e. an integer ranging from 0 to 2^256-1) and recommends
_but does not require_ them to be serially assigned.

### Syntax

The `asset_id` is a case-sensitive string in the form

```
asset_id:    asset_type + "/" + token_id 
token_id:   [-.%a-zA-Z0-9]{1,78}

```

Note that `-`, `%` and `.` characters are allowed, but no other
non-alphanumerics such as `:`, `/` or `\`.  Implementers are recommended to use
"URL encoding" (% + 2-character codes, canonically capitalized) as per [Section
2][rfc3986sec2.1] of [RFC 3986][rfc3986] to escape any further non-alphanumeric
characters, and to consider [homograph attack surfaces][homograph] in the handling
of any non-alphanumerics.  

More constrained character sets per namespace may be specified in each namespaces'
CAIP-19 profile, which outline some common asset types.

## Canonicalization

Note that for smart contract addresses used in some Asset Types (like ERC721 and
its equivalents), some namespaces like the EVM offer canonicalization schemes
that use capitalization (e.g. [EIP-55][]), an option suffix (e.g. [HIP-15][]),
or some other transformation. At the present time, this specification
does NOT require canonicalization, and implementers are advised to consider
deduplication or canonicalization in their consumption of CAIP-addresses.
CAIP-19 profiles in CASA [namespaces][] may contain additional information per
namespace.

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

## Changelog

- 2022-10-23: 
    - expanded charset to include `-`,`.`, and `%`
    - added canonicalization section and links
    - better language for use cases, wider-characterset syntax, etc 
- 2022-05-12: regex for token_id expanded to include entire `uint256` range
- 2021-06-25: regex max lengths raised and test cases updated accordingly
- 2020-06-23: added distinction between asset type and asset ID 

## Links

- [IETF RFC 3986][rfc3986] - the IETF standard for URL, URI and URN syntax
- [CAIP-2][] - CASA Chain ID specification
- [EIP-721][] - Ethereum Improvement Proposal for non-fungible tokens
- [EIP-55][] - Ethereum Improvement Proposal for canonicalizing ethereum addresses to by deterministic capitalization of a-f characters
- [HIP-15][] - Hedera Improvement Proposal defining a checksum suffix for addresses

[namespaces]: https://namespaces.chainagnostic.org/
[EIP-55]: https://eips.ethereum.org/EIPS/eip-55
[EIP-721]: https://eips.ethereum.org/EIPS/eip-721
[HIP-15]: https://github.com/hashgraph/hedera-improvement-proposal/blob/main/HIP/hip-15.md
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[rfc3986]: https://www.rfc-editor.org/rfc/rfc3986
[rfc3986sec2.1]: https://www.rfc-editor.org/rfc/rfc3986#section-2.1
[homograph]: https://en.wikipedia.org/wiki/IDN_homograph_attack

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
