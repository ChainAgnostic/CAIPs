---
caip: 74
title: CACAO - Chain Agnostic CApability Object
author: Sergey Ukustov (@ukstv), Haardik (@haardikk21)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/74
status: Draft
type: Standard
created: 2021-11-01
updated: 2022-07-12
---

## Simple Summary
Represent a chain-agnostic Object Capability (OCAP), created using [CAIP-122](), as an [IPLD](https://ipld.io) object.

## Abstract
In this document we define a way to record the result of [CAIP-122]() signing operation as an [IPLD](https://ipld.io)-based object capability (OCAP). This creates not just an event receipt of an authentication, but also a composable and replay-able authorization receipt for verifiable authorizations, when the message signed contains the appropriate fields. The first CACAO profile was tailored to the ethereum dapps supporting [EIP-4361][] but roughly equivalent profiles for other wallet/dapp ecosystems are being added over time.

## Motivation

"Sign-in with X" is a way for a user to authenticate into a service, and provide authorization. In essense, it is a signature of a well-formed payload.

We could see this as a stepping point for a _rich_ capability-based authorization system.

In order to do this, we would like to have a standardized IPLD-based representation of the payload and the signature, that together comprise a capability.

## Specification

### Container format

We start construction with declaring a container format, that represents a signed payload.
It should contain meta-information, payload and signatures. For reference let's call such container _CACAO_ (for Chain Agnostic CApability Object).
We use [IPLD schema language](https://ipld.io/docs/schemas/) to describe the format.
Reminder, unless a field is marked `optional`, it is mandatory.

```
type CACAO struct {
  h Header // container meta-information
  p Payload // payload
  s Signature // signature, single
}
```

Header uniquely identifies the payload format:

```
type Header struct {
  t String // specifies format of the payload
}
```

The header type will be `caip122` in reference to the [CAIP-122]() specification for the SIWx data model. In an [older version of the specification](https://github.com/ChainAgnostic/CAIPs/blob/91aaaff73038c2629ff11b88c2209f61521d8ece/CAIPs/caip-74.md), the header type was restricted to `eip4361` as it was designed to work only with Sign-in with Ethereum. As such, newer implementations MUST be able to deal with both header types appropriately.

The payload structure must be presented as follows:

```
type Payload struct {
  domain String // =domain
  iss String // = DID pkh
  aud String // =uri
  version String
  nonce String
  iat String // RFC3339 date-time =issued-at
  nbf optional String // RFC3339 date-time =not-before
  exp optional String // RFC3339 date-time = expiration-time
  statement optional String // =statement
  requestId optional String // =request-id
  resources optional [ String ] // =resources as URIs
}
```

It is important to note, that issuer here is [did:pkh](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md), which includes both blockid address and blockchain network information.
Also, as per [CAIP-122]() specificaction,`iat`, `nbf`, and `exp` are encoded as [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6) `date-time`, which could include milliseconds precision.

The signature in essence is just bytes, but we have to give a hint on how the signature verification should work. The signature verification type is referenced from methods that are listed as possible within the [CAIP-122]() namespace.

```
type Signature struct {
  t String
  m optional SignatureMeta
  s Bytes
}

type SignatureMeta struct {
}
```

This construction allows a dApp to uniformly request a SIWx signature regardless of the user's account nature. 

### Signature Verification

Signature signing and verification should follow the workflow as specified in the [CAIP-122]() namespaces. For example, for `eip155` chains, we reconstruct the SIWx payload as follows, resulting in a message conformant with EIP-4361:

```
{.p.domain} wants you to sign in with your Ethereum account:
{.p.iss[address]}

{.p.statement}

URI: {.p.aud}
Version: {.p.version}
Chain ID: {.p.iss[chainId.reference]}
Nonce: {.p.nonce}
Issued At: {.p.iat}
Resources:
- {.p.resources[0]}
- {.p.resources[1]}
...
- {.p.resources[n]}
```

Signature verification goes according to `t` in `SignatureMeta`:
For example,
- `eip191`: use [EIP-191](https://eips.ethereum.org/EIPS/eip-191),
- `eip1271`: use [EIP-1271](https://eips.ethereum.org/EIPS/eip-1271).

### Serialization

As a proper IPLD object, it can be deterministically serialized using [CBOR](https://ipld.io/docs/codecs/known/dag-cbor/) into bytes.
Performance is almost as fast as vanilla JSON serialization. For transport purposes we propose that a CACAO is passed inside a base64url-serialized [CAR](https://ipld.io/specs/transport/car/) file,
with root of the CAR file set to a tip of capability chain. Here and now we use [CARv1](https://ipld.io/specs/transport/car/carv1/) format, as [CARv2](https://ipld.io/specs/transport/car/carv2/) is still being worked on.

We propose, that all the necessary parent CACAOs are passed there as well. This way, even if a referenced CACAO is not yet available over IPFS, both consumer and presenter of CACAO still can access it.

## Rationale

- As a chain-agnostic standard, a capability should identify chain-specific signature methods.
- While "Sign-in with X" standardizes payload format, the payload could be extended in future.
- The standard should be usable for DID-based signing methods as well as blockchain based ones.
- The format we are creating here should be uniquely serialized as an IPLD object; we expect it to be identified by CID.
- A capability format described here should allow chaining capabilities together.
- We should standardize on a url-safe serialization format of a capability chain suitable for well-established non-binary transport protocols.

## Backwards Compatibility

In the [previous version of this specification](https://github.com/ChainAgnostic/CAIPs/blob/91aaaff73038c2629ff11b88c2209f61521d8ece/CAIPs/caip-74.md), the header type was restricted to `eip4361` as it was designed to work only with Sign-in with Ethereum. Newer implementations should support both header types - `eip4361` and `caip122`.

## Example

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

CACAO Serialized: base64url-encoded CARv1 file with the IPFS block of the CACAO above:

```
uOqJlcm9vdHOB2CpYJQABcRIgEbxa4r0lKwE4Oj8ZUbYCpULmPfgw2g_r12IcKX1CxNlndmVyc2lvbgHdBAFxEiARvFrivSUrATg6PxlRtgKlQuY9-DDaD-vXYhwpfULE2aNhaKFhdGdlaXA0MzYxYXCrY2F1ZHgbaHR0cDovL2xvY2FsaG9zdDozMDAwL2xvZ2luY2V4cHgdMjAyMi0wMy0xMFQxODowOToyMS40ODErMDM6MDBjaWF0eB0yMDIyLTAzLTEwVDE3OjA5OjIxLjQ4MSswMzowMGNpc3N4O2RpZDpwa2g6ZWlwMTU1OjE6MHhCQWM2NzVDMzEwNzIxNzE3Q2Q0QTM3RjZjYmVBMUYwODFiMUMyYTA3Y25iZngdMjAyMi0wMy0xMFQxNzowOToyMS40ODErMDM6MDBlbm9uY2VmMzI4OTE3ZmRvbWFpbm5sb2NhbGhvc3Q6MzAwMGd2ZXJzaW9uAWlyZXF1ZXN0SWRxcmVxdWVzdC1pZC1yYW5kb21pcmVzb3VyY2VzgnhCaXBmczovL2JhZnliZWllbXhmNWFiandqYmlrb3o0bWMzYTNkbGE2dWFsM2pzZ3BkcjRjanIzb3ozZXZmeWF2aHdxeCZodHRwczovL2V4YW1wbGUuY29tL215LXdlYjItY2xhaW0uanNvbmlzdGF0ZW1lbnR4QUkgYWNjZXB0IHRoZSBTZXJ2aWNlT3JnIFRlcm1zIG9mIFNlcnZpY2U6IGh0dHBzOi8vc2VydmljZS5vcmcvdG9zYXOiYXNYQVzLE0rT2HTLtAoys5lUnNMslT3F3IfcZGJKPj3AaE19SDMEPdfp9KaJSFP43FVfl7x-PH3T_MZkCeuYK_86RGcbYXRmZWlwMTkx
```

## Versioning

Present version of CAIP-74 updates and clarifies the previous versions:

- [Version 1 - CACAO for Sign-in with Ethereum](https://github.com/ChainAgnostic/CAIPs/blob/91aaaff73038c2629ff11b88c2209f61521d8ece/CAIPs/caip-74.md)

## Links

- [CAIP-122 "Sign-in with X"](https://github.com/ChainAgnostic/CAIPs/pull/122)
- [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md)
- [did:pkh Method Specification](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md)
- [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)
- [EIP-191: Signed Data Standard](https://eips.ethereum.org/EIPS/eip-191)
- [EIP-1271: Standard Signature Validation Method for Contracts](https://eips.ethereum.org/EIPS/eip-1271)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
