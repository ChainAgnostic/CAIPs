---
caip: <to be assigned>
title: Blockchain reference for the Algorand namespace
author: Dominic Hawton (@dominichawton)
discussions-to: []
status: Draft
type: Standard
created: 2021-07-24
updated: 2021-07-24
requires: 2
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->

## Simple Summary

<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->

This document details the Algorand namespace and references CAIP-2.

## Abstract

<!--A short (~200 word) description of the technical issue being addressed.-->

An implementation of the CAIP-2 blockchain identification scheme for the Algorand blockchain.

## Motivation

<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->

See CAIP-2.

## Specification

<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

# Algorand Namespace

The name "algorand" referes to the Algorand blockchain networks: MainNet, TestNet, and BetaNet.

# Reference Definition

The definition for the Algorand namespace will use the genesis block hash to differentiate between the MainNet, TestNet, and BetaNet networks.

The genesis block hash for the network of a node can be retrieved using `goal` by running:

```
goal node status -d [node-data-directory]
```

Genesis block hashes can also be found at the following urls:
`https://developer.algorand.org/docs/reference/algorand-networks/mainnet/`
`https://developer.algorand.org/docs/reference/algorand-networks/testnet/`
`https://developer.algorand.org/docs/reference/algorand-networks/betanet/`

## Rationale

<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->

Algorand networks in the "algorand" namespace are identified by their genesis block hash, as described in the Reference Definition section.

## Backwards Compatibility

<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->

Not applicable.

## Test Cases

<!--Please add test cases here if applicable.-->

This is a list of manually-composed examples:

```
# Algorand MainNet
algorand:wGHE2Pwdvd7S12BL5FaOP20EGYesN73ktiC1qzkkit8=
# Algorand TestNet
algorand:SGO1GKSzyE7IEPItTxCByw9x8FmnrCDexi9/cOUJOiI=
# Algorand BetaNet
algorand:mFgazF+2uRS1tMiL9dsj01hJGySEmPN28B/TjjvpVW0=
```

## Links

<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
