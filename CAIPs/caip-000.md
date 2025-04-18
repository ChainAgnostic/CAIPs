---
caip: 000
title: Binary serialization of blockchain IDs and addresses
author: Defi Wonderland (@defi-wonderland), Teddy (0xteddybear)
discussions-to: https://ethereum-magicians.org/t/erc-7930-interoperable-addresses/23365
status: Draft
type: Standard
created: 2025-04-18
requires: 2
---

## Simple Summary
A CAIP profile for every chain namespace to describe how to serialize their addresses and chain references into the Interoperable Address format described in [ERC-7930].

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
Every chain defines their own address types & also a way to identify networks within that chain's ecosystem.
[ERC-7930] is an address format for (address, chain) pairs supporting any chain, and also including metadata on the chain & addresses' type so no information needs to be communicated out of band.
This CAIP namespace aims to be a living & mutable repository of serialization methods for all chains to uniformly conform to Interoperable Addresses.

## Motivation
Standards like CAIP-2 and CAIP-10 are simple text representations of addresses & chain namespaces/references, but they do not address:
- Binary representation of addresses and/or chain namespace+reference: Relevant & desired for on-chain usage.
- Canonicity: CAIP-10, being a text format which leaves serialization to actual on-chain addresses undefined, makes no guarantees on a blockchain account (_target address_ in ERC-7930 parlance) having only one CAIP-10 representation, which makes them less useful as dictionary keys.
- Completeness: both formats have limits on the length of their identifiers, which are reasonable for human-readability but often identifiers have to drop meaningful information to conform to those length requirements. While that information might be easy to look up in the context of wallet software, doing so within a smart contract would not be possible.
- Succinctness: Text formats have an inherent overhead over binary ones when measured for information efficiency in a machine context.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->

## References
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-2] defines the CAIP document structure
- [ERC-7930] defined the binary Interoperable Address format and an optional text representation, comparable to CAIP-10.

[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
<!-- TODO: point to the EIP website when the PR is merged -->
[ERC-7930]: https://ethereum-magicians.org/t/erc-7930-interoperable-addresses/23365

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
