---
caip: 196
title: CACAO - Chain Agnostic CApability Object
author: Sergey Ukustov (@ukstv), Haardik (@haardikk21), Irakli Gozalishvili (@Gozala), Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/196
status: Draft
type: Standard
created: 2021-11-01
updated: 2023-05-19
replaces: 74
---

## Simple Summary

A Chain Agnostic CApability Object, or CACAO, is an [IPLD](https://ipld.io) representation of an object-capability. 

## Abstract

CACAO proposes a way to leverage [varsig](https://github.com/ChainAgnostic/varsig) and [multidid](https://github.com/ChainAgnostic/multidid/) as well as IPLD to create a common representation for various different object-capability formats, such as SIWE, SIWx, and UCAN. The IPLD representation contains common fields shared between these format. In addition this CAIP also registers varsig codes for both SIWE + ReCap and UCAN. 

## Motivation

There has been a proliferation of ways to create object-capabilities in the web3 space. Most notably [Sign-in with Ethereum](https://eips.ethereum.org/EIPS/eip-4361), [UCAN](https://github.com/ucan-wg/spec), [Sign-in with X](https://chainagnostic.org/CAIPs/caip-122), and [ReCap](https://eips.ethereum.org/EIPS/eip-5573). All of these approaches share similar characteristics such as an issuer, audience, signature, etc. However, they are structured quite differently and have different constraints when it comes to how they are serialized and signed. Having a common representation strategy in IPLD can make it easier to interface with these various formats.

## Specification

The specification consists of two main things, an IPLD schema that describes the data structure of a CACAO, and algorithms to convert SIWE(x), ReCap, and UCAN messages into and out of this data structure.

### Container IPLD schema

The container schema described SHOULD be encoded using the `dag-cbor` IPLD codec.

```verilog

type Prinicpal Bytes // a multidid
type Varsig Bytes
type Resource String // URL
type Ability String // e.g. crud/create

type NB { String : Any }
type Abilities { Ability : [NB] }
type Resources { Resource : Abilities }
type Fact { String: Any }

type CACAO struct {
  iss Principal
  aud Principal
  s Varsig
  
  v String
  att Resources
  nnc String
  prf optional [&CACAO]
  iat optional Int
  nbf optional Int
  exp optional Int
  fct optional Fact
}

```

Important to note is that in the `Abilitiy` array, every `NB` are treated as separate. For example,

```json
"crud/create": [
  {"day": "friday"},
  {"status": "draft"}
]
```

is treated as "You can post drafts to the blog, or post any status on Fridays". If you want to represent "You can post drafts to the blog on Fridays" it would need to be expressed as,

```json
"crud/create": [
  {"day": "friday", "status": "draft"}
]
```

### Decode to IPLD

This section describes how to convert different object-capability formats into the IPLD schema described above.

#### SIWx + ReCap

The following values can be easily translated from the SIWx (CAIP-122) specification:

* `iss` - a multidid encoded DID PKH constructued using `address` and `chain-id`
* `aud` - a multidid encoded DID based on `uri`
* `v` - set to `version`
* `nnc` - set to `nonce`

**Timestamps**

* `iat` - is based on `issued-at`
* `nbf` - is based on `not-before`
* `exp` - is based on `expiration-time`
* `fct.z-iat` - timezone info from `issued-at`
* `fct.z-nbf` - timezone info from `not-before`
* `fct.z-exp` - timezone info from `expiration-time`

See [Appendix A](#appendix-a) for the algorithm used for the conversion.

**ReCap**

CACAO only allows there to be one ReCap message per SIWx message.

* `prf` - set to `recap.prf`, make sure that the CIDs get encoded as IPLD links
* `att` - set to `recap.att`, these should map 1-to-1

**Additional fields**

* `fct.domain` should be set to `domain`
* `fct.statement` should be set to `statement` (if the SIWx message contain a ReCap, the redundant data MUST be removed, e.g. `recap-preamble 1*(" " recap-statement-entry ".")`, according to [ReCap eip](https://eips.ethereum.org/EIPS/eip-5573))
* `fct.request-id` should be set to `request-id`
* `fct.resources` should be set to an array containing all strings in `resources` except the ReCap resource

**Signature**

The `s` field is a signature encoded as a varsig and depends on which SIWx type is used. A few examples are outlined below,

***SIWx, eip191:***

* `content_multicodec` - set to *caip122-eip191*, `0xd51e`
* `multihash` - set to *keccak-256*, `0x1b`
* `key_multicodec` - set to *secp256k1*, `0xe7`
* `raw_signature` - the signature bytes

***SIWx, solana:***

According to the [solana namespace](https://namespaces.chainagnostic.org/solana/caip122),

* `content_multicodec` - set to *caip122*, `0xd510`
* `multihash` - set to *sha2-256*, `0x12`
* `key_multicodec` - set to *ed25519*, `0xed`
* `raw_signature` - the signature bytes

***SIWx, tezos:***

According to the [tezos namespace](https://namespaces.chainagnostic.org/tezos/caip122),

* `content_multicodec` - set to *caip122*, `0xd510`
* `multihash` - set to *sha2-256*, `0x12`
* `key_multicodec` - set to *ed25519*, `0xed` (or other curves based on the tezos namespace)
* `raw_signature` - the signature bytes

#### UCAN

Most fields in a UCAN should map 1-to-1 with the CACAO IPLD schema. 

**Additional fields**

* `v` - set to `ucv` from the JWT header

* `iss` - convert the `iss` string of the UCAN to a multidid
* `aud` - convert the `aud` string of the UCAN to a multidid

**Signature**

The `s` field is a signature encoded as a varsig and depends on which signature algorithm was used for the UCAN JWT,

* `content_multicodec` - set to *ucan-jwt*, `0xd001`
* `raw_signature` - the signature bytes

Examples based on *alg* in the JWT header:

***EdDSA:***

* `multihash` - set to *sha2-256*, `0x12`
* `key_multicodec` - set to *ed25519*, `0xed`

***ES256K:***

* `multihash` - set to *sha2-256*, `0x12`
* `key_multicodec` - set to *secp256k1*, `0xe7`

#### Other formats

Other formats can be added similarly to the examples above by registering a `content_multicodec` for the particular object-capability.

### Signature Verification

To verify a signature of a CACAO the varsig specification is followed. Before verifying the signature the `content_multicodec` must be used to compute the digest used by the hash function and signature verification algorithm. Below the `content_multicodec` is described for `0xd510`, `0xd51e`, and `0xd001`.

#### Content encoding: SIWx + ReCap

In order to verify the signature we first need to reconstruct the message that was signed. For *caip122-eip191* (`0xd51e`) and *caip122* (`0xd510`) we can start with the shared steps.

**Reconstruct ReCap data**

If present the ReCap URI and statement segment need to be reconstructed. Using the values from the CACAO reconstruct the ReCap json object (should be valid *dag-json*).

```javascript
{
  att: cacao.att,
  prf: cacao.prf
}
```

The recap statement segment is computed according to the [ReCap eip](https://eips.ethereum.org/EIPS/eip-5573), e.g. `recap-preamble 1*(" " recap-statement-entry ".")`.

**Reconstruct SIWx message**

Start by computing values for:

* `address` - extract address from DID PKH in `cacao.iss`
* `chain-id` - extract chain id reference from DID PKH in `cacao.iss`

***Timestamps:***

* `issued-at` - based on `cacao.iat` and `cacao.fct.z-iat`
* `not-before` - based on `cacao.nbf` and `cacao.fct.z-nbf`
* `expiration-time` - based on `cacao.exp` and `cacao.fct.z-exp`

See [Appendix A](#appendix-a) for the algorithm used for the conversion.

Finally, construct the SIWx string:

```
{cacao.fct.domain} wants you to sign in with your Ethereum account:
{address}

{cacao.fct.statement + recap-statement-segment}

URI: {cacao.aud}
Version: {cacao.v}
Chain ID: {chain-id}
Nonce: {cacao.nnc}
Issued At: {issued-at}
Expiration Time: ${expiration-time}
Not Before: ${not-before}
Request ID: ${cacao.fct.request-id}
Resources:
- {cacao.fct.resources[0]}
- {cacao.fct.resources[1]}
...
- {cacao.fct.resources[n]}
- {recap-uri}
```

**Construct signature digest, *caip122* `0xd510`:**

`digest = SIWx-string`

**Construct signature digest, *caip122-eip191* `0xd51e`:**

Simply prepend the message according to [eip191](https://eips.ethereum.org/EIPS/eip-191):

`digest = "\x19Ethereum Signed Message:\n" + SIWx-string`

#### Content encoding: UCAN (`0xd001`)

Converting a CACAO to a UCAN string that can be verified is relatively simple. Remove the `s` and `v` fields from the CACAO object and encode it as `dag-json`. Stringify the json object and encode using base64url. The protected header is constructed as follows,

* `typ` - MUST equal `"JWT"`
* `ucv` - is set to `cacao.v`
* `alg` - is based on `key_multicodec` and `multihash` in  `cacao.v`:
  * `"EdDSA"` if *ed25519* and *sha2-256*
  * `"ES256K"` if *secp256k1* and *sha2-256*

Stringify the protected header json object and encode it using base64url.

`digest = protected-base64url + "." + payload-base64url`

### Serialization

For transport purposes a CACAO can be passed inside a base64url-serialized [CAR](https://ipld.io/specs/transport/car/) file,
with root of the CAR file set to a tip of capability chain. Here and now we use [CARv1](https://ipld.io/specs/transport/car/carv1/) format, as [CARv2](https://ipld.io/specs/transport/car/carv2/) is still being worked on.

We propose, that all the necessary parent CACAOs are passed there as well. This way, even if a referenced CACAO is not yet available over IPFS, both consumer and presenter of CACAO still can access it.

## Rationale

A common way to represent multiple different types of capabilities can enable more interoperability between object-capability systems and establishes a common ground for further innovation. CACAO relies on existing standards, such as DIDs and multicodec as a base layer for this interoperability.

Using IPLD as a represetation layer allows CACAO to easily be transfered over the internet, using IPFS or other protocols that can leverage its integrity checks.

We choose SIWx + ReCap and UCAN as examples since they represent a majority of the existing object-capabilities in use in the blockchain community today.

## Backwards Compatibility

Present version of CACAO is a substantial change from the previous draft defined in [CAIP-74](https://chainagnostic.org/CAIPs/caip-74).

## Example

**TODO - update these examples**

Below you could find a CACAO, along with its serialized presentation in CAR file.

CACAO:

```
{
  "h": {
    "t": "eip4361"
  },
  "p": {
    "aud": "http://localhost:3000/login",
    "exp": "2022-03-10T18:09:21.481+03:00",
    "iat": "2022-03-10T17:09:21.481+03:00",
    "iss": "did:pkh:eip155:1:0xBAc675C310721717Cd4A37F6cbeA1F081b1C2a07",
    "nbf": "2022-03-10T17:09:21.481+03:00",
    "nonce": "328917",
    "domain": "localhost:3000",
    "version": "1",
    "requestId": "request-id-random",
    "resources": [
      "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq",
      "https://example.com/my-web2-claim.json"
    ],
    "statement": "I accept the ServiceOrg Terms of Service: https://service.org/tos"
  },
  "s": {
    "s": "5ccb134ad3d874cbb40a32b399549cd32c953dc5dc87dc64624a3e3dc0684d7d4833043dd7e9f4a6894853f8dc555f97bc7e3c7dd3fcc66409eb982bff3a44671b",
    "t": "eip191"
  }
}
```

CACAO Serialized: base64url-encoded CARv1 file with the IPLD block of the CACAO above:

```
uOqJlcm9vdHOB2CpYJQABcRIgEbxa4r0lKwE4Oj8ZUbYCpULmPfgw2g_r12IcKX1CxNlndmVyc2lvbgHdBAFxEiARvFrivSUrATg6PxlRtgKlQuY9-DDaD-vXYhwpfULE2aNhaKFhdGdlaXA0MzYxYXCrY2F1ZHgbaHR0cDovL2xvY2FsaG9zdDozMDAwL2xvZ2luY2V4cHgdMjAyMi0wMy0xMFQxODowOToyMS40ODErMDM6MDBjaWF0eB0yMDIyLTAzLTEwVDE3OjA5OjIxLjQ4MSswMzowMGNpc3N4O2RpZDpwa2g6ZWlwMTU1OjE6MHhCQWM2NzVDMzEwNzIxNzE3Q2Q0QTM3RjZjYmVBMUYwODFiMUMyYTA3Y25iZngdMjAyMi0wMy0xMFQxNzowOToyMS40ODErMDM6MDBlbm9uY2VmMzI4OTE3ZmRvbWFpbm5sb2NhbGhvc3Q6MzAwMGd2ZXJzaW9uAWlyZXF1ZXN0SWRxcmVxdWVzdC1pZC1yYW5kb21pcmVzb3VyY2VzgnhCaXBmczovL2JhZnliZWllbXhmNWFiandqYmlrb3o0bWMzYTNkbGE2dWFsM2pzZ3BkcjRjanIzb3ozZXZmeWF2aHdxeCZodHRwczovL2V4YW1wbGUuY29tL215LXdlYjItY2xhaW0uanNvbmlzdGF0ZW1lbnR4QUkgYWNjZXB0IHRoZSBTZXJ2aWNlT3JnIFRlcm1zIG9mIFNlcnZpY2U6IGh0dHBzOi8vc2VydmljZS5vcmcvdG9zYXOiYXNYQVzLE0rT2HTLtAoys5lUnNMslT3F3IfcZGJKPj3AaE19SDMEPdfp9KaJSFP43FVfl7x-PH3T_MZkCeuYK_86RGcbYXRmZWlwMTkx
```

### <a name="appendix-a"></a>Appendix A: Timestamp converstion algorithm

The values in SIWx are encoded as [RFC3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6) strings, while CACAO requires unix timestamps (in seconds). The algorithm used to convert between the two is outlined below.

### RFC3339 to UNIX + tz-info

1. TODO

### UNIX + tz-info to RFC3339

1. TODO

## Links

- [CAIP-122 "Sign-in with X"](https://github.com/ChainAgnostic/CAIPs/pull/122)
- [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md)
- [did:pkh Method Specification](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md)
- [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)
- [EIP-191: Signed Data Standard](https://eips.ethereum.org/EIPS/eip-191)
- [Varsig](https://github.com/ChainAgnostic/varsig)
- [Multidid](https://github.com/ChainAgnostic/multidid/)

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
