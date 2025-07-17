---
caip: 169
title: Simple Credential Interface Provider
author: Oliver Terbu (@awoie), Mircea Nistor (@mirceanis), Bumblefudge (@bumblefudge)
discussions-to: https://github.com/veramolabs/credential-provider-eip/pulls?q=is%3Apr+
status: Draft
type: Standard 
created: 2021-08-18
updated: 2022-11-07
requires: 25
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->

## Simple Summary

<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the EIP.-->

A common interface for decentralized applications and cryptocurrency wallets (or
other key management applications described in future CASA namespaces and CAIPs)
to communicate about and pass Verifiable Credentials and Verifiable
Presentations is needed for the chain-agnostic world to interact with emerging
decentralized/portable data applications. Many [elsewhere mandatory] aspects of
the verifiable credential and decentralized identifier stacks are left out of
scope to be handled in future CAIPs as needed, storage and encryption being two
notable examples.

This specification adds new methods to the multi-provider JSON-RPC for storing,
creating, selectively disclosing and proving control of offchain- and onchain
credentials under a new `wallet_creds_` prefix.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
After a dapp has requested and a wallet has declared basic verifiable
credentials capabilities in the CAIP-25 feature-discovery step, dapps can assume
the wallet will be able to support all the methods defined below. Note
that many of these calls should be routable to services, other software, or even
other dapps, without leaking information about the wallet or architecture to the
calling dapp.

This CAIP describes a few core RPC methods that combine to act as a wallet-side
Credential Provider (CP) to support _Verifiable Credentials_ (VCs) storage,
issuance, proof-of-control, and even (with some extensions) selective disclosure
or advancing proofing of those credentials. 

VCs are self-certifiable attestations from an issuer about the subject of the
VC. The holder of a VC can directly consent to disclosing information from those
VCs and, in combination with other tokens or cryptographic forms of evidence,
substantiate their context or the relationship between holder and subject. See
the [VC spec][] for more context. To facilitate developer experience and
progress towards end-to-end interoperability, this CAIP limits its scope to a
few supported proof types and focuses on a limited subset of today's VC
systems. This CAIP is important for use cases such as privacy-preserving
compliance, sign-in, sign-up, and decentralized reputation-based authorization.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->

