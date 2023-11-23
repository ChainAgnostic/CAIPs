---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: CAIP-X <X will be changed to the PR number if accepted>
title: Community-powered trust assessment in software artifacts
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
CAIP-x defines a way to assess trust in software artifacts in a decentralized manner.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
This proposal provides standardized data to uniformize the assertions made by communities, used to assess trust in software artifacts, as well as to uniformize the resulting trust score.
Software artifacts can be any executable code, in particular those from decentralized ecosystems such as self-custody wallets (such as MetaMask) and their extensions (such as Snaps), decentralized network clients (such as Geth), smart contracts, decentralized applications, etc.
This data gives shape to trust graphs specific to each account owner:

- **Assertions of trust / distrust in account owners** to enable anyone to claim their trusted peers and thus shape their trust graph;
- **Assertions of security in software artifacts** to enable anyone to publish security insights regarding software artifacts;
- **Endorsements / disputes of assertions of security in software artifacts** to enable anyone to provide feedbacks regarding published security insights.

This data translates explicit trust signals that could be enriched with more implicit on-chain and off-chain trust signals such as `Proof of Humanity`, `Proof of Membership`, `Proof of Contributions`, `Proof of Attendences`, `Social Graphs`, etc.

The calculation of trust graphs leveraging trust computers implementing recursive trust algorithms (such as EigenTrust and / or Weighted Average) provide trust scores relative to each account owner's trust graph.

