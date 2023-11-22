---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: CAIP-X <X will be changed to the PR number if accepted>
title: Community-powered trust assessment of software artifacts
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
Software artifacts can be any executable code, and in particular those from decentralized ecosystems such as self-custody wallets (such as MetaMask and their extensions (such as MetaMask snaps), decentralized network clients (such as geth), smart contracts, decentralized applications, etc.
This data gives shape to trust graphs specific to each account owner usable to calculate software artifacts trust scores relative to their own trust graph.

- **Assertions of trust / distrust in account owners** to enable anyone to claim their trusted peers and thus shape their trust graph;
- **Assertions of security in software artifacts** to enable anyone to publish security insights regarding software artifacts;
- **Endorsements and disputes of assertions of security** to enable anyone to provide feedbacks regarding published security insights.

This data translates explicit trust signals that could be enriched with more implicit on-chain and off-chain trust signals such as `Proof of Humanity`, `Proof of Membership`, `Proof of Contributions`, `Proof of Attendences`, `Social Graphs`, etc.

The calculation of trust graphs leveraging a trust computer using trust algorithms (such as EigenTrust and / or Weighted Average) result in software artifacts' trust scores specific to each account owner's trust graph. 

- **Assertion of trust score** to enable any trust computer to publish computed trust scores.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Software artifacts in a decentralized ecosystem tend to be distributed permissionlessly, which promotes permissionless innovation but at the same time opens the door to vulnerabilities and scams.
The majority of solutions for assessing software artifacts are centralized and therefore require trusted intermediaries, which affects the decentralized property of the ecosystem.
Standardizing data to shape a global trust graph reusable in any context would strengthen the reliability of assessments of software artifacts powered by the community.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Subjects identification
Decentralized Identifiers ([DID](https://www.w3.org/TR/did-core/)) are used to identify subjects such as `accounts owners`, `software artifacts` or the `assertions` themselves. Since `account owners` and `trust computers` are issuing assertions about subjects, each of them need to be identifiable.

- `PKH` DID method for account owners (e.g. `did:pkh:eth:<publicKeyHash>`, `did:pkh:btc:<publicKeyHash>`, `did:pkh:sol:<publicKeyHash>`);
- Custom DID methods for software artifacts (e.g. `did:snap:1?version=1.2`, `did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=`, `did:ethr:1:<smartContractAddress>`);
- CID of the assertion (`issuer`+`subjectCredential`) for assertions, generated leveraging [RFC 8785
JSON Canonicalization Scheme (JCS)](https://www.rfc-editor.org/rfc/rfc8785)) standard.

### Data
An account owner can issue attestations about the following subjects:
- Another account owner (issuance of account trust / distrust assertions);
- Software Artifact (issuance of software artifact security assertions);
- Software Artifact Security (issuance of endorsement / dispute assertions).

![image](https://github.com/dayksx/CAIPs/assets/77788154/1978f81b-fb8d-4965-b348-476f0cd63cbc)

*Diagram - Metamodel*

All subsequent documents follow the [Verifiable Credential Data Model](https://www.w3.org/TR/vc-data-model/) for the sake of representation, but this standard does not assume any particular document type, even if an internationally recognized standard can only be recommended.

#### Incoming Data: assertions
Assertion of trust to an account owner:
```json
"type": "AccountTrustAssertion",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eth:eriko.eth",
  "trustFor": "Software security", 
  "trustLevel": "high"
},
"proof": {}
```
*Example of values for `trustFor`:  "doing/Software security", "doing/Software development", "being/Honest", "being/Generous".*
*Enum for `trustLevel`:  "Low", "Medium", "High".*

Assertion of distrust to an account owner:
```json
"type": "AccountDistrustAssertion",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eth:sbf.eth",
  "distrustReason": "Scam"
},
"proof": {}
```
*Example of values for `distrustReason`:  "doing/Scam", "doing/Hack".*

Assertion of security to a software artifacts:
```json
"type": "SoftwareArtifactSecurityAssertion",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ="
  "findings": "Critical",
  "reportURI": "ipfs://123...",
  "applicableSecurityAssertion": [<CID>,]
},
"proof": {}
```
*Enum for `findings`:  "None", "Low", "Medium", "Critical".*
*Content for `reportURI`:  Standard JSON document.*

Security assertions can be linked together (`applicableSecurityAssertion`) to enable the issuer to only assess the gap between two assessed software versions. 

Endorsement or dispute to an Assertion of security:
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
*Enum for `status`:  "Disputed", "Endorsed".*
*Example of values for `statusReason`:  "Scam", "Incomplete".*

#### Outgoing data: Trust score
This incoming data enables to compute a trust score for software artifacts according to the concerned account owner's trust graph, with the following steps:
1. Retrieve the relevent trust graph;
2. Retrieve all the concerned accounts (accounts having issued endorsements, disputes, security assertions and if possible the software artifact developers) and calculate their trust scores;
3. Weight the endorsements and the disputes according to the issuers' trust scores;
4. Weight the security assertions according to the weight of the endorsements and disputes + the security assertions issuers' trust scores;
5. Weight the software artifact trust score according to the weight of each security assertion + if possible the software artifact's developers trust score.

Software artifact trust score:
```json
"type": "SoftwareArtifactTrustScore"
"issuer": "did:tbd:..."
"credentialSubject":
{
  "id": "did:snap:CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "trustComputerType": "Karma3 Labs Reputation Protocol",
  "trustScore": "",
  "scoreSetIdentifier": "ipfs://?",
},
"proof": {}
```

```json
"type": "ScoreSetIdentifier"
{
  "id": ""
  "inputData": {},
  "proof": {}
},
"proof": {}
```

### Data and trust score storage
Incoming and outgoing data can be stored in any datastore but it should meet the following high level requirements:
- Data availability: The datastore should be public and available to read the data
- Unfalsifiable: The datastore should guarantee the data integrity
- Uncensorable: The datastore should be censorship resistant
- Scalable: The datastore should projected data volume


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

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->

## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-1][CAIP-1] defines the CAIP document structure

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
