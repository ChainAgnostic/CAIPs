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
The purpose of every supporting profile is be to be able to deterministically and unambiguously convert between:
- Customary address text formats of the ecosystem (which may also be described in its [CAIP-10] profile) and the Interoperable Address text representation defined in [ERC-7930]
- Customary chain reference text formats of the ecosystem (which may also be described in [CAIP-2])and the text representation defined in [ERC-7930]
- [ERC-7930] text representation -> [ERC-7930] binary representation and back of the ecosystem's chain references
- [ERC-7930] text representation -> [ERC-7930] binary representation and back of the ecosystem's addresses *for all address formats of said ecosystem*

And must also define the binary id of the namespace itself, akin to [ERC-7930]'s `ChainType`

For this, every namespace MUST define the following sections in their definition of their CAIP-000 profile:

```
ChainType binary key: 0xXXXX
CAIP-2 namespace: <!-- namespace as defined by CAIP-2 the binary key above maps to -->

## Chain reference

### Text representation
<!-- a description of the format of chain namespace + reference intended for the text representation of ERC-7930 Interoperable Addresses -->
<!-- MUST include how to represent the ChainType without a reference, since that is supported by [ERC-7930] -->

##### Text representation -> CAIP-2 conversion
<!-- instructions for how to convert from the above to a CAIP-2 string -->

##### CAIP-2 - text representation conversion
<!-- instructions for how to convert from a CAIP-2 string to the Interoperable Address format -->

#### Binary representation
<!-- description of how will chain references be laid out in binary Interoperable Addresses' `ChainReference` field -->

#### Text -> binary conversion
<!-- instructions for converting from the text representation to the binary one -->

#### Binary -> text conversion
<!-- instructions for converting from the text representation to the binary one -->

#### Examples

## Addresses

### Text representation
<!-- a description of the format of addresses intended for the text representation of ERC-7930 Interoperable Addresses -->

##### Text representation -> customary text address formats conversion
<!-- instructions for how to convert from the above to the address formats normally used in the ecosystem -->
<!-- MUST cover all address types used in the ecosystem -->

##### customary text addresses -> text representation conversion
<!-- instructions for how to convert from text address normally used in the ecosystem to the Interoperable Address format -->
<!-- MUST cover all address types used in the ecosystem -->

#### Binary representation
<!-- description of how will addresses be laid out in binary Interoperable Addresses' `Address` field -->

#### Text -> binary conversion
<!-- instructions for converting from the text representation to the binary one -->

#### Binary -> text conversion
<!-- instructions for converting from the text representation to the binary one -->

#### Examples

### Extra considerations
<!-- Anything that is particular to this namespace and of interest to users, such as not being able to satisfy canonicity requirements -->
```

## Rationale
The main alternative to this standard would've been to define all formats & conversions in [ERC-7930], turning it into a Living ERC, which was not ideal since:
- Living standards are not a usual thing in the Ethereum ecosystem: the only other Living ERC is [EIP-1]
- It would be desireable to finalize the definition of the format itself while allowing the specification of every chain's serialization to be defined as needed.
- It would have placed all the editorial (and presumably most of the specification) work on the [ERC-7930] authors, while the CASA paradigm allows better distribution of work by leaving the definition of the profile for every chain namespace to the parties of its ecosystem interested in using Interoperable Addresses.

## Test Cases
Not applicable since this does not define a normative interface, instead being a meta-specification of normative interfaces.

## Security Considerations
It is possible that a CAIP-000 profile for a given chain namespace is not able to guarantee requirements of canonicity which are a desired property of Interoperable Addresses due to characteristics of the chain namespace, and systems relying on that canonicity may exhibit unexpected behavior as a result. When this is the case, it should be noted in the `Extra Considerations` section.

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
