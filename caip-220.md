---
caip: CAIP-X
title: Block Addressing
author: Juan Caballero (@bumblefudge), Sebastian Posth <sebastian@posth.com>, and Antonio Antonioni (@ntn-x2)
discussions-to: https://github.com/chainAgnostic/CAIPs/pull/220
status: Draft
type: Standard
created: 2023-03-20
updated: 2023-03-20
requires: 2
replaces: <CAIP number(s); if multiple, format as `[1,2]` array>
---

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
Universal syntax for addressing specific bundles of verifiable transactions
(i.e. "blocks") in blockchains, directed acyclical graphs, or other
cryptographical data systems.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
While namespace-specific syntaxes will always exist and be preferable for single-namespace 

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
While block explorers for newer systems tend to inherit their user experience
and syntax from prior art, there is much divergence in both their public-facing
routes (see [CAIP-200][]) and in the properties queriable per block. This
specification defines a minimal set of commonly-used properties to serve as a
translation layer across those syntaxes, with per-namespace profiles needed to
further constraint the values and add namespace-specific properties that may be
important to cross-namespace use cases.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
Blocks are addressed as follows:

```
block_address:        chain_id + ":block/" + account_address + ["." + property]?
chain_id:             [-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32} (See [CAIP-2][])
block_height:         [-%a-zA-Z0-9]{1,128}
property (optional):  (hash|height|time|nonce|prev|txncount|data)
```

The *name* of each value in its native namespace varies; a few illustrative
examples follow:

|CAIP-220 property|BTC block property|ETH2 block property|
|---|---|---|
|hash|hash|body.execution_payload_header.block_hash|
|height|n/a|body.execution_payload_header.block_number|
|time|time|body.execution_payload_header.timestamp|
|nonce|nonce|body.execution_payload.timestamp.prev_randao|
|prev|previousblockhash|body.execution_payload.parent_hash|
|txncount|txn_count|body.execution_payload.transactions.length*|
|txns|tx|body.execution_payload.transactions|
|size|size|body.execution_payload.gas_limit|
|data|coinbase*, tx.0.txinwitness (in RPC)|body.execution_payload.extra_data|

The *value* of each of these properties varies widely by namespace, so see the
[namespace profiles][namespaces] for each for validation guidance.

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
TBD - will just summarize PR debates if good input is forthcoming!

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->

```
# bip122:000000000019d6689c085ae165831e93:tx.0.txinwitness

```
Sources: [bip122](https://www.blockchain.com/explorer/blocks/btc/0) | 

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
TBD

## Links
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->

[namespaces]: https://namespaces.chainagnostic.org/

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
