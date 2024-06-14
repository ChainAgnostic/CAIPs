---
caip: X
title: Best Practices for using CAIP 2 - Blockchain ID Specification
author: Bumblefudge (@bumblefudge)
discussions-to: https://github.com/ChainAgnostic/namespaces/pull/107, https://github.com/ChainAgnostic/CASA/issues/107, https://github.com/ChainAgnostic/CAIPs/issues/22, https://github.com/ChainAgnostic/namespaces/issues/55
status: Draft
type: Informational
replaces: CAIP-2
created: 2024-06-14
updated: 2024-06-14
---

## Simple Summary

Since being finalized, [CAIP-2] usage has expanded and has new caveats worth specifying publicly.

## Motivation

CAIP-2 defined a way to identify a blockchain (e.g. Ethereum Mainnet, Görli, Bitcoin, Cosmos Hub) in a human-readable, developer-friendly and transaction-friendly way.
It formed the basis first for [CAIP-10] and [CAIP-19], which were later generalized into a more extensible and general [URN] framework first specified in [CAIP-104].
Adoption since finalizing the specification has surfaced corner cases, which led to terminological changes and new features.

## Changes to CAIP-2 Usage

### Language

Originally conceived as a translation layer between the identifier schemes for "chains" in different "blockchain communities," there has been interest in writing [104] namespace documents for cryptographic systems that do not organize instances into "chains" but the more general sense of networks of nodes with data in common; for this reason, "network" is preferred as the referent for [CAIP-2] identifiers, particularly when applied to DAGs, git-based systems, and other distributed systems not organized primarily around linked-list data structures.

### Non-Uniqueness of Addressed Resources

As mentioned in [CAIPs#22], a given network is assumed to be unique within a namespace, but not unique across all namespaces.
Concretely, this means that a given network addressable by a specific [CAIP-2] identifier in one namespace might also be addressable by the same or different identifiers in other namespaces.

### Special Case for non-network identifer

Implementer feedback from the Ethereum community led to PR [namespaces#107] describing an emerging pattern whereby a static identifier could be used to refer not to a specific entry in the `eip155`-defined namespace of networks, but to the wallet software itself as an actor outside of them, such as when a website communicates with a user-agent controlling an EOA over RPC methods.
This may be generalizable to other namespaces over time, or may be superseded by complementary or competing solutions. It is unclear if all namespaces have an equivalent or other non-network identifer needs.

## References

[namespaces#55]: https://github.com/ChainAgnostic/namespaces/issues/55
[namespaces#107]: https://github.com/ChainAgnostic/namespaces/pulls/107
[CAIPs#22]: https://github.com/ChainAgnostic/CAIPs/issues/22
[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[URN]: https://www.rfc-editor.org/rfc/rfc8141