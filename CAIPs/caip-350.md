---
caip: 350
title: Binary Serialization of Blockchain IDs and Addresses
author: Defi Wonderland (@defi-wonderland), Teddy (@0xteddybear), Joxes (@Joxess), Racu (@0xRacoon), Skeletor Spaceman (@0xskeletor-spaceman), TiTi (@0xtiti), Gori (@0xGorilla), Ardy (@0xArdy), Onizuka (@onizuka-wl)
discussions-to: https://ethereum-magicians.org/t/erc-7930-interoperable-addresses/23365
status: Draft
type: Standard
created: 2025-04-18
requires: 2
---

## Simple Summary

This standard, when profiled down for each namespace, describes a unified and general purpose method for serializing chain-specified addresses into the Interoperable Address format specified in [ERC-7930].

## Abstract

Every namespace defines their own address types and also a way to identify networks within that namespace's ecosystem.
[ERC-7930] is an address format for (address, chain) pairs supporting any chain, and also including metadata on the chain and addresses' type in a self-describing way, reducing the need to communicate metadata or context out-of-band.
This CAIP aims to append a living and mutable registry of chain-specific address serialization methods to the CASA system in the form of its namespace-specific profiles, enabling all current and future chains to uniformly conform to Interoperable Addresses.

## Motivation

Standards like CAIP-2 and CAIP-10 are simple text representations of addresses and chain namespaces and references, but they do not address:

- Binary representation of addresses and/or chain namespace+reference: Relevant and desired for on-chain usage.
- Canonicity: [CAIP-10], being a generic text format, leaves the canonization and serialization of text address to on-chain addresses to each namespace's profile. Unless a given namespace's profile specifies such logic, the standard itself makes no universal guarantees on a blockchain account (_target address_ in ERC-7930 parlance) having only one CAIP-10 representation. For namespaces where such canonicity is neither inherent nor specified by the CASA profile, duplicate entries can be created (e.g. when used as dictionary keys).
- Completeness: Both formats have limits on the length of their identifiers, which are reasonable for human-readability, but often identifiers have to drop meaningful information to conform to those length requirements. While that information might be easy to look up in the context of wallet software, doing so within a smart contract would not be possible.
- Succinctness: Text formats necessarily have to incur encoding overhead compared to binary ones, causing relative informational inefficiency.

## Specification

The purpose of each namespace's profile is to specify deterministic and unambiguous conversions between the following format-pairs:

- Customary address text formats of the ecosystem (which may also be described in its [CAIP-10] profile) and the Interoperable Names text representation defined in [ERC-7930]
- Customary chain reference text formats of the ecosystem (which may also be described in [CAIP-2]) and the text representation defined in [ERC-7930]
- [ERC-7930]'s Interoperable Address and [ERC-7930]'s Interoperable Name of the ecosystem's chain references
- [ERC-7930]'s Interoperable Address and [ERC-7930]'s Interoperable Name of the ecosystem's addresses *for all address formats of said ecosystem*

And MUST also define the binary id of the namespace itself, akin to [ERC-7930]'s `ChainType`

Every namespace MUST specify all of the above in a CAIP-350 profile to maximize interoperability and review.
A [template for these profiles](https://github.com/ChainAgnostic/namespaces/blob/main/_template/caip350.md) is defined in the Namespaces registry.

## Rationale

The main alternative to this standard would've been to define all formats and conversions in [ERC-7930], turning it into a Living ERC, which was not ideal since:

- Living standards are not a usual thing in the Ethereum ecosystem: the only other Living ERC is EIP-1
- It would be desireable to finalize the definition of the format itself while allowing the specification of every chain's serialization to be defined as needed.
- It would have placed all the editorial (and presumably most of the specification) work on the [ERC-7930] authors, while the CASA paradigm allows better distribution of work by leaving the definition of the profile for every chain namespace to the parties of its ecosystem interested in using Interoperable Addresses.

## Test Cases

Not applicable since this does not define a normative interface, instead being a meta-specification of normative interfaces.

Test cases SHOULD be included in each namespace profile.

## Security Considerations

It is possible that a CAIP-350 profile for a given chain namespace is not able to guarantee requirements of canonicity which are a desired property of Interoperable Addresses due to characteristics of the chain namespace, and systems relying on that canonicity may exhibit unexpected behavior as a result. When this is the case, it SHOULD be noted in the `Extra Considerations` section.

## Backwards Compatibility

This standard actively seeks to be exhaustive in its backwards compatibility with [CAIP-2] and [CAIP-10].

Namespace profiles SHOULD clarify which conversions to and from the aforementioned standards are:

- **Problematic**, by requiring extra resources network or storage-constrained clients might not have, such as the conversion from a CAIP-2 `solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp` (with a truncated genesis blockhash) to its CAIP-350 equivalent: `solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d`. While clients SHOULD support this kind of conversion, those unable to do so will still be considered compliant with the standard.
- **Impossible**, due to a standard's inability to represent a given chain or address. 
   + Example 1: an EVM chain with a chainid greater than 10^32, which could be represented losslessly in CAIP-350, but not in [CAIP-2] due to its length restriction on chain references
   + Example 2: a chain for which only [CAIP-10] is defined but not a `chainType`, which clients would not know how to serialize into CAIP-350.

Client libraries SHOULD produce different errors for the two aforementioned error cases.

## References

- [CAIP-104] defines CASA namespaces
- [CAIP-2] defines CASA chainId profiles per namespace
- [CAIP-10] defines a text format for blockchain accounts/target addresses per namespace, chain-specifying by using the given namespace's [CAIP-2] strings
- [ERC-7930] defined the binary Interoperable Address format and an optional text representation, comparable to [CAIP-10].

[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
<!-- TODO: point to the EIP website when the PR is merged -->
[ERC-7930]: https://ethereum-magicians.org/t/erc-7930-interoperable-addresses/23365

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
