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
This proposal introduces a standardized data framework aimed at uniformizing the assertions made by communities intrumental in evaluating the trustworthiness in software components, and in uniformizing the resulting trust score.
Software components encompass can be any executable code, particularly those originating from decentralized ecosystems. Examples includes self-custodial wallets (e.g., MetaMask), associated extensions (e.g., Snaps), decentralized network clients (e.g., Geth), smart contracts, decentralized applications, and more.
The proposed data framework shape trust graphs specific to account owner, comprising:

- **Trust/distrust assertions in account owners:** This allows individuals to identify their trusted peers, thereby shaping their trust graph;
- **Software component security reports:** This enables anyone to publish security insights about any software components;
- **Endorsements/disputes of security reports:** This allows technical individuals to provide feedbacks on published security insights.
- **Endorsements/disputes of software components** This enables any individual to provide feedback directly about software components.

This data which translates  explicit trust signals, can be supplemented with more implicit on-chain and off-chain trust signals. These may include `Proof of Humanity`, `Proof of Membership`, `Proof of Contributions`, `Proof of Attendences`, `Social Graphs`, and more.

Trust graphs can be processed through trust computers (i.e., protocols that leverage recursive algorithms such as `EigenTrust`) to calculate trust scores relative to each account owner. This includes:

- **Assertion of trust score:** This enables any trust computer to publish the computed trust scores about software components.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Software components within a decentralized ecosystem tend to be distributed permissionlessly. While this fosters permissionless innovation, it simultaneously exposes the system to potential vulnerabilities and scams.
Most existing solutions for evaluating software components are centralized, necessitating trusted intermediaries. This reliance on intermediaries compromises the decentralized property of the ecosystem.
By standardizing data to form a universally applicable trust graph reusable in any context, we strengthen the reliability of software components assessments powered by the communities.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Identification
Decentralized Identifiers ([DID](https://www.w3.org/TR/did-core/)) or Content Identifier (CID) are utilized to identify subjects such as `accounts owners`, `software components` or the `assertions` themselves, as well as issuers such as `account owners` and `trust computers`.

- `PKH` DID method for account owners (e.g. `did:pkh:eip155:1:<publicAddress>`, `did:pkh:bip122:<publicAddress>`, `did:pkh:solana:<publicAddress>`);
- Custom Identifiers for software components such as the checksum (e.g. `snap://<checksum>`, `did:pkh:eip155:1:<contractAddress>`);
- CID of the assertion (`issuer`+`subjectCredential`) for assertions, generated according to [RFC 8785
JSON Canonicalization Scheme (JCS)](https://www.rfc-editor.org/rfc/rfc8785);
- `KEY` or `PKH` DID method for trust computers.

### Data
An account owner can issue assertions about the following subjects:
- Another account owner, by issuing trust or distrust assertions;
- Software component, by issuing security reports or  endorsement/dispute assertions;
- Software component security report, by issuing endorsement/dispute assertions.

![image](https://github.com/dayksx/CAIPs/assets/77788154/03312c28-1502-47fb-a9a2-906fb65152f5)

*View - Software component Trust Assessment Metamodel*

All subsequent documents adhere to the [Verifiable Credential Data Model](https://www.w3.org/TR/vc-data-model/) for representation purposes. 
However this this standard does not prescribe any specific document type, even though internationally recognized standards are recommended.
The standard presumes that both the `issuer` property and the complete content of the `credentialSubject` will be only utilized once the wire-formats/signed-envelopes have been verified.

#### Incoming Data: Trust signals

**Assertion of trust to an account owner:**
```json
"type": ["TrustCredential"],
"issuer": "did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eip155:1:0xfA045B2F2A25ad0B7365010eaf9AC2Dd9905895c",
  "trustworthiness":
  [
    {
      "scope": "Honesty",
      "level": 0.5,
      "reason": ["Alumnus"]
    },
    {
      "scope": "Software development",
      "level": 1,
      "reason": ["Software engineer", "Ethereum core developer"]
    },
    {
      "scope": "Software security",
      "level": 0.5,
      "reason": ["White Hat", "Auditor"]
    }
  ]
},
"proof": {}
```
**Assertion of distrust to an account owner:**
```json
"type": ["TrustCredential"],
"issuer": "did:pkh:eip155:1:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "did:pkh:eip155:1:0xC3764761E297D6f121e79C32A65829Cd1dDb4D33",
  "trustworthiness":
  [
    {
      "scope": "Honesty",
      "level": -1
      "reason": ["Scam", "Rug pull"]
    },
    {
      "scope": "Data protection",
      "level": -1,
      "reason": ["Data monetization", "Data leak"]
    },
    {
      "scope": "Software security",
      "level": -0.5,
      "reason": ["Poor track record", "Lack of transparency"]
    },
    {
      "scope": "User experience design",
      "level": -0.5,
      "reason": ["Poor UX"]
    },
    {
      "scope": "Lawful",
      "level": -1,
      "reason": ["Money laundering", "Piracy", "Non-compliance"]
    }
  ]
},
"proof": {}
```
Modeling trust and distrust towards an individual or entity can be a complex task due to the subjective and multifaceted nature of trust. 
This standard proposes the followinge conceptualization for the trust concept:

- `scope`: This defines the applicable trust perimeter (`scope` should be a noun);
- `level`: This defines the extent of trust;
- `reason` (optional): This defines the motivation of trust.

The `scope` of trust needs to be standardized for interoperability purpose, but also need to be extendable to fit any use-case (cf. below `View - Scope of trustworthiness Data Model`).

The `level` of trust must remain within the following range: [-1,1]; Meanings: `Very low` (-1), `Low` (-0.5), `Neutral` (0), `High` (0.5), `Very High` (1).

This standard introduce the folowing references abilities/inabilities as initial scopes of trust/distrust: `Software security`, `Software development`, `Data protection`, `User experience design`, `Responsiveness`, `User support`; as well as the following references qualities/flows : `Honesty`, `Reliability`, `Lawful`, `Dishonesty`, `Unreliability`, `Unlawful`. These scopes are not prescritive, but serve as guidance to achieve higher interoperability. They can be reviewed or extended by inheriting high-level scopes to accomodate any use-case.

![image](https://github.com/dayksx/CAIPs/assets/77788154/7564794e-0a15-4498-b091-5d64ec715e65)

*View - Scope of trust Data Model*

**Security report to a software components:**
```json
"id": "QmPTqvH3vm6qcZSGqAUsq78MQa9Ctb56afRZg1WJ5sKLiu",
"type": ["SecurityReportCredential"],
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "securityStatus": "Unsecured",
  "securityFindings": [
    {
      "criticality": 1,
      "type": "Key leak",
      "description": "`snap_getBip44Entropy` makes the parent key accessible"
      "lang": "en"
    },
    {
      "criticality": 0.5,
      "type": "Buffer Overflow"
    },
    {
      "criticality": 0.25,
      "type": "Phishing"
    },
    {
      "criticality": 0,
      "type": "Data leak",
      "description": "API can communicate data to a centralized server"
    },
  ],
  "applicableSecurityReport": ["6qL5KqZv3qRtb9sLq1WJSGaHPTafmqc56AUsiLilvM78Qv"],
},
"proof": {}
```

Security report with no findings:
```json
"type": ["SecurityReportCredential"],
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "securityStatus": "Secured"
},
"proof": {}
```
- The `securityStatus` is the final result of the security assessment, that can be either `Secured` or `Unsecured`.
- The `findings` (optional) lists the security findings.
- The `criticality` of findings must remain within the following range: [0,1]; Meanings: `None` (0), `Low` (0.25), `Medium` (0.5), `High` (0.75), `Critical` (1).

This standard introduce the folowing references findings: `Key Exposure`, `Data Breach`, `Phishing`... As the trust scopes, these findings are not prescritive, but serve as guidance to achieve higher interoperability. They can be augmented or extended by inheriting high-level findings to accomodate any use-case.

![image](https://github.com/dayksx/CAIPs/assets/77788154/e2393fb3-17a4-4ade-ae35-057aa3a2427e)

*View - Security findings Types*


- `applicableSecurityReport` (optional) list the applicable security reports for the analysis.
The `result` corresponds to the highest security findings in the code, with the details of these findings listed under `findings`.

A security report can be based on a previous one (`applicableSecurityReport`) to reuse prior assessments and limit the report scope to the difference between two software component versions. 

*In the example below, the security report for the `snap version 2.0.1` leverages the previous security report for the `snap version 2.0.0` as the gap between the two versions if merely a patch for backward compatible bug fixes.*

![image](https://github.com/dayksx/CAIPs/assets/77788154/f8b5d888-7746-4eb3-9424-5afef6ef5c86)

View - Applicable Security Reports example 


**Endorsement or dispute of an Security report:**
```json
"type": ["StatusCredential"],
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "QmPTqvH3vm6qcZSGqAUsq78MQa9Ctb56afRZg1WJ5sKLiu",
  "currentStatus": "Disputed",
  "statusReason": {
    "value": "Sybil attack",
    "lang": "en"
  },
},
"proof": {}
```
The [DisputeCredential](https://www.w3.org/TR/vc-data-model/#disputes) is defined by the W3C in the Verifiable Credentials Data Model.
```json
"type": ["StatusCredential"],
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "d6f7052b6f28912f2703066a912ea577f2ce4da4caa5a5fbd8a57286c345c2f2",
  "currentStatus": "Endorsed"
},
"proof": {}
```
- *Enum for `currentStatus`:  "Disputed", "Endorsed".*


**Endorsement or dispute of a Software Component:**
```json
"type": ["StatusCredential"],
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "currentStatus": "Disputed",
  "statusReason": {
    "type": "Scam",
    "value": "Interact with a fraudulent smart contract",
    "lang": "en"
  },
},
"proof": {}
```

#### Outgoing data: Trust score

The trust signals (incoming data) are utilized to compute trust scores (outgoing data) for software components. 
While the computation steps may vary based on the trust computer algorithm, the following main steps give an idea of the processing:
1. Retrieve the relevant trust graph (all the acounts owners graph's nodes with direct and indirect relationships with the software component);
2. Retrieve the relevant `accounts` (accounts that have issued endorsements, disputes, security reports and if available, the account of the software component's developers) and calculate the `accounts trust scores`;
3. Weight the `endorsements` and `disputes` based on the issuers' `accounts trust scores`;
4. Weight the `security reports` based on the weight of the `endorsements` and `disputes` as wellas the issuers' `account trust scores`;
5. Determinate the final trust score for the `software component`based on the weight of the `security reports`, and if available, the software component's developers `account trust score`.

software component trust score (to be refined):
```json
"type": ["TrustScoreCredential"],
"issuer": "did:key:z6MkhaXgBZDvotDkL5257faiztiGiC2QtKLGpbnnEGta2doK",
"issuanceDate": "2023-11-24T12:24:42Z",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "trustScoreType": "EigenTrust",
  "trustScore": "0.10113570942",
  "scoreset": {
    "algorithm": {},
    "inputData": {},
  },
},
"proof": {}
```

The scoreset provides all the input data and information about the algorithm used to compute the trust score.


### Data and trust score storage
Incoming and outgoing data can be stored in any datastore, but it should meet some minimal requirements for verifiability and sustainability:
- Data availability: The datastore should make the assertions and proofs publicly available for consumption and verification purpose;
- Tamper-proof: The datastore should provide assertions data with proofs of completeness, ensuring that none have been alterned or obstructed;
- Scalability: The datastore should scale to meet the evolving demand of issued assertions.


## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
### Udentification
DID and CID are decentralized identification methods that are not reliant on any centralized identity provider, making them more sustainable.
1. Decentralized identifiers (DID) using `pkh` and `key` methods allow for the identification of account owners or trust computers in a chain-agnostic manner.
2. Content Identifiers (CID) enable anyone to generate identifiers based on the content of a document.

### Data
1. Trust in an individual or entity is based on their qualities, or their abilities; it is not binary and evolves over time;
2. Distrust assertions allow for the capture of suspicious behaviors;
3. The security of software components is assessed based on findings from security reports;
4. Endorsement and dispute solicit community feedback on issued security reports;
5. This data enables any trust score computer using trust graphs to be set up and calculate a software component trust score.

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
snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ= (sha-256)

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
