---
caip: 168
title: IPLD Timestamp Proof
author: Zach Ferland <@zachferland>, Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/168
status: Review
type: Standard
created: 2022-10-27
updated: 2022-11-7
requires: 74
---

## Simple Summary 

Create and verify IPLD based blockchain timestamp proofs. 

## Abstract

This specification describes the construction and verification of
[IPLD](https://ipld.io/) based blockchain timestamp proofs. Blockchain
timestamping or anchoring is a commonly used pattern to ensure the data
integrity of an arbitrarily large dataset that does not fit on chain (due to
cost, size, limits, etc), by only publishing a small unique digest of the
dataset on the blockchain. Authenticated DAG based data structures, like IPLD,
are a powerful and native way to represent datasets to be blockchain anchored
and to define related blockchain anchor proofs. IPLD also being general enough
to represent and reference many types of datasets, including existing DAG based
data structures like blockchains. 

## Motivation

IPLD based blockchain anchor proofs offer a primitive building block to
construct different types of databases and data models on top of a DAG based
data structure. Frequently blockchain anchors are used for timestamping, by
allowing some notion of time to be attributed to events in a decentralized way
and for the properties and security of an existing blockchain to support it.
This specification emphasis timestamping based use cases.

Further, when anchoring multiple events, this allows for event ordering and time
based conflict resolution. This provides the building blocks for different types
of consensus algorithms to be implemented over an IPLD based data structure.

## Specification

Merkle trees are a commonly used data structure, this specification focuses on
elaborating where needed to describe them in IPLD, while leaving other details
to common Merkle tree descriptions and implementations. Formats and types are
described using [IPLD schema language](https://ipld.io/docs/schemas/).

### IPLD Based Digest

In a general blockchain anchor implementation a message digest is published to
the blockchain. There are many ways to create such a digest and implementors
have to agree on construction, naming, resolution, etc. [IPLD
CIDs](https://github.com/multiformats/cid) already serve this purpose by
offering a standard self-describing format, cryptographic hashing and content
addressing. 

### IPLD Based Merkle Tree

Typically users, applications, and services/protocols are interested in
committing a set of data in a blockchain anchor. As it is can be inefficient to
commit a single piece of data. Generally Merkle trees have been used to
efficiently publish a blockchain anchor by publishing only the Merkle root over
a set of inputs and allowing verification that a given piece of data was
included in the set through a Merkle proof. 

Again IPLD offers a natural and standard way to encode a Merkle tree, with
the added benefit of allowing anyone to easily resolve the entire tree, paths or
subset of the tree when verifying a Merkle proof. We suggest encoding a IPLD
Merkle tree as follows, but IPLD is self descriptive enough that verification is
possible with any construction. 

The set of data to be committed, or “leaves” of the Merkle tree are expected to
be a set of IPLD CIDs. A binary Merkle tree is used, and a node value is
constructed with two “leaves” or CIDs using a list. The data is encoded in IPLD
with [DAG-CBOR](https://ipld.io/specs/codecs/dag-cbor/spec/), the resulting IPLD
block results in another CID for this tree node. 

IPLD Merke Tree Node Type

```tsx
type NodeList [Link]
```

Example 1

```tsx
[CID(bafyreig3lwe3xf2qmip6radn7iuars7g4xr2ktrgoxyryx65cwe4jpbmny), CID(bafyreieaqjqd7oee3aaghcal3woxbxmjy6l52wt4yp24o6ei3cayeiu2cm)
```

Non leaf nodes are then recursively built in the same way using list pairs of
CIDs until you have constructed the entire Merkle tree and a root Node/CID. 

### IPLD Blockchain Anchor

A blockchain anchor is made up of both a Merkle tree root CID and the
corresponding reference to a blockchain transaction which includes the Merkle
tree root. The blockchain transaction is referenced by an IPLD CID using the
corresponding IPLD codec for the blockchain and the [CAIP-2][] chainID. There
already exist IPLD codecs for two of the most commonly used blockchains for
anchoring, Bitcoin and Ethereum. IPLD codec support may have to be added to
support other chains. 

```tsx
type BlockchainAnchor struct {
  root Link
  chainID String 
  txHash Link
  txType String
}
```

Example 2
```tsx
{
  root: CID(bafyreiaxdhr5jbabn7enmbb5wpmnm3dwpncbqjcnoneoj22f3sii5vfg74)
  chainID: "eip155:1"
  txHash: CID(bagjqcgzanbud4sqdsywfp2mckuj57qsffsovgyjhh7sxebkqwr335hzy2zbq)
  txType: 'raw'
}
```

Blockchain transaction resolution, parsing, and verification are to be defined
by a per-namespace CAIP-168 profile in the CASA [Namespaces][] registry.
Implementations will vary and will even vary for the same chain. The `txType`
parameter string is used to discriminate between different transaction
**verification methods** for the same chain. Every `txType` is expected to be
defined in the corresponding blockchain namespace, included in the [CAIP-2][]
identifier. The example above references the `raw` transaction type verification
method, documented in the [`eip155` namespace][]. 

### IPLD Based Merkle Proofs

A Merkle proof allows verification that a given piece of data or “leaf” was
included in the set of data for the given Merkle Tree. A  typical proof includes
a Merkle root of the tree and a subset list of tree node hashes that allow
verification by reconstructing the Merkle root starting from the data or "leaf"
you are interested in.

In IPLD a Merkle root (CID) and a “path” are provided that allows you to
traverse the tree from the root to the piece of data your interested in. Paths
are strings and refer to [Pathing in
IPLD](https://ipld.io/docs/data-model/pathing/). Following an IPLD path, and
ultimately arriving at the data or "leaf" you are interested in from the root,
is equivalent to a standard proof. Implementations may resolve the subset of the
tree needed for verification on demand from a network (for example IPFS) or may
package all necessary blocks/nodes together and present them alongside the
proof. 

As defined above, an IPLD Merkle tree node includes a list pair. List traversal
in IPLD is defined by index, for example 0 referencing the first item, 1
referencing the second. An example IPLD path through a tree could look as such
`0/0/1/0/1/1`. It is also possible to reference trees as part of other paths,
for example `myprotocol/mytree/0/0/1/0/1/1` or any general path if the suggested
construction above is not followed. 

### IPLD Anchor Proof

The combination of an IPLD based Merkle proof and blockchain anchor now make up
the entirety of an anchor proof for a given piece of data. 

```tsx
type AnchorProof struct {
  proof Link
  path String 
}
```

Example 4
```tsx
{
  proof: CID(bafyreietaedczjxh7omxfjm3oj55nbmxaga3himruubppgj3hp6dayonqq)
  path: "0/0/1/0/1/1"
}
```

Additional parameters can be added specific to a protocol or implementation that
directly reference the data or support additional verification steps. For
example a `data` parameter mapping to a CID, or a `prev` parameter in a log
based IPLD data structure mapping to the prior CID in a log. 

### Anchor Proof Verification (Generic)

The following algorithm describes the steps required to verify a given anchor
proof. If any step fails, an error MUST be raised. 

1) Resolve blockchain anchor CID (anchor_proof.proof).
2) Resolve blockchain transaction by `txHash` CID and `chainId`.
3) Verify that the blockchain transaction includes the blockchain anchor root
   (`blockchain_anchor.root`) in its payload. How it is included in the
   transaction will be namespace specific, and specified per namespace.
4) Verify that the blockchain transaction is valid, as defined by that
   blockchain and the norms of the given namespace and blockchain. Transaction
   validity is out of scope here. 
5) Resolve the path (`anchor_proof.path`) in IPLD. Path resolution MUST resolve.
   If it does not, an error MUST be raised. 
6) Return, anchor proof is valid for the data resolved by the path (`anchor_proof.path`).

## Known Implementations

- [Ceramic Network's
  `js-ceramic`](https://github.com/ceramicnetwork/js-ceramic/blob/develop/packages/core/src/anchor/ethereum/ethereum-anchor-validator.ts)

## Links
- [IPLD][] Core documentation
- [Pathing in IPLD][] General introduction
- [IPLD CIDs][] Specification on Multiformats (github)
- [IPLD Schema Language][] Specification at ipld.io
- [DAG-CBOR][] Specification at ipld.io

[CAIP-2]: https://chainagnostic.org/CAIPs/CAIP-2
[Namespaces]: https://namespaces.chainAgnostic.org/
[`eip155` namespace]: https://namespaces.chainAgnostic.org/eip155/caip168
[IPLD]: https://ipld.io/
[IPLD CIDs]: https://github.com/multiformats/cid
[IPLD schema language]: https://ipld.io/docs/schemas/
[dag-cbor]: https://ipld.io/specs/codecs/dag-cbor/spec/
[Pathing in IPLD]: https://ipld.io/docs/data-model/pathing/

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
