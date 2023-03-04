---
caip: CAIP-218
title: ChainProofs - Fresh Facts Straight from the Blockchain
author: Joel Thorstensson (joel@3box.io), Wayne Chang (wayne@spruceid.com)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/218
status: Draft
type: Standard
created: 2023-03-04
updated: 2023-03-04
requires: 196
---

# TODO BEFORE MERGE

- [ ] Fully define ChainProof EVM Profile mechanisms for identifying minimal states necessary from `eth_getProof` for EVM verification of contract calls and blocks. currently under "Instruction things".
- [ ] Flesh out ChainProof Method definition and examples, especially the EVM NFT Ownership method.
- [ ] Example code and test vectors.
- [ ] Clean up ChainProof Methods section, move EVM-specific stuff to the EVM Profile.
- [ ] Discussion of ChainProof inclusion within ReCaps, UCANs, and W3C Verifiable Credentials, including their relationship with cryptographic signatures in these formats.
- [ ] Complete remaining empty sections.

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->
## Simple Summary

<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
ChainProofs are a cross-chain mechanism for proving human understandable statements using evidence from blockchain state.

## Abstract

A ChainProof can be used to prove any statements using blockchain evidence. These proofs are encoded as CACAOs and can be incorporated into any verifiable data format, such as object capabilities (ReCap, UCAN) or credentials (W3C Verifiable Credentials) to create interoperable digital statements that are rooted in the blockchain. These proofs contain a formula that verifiers can check against blockchains for truthiness, and these proofs therefore do not necessarily contain cryptographic signatures.


## Motivation

<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
ChainProofs can be used for many purposes to prove statements from blockchain evidence

For example, they can prove that an Ethereum account,

- is the owner of an ERC-721 NFT on mainnet Ethereum for the block 16756824.
- has a locked token balance of $SPORK in a specific smart contract 0x83..e2 on Polygon in the block range [16756824, 16756900).
- has been a member of the $SPORK DAO multisig for longer than 4,000 blocks total.

ChainProofs can also prove general statements that aren't necessarily about accounts, such as:

- The TVL in the DEX contract 0x48..ff is under $5,000,000,000 USD in value, using the ETH/USD price table defined in 0x77..22.
- The staking contract 0x5e..32 has over 5,000 calls to `stakeEth()` that also sent greater than or equal to 1 ETH.



## Specification


### ChainProof Data Model


A ChainProof is a CACAO that defines a new subsection of `fct` called `cprf` containing:
- The ChainProof Method ID `mid`, the `String` name of the desired method, currently defined in this CAIP but potentially in an extensible registry.
- The ChainProof Method Parameters `mps`, the inputs to the specified ChainProof Method as a `{String: Any}` dictionary.


```ipldsch=
type Principal Bytes // multidid

type ChainProofCACAO struct {
  iss Principal
  aud Principal
  s null // null
  
  v "cprf-0"
  att []
  nnc ""
  prf optional [&CACAO]
  fct Fact
}

type Fact struct { 
  cprf: ChainProofFact 
}

type ChainProofFact struct {
  id: String           // method-id
  rc: Link             // method-root-cid
  prm: { String: Any } // method-parameters
}
```

### ChainProof Methods
A ChainProof MUST specify a ChainProof Method, which describes
1. **Claim Name**: The claim being made
2. **Claim Description**: 
3. **Parameters**: Input parameters to verify the claim which can also be used for (1).
4. **Steps**: the exact steps that a verifier must take to verify the claim.

The claim MUST be described in a human understandable fashion, and the steps MUST be described in a code-implementable fashion with a reference implementation made available, and SHOULD also include test vectors.

#### ChainProof Method Example: NFT Ownership on EVMs
`chainproof-method-id`: EvmNftOwnership
`chainproof-method-parameters`: 

### Verifying a chainproof

1. Load state proof from `fct.cprf` into an EVM state machine
2. Call the evm verification method specified by the *chainproof method*


### Chainproof methods

### ChainProof EVM Profile

In order to create a chainproof using the EVM profile, the following information needs to be retrieved from ethereum:

* Storage slot(s) merkle proof(s)
* Smart contract code

This information needs to be encoded using the dag-eth IPLD codec. The final result is a set of IPLD blocks and the CID representing the block hash.

```
  ┌───────┐
  │ Block │
  └───┬───┘
      │
      │
  ┌───▼────┐
  │Contract│
  └┬──────┬┘
   │      │
   │      │
┌──▼─┐  ┌─▼──────────┐
│Code│  │StorageSlots│
└────┘  └────────────┘
```

The specific storage slots that are required for the proof depends on the specific ChainProof method used, and should be enough to load the state into the EVM and call a function on a smart contract. 


#### NFTProof

Issuer: `did:nft`
Audience: `did:pkh`
EVM method: `ownerOf(uint256): address`
VerificationMethod name: `erc721owner`

**Verification algorithm**
1. Call the `ownerOf(uint256): address` using the `did:nft` identifier as the `uint256` parameter
2. Verify that the `address` returned is equal to the `did:pkh` identifier

#### SafeProof

Issuer: `did:safe`
Audience: `did:pkh`
EVM method: `ownerOf(uint256): address`
VerificationMethod name: `erc721owner`



#### Instruction things

1. Call `eth_getProof(address, keys)` to get the merkle proof
    a. `address`, and `keys` are defined by the *chainproof method* used
3. Encode the merkle proof in IPLD using [dag-eth](https://ipld.io/specs/codecs/dag-eth/state/)
4. Encode this information as a CACAO using the format below
    a. `iss` and `aud` needs to be DID methods as defined by the *chainproof method* 

## Rationale

<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->

## Test Cases

<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->

## Security Considerations

<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Privacy Considerations

<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Backwards Compatibility

<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.

## Links

<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->
Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
