---
caip: CAIP-363
title: Chain ID Wildcard
author: Daniel Rocha (@danroc)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/364
status: Draft
type: Standard
created: 2025-07-01
requires: CAIP-2, CAIP-10
---

## Simple Summary

This CAIP extends CAIP-2 and CAIP-10 by reserving the `_` character as a
wildcard reference for "all chain IDs" within any CAIP-2 namespace. This allows
for the representation of multichain accounts, enabling wallets and
applications to express account identity across all chains of a given
namespace.

## Abstract

By reserving the `_` wildcard into CAIP-2-compliant chain ID definitions, and
supporting it within CAIP-10 account identifiers, this CAIP enables an address
to represent a multichain account. For example, `eip155:_:<address>` signifies
the given address across all chains in the `eip155` namespace.

## Motivation

Wallets and identity systems increasingly need to represent multichain
accounts. The lack of a standard for denoting an address across all chains in a
namespace hinders interoperability. Reserving `_` as a wildcard chain ID solves
this by enabling a consistent and extensible mechanism for such expressions.

## Specification

### CAIP-2 Extension

In addition to the existing CAIP-2 specification:

> A new reserved chain ID value `_` MUST be supported in all namespaces to
> indicate "all chains" in that namespace.

Thus, the chain ID format becomes:

```text
<namespace>:<reference | _>
```

Where `_` is interpreted as "all chain references within this namespace."

### CAIP-10 Extension

Extend the CAIP-10 account ID to support the wildcard chain reference:

```text
<namespace>:_:<account-address>
```

For example:

- `eip155:_:0x59f3...d0c3` represents the same address across all `eip155`
  chains.

- `solana:_:DAXa...bx77` represents a Solana account across all Solana chains
  (for example, Mainnet, Testnet, and Devnet).

### Reserved Character

A single `_` character is reserved solely for the wildcard chain reference and MUST
NOT be designated as a valid reference or account identifier in any future [CAIP-2] profile.

## Rationale

The `_` character is not currently a valid chain ID reference in any known
namespace, minimizing the risk of collision. It is also supported by the CAIP-2
grammar, ensuring that it can be parsed correctly without breaking existing
implementations.

## Backwards Compatibility

This CAIP is backwards compatible. Existing implementations that do not
support `_` will simply reject these identifiers as invalid, while updated
systems can handle them appropriately.

## Reference Implementation

No code changes are required in CAIP-2 and CAIP-10, but supporting
implementations (e.g., wallets, dapps) should treat `_` as matching any chain
reference within the specified namespace.

## Copyright

Copyright and related rights waived via CC0.

## References

- [CAIP-2]
- [CAIP-10]

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10

