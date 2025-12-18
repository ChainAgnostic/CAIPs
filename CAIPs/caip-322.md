---
caip: 322
title: URI Scheme for CAIP identifiers
author: [Pedro Gomes] (@pedrouid), [ligi](ligi@ligi.de), [bumblefudge](@bumblefudge)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/67
status: Draft
type: Standard
created: 2024-08-08
requires: 2, 10, 19, 20, 21, 22, 104
---

## Simple Summary

Various CAIPs to date have defined generic chain-agnostic identifier schemes for certain informational primitives common to blockchain and other decentralized peer-to-peer systems.
This URI scheme combines them into a structure that can be parsed heirarchically.

## Abstract

A general-purpose heirarchical URI scheme for identifiers that dereference to "on-chain" or otherwise cryptographically self-certifying records from peer-to-peer cryptographic systems.
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
All segments after the namespace are optional but heirarchical, i.e., a [CAIP-2][] chain identifier without a preceding namespace segment is invalid, and a [CAIP-10][] account identifier without a preceding chain identifier is invalid.

## Rationale

The membership of the Chain-Agnostic Standards Alliance has been experimenting with, refining, and expanding the applicability of these identifier schemes since 2021.
Detailed rationales for each component of this scheme can be found following the "discussions-To" link in each applicable CAIP's metadata.

## Test Cases

// TODO

## Security Considerations

These identifiers should be thought of as pointers and the persistence or availability of their referents cannot be guaranteed.
They inherit the availability, discoverability, and garbage-collection properties of the cryptographic record systems that they register, so close attention to the first segment and the descriptive documentation provided in their registration entries is crucial;
these should be allowlisted one at a time, as no universal assumptions can be made about them, including the applicability of a [CAIP-2][] identifier scheme for specific networks in that namespace.

## Privacy Considerations

Similarly, these identifiers refer to data that is most often immutably public, as it has been in all completed registrations to date.
As such, this should be the baseline assumption unless contradicted or caveated by the registration of top-level namespaces.

## Backwards Compatibility

It is important to note that most usage to date of [CAIP-2][], [CAIP-10][], and [CAIP-19][] identifiers has used these without a `caip:` prefix or a `caip:///` prefix, in contexts where these identifiers are unlikely to be encountered outside of their meaningful context.
Care should be taken to add the `caip:` prefix when merging such lists or datasets into more 

## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-1][] defines the CAIP document structure
- [CAIP-104][] defines the CAIP namespaces directory
- [CAIP-2][] defines the network-identifier syntax for each namespace's network topology, which in some cases includes wildcard or subnet-wide identifiers
- [CAIP-10][] defines the "account"-identifier syntax for each namespace's actor model
- [CAIP-19][] defines the "asset"-identifier syntac for each namespace's stably-addressable assets with an eye to commonalities


[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[CAIP-19]: https://ChainAgnostic.org/CAIPs/caip-19
[CAIP-104]: https://ChainAgnostic.org/CAIPs/caip-104

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