Web3 applications like DAOs, Defi, NFT market places etc. need verifiable
offchain and onchain reputation to enable certain features for their end users.
Web3 is missing a coherent method for requesting identity assertions from their
users, e.g. for sign-in and sign-up. The majority of Web3 projects are using an
approach where they cryptographically bind a signature produced by a
single-context (and single-chain) wallet to the identity assertion; in the EVM,
for example, these approaches usually rely on either
[eth.personal.sign](https://web3js.readthedocs.io/en/v1.4.0/web3-eth-personal.html#sign)
or [EIP-712](https://eips.ethereum.org/EIPS/eip-712). The identity assertion
becomes self-certifiable with this approach, but tightly bound to the Ethereum
address as the only identifier. To improve privacy it is important to introduce
a mechanism that allows people to selectively disclose the linkage between
another identifier (such as a chain-agnostic or off-chain DID) and their
blockchain account address. 

This can be done through VCs and DIDs. This CAIP introduces new JSON-RPC methods
that are needed to build decentralized reputation for offchain and onchain uses
alike.

The identifiers by which issuers and holders and their key material are
dereferenced for verification and binding purposes are out of scope of this
specification, but complemented nicely by DID-based approaches such as those
established in the EVM space by [EIP-2844][] or the cross-chain blockchain-based
approach indexed by the [did pkh][] multicodec. Implementing verifiable
credential exchange without DIDs may lead to a closed and limited system with
limited interoperability or exportability; systems that can extend and federate
over time are encouraged to build in flexibility at both the credential format
level as well as the decentralized identifier level.

## Specification

Three new JSON-RPC methods are specified under the new `wallet_creds_*` prefix.

### Verifiable Credential Proofs

This section provides guidance on recommended [LD-Proof
Suites](https://w3c-ccg.github.io/ld-proofs/) and [IANA JWS
algorithm](https://www.iana.org/assignments/jose/jose.xhtml) support of embedded
and external proofs for VCs.  The abbreviations for common VC formats in
production today are taken from the [DIF Claim Format registry][] of the
[Presentation Exchange][] meta-protocol specification governed by the
Decentralized Identity Foundation, while the Verifiable Credentials profile
defining their proof formats are specified in the [VC spec][] itself.

#### Embedded Proofs

Credential issuance systems MUST support the following LD-Proof types for
embedded proofs (i.e. VC-LDP):
- [`EthereumEip712Signature2021`](https://w3id.org/security/suites/eip712sig-2021)
- [`JsonWebSignature2020`](https://w3id.org/security/suites/jws-2020), only
  Ed25519 and secp256k1

Credential issuance systems are RECOMMENDED to support the following LD-Proof
types for wide support of VC formats with embedded proofs (i.e. VC-LDP):
- [`BbsBlsSignature2020`](https://w3id.org/security/suites/bls12381-2020)
- [`BbsBlsBoundSignature2020`](https://w3id.org/security/suites/bls12381-2020)

#### External Proofs

CPs SHOULD support the following
[IANA](https://www.iana.org/assignments/jose/jose.xhtml) JWS algorithms for
external proofs (i.e. VC-JWT):
- [`ES256K`](https://www.rfc-editor.org/rfc/rfc8812.html)
- [`EdDSA`](https://www.rfc-editor.org/rfc/rfc8037.html)

A helpful test suite for conformance-testing VC-JWT signing can be found at [JWS-test-suite][].

### Supported Verifiable Credentials Profile

VCs that can be used with this specification MUST be valid JSON-LD as defined in
the [VC spec][]. VCs and VPs MUST use either the proofs recommended by [Embedded
Proofs](#EmbeddedProofs) or [External Proofs](#ExternalProofs) above to conform
to this specification; implementations are RECOMMENDED to limit themselves to
algorithms and key types corresponding to registered claim formats in the [DIF
Claim Format registry][] if they are expecting reasonable interoperability in
the near term, and to carefully construct their [presentation_definition
object][] accordingly.

### Present

The application calls the wallet to request verifiable claims from the CP. For
the query, we will use the [DIF Presentation
Exchange](https://identity.foundation/presentation-exchange/) data model.
Optionally, holder binding can also be requested. 

#### Method:

`wallet_creds_present`

##### Params:

- `presentation_definition` - A [presentation_definition object][] as defined in
  the [Presentation Exchange][] specification; among other parameters such as
  claim format, this can also include constraints about selective disclosure, LD
  framing, and optional holder binding requirements.
- `domain` - OPTIONAL. If holder binding was requested, this parameter is mandatory.
- `challenge` - OPTIONAL. If holder binding was requested, this parameter is mandatory.

##### Returns:

- `vp` - OPTIONAL. Present if the call was successful. It contains a _Verifiable
  Presentation_ (VP) structured as a JSON object that contains the requested VCs
  from the CP.
- `error` - OPTIONAL. Present if `presentation_definition` was malformed, does
  not comply with the Verifiable Credentials Profile defined in this
  specification.

#### Example:

```json
{
  "@context": [
    "https://www.w3.org/2018/credentials/v1",
    "https://identity.foundation/presentation-exchange/submission/v1"
  ],
  "type": ["VerifiablePresentation", "PresentationSubmission"],
  "holder": "did:example:123",
  "presentation_submission": {
    "id": "1d257c50-454f-4c96-a273-c5368e01fe63",
    "definition_id": "32f54163-7166-48f1-93d8-ff217bdb0654",
    "descriptor_map": [
      {
        "id": "vaccination_input",
        "format": "ldp_vp",
        "path": "$.verifiableCredential[0]"
      }
    ]
  },
  "verifiableCredential": [
    {
      "@context": [
        "https://www.w3.org/2018/credentials/v1",
        "https://w3id.org/vaccination/v1",
        "https://w3id.org/security/bbs/v1"
      ],
      "id": "urn:uvci:af5vshde843jf831j128fj",
      "type": ["VaccinationCertificate", "VerifiableCredential"],
      "description": "COVID-19 Vaccination Certificate",
      "name": "COVID-19 Vaccination Certificate",
      "expirationDate": "2029-12-03T12:19:52Z",
      "issuanceDate": "2019-12-03T12:19:52Z",
      "issuer": "did:example:456",
      "credentialSubject": {
        "id": "urn:bnid:_:c14n2",
        "type": "VaccinationEvent",
        "batchNumber": "1183738569",
        "countryOfVaccination": "NZ"
      },
      "proof": {
        "type": "BbsBlsSignatureProof2020",
        "created": "2021-02-18T23:04:28Z",
        "nonce": "JNGovx4GGoi341v/YCTcZq7aLWtBtz8UhoxEeCxZFevEGzfh94WUSg8Ly/q+2jLqzzY=",
        "proofPurpose": "assertionMethod",
        "proofValue": "AB0GQA//jbDwMgaIIJeqP3fRyMYi6WDGhk0JlGJc/sk4ycuYGmyN7CbO4bA7yhIW/YQbHEkOgeMy0QM+usBgZad8x5FRePxfo4v1dSzAbJwWjx87G9F1lAIRgijlD4sYni1LhSo6svptDUmIrCAOwS2raV3G02mVejbwltMOo4+cyKcGlj9CzfjCgCuS1SqAxveDiMKGAAAAdJJF1pO6hBUGkebu/SMmiFafVdLvFgpMFUFEHTvElUQhwNSp6vxJp6Rs7pOVc9zHqAAAAAI7TJuDCf7ramzTo+syb7Njf6ExD11UKNcChaeblzegRBIkg3HoWgwR0hhd4z4D5/obSjGPKpGuD+1DoyTZhC/wqOjUZ03J1EtryZrC+y1DD14b4+khQVLgOBJ9+uvshrGDbu8+7anGezOa+qWT0FopAAAAEG6p07ghODpi8DVeDQyPwMY/iu2Lh7x3JShWniQrewY2GbsACBYOPlkNNm/qSExPRMe2X7UPpdsxpUDwqbObye4EXfAabgKd9gCmj2PNdvcOQAi5rIuJSGa4Vj7AtKoW/2vpmboPoOu4IEM1YviupomCKOzhjEuOof2/y5Adfb8JUVidWqf9Ye/HtxnzTu0HbaXL7jbwsMNn5wYfZuzpmVQgEXss2KePMSkHcfScAQNglnI90YgugHGuU+/DQcfMoA0+JviFcJy13yERAueVuzrDemzc+wJaEuNDn8UiTjAdVhLcgnHqUai+4F6ONbCfH2B3ohB3hSiGB6C7hDnEyXFOO9BijCTHrxPv3yKWNkks+3JfY28m+3NO0e2tlyH71yDX0+F6U388/bvWod/u5s3MpaCibTZEYoAc4sm4jW03HFYMmvYBuWOY6rGGOgIrXxQjx98D0macJJR7Hkh7KJhMkwvtyI4MaTPJsdJGfv8I+RFROxtRM7RcFpa4J5wF/wQnpyorqchwo6xAOKYFqCqKvI9B6Y7Da7/0iOiWsjs8a4zDiYynfYavnz6SdxCMpHLgplEQlnntqCb8C3qly2s5Ko3PGWu4M8Dlfcn4TT8YenkJDJicA91nlLaE8TJbBgsvgyT+zlTsRSXlFzQc+3KfWoODKZIZqTBaRZMft3S/",
        "verificationMethod": "did:example:123#key-1"
      }
    }
  ],
  "proof": {
    "type": "Ed25519Signature2018",
    "verificationMethod": "did:example:123#key-0",
    "created": "2021-05-14T20:16:29.565377",
    "proofPurpose": "authentication",
    "challenge": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
    "jws": "eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..7M9LwdJR1_SQayHIWVHF5eSSRhbVsrjQHKUrfRhRRrlbuKlggm8mm_4EI_kTPeBpalQWiGiyCb_0OWFPtn2wAQ"
  }
}
```


### Store

Stores the given VC in the CP.

#### Method:

`wallet_creds_store`

##### Params:

- `vc` - A Verifiable Credential.

##### Returns:

- `error` - OPTIONAL. If `vc` was malformed or does not comply with the
  Verifiable Credentials Profile defined in this specification. Note that some
  wallets will call `wallet_creds_verify` locally or remotely and pass back an
  error message received thereby, depending on security context.
    + 400 - invalid parameters
    + 500 - processing or internal error
    + 501 - invalid cryptographic primitives (see `wallet_creds_metadata`)

### Verify

Verifies the proof section of a single verifiable credential after dereferencing
its `issuer` property for key material. Note that in an application<>wallet
connection, the application calls the wallet for the wallet to either perform
verification locally or remotely; in either case, the application will await
asynchronously for a success or error code, regardless of how the wallet
verifies the passed credential. Wallets that cannot return appropriate error
codes back MUST NOT authorize apps to call this method as undefined behavior may
occur.

#### Method:

`wallet_creds_verify`

##### Params:

- `vc` - A Verifiable Credential.

##### Returns:

- `error` - OPTIONAL. If `vc` was malformed or does not comply with the
  Verifiable Credentials Profile defined in this specification.

### Issue

Called **by the wallet** to the application, providing the parameters needed for
a credential issuance and expecting back a verifiable credential OR an error.
The parameters are formated as a [credential_application object][] as specified
in the [Credential Manifest][] specification.

Note that the optional `preferred_proofs` parameter is redundant if the
`credential_application` contains `format` properties subsetted from the formats
listed in the original `credential_manifest` object (delivered previously with
the issuer metadata method below). In this case, the `preferred_proofs` object
should be dropped and the `formats` in the application object should be
considered authoritative.  The second parameter is a fallback in cases where a
`credential_manifest` was not available or malformed, or for legacy
compatibility with non-conforming apps (e.g. apps exposing only OIDC
capabilities, not credential manifest capabilities).

#### Method:

`wallet_creds_issue`

##### Params:

- `credential_application` - REQUIRED. This can vary from the full contents of
  the payload of the to-be-issued credential to a mere consent event per
  use-case, but in either case MUST be formated as a valid
  [credential_application object][] as specified in the [Credential Manifest][]
  specification.
- `preferred_proofs` - OPTIONAL.  An **ordered** array (from most to least
  preferred) of preferred proof formats and types for the VC to be issued. Each
  array item is an object with two properties, `format` and `type`. `format`
  indicates the preferred proof type, which is either `jwt` for (External
  Proofs) or `ldp` for (Embedded Proofs). The `type` refers to proof type of the
  VC (see [Verifiable Credentials Proofs](#Verifiable-Credentials-Proofs) for a
  list of valid combinations). If the wallet does not support any of the
  preferred proofs, the wallet can select a format and type from the list
  defined in [Verifiable Credentials Proofs](#Verifiable-Credentials-Proofs) as
  a fallback.

##### Returns:

- `vc` - OPTIONAL. Present if the call was successful. A Verifiable Credential
  that was issued to the CP by the application.
- `error` - OPTIONAL. If `payload` was malformed, or does not comply with the
  Verifiable Credentials Profile defined in this specification.
    + 400 - invalid `credential_application` or payload construction
    + 500 - processing or internal error


### Wallet Metadata

Called by the application to fetch a configuration object describing signing and
verification capabilities of the wallet.  This method is an optional way for
CAIP-169 supporting wallets to signal signing and verifying capabilities *in
addition to* those necessary for the blockchain/VM-specific accounts and methods
already exposed; it is required for interoperability with OIDC-based protocols,
and its absence can be interpreted as an absence of interoperability with
protocols requiring it. 

The formatting and values of the metadata object are borrowed from the
[OIDC4VP][] specification, which in references the [DIF Claim Format registry][]
for the names of objects and [IANA JOSE Registry][] for the exact,
case-sensitive abbreviations contained therein. Note that no other values from a
full OAuth2 Authorization Server metadata need to be present, and they will be
ignored if included.

NOTE: `alg` value `none` SHOULD NOT be accepted.

#### Method:

`wallet_creds_metadata`

##### Params:

- `URL_accepted` - OPTIONAL. Boolean, default to false if not present.  If true,
  metadata object MAY be passed as an HTTPS URL OR as an object; otherwise, MUST
  be passed as a JSON object.

##### Returns:

- `metadata_object` - OPTIONAL. See [OIDC4VP][] section "Authorization Server
  Metadata" for properties.
- `error` - OPTIONAL. 
    + 400 - invalid request
    + 500 - error forming response from selected credentials
    + 501 - invalid cryptographic primitives (see `wallet_creds_metadata`)

#### Example

```jsonc
{
  "vp_formats_supported": {
  ‌  "jwt_vc": {
      "alg_values_supported": [
        "ES256K",
        "ES384"
    ‌ ]
  ‌  },
  ‌  "jwt_vp": {
      "alg_values_supported": [
        "ES256K",
        "EdDSA"
    ‌ ]
   ‌ }
  }
}
```

### Issuer Metadata

Called by the application to **send** a configuration object describing the
specific credentials an issuer can issue to a wallet, including required user
inputs or triggers if applicable and including default values or values already
known to the application (i.e. "preview" of issuable credential).  This method
is an optional way for CAIP-169 supporting applications to enable wallets to
interact with variable or complex issuance processes. 

The formatting and values of the metadata object are taken verbatim from the
`credential_manifest` object defined in the DIF [Credential Manifest][]
specification. Note that as per the [Credential Manifest][] specification, some
issuers will require a `credential_application` object to be passed in the
issuance method based on the contents of a `credential_manifest` object sent by
this optional method; thus, a wallet signaling support for this method in a
CAIP-25 response implies the capacity to form a complex `credential_application`
object from a `credential_manifest` object.

#### Method:

`wallet_creds_manifest`

##### Params:

- `credential_manifest` object, defined in the DIF [Credential Manifest][]
  specification

##### Returns:

- `error` - OPTIONAL. 
    + 400 - invalid request
    + 500 - error parsing manifest object
    + 501 - unrecognized or unsupported manifest version

#### Example

See [Credential Manifest
section](https://verite.id/verite/appendix/messages#credential-manifest) of
Verite.id developer documentation for an example of `credential_manifest` object
design.

## Rationale

<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->

The [VC Data Model](https://www.w3.org/TR/vc-data-model/) defines a [Verifiable
Presentation](https://www.w3.org/TR/vc-data-model/#presentations) but does not
provide detail on how to express the constraints that a relying party can impose
on a presentation. The [Presentation Exchange Data
Model](https://identity.foundation/presentation-exchange/) defines a definition
and submission format for among other things, verifiable presentations.

The [Universal Wallet Interop Spec](https://w3id.org/wallet) describes how to
use concrete protocols such as [Wallet Connect](https://docs.walletconnect.org/)
and [WACI Presentation
Exchange](https://identity.foundation/waci-presentation-exchange/) with [DID
Comm Messaging](https://identity.foundation/didcomm-messaging/spec/).

In cases where a holder is directly connected to a verifier over a secure
transport, encryption and messaging related standards such as DIDComm are not
required, however interoperable data models for expressing presentation
requirements and submissions are still needed to support interoperability with
existing standards.

This proposal defines a set of API extensions that would enable web3 wallet
providers to offer wallet and credential interactions to web origins that
already support web3 wallet providers.

This functionality is similar to the interfaces supported by the [credential
handler api](https://w3c-ccg.github.io/credential-handler-api/), which does not
support the [Presentation Exchange Data
Model](https://identity.foundation/presentation-exchange/) specification but
does offer more flexible support for Linked Data verifiable credential formats
and a rich interface for wallet selection via the Credential Handler API (CHAPI).
Interoperability with other set of interfaces may require extensions or breaking
changes to this specification, and is out of scope for now.


## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
TBD

## Test Cases

See:
- [/assets/CAIP-169/presentation-definition-simple_example.json](
  "../assets/CAIP-169/presentation-definition-simple_example.json")
- [/assets/CAIP-169/presentation-submission-simple_example.json](
  "../assets/CAIP-169/presentation-submission-simple_example.json")

## Known Implementations

<!--The implementations must be completed before any EIP is given status "Final", but it need not be completed before the EIP is accepted. While there is merit to the approach of reaching consensus on the specification and rationale before writing code, the principle of "rough consensus and running code" is still useful when it comes to resolving many discussions of API details.-->

- TBD
- TBD
- TBD

## Security Considerations

<!--All EIPs must contain a section that discusses the security implications/considerations relevant to the proposed change. Include information that might be important for security discussions, surfaces risks and can be used throughout the life cycle of the proposal. E.g. include security-relevant design decisions, concerns, important discussions, implementation-specific guidance and pitfalls, an outline of threats and risks and how they are being addressed. EIP submissions missing the "Security Considerations" section will be rejected. An EIP cannot proceed to status "Final" without a Security Considerations discussion deemed sufficient by the reviewers.-->

User consent must be obtained prior to accessing wallet APIs. One way of
achieving this is with a strong UX around CAIP-25 authorization to ensure
meaningful user consent to access levels per account, per method/wallet
capability (including those specified in this specification), and per additional
resource.

The relying party MUST ensure that: the challenge required by the verifiable
presentation is sufficiently random; that it is used only once, tracked against
some form of session object (see [CAIP-170][] and [CAIP-171][]), etc; or that it
is some form of expiring verifiable credential encoded as a string.
Man-in-the-middle protection and other security assumptions fall on the relying
party (i.e. the calling application) to secure.

Appropriate domain-binding for web/http-based dapp-wallet connections is assumed
and out of scope for this specification. Other CAIPs may be forthcoming for
hardening this assumption, or may rely on the domain-binding enforced by a
preceding [CAIP-122][] flow.

Similarly, holder binding (e.g. binding the current wallet controller to the
wallet controller at time of credential issuance) is out of scope of this
specification but assumed by it. Where no holder binding beyond wallet control
is enforced, the submission details can be tampered with and should be
considered unsecured. Some useful forms of holder binding can be provided by:
wallet-level strong authentication; supplemental liveness or multi-factor
authentication mechanisms; complex holder-binding mechanisms registered as
extensions to the [VC Extension Registry][]; future CAIPs; etc.


<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->


## Links
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->

Specifications (Dependencies)
- [VC spec][] - Core data model for verifiable credentials 
- [DID EIP][] - Prior art for handling basic DID functions as methods in the
  standard ethereum provider; these are not required for VC functionality per se
  but can greatly extend the verification model for issuers and holders of VCs
  alike, enable on-chain VC verification, etc.
- [Data Integrity spec][] - Formerly referred to as "Linked Data Proofs," this
  specification for embedding proofs in structured data objects like JSON-LD
  documents and verifiable credentials specifies the handling of distinct
  credentials that flatten an open-world/RDF-style graph to be signed and
  verified.
- [Presentation Exchange][] - DIF-incubated *high-level* VC protocol (optimized
  for handling both JWT-VCs and JWTs at scale)
- [Credential Manifest][] - DIF-incubated *high-level* VC issuance protocol
- [DIF Claim Format registry][] - A registry of formats supported by
  Presentation Exchange

Specifications (Optional Dependencies and Prior Art)
- [VC API][] - W3C-CCG-incubated VC protocol (optimized for LinkedData VCs and
  the Credential Handler API)
- [DIDComm][] - DIF-incubated messaging layer, which includes sub-protocols for
  VCs extending the earlier "Present Proof" protocols incubated in Hyperledger
  Aries community.
- [OIDC4VP][] - A specification written at and governed by the OIDF that
  describes VC issuance from OIDC servers and conceives of self-custody
  cryptographic wallets as "Authorization Servers" in OIDC terminology. 

Prior Art and Reference Implementations
- [JWS-test-suite][] - A self-serve, open-source conformance test suite for
  VC-JWT implementations
- [Veramo][] Project - EVM-friendly but multi-chain sample libraries for
  issuing, signing, holding, verifying and presenting verifiable credentials 
- [Walt.id prototype][] - note that WC Chat API is used as a shim for the
  interface defined above; otherwise, a helpful prototype for illustrating a
  lightweight flow

[CAIP-170]: https://chainagnostic.org/CAIPs/caip-170
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171

[VC spec]: https://www.w3.org/TR/vc-data-model/
[Data Integrity spec]: https://www.w3.org/TR/vc-data-integrity/
[DIF Claim Format registry]: https://identity.foundation/claim-format-registry/#registry
[OIDC4VP]: https://openid.net/specs/openid-4-verifiable-presentations-1_0.html#section-8.1
[IANA JOSE Registry]: https://www.iana.org/assignments/jose/jose.xhtml#web-signature-encryption-algorithms
[JWS-test-suite]: https://identity.foundation/JWS-Test-Suite/
[Veramo]: https://veramo.io/
[VC API]: https://w3c-ccg.github.io/vc-api/
[Presentation Exchange]: https://identity.foundation/presentation-exchange/spec/v2.0.0/
[Credential Manifest]: https://identity.foundation/credential-manifest/
[DIDComm]: https://identity.foundation/didcomm-messaging/spec/v2.0/
[Walt.id prototype]: https://github.com/waltid-ethlisbon2022
[DID EIP]: https://eips.ethereum.org/EIPS/eip-2844
[did pkh]: https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md
[presentation_definition object]: https://identity.foundation/presentation-exchange/spec/v2.0.0/#presentation-definition
[credential_application object]: https://identity.foundation/credential-manifest/#credential-application

## Copyright
Copyright and related rights waived via
[CC0](https://creativecommons.org/publicdomain/zero/1.0/).