- **Assertion of trust score** to enable any trust computer to publish computed trust scores.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Software artifacts in a decentralized ecosystem tend to be distributed permissionlessly, which promotes permissionless innovation but at the same time opens the door to vulnerabilities and scams.
The majority of solutions for assessing software artifacts are centralized and therefore require trusted intermediaries, which affects the decentralized property of the ecosystem.
Standardizing data to shape a global trust graph reusable in any context would strengthen the reliability of assessments of software artifacts powered by the community.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Subjects identification
Decentralized Identifiers ([DID](https://www.w3.org/TR/did-core/)) are used to identify subjects such as `accounts owners`, `software artifacts` or the `assertions` themselves. 
Since `account owners` and `trust computers` are issuing assertions about subjects, issuers and subjects need to be identifiable.

- `PKH` DID method for account owners (e.g. `did:pkh:eip155:1:<publicKeyHash>`, `did:pkh:bip122:<publicKeyHash>`, `did:pkh:solana:<publicKeyHash>`);
- Custom DID methods for software artifacts (e.g. `did:snap:1?version=1.2`, `did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=`, `did:pkh:eip155:1:<smartContractAddress>`);
- CID of the assertion (`issuer`+`subjectCredential`) for assertions, generated leveraging [RFC 8785
JSON Canonicalization Scheme (JCS)](https://www.rfc-editor.org/rfc/rfc8785)) standard.
- `KEY` DID method for trust computers

### Data
An account owner can issue attestations about the following subjects:
- Another account owner (issuance of account trust / distrust assertions);
- Software Artifact (issuance of software artifact security assertions, issuance of endorsement / dispute assertions by the end-users);
- Software Artifact Security (issuance of endorsement / dispute assertions).

![image](https://github.com/dayksx/CAIPs/assets/77788154/ada8ef50-a743-4fda-9819-4f415a9cbfc2)

*View - Software Artifact Trust Metamodel*

All subsequent documents follow the [Verifiable Credential Data Model](https://www.w3.org/TR/vc-data-model/) for the sake of representation, but this standard does not assume any particular document type, even if an internationally recognized standard can only be recommended.

#### Incoming Data: assertions
Assertion of trust to an account owner:
```json
"type": "AccountTrustAssertion",
"issuer": "did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eth:0xfA045B2F2A25ad0B7365010eaf9AC2Dd9905895c",
  "trustFor": "Software security",
  "trustLevel": "High"
},
"proof": {}
```
Trust in someone can be conceptualized along the following two dimensions:
1. Trust someone for who they are (Honest, Insightful...).
2. Trust someone for what they do (Software development, Software security...).

- *Example of values for `trustFor`: trust someone for doing "Software security", "Software development", and trust someone for being "Honest", "Insightful"...*
- *Enum for `trustLevel`:  "Low", "Medium", "High".*

Assertion of distrust to an account owner:
```json
"type": "AccountDistrustAssertion",
"issuer": "did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eth:0xB3764761E297D6f121e79C32A65829Cd1dDb4D32",
  "distrustReason": "Scam",
},
"proof": {}
```
- *Example of values for `distrustReason`:  "doing/Scam", "doing/Hack".*

Assertion of security to a software artifacts:
```json
"type": "SoftwareArtifactSecurityAssertion",
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

Security assertions can be linked together (`applicableSecurityAssertion`) to enable the issuer to only assess the gap between two assessed software versions. 

Endorsement or dispute of an Assertion of security:
```json
"type": ["DisputeAssertion", "EndorsementAssertion"],
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
The incoming data enables to compute ougoing data, i.e. trust scores for software artifacts, with differents steps according to the used algorithm.
Here, as an indication, are the main steps that a trust algorithm executes:
1. Retrieve the relevent trust graph (all the nodes from the accounts owners' graph with direct or indirect relationshio with the software artifact);
2. Retrieve all the concerned accounts (accounts having issued endorsements, disputes, security assertions and if available the software artifact developers accounts) and calculate their trust scores;
3. Weight the endorsements and the disputes according to the issuers' trust scores;
4. Weight the security assertions according to the weight of the endorsements and disputes + the security assertions issuers' trust scores;
5. Weight the software artifact final trust score according to the weight of the security assertions + if available the software artifact's developers trust score.

Software artifact trust score (to be refined):
```json
"type": "SoftwareArtifactTrustScore"
"issuer": "did:tbd:..."
"credentialSubject":
{
  "id": "did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "trustScoreType": "EigenTrust",
  "trustScore": "0.10113570942",
  "scoreset": "ipfs://123...",
  "timestamp": "1700733480"
},
"proof": {}
```

Scoreset (to be defined):
```json
"type": "Scoreset"
{
  "algorithm": {},
  "inputData": {},
  "proof": {}
},
"proof": {}
```
The scoreset provide all the input data, the algorithm used to computed the trust score.

### Data and trust score storage
Incoming and outgoing data can be stored in any datastore but it should meet some minimal requirements:
- Data availability: The datastore should make the assertions & proofs continuously available for consumption and verification purpose;
- Tamper-proof: The datastore should provide assertions data with proofs of completeness, i.e. that none have been alterned or obstructed;
- Scalability: The datastore should scale to meet the evolving demand of issued assertions.


## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
### Subjects identification
Decentralized identifiers (DID) offer a way to identify in a decentralized and chain-agnostic way any subjects.

### Data
1. The trust of an account owner is based on the quality of a person, what they are, or what they do; trust is not binary; trust evolves over time;
2. Distrust assertions enable to capture suspicious behaviors;
3. The security of software artifacts is assessed based on security audit findings;
4. Endorsement and dispute solicit community feedback on issued security assertions;
5. This data enables any trust score computer using trust graphs to be set up and to calculate a software artifact trust graph.

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

# Software Artifacts / Wallet: MetaMask
nkbihfbeogaeaoehlefnkodbefgpgknn (id)

# Software Artifacts / Wallet extension: MetaMask Snaps
CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ= (sha-256)

# Software Artifacts / Smart contract deployed in Ethereum
did:pkh:eip155:1:0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984 (public address)

# Software Artifacts / dApp deployed in IPFS
ipfs://QmUqy1Yrv2R81mcYA5sM3qUinkwk6RaKJ4qq1XE6F3BDhM (ipfs CID)

# Software Artifacts / client: Geth
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
The influencer account becomes malicious and can have disproportionate impact on the outcomes (their endorsement of a malicious software artifact is way stronger than multiple negative audits or counter-recommendations formed in a much less active community where there's less trust to throw around).
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
