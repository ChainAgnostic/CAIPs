---
caip: <to be assigned>
title: Simple Verifiable Credential Interface
author: <a list of the author's or authors' name(s) and/or username(s), or name(s) and email(s), e.g. (use with the parentheses or triangular brackets): Juan Caballero (@bumblefudge)
discussions-to: <URL>
status: Draft
type: Standard 
created: 2022-11-07
updated: 2022-11-07
requires: 25
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->


## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
A common interface for decentralized applications and cryptocurrency wallets (or other key management applications described in future CASA namespaces and CAIPs) to communicate about and pass Verifiable Credentials and Verifiable Presentations is needed for the chain-agnostic world to interact with emerging decentralized/portable data applications. Many [elsewhere mandatory] aspects of the verifiable credential and decentralized identifier stacks are left out of scope to be handled in future CAIPs as needed, storage and encryption being two notable examples.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
After a dapp has requested and a wallet has declared basic verifiable
credentials capabilities in the CAIP-25 feature-discovery step, dapps can assume
the wallet will be able to support all the mandatory methods listed below. Note
that many of these calls should be routable to services, other software, or even
other dapps, without leaking information about the wallet or architecture to the
calling dapp.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Erring on the side of protocol neutrality and making the dapp-wallet connection
a simple interface that bootstraps to more complex ones was the design goal
here. It may even be too minimal for many use-cases, but hopefully at least a
building block for more complex protocols down the line.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

```
method provider.issue (vc, $params)
(return bool)

method provider.request (presentation_request, $params)
(post back VP)

method provider.verify (vc, $params, $error)
(return bool, $error)

```

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
Full support for prior-art protocols from web2 (the CCG's VC-API, the DIF's
WACI, and/or the DIF's DIDComm) would also be a very large implementation burden
for today's cryptocurrency wallets.  By assuming a Wallet-Connect connection or
equivalent and a CAIP-25 feature discovery context greatly reduces the
complexity and technical risks compared to a more bespoke wallet-dapp protocol
for issuing and presenting verifiable credentials.

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
TBD

## Test Cases
<!--Please add test cases here if applicable.-->
Please add test cases here if applicable. (links to sample credential manifests
and vp's from presentation exchange v2 spec might be enough here)

## Links
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->
- [VC spec][] - Core data model for verifiable credentials 
- [Veramo][] Project - EVM-friendly but multi-chain sample libraries for issuing, signing, holding, verifying and presenting verifiable credentials 
- [VC API][] - W3C-CCG-incubated VC protocol (optimized for LinkedData VCs and the Credential Handler API)
- [Presentation Exchange][] - DIF-incubated *high-level* VC protocol (optimized for handling both JWT-VCs and JWTs at scale)
- [DIDComm][] - DIF-incubated messaging layer, which includes sub-protocols for VCs extending the earlier "Present Proof" protocols incubated in Hyperledger Aries community.
- [Walt.id prototype][] - note that WC Chat API is used as a shim for the interface defined above 


[VC spec]: https://www.w3.org/TR/vc-data-model/
[Veramo]: https://veramo.io/
[VC API]: https://w3c-ccg.github.io/vc-api/
[Presentation Exchange]: https://identity.foundation/presentation-exchange/spec/v2.0.0/
[DIDComm]: https://identity.foundation/didcomm-messaging/spec/v2.0/
[Walt.id prototype]: https://github.com/waltid-ethlisbon2022

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
