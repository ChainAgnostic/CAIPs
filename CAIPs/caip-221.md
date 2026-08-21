---
caip: CAIP-X
title: Transaction Object Addressing
author: Bumblefudge (@bumblefudge), Sebastian Posth <sebastian@posth.com>, and Antonio Antonino (@ntn-x2)
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
Universal syntax for addressing a specific transaction in current state of
blockchains, directed acyclical graphs, or other cryptographical data systems.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
A general-purpose abstraction for how various namespaces address transaction
objects and a minimal set of properties commonly contained in them across
namespaces.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
While block explorers for newer systems tend to inherit their user experience
and syntax from prior art, there is much divergence in both their public-facing
routes (see [CAIP-200][]) and in the properties queriable per transaction. This
specification defines a minimal set of commonly-used properties to serve as a
translation layer across those syntaxes, with per-namespace profiles needed to
further define and caveat the values and add namespace-specific properties that
may be important to cross-namespace use cases.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
Transactions are addressed as follows:

```
block_address:        chain_id + ":" [ + "block:"]? + "tx/" + transaction_id + ["." + property]?
chain_id:             [-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32} (See [CAIP-2][])
transaction_id:       [-%a-zA-Z0-9]{1,128}
property (optional):  (signer|recipients|type|outputs|inputs)
```

The *name* of each value in its native namespace varies; a few illustrative
examples follow:

|CAIP-220 property|BTC block property|ETH2 block property|
|---|---|---|
|signer|*(derived from scriptsig)|from|
|recipients|*(derived from outputs)|to|
|type|?|TransactionType (see [EIP-2718][])|
|outputs|outputs[]|value|
|inputs|inputs[]|data[]|

The *value* of each of these properties varies widely by namespace, so see the
[namespace profiles][namespaces] for each for validation guidance.

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
TBD - will just summarize PR debates if good input is forthcoming!

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->

```
# bip122:000000000019d6689c085ae165831e93:666666:tx/057954bb28527ff9c7701c6fd2b7f770163718ded09745da56cc95e7606afe99.inputs
## https://www.blockchain.com/explorer/transactions/btc/057954bb28527ff9c7701c6fd2b7f770163718ded09745da56cc95e7606afe99

OP_RETURN OP_PUSHBYTES_70 446f206e6f74206265206f766572636f6d65206279206576696c2c20627574206f766572636f6d65206576696c207769746820676f6f64202d20526f6d616e732031323a3231

# Decoded:

Do not be overcome by evil, but overcome evil with good - Romans 12:21

# eip155:6:tx/0x3edb98c24d46d148eb926c714f4fbaa117c47b0c0821f38bfce9763604457c33.inputs
## https://goerli.etherscan.io/tx/0x3edb98c24d46d148eb926c714f4fbaa117c47b0c0821f38bfce9763604457c33

0x82f71739000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000000000000000000000000000000000000000014000000000000000000000000000000000000000000000000000000000000000374b4143584443544e5250524352595042444a4e594f594c575452414f4b54544341555334325a4f355434495a56553436374b48484e5559000000000000000000000000000000000000000000000000000000000000000000000000000000004368747470733a2f2f697066732e696f2f697066732f516d53424638314b577147414e564363384178675276796f44316b69546a687263354d79704c65764a6f67755638000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000134c49434349554d5f544553544445434c41524500000000000000000000000000

## decoded:

#	Name	Type	Data
0	iscc    string	KACXDCTNRPRCRYPBDJNYOYLWTRAOKTTCAUS42ZO5T4IZVU467KHHNUY
1	url     string	https://ipfs.io/ipfs/QmSBF81KWqGANVCc8AxgRvyoD1kiTjhrc5MypLevJoguV8
2	message string	LICCIUM_TESTDECLARE
```

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
[EIP-2718]: https://eips.ethereum.org/EIPS/eip-2718

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
