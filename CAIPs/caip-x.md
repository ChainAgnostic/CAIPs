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
- Assertion of trust for an account owner,
- Assertion of security for software artifacts,
- Endorsement or dispute of an assertion of security.
This data translates explicit trust signals that could be enriched with implicit on-chain and off-chain trust signals such as `Proof of Humanity`, `Proof of Membership`, `Proof of Contributions`, `Proof of Attendences`, `Social Graphs`, etc.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Software artifacts in a decentralized ecosystem tend to be distributed permissionlessly, which promotes permissionless innovation but at the same time opens the door to vulnerabilities and scams.
The majority of solutions for assessing software artifacts are centralized and therefore require to trust intermediaries, which negatively affects the decentralized property of the ecosystem.
Standardizing data would enable us to shape a global trust graph that would strengthen the reliability of the assessments of software artifacts powered by the community and therefore free from intermediaries.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Subjects identification
Decentralized Identifiers (DID) are used to identify accounts owners and software artifacts.

- `PKH` DID method for account owners (e.g. `did:pkh:eth:<publicKeyHash>`, `did:pkh:btc:<publicKeyHash>`, `did:pkh:sol:<publicKeyHash>`),
- Custom DID methods for software artifacts (e.g. `did:snap:1?version=1.2`, `did:snap:<checksum>`, `did:ethr:1:<smartContractAddress>`)

### Data
Assertion of trust for an account owner:
```json
"type": "AccountTrustAssertion"
"issuer": "did:pkh:<publicKeyHash>"
"credentialSubject":
  {
    "trustFor": "", # (e.g. "Software security", "Software development", "Honesty")
    "trustLevel": "" # (enum: "Low", "Medium", "High")
  }
```
Assertion of distrust for an account owner:
```json
"type": "AccountDistrustAssertion"
"issuer": "did:pkh:..."
"credentialSubject":
  {
    "distrustReason": "" # (e.g. "Scam", "Suspicious activity")
  }
```

Assertion of security for a software artifacts:
```json
"type": "SoftwareArtifactSecurityAssertion"
"issuer": "did:tbd:..."
"credentialSubject":
  {
    "findings": "", # (enum: "None", "Low", "Medium", "Critical")
    "reportURI": "", # (Standardized JSON document)
    "applicableSecurityAssertion": "" # (SoftwareArtifactSecurityAssertion identifier)
  }
```
Security assertions can be linked together (`applicableSecurityAssertion`) to enable the issuer to only assess the gap between two assessed software versions. 

Endorsement or dispute for an Assertion of security:
```json
"type": "SoftwareArtifactSecurityAssertion"
"issuer": "did:tbd:..."
"credentialSubject":
  {
    "status": "" # (enum: "Dispute", "Endorsed") 
  }
```
### Trust score calculation
This data enables to compute a trust score for a given software artifacts for a specific person having their own trust graph, with the following steps:
1. Identify the concerned trust graph;
2. Retrieve concerned accounts an calculate a trust score;
3. Weight accordingly the endorsements, the disputes and the security assertions;
4. Calculate the software artifact trust score.

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
### Subjects identification
Decentralized identifiers (DID) offer a way to identify in a decentralized and chain-agnostic way any subjects.

### Data
1. The trust of an account owner is based on the quality of a person, what they are, or what they do; trust is not binary; trust evolves over time;
2. Distrust assertions enable the capture of suspicious behaviors;
3. The security of software artifacts is assessed based on security audit findings;
4. Endorsement and dispute solicit community feedback on security assertions;
5. This data enables any trust score computer leveraging trust graphs to be configured and to calculate a software artifact trust graph.

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
