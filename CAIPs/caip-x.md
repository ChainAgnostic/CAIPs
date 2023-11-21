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
This proposal provides standardized data representing assertions made by communities to assess trust in software artifacts such as wallet extensions (i.e. MetaMask Snaps), Smart Contracts, decentralized applications, etc.
This data gives shape to a trust graph, which can be used to calculate a trust score for software artifacts.
Proposed data:
- Assertion of trust to an account owner,
- Assertion of security to a software artifacts,
- Endorsement or dispute to an assertion of security.

This data translates explicit trust signals that could be enriched with implicit on-chain and off-chain trust signals such as `Proof of Humanity`, `Proof of Membership`, `Proof of Contributions`, `Proof of Attendences`, `Social Graphs`, etc.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Software artifacts in a decentralized ecosystem tend to be distributed permissionlessly, which promotes permissionless innovation but at the same time opens the door to vulnerabilities and scams.
The majority of solutions for assessing software artifacts are centralized and therefore require to trust intermediaries, which affects the decentralized property of the ecosystem.
Standardizing data to shape a global trust graph would strengthen the reliability of assessments of software artifacts powered by the community.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Subjects identification
Decentralized Identifiers (DID) are used to identify accounts owners and software artifacts. Since account owners are issuing assertions to subjects (an account owner, a software artifact or an assertion) identifiers are need for each entity.

- `PKH` DID method for account owners (e.g. `did:pkh:eth:<publicKeyHash>`, `did:pkh:btc:<publicKeyHash>`, `did:pkh:sol:<publicKeyHash>`);
- Custom DID methods for software artifacts (e.g. `did:snap:1?version=1.2`, `did:snap:<checksum>`, `did:ethr:1:<smartContractAddress>`);
- CID of the `credentialSubject` payload for assertions.

### Data
Assertion of trust to an account owner:
```json
"type": "AccountTrustAssertion"
"issuer": "did:pkh:<publicKeyHash>"
"credentialSubject":
  {
    "trustFor": "Software security", 
    "trustLevel": "High"
  }
```
**Example of values for `trustFor`:**  "Software security", "Software development", "Honesty".
**Enum for `trustLevel`:**  "Low", "Medium", "High".

Assertion of distrust to an account owner:
```json
"type": "AccountDistrustAssertion"
"issuer": "did:pkh:..."
"credentialSubject":
  {
    "distrustReason": "Scam activity"
  }
```
**Example of values for `distrustReason`:**  "Scam", "Hack".

Assertion of security to a software artifacts:
```json
"type": "SoftwareArtifactSecurityAssertion"
"issuer": "did:tbd:..."
"credentialSubject":
  {
    "findings": "",
    "reportURI": "",
    "applicableSecurityAssertion": "" # (SoftwareArtifactSecurityAssertion identifier)
  }
```
**Enum for `findings`:**  "None", "Low", "Medium", "Critical".
**Content for `reportURI`:**  Standard JSON document.

Security assertions can be linked together (`applicableSecurityAssertion`) to enable the issuer to only assess the gap between two assessed software versions. 

Endorsement or dispute to an Assertion of security:
```json
"type": "SoftwareArtifactSecurityAssertion"
"issuer": "did:tbd:..."
"credentialSubject":
  {
    "currentStatus": "Dispute",
    "statusReason": "IncorrectFindings"
  }
```
**Enum for `status`:**  "Dispute", "Endorsed".
**Example of values for `statusReason`:**  "Scam", "Inadequate findings".

### Trust score calculation
This data enables to compute a trust score for a software artifacts according to the requesting person's trust graph, with the following steps:
1. Capture the relevent trust graph;
2. Retrieve concerned accounts and calculate their trust scores;
3. Weight the endorsements and the disputes according to the issuers accounts trust scores;
4. Weight the security assertions according to the weight of the endorsements and disputes, + the issuers accounts trust scores;
5. Weight the software artifact trust score according to the weight of the security assertions.

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
