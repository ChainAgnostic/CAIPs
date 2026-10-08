---
caip: CAIP-X
title: Label ID Specification
author: Vakhtanh Chikhladze (@Pozzitron1337)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/409
status: Draft
type: Standard
created: 2026-10-08
updated: 2026-10-08
requires: [2, 10, 19]
---

## Simple Summary

A compact **named label** that identifies a protocol, exchange, aggregator,
source, intent network, or other non-account agent on a specific blockchain, by
placing a short name before a [CAIP-2][] chain id, separated by `@`.

Example: `cow@eip155:1` — read as “cow at Ethereum”.

## Abstract

[CAIP-2][] identifies chains.
[CAIP-10][] identifies accounts *on* a chain.
[CAIP-19][] identifies assets *on* a chain.

Applications also need **named labels** scoped to a chain: a DEX router family,
an intent settlement network, a bridging aggregator, or any other off-chain or
on-chain agent that is neither an account address nor an asset type.

This CAIP defines a `label_id` of the form:

```
name + "@" + chain_id
```

where `chain_id` is a [CAIP-2][] blockchain id and `name` is a short, URL-safe
string registered or agreed out of band for that agent.

The `@` delimiter follows the long-standing Unix `user@host` pattern and also
mirrors the *legacy* [CAIP-10][] `account@chain` shape (superseded in 2021),
while remaining unambiguous against current CAIP-10 (`chain:account`) because
the left-hand side is a named label, not an account address.

## Motivation

Unix systems have used `user@host` (and later `user@host:path`) for decades to
mean “this principal, on that machine”, or `alice@mail.example.com`,
`root@192.168.0.1`, `git@github.com`. The left-hand side is a **local name**;
the right-hand side is the **scope** where that name is meaningful. The same
local name on two hosts is two different identities (`alice@host-a` ≠
`alice@host-b`).

Multi-chain applications need the same pattern for **named labels**: a short
local name for a protocol or service, scoped to a blockchain. Practitioners
already speak this way in prose like “CoW on Arbitrum”, “LI.FI on Base”, “0x on
Ethereum”, but lack a shared string form analogous to `user@host`.

Today such references are ad hoc:

- opaque registry hashes with no chain binding in the preimage;
- CAIP-2 alone (`eip155:1`), which names the chain but not the agent on the
  chain;
- CAIP-10, which names an *account* (EOA or contract address), not a labeled
  family that may span many contracts or off-chain APIs.

Without a shared syntax:

1. Registries cannot key “same named label, different chain” consistently.
2. Off-chain clients and on-chain kind identifiers diverge.
3. UIs and explorers cannot display a stable human-readable label next to a
   chain id.

Mapping the Unix analogy onto CAIP:

| Unix | This CAIP |
| --- | --- |
| `user` | `name` (local label) |
| `host` | `chain_id` ([CAIP-2][]) |
| `user@host` | `name@chain_id` (`label_id`) |

[CAIP-2][], [CAIP-10][], and [CAIP-19][] deliberately leave this gap. This CAIP
fills it with a minimal, parseable form that reuses CAIP-2 for the chain
component and `@` for the same “name at scope” reading Unix established.

## Specification

### Syntax

The `label_id` is a case-sensitive string in the form

```
label_id:  name + "@" + chain_id
name:      [-.%a-zA-Z0-9]{1,128}
chain_id:  [-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32}   (See [CAIP-2][])
```

Notes:

