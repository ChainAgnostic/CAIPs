---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: CAIP-x
title: Community-Powered Assessment of Trust in Discrete Resources
author: Dayan | dayan.fc | dayan.lens | dayanx.eth | (@dayksx) <dayksx@protonmail.com>
discussions-to: <URL(s); if multiple, list separated by , without " or []> 
status: Draft
type: Standard
created: 2023-11-21
updated: 2023-11-21
requires: CAIP-261 
---

## Simple Summary

CAIP-x introduces a data framework to represent assertions for evaluating discrete resources, such as software components, packages, or media files, by utilizing community assertions and pulling in trust data from webs of trust and other implicit trust signals.

## Abstract

The evaluation of discrete resources necessitates different kind of community feedback according to the specific purpose of the assessment. 
Whether assessing resources for security concerns or for their user-friendliness, the type and depth of feedback required differ significantly. 

Since these feedback are issues by peers part of a peer-to-peer network incorporating pseudonimous peers and potential malicious peers, peers reputation needs to be evaluated by calculating insight from web of trust. 
CAIP-261: Web of Trust Primitives introduces a data framework to represent trust assertions among peers.

**Peers**
- **Peer Trust Assertion:** (Defined in the CAIP-261) This constitute web of trust, with trust and distrust assertions among peers;
- **Peer Trust Score:** (Defined in the CAIP-261) This represent the calculated synthetic trust scores of a peer which reflect the overall sentiment of the community.

This proposal incorporates the following basic primitives for Resources assessment as inputs :

**Discrete Resources**
- **Report Assertion:** This represents detailed presentation of factual information and objective analysis. This type of content is factual and analytical, often backed by data, research, and objective methodologies. It's designed to inform or provide insights based on evidence and analysis, such as security or compliance report;
- **Review Assertion:** This represents a subjective assessment reflecting personal opinions and experiences. Unlike report assertions, review assertions are inherently subjective, based on personal viewpoints, experiences, or interpretations of the reviewer;
- **Reaction Assertion:** This represents a quantifiable expression of agreement or disagreement with a report or a review's content, typically reflecting the collective sentiment of the audience. This is a more interactive form of content, where the audience engages with the content through likes, dislikes, endorsements, or disputes.
- **Resource Trust Score:** This represent the calculated synthetic trust scores of a resource which reflect the overall sentiment of the community.

## Motivation
In the evolving landscape of the decentralized web, the permissionless distribution of discrete resources fosters innovation and participation without gatekeepers but also exposes the ecosystem to vulnerabilities such as misinformation, scams, and security threats. 

Currently, in the absence of a robust, decentralized, community-powered trust assessment mechanism, verification is nearly absent or still heavily reliant on centralized solutions. 
This reliance on trusted intermediaries inadvertently creates bottlenecks and control points, compromising the decentralized properties of the ecosystem.

Therefore, the motivation behind standardizing data for community-powered assessment extends beyond merely enhancing the reliability of discrete resources. 
It aims to establish a universally applicable trust graph, reusable across various use-cases, to further mature and fortify the decentralized nature of the ecosystem.

## Specification

### Resources Trust Representation 
#### Identifier Scheme
##### Discrete Resource identification
Discrete resources, by their nature, are static entities and should be identified with an identifier that points to a specific, unchangeable version of the resource. 
To ensure the integrity and traceability of these resources, each new version must be assigned a unique identifier, distinct from its predecessors.

A recommended approach for generating these identifiers is to use the resource content's fingerprint, such as its hash. Utilizing a [Content Identifier (CID)] is an effective method for this purpose. 
CIDs offer a robust, cryptographic hash of the resource's content, is self-contained and ensure that any alteration of the content would result in a different identifier, thereby preserving the integrity of the resource.

