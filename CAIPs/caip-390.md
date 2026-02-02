---
caip: 390
title: Minimal Cross-Chain Asset Metadata Standard
status: Draft
type: Standard
category: Interface
author: Yan (@xzensh)
created: 2025-12-22
requires: 2, 19
---

## Simple Summary

A standardized JSON schema for retrieving and verifying off-chain metadata (profile, visual assets, and links) for crypto assets, with built-in support for cross-chain identity discovery.

## Abstract

This proposal defines a minimal, chain-agnostic JSON structure for asset metadata. It addresses the fragmentation of token information across Web3 by standardizing how wallets and dApps fetch essential data (logo, name, symbol, decimals) and extended data (links, cross-chain locations). The standard prioritizes URI-based resource referencing and introduces a "locations" field to map a single asset's existence across multiple blockchains using [CAIP-19] identifiers.

## Motivation

Currently, asset metadata is fragmented across various repositories (Coingecko, CoinMarketCap, TrustWallet Assets, Tokenlists.org), each using proprietary JSON structures.

1.  **Inconsistency**: A developer must integrate multiple APIs to get a token's logo, website, and social links.
2.  **Cross-Chain Fragmentation**: Assets bridged across layers (e.g., L1 to L2) or chains are often treated as entirely separate entities by wallets, making portfolio views difficult to aggregate.
3.  **Ambiguity**: Link data is often unstructured (e.g., keys like `twitter`, `social_twitter`, `x_url`), requiring complex parsing logic.

We propose a unified schema that is compatible with existing NFT standards (ERC-721/OpenSea) while adding specific support for fungible token needs and cross-chain peer discovery.

## Specification

The metadata MUST be returned as a JSON object. The schema is defined as follows:

