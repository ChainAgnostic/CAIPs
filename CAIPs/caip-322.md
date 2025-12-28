---
caip: 322
title: URI Scheme for CAIP identifiers
author: [Pedro Gomes] (@pedrouid), [ligi](ligi@ligi.de), [bumblefudge](@bumblefudge)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/67
status: Draft
type: Standard
created: 2024-08-08
requires: 2, 10, 19, 104
---

## Simple Summary

Various CAIPs to date have defined generic chain-agnostic identifier schemes for certain informational primitives common to blockchain and other decentralized peer-to-peer systems.
This URI scheme combines them into a structure that can be parsed hierarchically.

## Abstract

A general-purpose hierarchical URI scheme for identifiers that dereference to "on-chain" or otherwise cryptographically self-certifying records from peer-to-peer cryptographic systems.
The model is not equally amenable to all native identifier schemes but is engineered to facilitate translation between, and access to, those systems as broadly as possible.

## Motivation

Cryptographic and peer-to-peer systems, particularly replicated state machines like "blockchains" that concentrate all shared records in a self-certifying transaction log referred to commonly as "chain-state".
These systems tend to make a live connection with a consensus node the primary way of discovering, fetching, and interacting with that information.
Off-chain, cross-chain, multi-chain and complex systems often need to translate, annotate, or notarize that information for consumers without live connections to the network, produce auditable logs, etc.
For these contexts, a machine-readable, validation-friendly URI scheme these identifiers and pointers is described below.

## Specification

The basic structure of a CAIP URI is as follows:

```c
caip:<[CAIP-104] namespace>:<[CAIP-2] chain identifier>:<{[CAIP-10]|[CAIP-19]} on-chain identifier>
```

The top-level segment refers to short ASCII strings identifying entries in the Chain-Agnostic Namespace registry.
The applicability of other CAIPs, as well as any namespace-specific constraints, validation syntax, and caveats, are defined in entries there.
All segments after the namespace are optional but hierarchical, i.e., a [CAIP-2][] chain identifier without a preceding namespace segment is invalid, and a [CAIP-10][] account identifier without a preceding chain identifier is invalid.

## Rationale

The membership of the Chain-Agnostic Standards Alliance has been experimenting with, refining, and expanding the applicability of these identifier schemes since 2021.
Detailed rationales for each component of this scheme can be found following the "discussions-To" link in each applicable CAIP's metadata.

## Test Cases

### Chain Identifiers (CAIP-2)

```
# Ethereum Mainnet
caip:eip155:1

# Bitcoin Mainnet
caip:bip122:000000000019d6689c085ae165831e93

# Cosmos Hub
caip:cosmos:cosmoshub-4

# Polkadot Relay Chain
caip:polkadot:91b171bb158e2d3848fa23a9f1c25182

# Litecoin
caip:bip122:12a765e31ffd4059bada1e25190f6e98
```

### Account Identifiers (CAIP-10)

```
# Ethereum EOA
caip:eip155:1:0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb

# Bitcoin Address
caip:bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6

# Cosmos Account
caip:cosmos:cosmoshub-4:cosmos1t2uflqwqe0fsj0shcfkrvpukewcw40yjj6hdc0

# Polkadot Account
caip:polkadot:91b171bb158e2d3848fa23a9f1c25182:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy
```

### Asset Identifiers (CAIP-19)

```
# Ether (native token via SLIP-44)
caip:eip155:1/slip44:60

# Bitcoin (native token via SLIP-44)
caip:bip122:000000000019d6689c085ae165831e93/slip44:0

# DAI Token (ERC-20)
caip:eip155:1/erc20:0x6B175474E89094C44Da98b954EedeAC495271d0F

# CryptoKitties Collectible (ERC-721 collection)
caip:eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d

# CryptoKitty #771769 (specific NFT)
caip:eip155:1/erc721:0x06012c8cf97BEaD5deAe237070F9587f8E7A266d/771769

# Hedera NFT
caip:hedera:mainnet/nft:0.0.55492/12
```

## Security Considerations

These identifiers should be thought of as pointers and the persistence or availability of their referents cannot be guaranteed.
They inherit the availability, discoverability, and garbage-collection properties of the cryptographic record systems that they register, so close attention to the first segment and the descriptive documentation provided in their registration entries is crucial;
these should be allowlisted one at a time, as no universal assumptions can be made about them, including the applicability of a [CAIP-2][] identifier scheme for specific networks in that namespace.

## Privacy Considerations

Similarly, these identifiers refer to data that is most often immutably public, as it has been in all completed registrations to date.
As such, this should be the baseline assumption unless contradicted or caveated by the registration of top-level namespaces.

## Backwards Compatibility

It is important to note that most usage to date of [CAIP-2][], [CAIP-10][], and [CAIP-19][] identifiers has used these without a `caip:` prefix, in contexts where these identifiers are unlikely to be encountered outside of their meaningful context.
Care should be taken to add the `caip:` prefix when merging such lists or datasets into more general-purpose systems or URI-aware contexts.

When migrating to URI-based systems:
- Add the `caip:` prefix when exporting identifiers to external systems
- Strip the `caip:` prefix when importing into CAIP-aware systems that expect unprefixed identifiers
- Validate the full URI syntax before use

## IANA Considerations

This specification requires registration of the `caip` URI scheme with IANA per [RFC 7595][].

## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-1][] defines the CAIP document structure
- [CAIP-104][] defines the CAIP namespaces directory
- [CAIP-2][] defines the network-identifier syntax for each namespace's network topology, which in some cases includes wildcard or subnet-wide identifiers
- [CAIP-10][] defines the "account"-identifier syntax for each namespace's actor model
- [CAIP-19][] defines the "asset"-identifier syntax for each namespace's stably-addressable assets with an eye to commonalities


[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[CAIP-19]: https://ChainAgnostic.org/CAIPs/caip-19
[CAIP-104]: https://ChainAgnostic.org/CAIPs/caip-104
[RFC 7595]: https://www.rfc-editor.org/rfc/rfc7595
[namespaces]: https://namespaces.chainagnostic.org/

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
