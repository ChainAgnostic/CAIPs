---
caip: 2
title: Blockchain ID Specification
author: Simon Warta (@webmaster128), ligi <ligi@ligi.de>, Pedro Gomes (@pedrouid), Antoine Herzog (@antoineherzog)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/1, https://github.com/UCRegistry/registry/pull/13, https://ethereum-magicians.org/t/caip-2-blockchain-references/3612,
status: Draft
type: Standard
created: 2019-12-05
updated: 2020-01-17
---

## Simple Summary

CAIP-2 defines a way to identify a blockchain (e.g. Ethereum Mainnet, Görli, Bitcoin, Cosmoshub)
in a human readably, developer friendly and transaction-friendly way.

## Abstract

Often you need to reference a blockchain. For example when you want to state where some asset or smart contract is located. In Ethereum the [EIP155](https://eips.ethereum.org/EIPS/eip-155) chain ID is used most of the time. But with an Ethereum chain ID you cannot reference e.g. a Bitcoin or Cosmos chain.

## Motivation

The final trigger to create this CAIP (and the CAIP process itself) was a discussion around [EIP2256] at [Ethereum-Magicians](https://ethereum-magicians.org/t/eip-2256-add-wallet-getownedtokens-json-rpc-method/3600/14).
Independently, the [Universal Chain Registry](https://github.com/UCRegistry) was created that needs properly specified chain identifiers at its core. A [discussion about the network ID format](https://github.com/UCRegistry/registry/pull/13) brought this group together with ChainAgnostic.

## Specification

The blockchain ID (short "chain ID") is a string designed to uniquely identify blockchains in a developer-friendly fashion.

### Syntax

The `chain_id` is a case-sensitive string in the form

```
chain_id:    namespace + ":" + reference
namespace:   [-a-z0-9]{3,16}
reference:   [-a-zA-Z0-9]{1,47}
```

### Semantics

Each `namespace` covers a class of similar blockchains.
Usually it describes an ecosystem or standard, such as e.g. `cosmos` or `eip155`.
One namespace should include as many blockchains as possible.
`reference` is a way to identify a blockchain within a given namespace.
The semantics as well as the more granular syntax are of the reference are delegated to ecosystem specific documents, to be expected as separate CAIPs.

## Rationale

The goals of the general chain ID format is:

- Uniqueness within the entire blockchain ecosystem
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
# Ethereum mainnet
eip155:1

# Bitcoin mainnet (see https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki#definition-of-chain-id)
bip122:000000000019d6689c085ae165831e93

# Litecoin
bip122:12a765e31ffd4059bada1e25190f6e98

# Feathercoin (Litecoin fork)
bip122:fdbe99b90c90bae7505796461471d89a

# Cosmos Hub (Tendermint + Cosmos SDK)
cosmos:cosmoshub-2
cosmos:cosmoshub-3

# Binance chain (Tendermint + Cosmos SDK; see https://dataseed5.defibit.io/genesis)
cosmos:Binance-Chain-Tigris

# IOV Mainnet (Tendermint + weave)
cosmos:iov-mainnet

# Lisk Mainnet (LIP-0009; see https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md)
lip9:9ee11e9df416b18b

# Dummy max length (16+1+47 = 64 chars/bytes)
max-namespace-16:xip3343-8c3444cf8970a9e41a706fab93e7a6c4-xxxyyy
```

## Links

- [EIP155](https://eips.ethereum.org/EIPS/eip-155)
- [BIP122](https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki)
- [EIP2256](https://eips.ethereum.org/EIPS/eip-2256)
- [LIP9](https://github.com/LiskHQ/lips/blob/master/proposals/lip-0009.md)
- [Cosmos chain ID best practice](https://github.com/cosmos/cosmos-sdk/issues/5363)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
