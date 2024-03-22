---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: CAIP-x
title: Community-Powered Trust Assessment
author: Dayan | dayan.fc | dayan.lens | dayanx.eth | (@dayksx) <dayksx@protonmail.com>
discussions-to: <URL(s); if multiple, list separated by , without " or []> 
status: Draft
type: Standard
created: 2023-11-21
updated: 2023-11-21
requires: CAIP-261 
---

## Simple Summary

CAIP-x introduces a data framework to represent assertions for evaluating discreet resources, such as software components, packages, or media files, by utilizing community assertions and pulling in trust data from webs of trust and other implicit trust signals.

## Abstract

The evaluation of discrete resources necessitates different kind of community feedback according to the specific purpose of the assessment. 
Whether assessing resources for security concerns or for their user-friendliness, the type and depth of feedback required differ significantly. 

Since these feedback are issues by peers part of a peer-to-peer network incorporating pseudonimous peers and potential malicious peers, peers reputation needs to be evaluated by calculating insight from web of trust. 
CAIP-261: Web of Trust Primitives introduces a data framework to represent trust assertions among peers;

**Peers**
- **Peer Trust Assertion:** (Defined in the CAIP-261) This constitute web of trust, with trust and distrust assertions among peers;
- **Peer Trust Score:** (Defined in the CAIP-261) This represent the calculated trust scores of a peer at some point in time.

This proposal incorporates the following basic primitives for Resources assessment as inputs :
**Discreet Resources**
- **Report Assertion:** This represents detailed presentation of factual information and objective analysis. (e.g. an audit in the case of software components);
- **Review Assertion:** This represents a subjective assessment reflecting personal opinions and experiences;
- **Reaction Assertion:** This represents a quantifiable expression of agreement or disagreement with a report or a review's content, typically reflecting the collective sentiment of the audience.
- **Resource Trust Score:** This represent the calculated trust scores of a resource at some point in time

All these data can be ultimately utilized to compute synthetic resource's trust scores which reflect the overall sentiment of the community.

## Motivation
Discreet Resources within a decentralized web tend to be distributed permissionlessly.
While this fosters permissionless innovation, it simultaneously exposes the system to potential vulnerabilities and scams, for lack of open trust and reputation mechanisms.
Most existing solutions for evaluating discreet resources are centralized, necessitating trusted intermediaries.
This reliance on trusted intermediaries near the edges compromises the decentralized properties of the core of the ecosystem.
By standardizing data to form a universally applicable trust graph reusable across layers of the system, we strengthen the reliability of discreet resources assessments powered by communities.

## Specification

### Identifier Scheme

The flexibility of the system requires stable and translatable identifiers for both actors and resources.
We chose to identify all actors (including software actors like trust computers or oracles) by [Decentralized Identifiers][DID] and all static resources (as well as the claims, trust assertions, and other data points) by [Content Identifiers][CID].

Our data framework has been prototyped to use the following identifiers, although other systems might apply additional identifier and serialization schemes:

- **Peers:** Describe in CAIP-261: Web of Trust primitive
- **Resources:** Custom identifiers were used per category of software components, such as checksum for specific builds/binaries (e.g. `snap://<checksum>`) and onchain addresses for deployed smart contracts (e.g. _ `did:pkh:eip155:1:<contractAddress>`
- **Assertions:** Documents like those defined and excerpted below were encoded as JSON and canonicalized according to the [JSON Canonicalization Scheme][JCS] before being serialized as a [multihash][] with a ["raw JSON" prefix][multicodec-json] to be stored in a IPFS-style syncing-friendly, [CID-queryable][CID] key/value store.
- **Software entities:** Our prototype addressed all offchain entities that produce or consume trust assertions by `did:key` public-key identifiers to simplify mutual authentication and data authentication, and all onchain entities by `did:pkh` for the addresses to which they were deployed.

### Data Model

In order to assess discreet resources, a peer can issue assertions about a discreet resource or about reviews or reports to react on their content.


![diagram1](https://github.com/dayksx/CAIPs/assets/77788154/e58a0368-8164-4175-b77d-1491a6c719d1)

#### Discreet Resource Trust Assessment Metamodel
All subsequent documents conform to the [Verifiable Credential Data Model](https://www.w3.org/TR/vc-data-model/) for the purpose of representation. 
However, this standard does not prescribe any specific document type, though it may recommend internationally recognized standards. 

#### Report Assertion
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

#### Review Assertion
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

###### Reaction Assertion

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

**A reaction on a discreet resource**
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

### Assertions Management
cf. caip-261.md

### Assertions Verification
cf. caip-261.md

### Assertions Consumption
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