### Schema Definition

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "CAIP Asset Metadata",
  "type": "object",
  "required": ["name", "symbol", "decimals", "image"],
  "properties": {
    "name": {
      "type": "string",
      "description": "Human-readable name of the asset."
    },
    "symbol": {
      "type": "string",
      "description": "Abbreviated symbol of the asset (ticker)."
    },
    "decimals": {
      "type": "integer",
      "minimum": 0,
      "description": "The number of decimals for the token balance. MUST be 0 for non-divisible assets (NFTs)."
    },
    "image": {
      "type": "string",
      "format": "uri",
      "description": "URI to the asset's logo or primary image. WebP, SVG or PNG formats are RECOMMENDED."
    },
    "description": {
      "type": "string",
      "maxLength": 1000,
      "description": "Brief description of the asset."
    },
    "external_url": {
      "type": "string",
      "format": "uri",
      "description": "URL to the official website or external resource of the asset. Compatible with ERC-721 metadata."
    },
    "links": {
      "type": "array",
      "description": "A list of auxiliary links related to the asset.",
      "items": {
        "type": "object",
        "required": ["name", "url", "rel"],
        "properties": {
          "name": {
            "type": "string",
            "description": "Display label for the link."
          },
          "url": {
            "type": "string",
            "format": "uri",
            "description": "The destination URL."
          },
          "rel": {
            "type": "string",
            "description": "Relationship type used for semantic classification.",
            "enum": [
              "homepage",
              "whitepaper",
              "documentation",
              "source_code",
              "governance",
              "audit",
              "social",
              "browser",
              "exchange",
              "bridge"
            ]
          }
        }
      },
      "uniqueItems": true
    },
    "locations": {
      "type": "array",
      "description": "List of CAIP-19 identifiers representing this asset's peer contracts across different chains.",
      "items": {
        "type": "string",
        "pattern": "^[-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32}/[-a-z0-9]{3,8}:[-.%a-zA-Z0-9]{1,128}$"
      },
      "uniqueItems": true
    }
  }
}
```

### Field Descriptions

#### Core Fields
*   **`decimals`**: This field is mandatory. For NFTs or non-divisible assets, it MUST be set to `0`. This eliminates ambiguity for consumers (wallets) regarding whether a value is missing or intentionally zero.
*   **`external_url`**: Used instead of "website" to maintain compatibility with existing NFT metadata standards (e.g., OpenSea).

#### `links` Object
To prevent key-name fragmentation (e.g., `twitter_url` vs `socials.twitter`), links are defined as an array of objects containing a mandatory `rel` attribute.

Recommended values for `rel`:
*   `homepage`: Main project website.
*   `whitepaper`: Technical paper or economic model.
*   `documentation`: Developer docs or wikis.
*   `source_code`: Code repositories (GitHub, GitLab).
*   `governance`: Voting portals or forums.
*   `audit`: Security audit reports.
*   `social`: Social media profiles (X/Twitter, Discord, Telegram).
*   `browser`: Block explorer links.
*   `exchange`: Direct links to trading pairs on DEXs or CEXs (e.g., Uniswap Pool, Binance Spot).
*   `bridge`: Interfaces allowing users to bridge this asset across chains.

#### `locations` (Cross-Chain Discovery)
This field allows an asset to declare its "peer" contracts on other networks. The value MUST be an array of valid [CAIP-19] strings.
*   Example: A `USDT` token on Ethereum mainnet can list its representations on BNB Chain and Solana in this field.
*   This enables wallets to aggregate balances of the "same" asset across chains without relying on centralized bridge mappings.

## Example

```json
{
  "name": "ICPanda",
  "symbol": "PANDA",
  "decimals": 8,
  "image": "https://panda.fans/_assets/logo.svg",
  "description": "Building the open-source stack for AI agents to remember, transact, and evolve as first-class citizens in Web3.",
  "external_url": "https://panda.fans",
  "links": [
    {
      "name": "Twitter",
      "url": "https://x.com/ICPandaDAO",
      "rel": "social"
    },
    {
      "name": "Source Code",
      "url": "https://github.com/ldclabs/ic-panda",
      "rel": "source_code"
    },
    {
      "name": "ICPSwap",
      "url": "https://app.icpswap.com/swap/pro?input=ryjl3-tyaaa-aaaaa-aaaba-cai&output=druyg-tyaaa-aaaaq-aactq-cai",
      "rel": "exchange"
    },
    {
      "name": "Official Bridge",
      "url": "https://1bridge.app/?token=PANDA",
      "rel": "bridge"
    }
  ],
  "locations": [
    "icp:1/token:druyg-tyaaa-aaaaq-aactq-cai",
    "eip155:56/bep20:0xe74583edaff618d88463554b84bc675196b36990",
    "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp/token:PANDAvvWniWYKRbrCYQEAyeSJ5uUk1nc49eLqT6yQyL"
  ]
}
```

## Rationale

### URI-First for Visuals
We opted for a single `image` URI string rather than a complex object containing thumbnails or themes. Modern UI frameworks and CDNs handle image resizing and format negotiation efficiently. Sticking to a single string simplifies the parsing logic for wallets.

### Structured Links vs. Flat Keys
Arbitrary keys in a JSON object (e.g., `"twitter": "..."`) lead to a chaotic ecosystem where consumers must maintain a mapping of thousands of non-standard keys. Using a structured array with a `rel` property enforces a controlled vocabulary while allowing flexibility for new types in the future.

### Peer-to-Peer Locations
Existing token lists often treat bridged assets as separate entries. By including `locations`, we embed the "cross-chain graph" directly into the asset's metadata. We use [CAIP-19] because it is the standard for uniquely identifying asset instances in a multi-chain environment.

## Backwards Compatibility

*   **ERC-721**: The usage of `name`, `description`, `image`, and `external_url` aligns with the widely adopted OpenSea metadata standard.
*   **Token Lists**: The structure implies that an array of these objects can easily be transformed into a Uniswap-style Token List.

## Security Considerations

1.  **Phishing Risks**: Consuming applications (wallets) MUST exercise caution when rendering `external_url` or `links`. We recommend displaying the domain clearly to the user before redirection.
2.  **Image Validation**: Applications should sanitize SVG images referenced in the `image` field to prevent XSS attacks via embedded scripts.
3.  **Trust**: This standard defines the *format* of the data, not the *validity*. Consumers should verify the source of this JSON (e.g., ensuring it is served from a trusted domain or a verifiable on-chain registry).

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

[CAIP-19]: https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-19.md