To further enhance accessibility and integration within the decentralized web, the CID should be encapsulated within a Uniform Resource Identifier (URI). 
This encapsulation allows for the use of familiar and widely supported schemes, such as the IPFS scheme (ipfs://), or a custom scheme (e.g., example://). 
By doing so, it provides a contextual identifier that not only points to the resource in a static, immutable manner but also offers insights into the nature or origin of the resource through the choice of URI scheme.

##### Assertions Identification
cf. CAIP-261

#### Data Model
In order to assess discrete resources, a peer can issue assertions about a discrete resource or about reviews or reports to react on their content.
![diagram1](https://github.com/dayksx/CAIPs/assets/77788154/1948e150-f964-4975-b3ee-ec6fe0a1545e)

All subsequent documents conform to the [Verifiable Credential Data Model](https://www.w3.org/TR/vc-data-model/) for the purpose of representation. 
However, this standard does not prescribe any specific document type, though it may recommend internationally recognized standards. 

##### Report Assertion
A report presents a detailed presentation of factual information and objective analysis.

```json
"type": ["VerifiableCredential", "ReportCredential"],
"issuanceDate": "2024-02-15T07:05:56.273Z",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "type": "Security",
  "result": -1,
  "issues": [
    {
      "criticality": 1,
      "type": "Key leak",
      "description": "`snap_getBip44Entropy` makes the parent key accessible",
      "uri": "ipfs://QmEQtreH3vm6qcASGqAUsq78MQa9Ctb56afRZg1WJ5sKLdq"
    },
    {
      "criticality": 0.5,
      "type": "Buffer Overflow",
      "uri": "ipfs://QmDlreH3vm6qcASGqAUsq78MQa9Ctb56afRZg1WJ5sKCqd"
    },
    {
      "criticality": 0.25,
      "type": "Phishing",
      "uri": "ipfs://QmElreH3vm6qcASGqAUsq78MQa9Ctb56afRZg1WJ5sKLpl"
    },
    {
      "criticality": 0,
      "type": "Data leak",
      "description": "API can communicate data to a centralized server"
    },
  ]
},
"proof": {}
```

Security report with no findings:

```json
"type": ["VerifiableCredential", "ReportCredential"],
"issuanceDate": "2024-02-15T07:05:56.273Z",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "type": "Security",
  "result": 1,
},
"proof": {}
```

- The `result` is the final result of the security assessment; It MUST remain within the following range: [-1,1]. This could be translated as follows: 'Very Negative' (-1), 'Negative' (-0.5), 'Neutral' (0), 'Positive' (0.5), 'Very Positive' (1);
- The `issues` (optional) lists the issues.
- The `criticality` of findings must remain within the following range: [0,1]; This could be interpreted as follows: `None` (0), `Low` (0.25), `Medium` (0.5), `High` (0.75), `Critical` (1).

Any standard inheriting this CAIP COULD propose reference lists of "type" to facilitate interoperability across different systems.

##### Review Assertion
A reaction represents a quantifiable expression of agreement or disagreement with the report's content, typically reflecting the collective sentiment of the community.

```json
"type": ["VerifiableCredential", "ReviewCredential"],
"issuanceDate": "2024-02-15T07:05:56.273Z",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "ipfs://QmPTqvH3vm6qcZSGqAUsq78MQa9Ctb56afRZg1WJ5sKLiu",
  "rating": 0.6,
  "comment": "",
},
"proof": {}
```

##### Reaction Assertion
A reaction represents a quantifiable expression of agreement or disagreement with the report's content, typically reflecting the collective sentiment of the community.

**A reaction on a report**
```json
"type": ["VerifiableCredential", "ReactionCredential"],
"issuanceDate": "2024-02-15T07:05:56.273Z",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "ipfs://QmPTqvH3vm6qcZSGqAUsq78MQa9Ctb56afRZg1WJ5sKLiu",
  "reaction": "Endorsed",
  "reason": ["Provide important context", "Vulnerabilities clearly defined"],
},
"proof": {}
```

**A reaction on a discrete resource**
Reaction can also be used directly on a software component to share a reaction.

```json
"type": ["VerifiableCredential", "ReactionCredential"],
"issuanceDate": "2024-02-15T07:05:56.273Z",
"issuer": "did:pkh:eth:0x44dc4E3309B80eF7aBf41C7D0a68F0337a88F044",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "reaction": "Disputed",
  "reason": ["Scam"]
},
"proof": {}
```

- `reaction`: This defines the reaction, the standard define `Disputed` or `Endorsed` as reaction, but this can be extend to any reaction.
- `reason` (optional): This defines the reason for a given review status.

### Resources Trust Management
cf. caip-261.md

### Resources Trust Verification
cf. caip-261.md

### Resources Trust Consumption
Following the verification process, the trust graph can be utilized by any consumer to calculate insight relative to any use-case.

Please note that the method for calculating the trust scores is entirely open, and this standard does not provide specific guidelines for it.

The trust signals (incoming data) are leveraged to calculate the trust scores (outgoing data) for peers and software components.
While the computation steps may vary based on the chosen trust score computation, the following main steps give an idea of some generic processing logic from a given peer point of view:

1. Retrieve the peers (directly and indirectly connected peers that have issued reviews, security reports of the given software component),
2. Calculate the peers' trust scores (relatively to the requesting peer's point of view),
3. Weight the reviews (endorsements and disputes) based on the issuers' peers scores,
4. Weight the security reports based on the weight of the endorsements and disputes as well as the issuers' peers scores;
5. Calculate the software component's trust score based on the weight of the security reports, and if available, the software component's developers peer trust score.

Resource Trust Score:

```json
"type": ["VerifiableCredential", "ResourceTrustScoreCredential"],
"issuanceDate": "2023-11-24T12:24:42Z",
"issuer": "did:pkh:eip155:1:0x23d86aa31d4198a78baa98e49bb2da52cd15c6f0",
"credentialSubject":
{
  "id": "snap://CLwZocaUEbDErtQAsybaudZDJq65a8AwlEFgkGUpmAQ=",
  "trustScore": {
    "confidence": 0.0555555559694767,
    "value": 1
  },
  "trustScoreType": "IssuerTrustWeightedAverage"
},
"proof": {}
```

## Rationale

### Modularity and extensibility

The standard has been designed with modularity and solution-agnosticism, to maximize flexibility and reusability:

- Data elements are independent from each other, allowing for the use of only a subset of it,
- The data framework is agnostic to any specific trust computer, enabling any trust score computation logic,
- Flexible data ranges leveraging floats type facilitating the creation of tailored user experiences,
- Data structures has been designed to be agnostic, enabling the reusability of the data across different use-cases.

### Identification

[DID][]s and [CID][] are decentralized identification methods that are not reliant on any centralized identity provider, making them more sustainable.

1. [Decentralized identifiers][DID] using the `pkh` and `key` methods allow for the identification of account owners or trust computers in a chain-agnostic manner without the complexity of on-chain resolution.
2. [Content Identifiers][CID] enable anyone to deterministically generate identifiers based on the canonicalized content of a given JSON document, and store it in a compact, tamper-evident way conducive to merging, syncing, or even CRDT patterns.

### Data

3. The security of software components is assessed based on findings from security reports,
4. The security reports can be approved or challenged by the community, through endorsement and dispute form community,

## Test Cases


## Privacy Considerations
Issuing assertions makes public the opinion of issuers (identified by their public address), and therefore should be informed about the consequence of their action.

## References
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-1][CAIP-1] defines the CAIP document structure

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1
[DID]: https://www.w3.org/TR/did-core/
[CID]: https://github.com/multiformats/cid
[did:pkh]: https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md
[multihash]: https://github.com/multiformats/multihash
[multicodec-json]: https://github.com/multiformats/multicodec/blob/master/table.csv#L138
[JCS]: <https://www.rfc-editor.org/rfc/rfc8785>

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