- `name` uses the same character set as [CAIP-10][] `account_address`.
- `name` MUST NOT contain `@` or `:`.
- `name` MAY be either:
  - a short brand / protocol label (RECOMMENDED: lowercase `[-a-z0-9]{1,32}`),
    or
  - a chain-native account or contract address used as the named label
    (same address syntax as that chain's [CAIP-10][] profile).
- `chain_id` MUST be a valid [CAIP-2][] blockchain id.
- Exactly one `@` separates `name` from `chain_id`.
- There is no path, query, or fragment component.

A regular expression sufficient for coarse validation (not a substitute for
CAIP-2 validation of the right-hand side):

```
^[-.%a-zA-Z0-9]{1,128}@[-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32}$
```

### Semantics

- **`name`** is the local part of a named label: a protocol brand, aggregator,
  settlement network, other application-defined agent kind, **or** a specific
  account/contract address that names the agent. Resolution of names to
  deployments, APIs, or bytecode is **out of scope** for this CAIP (see
  [Rationale](#rationale)).
- **`chain_id`** scopes that name to one blockchain per [CAIP-2][].
- Equality is exact string equality of the full `label_id` (case-sensitive).
- Two label ids with the same `name` and different `chain_id` are distinct
  named labels (e.g. `cow@eip155:1` ≠ `cow@eip155:42161`).

### Relationship to other CAIPs

| Form | CAIP | Identifies |
| --- | --- | --- |
| `eip155:42161` | [CAIP-2][] | Chain |
| `eip155:42161:0xab16…` | [CAIP-10][] | Account on a chain |
| `eip155:42161/erc20:0xaf88…` | [CAIP-19][] | Asset on a chain |
| `cow@eip155:42161` | **this CAIP** | Named label on a chain (brand name) |
| `0x9008…@eip155:1` | **this CAIP** | Named label on a chain (address as name; backwards-compatible with legacy [CAIP-10][]) |

When `name` is an address, the string form coincides with *legacy* [CAIP-10][]
(`account@chain`). That overlap is intentional: an address is a valid named
label. Semantics differ by context — a `label_id` names an agent; a [CAIP-10][]
account id (current `chain:address` form) identifies an account for
authentication and balance. Consumers MUST not treat a `label_id` as proof of
control of the address.

### Optional: hashing for on-chain registries

When a fixed-width identifier is required on-chain (e.g. `bytes32` registry
keys), this CAIP RECOMMENDS:

```
label_kind = keccak256(utf8(label_id))
```

using the Ethereum [keccak256][] hash of the UTF-8 encoding of the full
`label_id` string (including `@` and the CAIP-2 chain id). Other hash
functions MAY be used if declared by the consuming protocol; this CAIP does
not mandate keccak256 outside EVM contexts.

Example (illustrative):

```
keccak256("cow@eip155:42161")
```

## Rationale

### Why `@` instead of `:` or `/`

- `:` is already overloaded inside CAIP-2 (`eip155:42161`) and CAIP-10
  (`eip155:42161:0x…`). Nesting another colon-delimited field is ambiguous
  without a new structural rule.
- `/` is reserved by CAIP-19 for asset paths.
- `@` is the established “name at scope” delimiter in Unix (`user@host`) and
  was previously used by legacy CAIP-10 for “entity on chain”
  (`account@chain`). Reusing it for **named labels** keeps that reading while
  current CAIP-10 no longer claims that shape.

### Why not put the name after the chain

Forms like `eip155:42161/cow` collide with CAIP-19 asset_type syntax
(`chain_id/asset_namespace:asset_reference`). Putting the name first makes
label ids visually and structurally distinct.

### Why not standardize a global label registry here

CASA already separates **syntax** (CAIPs) from **namespace profiles** and
ecosystem registries. A mandatory on-chain or off-chain name registry would
block adoption. This CAIP only fixes the string shape. Communities MAY publish
profiles listing recommended `name` values (similar to CAIP-19 asset
namespaces).

### Alternate designs considered

1. **`chain_id:name`** — rejected: ambiguous vs CAIP-10 (`chain:account`) when
   `name` looks like an address fragment.
2. **`chain_id/label:name`** — rejected: ambiguous vs CAIP-19.
3. **URN / DID** (`did:label:cow:eip155:42161`) — deferred; heavier than needed
   for registries and application wiring.
4. **Opaque hashes only** — rejected: not human-readable; still need a
   canonical preimage, which this CAIP supplies.

## Test Cases

```
# CoW Protocol on Arbitrum One
cow@eip155:42161

# LI.FI (classic or intents) on Ethereum mainnet
lifi@eip155:1

# 0x Swap API on Base
0x@eip155:8453

# Named label on Solana mainnet (CAIP-2 genesis-hash reference)
jupiter@solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp

# Hyphenated name
one-inch@eip155:1

# ENS-style name (`.` allowed in name charset; resolution out of scope)
vitalik.eth@eip155:1

# Named label by contract address (EVM)
0x9008D19f58AAbD9eD0D60971565AA8510560ab41@eip155:1

# Named label by account address (Solana)
JUP6LkbZbjS1jKKwapdHNy74zcZ3tLUZoi5QNyVTaV4@solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp

# Invalid: colon inside name
# co:w@eip155:1

# Invalid: missing chain reference
# cow@eip155

# Invalid: second @
# cow@intent@eip155:1

# Not a label_id (current CAIP-10 account id uses chain:address)
eip155:42161:0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb
```

## Security Considerations

- **Homographs / spoofing:** Prefer short lowercase names for brands.
  Address-as-name forms inherit the address charset of the chain; apply the
  same canonicalization guidance as [CAIP-10][] (e.g. EIP-55) when comparing.
- **Name squatting:** Without a registry, two parties may claim the same brand
  name. Consumers MUST treat names as trust-anchored by their own allowlists or
  governance, not by this CAIP alone.
- **Chain binding:** Hashing only `name` (without `chain_id`) enables
  cross-chain confusion. Prefer hashing the full `label_id`.
- **Address-as-name:** `address@chain_id` is a valid `label_id`. It does **not**
  imply the caller controls that address; do not use a label id as an
  authentication credential.

## Privacy Considerations

`label_id` strings are not personal data. When a `name` is an address, or when
a label id is logged next to [CAIP-10][] account ids, it may contribute to
correlation of activity. Consumers SHOULD apply the same care as when logging
CAIP-10 identifiers.

## Backwards Compatibility

- No existing Final CAIP defines `name@caip-2` for named labels.
- Legacy [CAIP-10][] used `account_address@chain_id`. That string form is a
  valid `label_id` when the left-hand side is used as a named label; see
  [Relationship to other CAIPs](#relationship-to-other-caips).
- Introducing this CAIP does not change CAIP-2, CAIP-10, or CAIP-19.

## References

- [CAIP-1][] defines the CAIP document structure
- [CAIP-2][] Blockchain ID Specification
- [CAIP-10][] Account ID Specification (includes legacy `account@chain`)
- [CAIP-19][] Asset Type and Asset ID Specification
- [ChainAgnostic/CAIPs][caips-repo] contribution process

[CAIP-1]: https://chainagnostic.org/CAIPs/caip-1
[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-19]: https://chainagnostic.org/CAIPs/caip-19
[caips-repo]: https://github.com/ChainAgnostic/CAIPs
[keccak256]: https://ethereum.org/en/developers/docs/apis/json-rpc/#web3_sha3

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
