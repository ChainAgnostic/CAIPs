---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: CAIP-X <X will be changed to the PR number if accepted>
title: Community-powered trust assessment in software components
author: Dayan | dayan.lens | dayksx.eth (@dayksx) <dayksx@protonmail.com>
discussions-to: <URL(s); if multiple, list separated by , without " or []> 
status: Draft
type: Standard
created: 2023-11-21
updated: 2023-11-21
requires (*optional): 
---

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
CAIP-x defines a way to assess trust in software components leveraging social relationships of trust.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
This proposal provides standardized data to uniformize the assertions made by communities useful to assess the trustworthiness in software components, as well as to uniformize the resulting trust score.
Software components can be any executable code, in particular those from decentralized ecosystems such as self-custodial wallets (such as MetaMask) and their extensions (such as Snaps), decentralized network clients (such as Geth), smart contracts, decentralized applications, etc.
This data gives shape to accounts owner-specific trust graphs primarily comprised of:

- **Assertions of trust / distrust in account owners** to enable anyone to claim their trusted peers and thus shape their trust graph;
- **Assertions of security in software components** to enable anyone to publish security insights regarding software components;
- **Endorsements / disputes of assertions of security in software components** to enable anyone to provide feedbacks regarding published security insights.

This data translating explicit trust signals can be enriched with more implicit on-chain and off-chain trust signals such as `Proof of Humanity`, `Proof of Membership`, `Proof of Contributions`, `Proof of Attendences`, `Social Graphs`, etc.

The processing of trust graphs through trust computers (i.e. protocols leveraging recursive algorithms such as `EigenTrust`)  can calculate trust scores relative to each account owner:

