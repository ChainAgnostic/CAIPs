---
caip: <to be assigned>
title: IPLD Anchor Events and Proofs 
author: Zach Ferland <@zachferland>, Joel Thorstensson (@oed)
discussions-to: <>
status: Draft
type: Standard
created: 2022-10-27
updated: 2022-10-27
requires: CAIP-74
---

## Simple Summary 

Create and verify IPLD based blockchain anchor events and proofs. 

## Abstract

This specification describes the construction and verification of [IPLD](https://ipld.io/) based blockchain anchor events and proofs. Blockchain anchors are a commonly used pattern to ensure the data integrity of an arbitrarily large dataset that does not fit on chain (due to cost, size, limits, etc), by only publishing a small unique digest of the dataset on the blockchain. Authenticated DAG based data structures, like IPLD, are a powerful and native way to represent datasets to be blockchain anchored and to define related blockchain anchor events. IPLD also being general enough to represent and reference many types of datasets, including existing DAG based data structures like blockchains. 

## Motivation

IPLD blockchain based anchor events offer a primitive building block to construct different types of databases and data models on top of a DAG based data structure. Frequently blockchain anchors are used for time-stamping, by allowing some notion of time to be attributed to events in a decentralized way and for the properties and security of an existing blockchain to support it. 

Further, when anchoring multiple events, this allows for event ordering and time based conflict resolution. This provides the building blocks for different types of consensus algorithms to be implemented over an IPLD based data structure.

## Specification

Merkle trees are a commonly used data structure, this specification focuses on elaborating where needed to describe them in IPLD, while leaving other details to common Merkle tree descriptions and implementations. Formats and types are described using [IPLD schema language](https://ipld.io/docs/schemas/).

### IPLD Based Digest

In a general blockchain anchor implementation a message digest is published to the blockchain. There are many ways to create such a digest and implementors have to agree on construction, naming, resolution, etc. [IPLD CIDs](https://github.com/multiformats/cid) already serve this purpose by offering a standard self-describing format, cryptographic hashing and content addressing. 

### IPLD Based Merkle Tree

Typically users, applications, and services/protocols are interested in committing a set of data in a blockchain anchor. As it is can be inefficient to commit a single piece of data. Generally Merkle trees have been used to efficiently publish a blockchain anchor by publishing only the Merkle root over a set of inputs and efficiently verify a given piece of data was included in the set through a Merkle proof. 

Again IPLD offers natural and standard way to to encode a Merkle tree, with the added benefit of allowing anyone to easily resolve the entire tree, paths or subset of the tree when verifying a Merkle proof. We encode a IPLD Merkle tree as follows. 

The set of data to be committed, or “leaves” of the Merkle tree are expected to be a set of IPLD CIDs. A binary Merkle tree is used, and a node value is constructed with two “leaves” or CIDs using a list. The data is encoded in IPLD with [dag-cbor](https://ipld.io/specs/codecs/dag-cbor/spec/), the resulting IPLD block results in another CID for this tree node. 

IPLD Merke Tree Node Type

```tsx
type NodeList [Link]
```

Example 1

```tsx
[CID(bagcqcerav5zyrlelnnx24blbgob6stz6y4oiwenn3hodbmyf7bnnguh6gdeq), CID(bagcqcera4nnpzmwsk2bbww6n4bgq3cwne6gmhkouciam44nyjbindkxpz2eq)
```

Non leaf nodes are then recursively built in the same way using list pairs of CIDs until you have constructed the entire Merkle tree and a root Node/CID. 

### IPLD Anchor Event

An Anchor event is made up of both a Merkle tree root CID and the corresponding reference to a blockchain transaction which includes the Merkle tree root. The blockchain transaction can be referenced by a IPLD CID if there is a corresponding IPLD codec for the blockchain and the CAIP-2 chainID or it can be referenced by a transaction id string. 

Note: There exists IPLD codecs for two of the most commonly used blockchains for anchoring, Bitcoin and Ethereum. An IPLD codec for a blockchain should not necessarily be required to represent an Anchor Event, so a transaction id string is supported as well. Transaction ids should have a corresponding CAIP before use, for now they are only referenced as an example and considered simple strings that any implementation must agree on or define in a future standard. 

If there is `chainID` parameter, we expect the `txId` to be an IPLD link to the transaction encoded using the related blockchain IPLD codec.

```tsx
type AnchorEvent struct {
  root Link
	chainID String 
	txId Link
}
```

Example 2
```tsx
{
  root: CID(bafyreiaxdhr5jbabn7enmbb5wpmnm3dwpncbqjcnoneoj22f3sii5vfg74)
	chainID: "eip155:1"
	txId: CID(bagjqcgzanbud4sqdsywfp2mckuj57qsffsovgyjhh7sxebkqwr335hzy2zbq)
}
```

If there is not a `chainID` parameter, we expect `txId` to be a transaction id string.

```tsx
type AnchorEvent struct {
  root Link
	txId String
}
```

Example 3
```tsx
{
  root: CID(bafyreiaxdhr5jbabn7enmbb5wpmnm3dwpncbqjcnoneoj22f3sii5vfg74)
	txId: "eip155:1/txid:0x58be3f7b9d508d12b0c1cf2015a54169598210f1b40bf645777bcaf9cae656e8"
}
```

Blockchain transaction resolution, parsing, and verification can later be defined by each blockchain CAIP-2 namespace. Implementations will vary and may even vary for the same chain. Optional parameters can be included here to indicate the methods required. For now transaction resolution, parsing, and verification is out of scope for this specification. 

### IPLD Based Merkle Proofs

A Merkle proof allows verification that a given piece of data or “leaf” was included in the set of data for the given Merkle Tree. A  typical proof includes a Merkle root of the tree and a subset list of tree node hashes that allow verification by reconstructing the Merkle root starting from the data or "leaf" you are interested in.

In IPLD a Merkle root (CID) and a “path” are provided that allows you to traverse the tree from the root to the piece of data your interested in. Paths are strings and refer to [Pathing in IPLD](https://ipld.io/docs/data-model/pathing/). Following an IPLD path, and ultimately resolving the data or "leaf" you are interested in from the root, is equivalent to a standard proof. 

As defined above, an IPLD Merkle tree node includes a list pair. List traversal in IPLD is defined by index, for example 0 referencing the first item, 1 referencing the second. An example IPLD path through a tree could look as such `0/0/1/0/1/1`

### IPLD Anchor Proof

The combination of an IPLD based Merkle proof and anchor event now make up the entirety of an anchor proof for a given piece of data. 

```tsx
type AnchorProof struct {
  data Link
	proof Link
	path String 
}
```

Example 4
```tsx
{
  data: CID(bagcqcerav5zyrlelnnx24blbgob6stz6y4oiwenn3hodbmyf7bnnguh6gdeq)
  proof: CID(bafyreietaedczjxh7omxfjm3oj55nbmxaga3himruubppgj3hp6dayonqq)
	path: "0/0/1/0/1/1"
}
```

### Anchor Proof Verification 

The following algorithm describes the steps required to verify a given anchor proof. If any step fails, an error MUST be raised. 

1) Resolve anchor event CID (anchor_proof.proof)
2) Resolve blockchain transaction by `txId` CID if `chainId` is present in anchor event, or `txId` string if not. 
3) Verify that the blockchain transaction includes the anchor event root (anchor_event.root) in its payload. How it is inlcuded in the transaction will be defined in the blockchain CAIP-2 namespace and out of scope here. 
3) Verify that the blockchain transaction is valid, as defined by that blockchain and in the blockchain CAIP-2 namespace. Transaction validiy is out of scope here. 
4) Resolve the path (anchor_proof.path) in IPLD. Path resolution MUST resolve to the anchor proof data (anchor_proof.data). If they are not equal, an error MUST be raised. 
5) Return, anchor proof is valid over given data (anchor_proof.data)


## Links
- [IPLD](https://ipld.io/)
- [IPLD CIDs](https://github.com/multiformats/cid)
- [IPLD schema language](https://ipld.io/docs/schemas/)
- [dag-cbor](https://ipld.io/specs/codecs/dag-cbor/spec/)
- [Pathing in IPLD](https://ipld.io/docs/data-model/pathing/)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
