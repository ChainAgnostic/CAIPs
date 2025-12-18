---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: CAIP-X
title: Universal EVM-Cross-Chain Address
author: Zainan Victor Zhou (@xinbenlv)
discussions-to: TBD
status: Draft
type: Standard
created: 2024-07-10
updated: 2024-07-10
---

## Simple Summary
A standard for an extended address format for EVM chain that includes chainId with consideration of future extensions, using 4bytes prior the 20bytes of normal address.


## Abstract
Currently EVM address are single chain, and is 20bytes. We envision native support to make contract call between EVM chains with different chainIds.
This proposal presents one possible solutions: 
1. limiting chainId number space hence the length of its possible value; 
2. pack it into the 32bytes/u256 native word length for maximum compatibility; and
3. reserve additional bytes for future extension

## Motivation
We envision one day EVM contract can make contract calls to other EVM chains (which have different chainIds).
For a contract to make such call, they needs to be able to specify a contract when they are on another EVM chain with a chainId. Hence we need a cross-chain contract address.

## Specification

An EVM-Cross-Chain Address aka "Xaddress" is defined follows

1. Extension section of 4bytes(32bits), denoted as `Ext`
2. Main Address 0x<20bytes single chain Address>

`Ext === 0x0000` is reserved for "only the address of the same chain"
`Ext === 0xFFFF` is reserved for "the original address of ANY chainId"

3. Only EIP-2294 "Safe Range" is supported by this CAIP at the version. ChainId beyond Safe Range is out of the scope of this version.
4. Confirming contracts SHALL NOT make call to address beyond Safe Range of ChainId, but confirming contracts SHOULD consider accepting chainIds beyond this range (restrictive in write, flexible on read)


## Rationale
1. The reason we use a 4bytes length is based on [EIP-2294](https://eips.ethereum.org/EIPS/eip-2294) to specify a SAFE boundary of max chain Id to be `2^31 - 1` based on discussion of current usage of the chainId.
2. We didn't directly use [CAIP-50](./caip-50.md) because we see the support for EVM cross-chain function call more native. We envision in the future this CAIP becomes a sub-standard UNDER CAIP-50 or its future version.
3. We provide reserved Ext=0x0000 which is inspired by 0.0.0.0 in IPv4. And 0xFFFF as masking in IPv4 as well. We see possibility in a sharded or multi-chain situation, EOA or future EIP-7702 could potential be represented the same address across any EVM chain in the same base address.


## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Backwards Compatibility
There are onchain memory and storage representations where contract addresses were assumed to be 20bytes and only 20bytes and conduct packing for storage optimization.


## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-50](./caip-50.md)
- [EIP-2294](https://eips.ethereum.org/EIPS/eip-2294)

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