- **Assertion of trust score** to enable any trust computer to publish the computed trust scores about software components.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Software components in a decentralized ecosystem tend to be distributed permissionlessly, which promotes permissionless innovation but at the same time opens the door to vulnerabilities and scams.
The majority of solutions for assessing software components are centralized and therefore require trusted intermediaries, which affects the decentralized property of the ecosystem.
Standardizing data to shape a global trust graph reusable in any context would strengthen the reliability of assessments of software components powered by the community.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Subjects identification
Decentralized Identifiers ([DID](https://www.w3.org/TR/did-core/)) or Content Identifier (CID) are used to identify subjects such as `accounts owners`, `software components` or the `assertions` themselves. 
Since `account owners` and `trust computers` are issuing assertions about subjects, issuers and subjects need to be identifiable.

- `PKH` DID method for account owners (e.g. `did:pkh:eip155:1:<publicKeyHash>`, `did:pkh:bip122:<publicKeyHash>`, `did:pkh:solana:<publicKeyHash>`);
- Custom Identifiers for software components such as the checksum (e.g. `did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=`, `did:pkh:eip155:1:<smartContractAddress>`);
- CID of the assertion (`issuer`+`subjectCredential`) for assertions, generated respecting [RFC 8785
JSON Canonicalization Scheme (JCS)](https://www.rfc-editor.org/rfc/rfc8785));
- `KEY` DID method for trust computers.

### Data
An account owner can issue assertions about the following subjects:
- Another account owner (issuing account trust / distrust assertions);
- Software component (issuing software component security assertions, issuing endorsement / dispute assertions);
- Software component Security (issuing endorsement / dispute assertions).

![image](https://github.com/dayksx/CAIPs/assets/77788154/448793ff-88ce-485a-84a6-531501ee0fed)

*View - Software component Trust Assessment Metamodel*

All subsequent documents follow the [Verifiable Credential Data Model](https://www.w3.org/TR/vc-data-model/) for the sake of representation, but this standard does not assume any particular document type, even if internationally recognized standards can only be recommended.

#### Incoming Data: assertions
Assertion of trust to an account owner:
```json
"type": "AccountTrustCredential",
"issuer": "did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eip155:1:0xfA045B2F2A25ad0B7365010eaf9AC2Dd9905895c",
  "trustworthiness": [
  {
    "type": "Quality",
    "scope": "Reliability",
    "level": "High"
  },
  {
    "type": "Ability",
    "scope": "Software development",
    "level": "Moderate"
  },
  {
    "type": "Ability",
    "scope": "Software security",
    "level": "Very high",
    "reason": ["White Hat", "Auditor"]
  }
  ]
},
"proof": {}
```
Modeling trust between people can be a complex task due to the subjective and multifaceted nature of trust. Here is a proposal to conceptualized trust regarding a person with the following attributes:
- `type`: Definition of the type of trust placed in a person, if the trust relate to an overall `quality` of the person or to a specific `ability` of the person;
- `scope`: Definition of the scope of trust (`scope` should be a noun);
- `level`: Definition of the extent of trust.
- `reason` (optional): Definition of the reason of trust

Trust `type` enable to introduce different applicabilities understandable by computers, for example a `quality` or a `flaw` is general and applicable for any use-case, whereas an `abilitity` is useful only for specific situations.
Trust `scope` needs to be standardized for interoperability purpose, but also need to be extendable (cf. below `View - Trust abilities specialization Data Model`).
Trust `level` is subjective, therefore the level range can be flexible according to the use-case, but it must nevertheless remain inthe following range for interoperability purpose: `Very low`, `Low`, `Moderate`, `High`, `Very High`.

This standard defines the folowing abilities as a scope of trust: `Software security`, `Software development`; as well as the follow qualities : `Honesty`, `Reliability`; but can be extended by inheriting high-level scopes.

![image](https://github.com/dayksx/CAIPs/assets/77788154/eb36574a-bde9-44ff-9665-39d1cd1931ab)

*View - Scope of trust Data Model*

Assertion of distrust to an account owner:
```json
"type": "AccountDistrustCredential",
"issuer": "did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eip155:1:0xC3764761E297D6f121e79C32A65829Cd1dDb4D33",
  "trustworthiness": [
  {
    "type": "Flaw",
    "scope": "Dishonesty",
    "level": "High"
    "reason": ["Scam", "Rug pull"]
  },
  {
    "type": "Flaw",
    "scope": "Unlawful",
    "level": "Low"
    "reason": ["Money laundering", "Piracy"]
  }
},
"proof": {}
```
The model is similar for trust and distrust.

Assertion of security to a software components:
```json
"type": "SoftwareSecurityCredential",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ="
  "findings": "Critical",
  "reportURI": "ipfs://123...",
  "applicableSecurityAssertion": ["<CID>"]
},
"proof": {}
```
- *Enum for `findings`:  "None", "Low", "Medium", "Critical".*
- *Content for `reportURI`:  Standard JSON document.*

Security assertions can be linked together (`applicableSecurityAssertion`) to reuse previous assessment to enable assessing only the gap between two assessed versions. 

Endorsement or dispute of an Assertion of security:
```json
"type": ["DisputeCredential", "EndorsementCredential"],
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "<CID>",
  "currentStatus": "Disputed",
  "statusReason": "Scam"
},
"proof": {}
```
- *Enum for `status`:  "Disputed", "Endorsed".*
- *Example of values for `statusReason`:  "Scam", "Incomplete".*

#### Outgoing data: Trust score
The incoming data is used to compute trust scores outgoing data for software components. The computation steps might vary according to the trust computer algorithm, but in general they can be summarized as follows:
1. Retrieve the relevent trust graph (all the nodes from the accounts owners' graph with direct and indirect relationships with the software component);
2. Retrieve the concerned `accounts` (accounts having issued endorsements, disputes, security assertions and if available the software component developers account) and calculate the `accounts trust scores`;
3. Weight the `endorsements` and the `disputes` according to the issuers' `accounts trust scores`;
4. Weight the `security assertions` according to the weight of the `endorsements` and `disputes` + the issuers' `account trust scores`;
5. Weight the `software component` final trust score according to the weight of the `security assertions` + if available the software component's developers `account trust score`.

software component trust score (to be refined):
```json
"type": "SoftwareTrustScoreCredential",
"issuer": "did:key:z6MkhaXgBZDvotDkL5257faiztiGiC2QtKLGpbnnEGta2doK",
"issuanceDate": "2023-11-24T12:24:42Z",
"credentialSubject":
{
  "id": "did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "trustScoreType": "EigenTrust",
  "trustScore": "0.10113570942",
  "scoreset": "ipfs://123...",
},
"proof": {}
```

Scoreset (to be defined):
```json
{
  "algorithm": {},
  "inputData": {},
  "proof": {}
}
```
The scoreset provide all the input data and information about the algorithm used to compute the trust score.

### Data and trust score storage
Incoming and outgoing data can be stored in any datastore but it should meet some minimal requirements for verifiability and sustainability purpose:
- Data availability: The datastore should make the assertions & proofs publicly available (availability ratio depends of the use-case) for consumption and verification purpose;
- Tamper-proof: The datastore should provide assertions data with proofs of completeness, i.e. that none have been alterned or obstructed;
- Scalability: The datastore should scale to meet the evolving demand of issued assertions.


## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
### Subjects identification
DID and CID are decentralized identification methods, free from any centralized identity provider and therefore more sustainable.
1. Decentralized identifiers (DID) using `pkh` and `key` methods enable to identify accounts owners or trust computers in a chain-agnostic manner.
2. Content Identifiers (CID) enable anyone to uniquely generate identifiers based on the content of the document.

### Data
1. Trust in a person is based on the qualities of a person, what they are, or what they do; trust is not binary; trust evolves over time;
2. Distrust assertions enable to capture suspicious behaviors;
3. The security of software components is assessed based on security audit findings;
4. Endorsement and dispute solicit community feedback on issued security assertions;
5. This data enables any trust score computer using trust graphs to be set up and to calculate a software component trust graph.

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->
### Decentralized Identifiers
```
## Account owner
did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044
did:pkh:bip122:000000000019d6689c085ae165831e93:128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6
did:pkh:solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ:CKg5d12Jhpej1JqtmxLJgaFqqeYjxgPqToJ4LBdvG9Ev

# Trust Computers
did:key:z6MkhaXgBZDvotDkL5257faiztiGiC2QtKLGpbnnEGta2doK

# Software components / Wallet: MetaMask
nkbihfbeogaeaoehlefnkodbefgpgknn (id)

# Software components / Wallet extension: MetaMask Snaps
did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ= (sha-256)

# Software components / Smart contract deployed in Ethereum
did:pkh:eip155:1:0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984 (ethereum address)

# Software components / dApp deployed in IPFS
ipfs://QmUqy1Yrv2R81mcYA5sM3qUinkwk6RaKJ4qq1XE6F3BDhM (ipfs CID)

# Software components / client: Geth
4dbe63f7f8c03f655ee5c090369703b6 (MD5)

# Assertions
QmUqy1Yrv2R81mcYA5sM3qUinkwk6RaKJ4qq1XE6F3BDhM (CID)
```
### Snaps permissionless distribution
Snaps permissionless distribution aim at providing trust insights leveraging trust scores to guide the end-users for expanding their MetaMask Wallet with snaps developed by the community.


## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
A community-powered trust assessment presents several risks due to its permissionless nature.
All the potential attacks should be considered when setting up the trust computer.

### Sybil attack
Subversion of the reputation system by creating a large number of pseudonymous accounts and uses them to gain a disproportionately large influence, and promote vulnerable software or, on the contrary, reduce trust in trustworthy software.

### Bored influencer
An account becomes popular during a rapid growth stage of one sub-community. 
Later, the community becomes much less appealing/active resulting in many others that expressed trust in the account becoming unavailable/disengaged. 
There is noone to revoke the trust in the original influencer account. 
The influencer account becomes malicious and can have disproportionate impact on the outcomes (their endorsement of a malicious software component is way stronger than multiple negative audits or counter-recommendations formed in a much less active community where there's less trust to throw around).
Early users being the most trusted is not uncomon. 
See stackoverflow - certain levels of reputation that exist in the community are no longer reachable and never will be. 

### Trickle sybil
Assuming a mechanism exists to prevent "bored influencer" or trust scores are being diminished over time or as participation dwindles, the effect can be used to disproportionately grow influence over time.
An account  with malicious intentions could slowly grow a following (either fake or real, but devoted) and ensure that while the natural community dynamic is on a downwards trend, they keep their supporters active. Over time their influence grows substantially.

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
Issuing assertions makes public the opinion of issuers (identified by their public address), and therefore should be informed about the consequence of their action.

## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-1][CAIP-1] defines the CAIP document structure

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
