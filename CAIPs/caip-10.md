---
caip: 10
title: Account ID Specification
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/10
status: Review
type: Standard
created: 2020-03-13
updated: 2022-10-23
requires: 2
---

## Simple Summary

CAIP-10 defines a way to identify an account in any blockchain specified by
CAIP-2 blockchain id.

## Abstract

This proposal aims to facilitate specifying accounts for any blockchain
extending [CAIP-2][] chain id specification. This is useful for both
decentralized applications and wallets to communicate user accounts (EOA in EVM
terminology) or smart contracts/abstraction for multiple chains using string
identifiers specific to each chain. Currently, wallets are usually designed for
each chain and multi-chain wallets use proprietray data structures to
differentiate accounts. This proposal aims to standardize these identifiers for
accounts to allow interoperability.

## Motivation

The motivation for proposal stem from designing a chain-agnostic protocol for
communication between dapps and wallets that was independent of any blockchain
but provide the flexibility to be backwards compatible with existing
applications.

## Specification

The account id specification will be prefixed with the [CAIP-2][] blockchain ID
and delimited with a colon sign (`:`)

### Syntax

The `account_id` is a case-sensitive string in the form

```
account_id:        chain_id + ":" + account_address
chain_id:          [-a-z0-9]{3,8}:[-a-zA-Z0-9]{1,32} (See [CAIP-2][])
account_address:   [-.%a-zA-Z0-9]{1,128}
```

Note that `-`, `%` and `.` characters are allowed, but no other
non-alphanumerics such as `:`, `/` or `\`.  Implementers are recommended to use
"URL encoding" (% + 2-character codes, canonically capitalized) as per [Section
2][rfc3986sec2.1] of [RFC 3986][rfc3986] to escape any further non-alphanumeric
characters, and to consider [homograph attack surfaces][homograph] in the handling
of any non-alphanumerics.

### Semantics

The `chain_id` is specified by the [CAIP-2][] which describes the blockchain id.
The `account_address` is a case sensitive string which its format is specific to
the blockchain that is referred to by the `chain_id`.

## Rationale

The goals of the general account ID format is:

- Uniqueness between chains regardless if they are mainnet or testnet
- Readibility using the prefix of a chainId to quickly identify before parsing the address
- Restricted to constrained set of characters and length for parsing

## Canonicalization

Note that some namespaces like the EVM offer canonicalization schemes that use
capitalization (e.g. [EIP-55][]), an option suffix (e.g. [HIP-15][]), or some
other transformation. At the present time, this specification does NOT require
canonicalization, and implementers are advised to consider deduplication or
canonicalization in their consumption of CAIP-addresses. CAIP-10 profiles in
CASA [namespaces][] may contain additional information per namespace.

## Test Cases

This is a list of manually composed examples

```
# Ethereum mainnet (canonicalized with [EIP-55][] checksum)
eip155:1:0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb

# Bitcoin mainnet
bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6

# Cosmos Hub
cosmos:cosmoshub-3:cosmos1t2uflqwqe0fsj0shcfkrvpukewcw40yjj6hdc0

# Kusama network
polkadot:b0a8d493285c2df73290dfb7e61f870f:5hmuyxw9xdgbpptgypokw4thfyoe3ryenebr381z9iaegmfy

# Dummy max length (64+1+8+1+32 = 106 chars/bytes)
chainstd:8c3444cf8970a9e41a706fab93e7a6c4:6d9b0b4b9994e8a6afbd3dc3ed983cd51c755afb27cd1dc7825ef59c134a39f7

# Hedera address (with optional checksum suffix per [HIP-15][])
hedera:mainnet:0.0.1234567890-zbhlt

```

## Backwards Compatibility

Previously, the character set was much more restrictive for CAIP-10s, allowing
no non-alphanumeric characters.  See [pre-2022-10-23
version](https://github.com/ChainAgnostic/CAIPs/blob/8fdb5bfd1bdf15c9daf8aacfbcc423533764dfe9/CAIPs/caip-10.md)
of specification for details.

Before that, legacy CAIP-10 schema was defined by appending as suffix the CAIP-2
chainId delimited by the at sign (`@`). See [pre-2021-08-21
version](https://github.com/ChainAgnostic/CAIPs/blob/0697e26601d30d8e99df17954ed3e5a1fd59e049/CAIPs/caip-10.md)
of specification for details.

```
# Legacy example pre-2021-08-21
0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb@eip155:1
```

## Changelog

- 2022-10-23: expanded charset to include `-`,`.`, and `%`; also added
  canonicalization section and links
- 2022-03-10: update RegEx to incorporate CAIP-2 reference
- 2021-08-11: switch from `{account id}@{chain id}` to `{chain id}:{account id}`
  syntax

## Links

- [IETF RFC 3986][rfc3986] - the IETF standard for URL, URI and URN syntax
- [CAIP-2][] - CASA Chain ID specification
- [EIP-55][] - Ethereum Improvement Proposal for canonicalizing ethereum addresses to by deterministic capitalization of a-f characters
- [HIP-15][] - Hedera Improvement Proposal defining a checksum suffix for addresses

[namespaces]: https://namespaces.chainagnostic.org/
[EIP-55]: https://eips.ethereum.org/EIPS/eip-55
[HIP-15]: https://github.com/hashgraph/hedera-improvement-proposal/blob/main/HIP/hip-15.md
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[rfc3986]: https://www.rfc-editor.org/rfc/rfc3986
[rfc3986sec2.1]: https://www.rfc-editor.org/rfc/rfc3986#section-2.1
[homograph]: https://en.wikipedia.org/wiki/IDN_homograph_attack

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
