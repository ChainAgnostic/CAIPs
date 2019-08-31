---
caip: 2
title: Blockchain references
author: ligi <ligi@ligi.de>
discussions-to: <URL>
status: Draft
type: Standard
created: 2019-08-31
---

## Simple Summary

CAIP-2 defines a way to reference a blockchain. E.g. Ethereum mainnet, Görli, BTC, ..

## Abstract

Often you need to reference a blockchain. For example when you want to state where some asset or smart contract is located. In Ethereum the [EIP155] ChainId is used most of the time. But with an Ethereum chainId you cannot reference e.g. a Bitcoin or Cosmos chain.

## Motivation

The final trigger to create this CAIP (and the CAIP process itself) was a discussion around [EIP2256] at [Ethereum-Magicians](https://ethereum-magicians.org/t/eip-2256-add-wallet-getownedtokens-json-rpc-method/3600/14)

## Specification

The blockchain reference in CAIP-2 is a string. The string starts with a prefix. This prefix indicates what type of chain it is and how the blockchain reference is to be interptreted. For [EIP155] compatible chains the prefix is `eip155-` and is then followed by the chainId in decimal. This means `eip155-1` is the Ethereum mainnet and `eip155-5` is Görli. Bitcoin chains have the prefix `bitcoin-` and the prefix is followed by the genesis hash (like in [BIP122]) - this means `bitcoin-000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f` is the bitcoin mainnet. `bitcoin-000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943` is the bitcoin testnet and `bitcoin-12a765e31ffd4059bada1e25190f6e98c99d9714d334efa41a195a7e7e04bfe2` is litecoin

## Rationale

Another approach would be to just use the genesis hash as in [BIP122] - but this would make it hard to determine what kind of chain it is. Also having the prefix allows to easily enhance current [EIP155] references by just adding the prefix.

## links

[EIP155]: https://eips.ethereum.org/EIPS/eip-155
[EIP2256]: https://github.com/ethereum/EIPs/pull/2256
[BIP122]: https://github.com/bitcoin/bips/blob/master/bip-0122.mediawiki

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
