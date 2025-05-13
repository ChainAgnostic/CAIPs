---
caip: 350
title: Binary serialization of blockchain IDs and addresses
author: Defi Wonderland (@defi-wonderland), Teddy (@0xteddybear), Joxes (@Joxess), Racu (@0xRacoon), Skeletor Spaceman (@0xskeletor-spaceman), TiTi (@0xtiti), Gori (@0xGorilla), Ardy (@0xArdy), Onizuka (@onizuka-wl)
discussions-to: https://ethereum-magicians.org/t/erc-7930-interoperable-addresses/23365
status: Draft
type: Standard
created: 2025-04-18
requires: 2
---

## Simple Summary
A CAIP profile for every chain namespace to describe how to serialize their addresses and chain references into the Interoperable Address format described in [ERC-7930].

## Abstract
Every chain defines their own address types & also a way to identify networks within that chain's ecosystem.
[ERC-7930] is an address format for (address, chain) pairs supporting any chain, and also including metadata on the chain & addresses' type so no information needs to be communicated out of band.
This CAIP aims to append a living & mutable repository of chain-specific address serialization methods to the CASA system in the form of its namespace-specific profiles, enabling all current and future chains to uniformly conform to Interoperable Addresses.

## Motivation
Standards like CAIP-2 and CAIP-10 are simple text representations of addresses & chain namespaces/references, but they do not address:
- Binary representation of addresses and/or chain namespace+reference: Relevant & desired for on-chain usage.
- Canonicity: CAIP-10, being a generic text format, leaves the canonization and serialization of text address to on-chain addresses to each namespace's profile; unless a given namespace's profile specifies such logic, the standard itself makes no universal guarantees on a blockchain account (_target address_ in ERC-7930 parlance) having only one CAIP-10 representation; for namespaces where such canonicity is neither inherent or specified by the CASA profile, duplicate entries can be created (e.g. when used as dictionary keys).
- Completeness: both formats have limits on the length of their identifiers, which are reasonable for human-readability but often identifiers have to drop meaningful information to conform to those length requirements. While that information might be easy to look up in the context of wallet software, doing so within a smart contract would not be possible.
- Succinctness: Text formats necessarily have to incur encoding overhead compared to binary ones, causing relative informational inefficiency.

## Specification
The purpose of each namespace's profile is to specify deterministic and unambiguous conversions between the following format-pairs:
- Customary address text formats of the ecosystem (which may also be described in its [CAIP-10] profile) and the Interoperable Names text representation defined in [ERC-7930]
- Customary chain reference text formats of the ecosystem (which may also be described in [CAIP-2]) and the text representation defined in [ERC-7930]
- [ERC-7930]'s Interoperable Address and [ERC-7930]'s Interoperable Name of the ecosystem's chain references
- [ERC-7930]'s Interoperable Address and [ERC-7930]'s Interoperable Name of the ecosystem's addresses *for all address formats of said ecosystem*

And must also define the binary id of the namespace itself, akin to [ERC-7930]'s `ChainType`

For this, every namespace MUST define specify all of the above in a CAIP-350 profile to maximize interoperability and review. A [template for these profiles](https://github.com/ChainAgnostic/namespaces/blob/main/_template/caip350.md) is defined in the Namespaces registry.

## Rationale
The main alternative to this standard would've been to define all formats & conversions in [ERC-7930], turning it into a Living ERC, which was not ideal since:
- Living standards are not a usual thing in the Ethereum ecosystem: the only other Living ERC is EIP-1
- It would be desireable to finalize the definition of the format itself while allowing the specification of every chain's serialization to be defined as needed.
- It would have placed all the editorial (and presumably most of the specification) work on the [ERC-7930] authors, while the CASA paradigm allows better distribution of work by leaving the definition of the profile for every chain namespace to the parties of its ecosystem interested in using Interoperable Addresses.

## Test Cases
Not applicable since this does not define a normative interface, instead being a meta-specification of normative interfaces.

## Security Considerations
It is possible that a CAIP-350 profile for a given chain namespace is not able to guarantee requirements of canonicity which are a desired property of Interoperable Addresses due to characteristics of the chain namespace, and systems relying on that canonicity may exhibit unexpected behavior as a result. When this is the case, it should be noted in the `Extra Considerations` section.

## Backwards Compatibility
This standard actively seeks to be exhaustive in its backwards compatibility with CAIP-2 and CAIP-10.

## References

- [CAIP-2] defines CAIP namespaces
- [CAIP-10] defines a text format for blockchain accounts/target addresses
- [ERC-7930] defined the binary Interoperable Address format and an optional text representation, comparable to CAIP-10.

[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
<!-- TODO: point to the EIP website when the PR is merged -->
[ERC-7930]: https://ethereum-magicians.org/t/erc-7930-interoperable-addresses/23365

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
