---
caip: <to be assigned>
title: CACAO: Chain Agnostic CApability Object
author: Sergey Ukustov (@ukstv)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/74
status: Draft
type: Standard
created: 2021-11-01
updated: 2021-11-01
---

## Simple Summary

Represent a chain-agnostic Object Capability (OCAP), created using [EIP-4361](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md) (or similar for other blockchains), as an [IPLD](https://ipld.io) object.

## Abstract

In this document we define a way to present a result of [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md)
signing operation as an [IPLD](https://ipld.io)-based object capability (OCAP).
As we expect other blockchains to follow a path similar to Ethereum, CAIP seems to be the best place for such a proposal.

## Motivation

"Sign-in with Ethereum" is a way for a user to authenticate into a service, and provide authorization. In essence, it is a signature of a well-formed payload, that can be read by a human as well as a machine.
We could see this as a stepping point for a _rich_ capability-based authorization system.
In order to do this, we would like to have a standardized IPLD-based representation of the payload and the signature, that together comprise a capability.

## Specification

### Container format

We start construction with declaring a container format, that represents a signed payload.
It should contain meta-information, payload and signatures. For reference let's call such container _CACAO_:

```
type CACAO struct {
  h Header // container meta-information
  p Payload // payload
  s Signature // signature, single
}
```

Header uniquely identifies signature verification process:

```
type Header struct {
  t String // specifies signature verification algorithm and format of the payload
}
```

For now we expect this to be either "eip4361-eip191" or "eip4361-eip1271". For both formats the payload structure must be presented as follows:

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

It is important to note, that issuer here is [did:pkh](https://github.com/spruceid/ssi/blob/main/did-pkh/did-pkh-method-draft.md), which includes both blockchain address and blockchain network information.
Also, as per [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md) specificaction,
`iat`, `nbf`, and `exp` are encoded as [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6) `date-time`, which could include milliseconds precision.

The difference is how we do signature verification.

The signature in essence is just bytes, but we also would like to provide additional meta-information at a later time.

```
type Signature struct {
  m optional SignatureMeta // For future extension
  s Bytes
}

type SignatureMeta struct {
}
```

### Signature Verification

We reconstruct the EIP4361 payload as follows:

```
{.p.domain} wants you to sign in with your Ethereum account:
{.p.iss[address]}

{.p.statement}

URI: {.p.aud}
Version: {.p.version}
Chain ID: {.p.iss[chainId]}
Nonce: {.p.nonce}
Issued At: {.p.iat}
Resources:
- {.p.resources[0]}
- {.p.resources[1]}
...
- {.p.resources[n]}
```

We then verify the signature of the payload above, according to [EIP-191](https://eips.ethereum.org/EIPS/eip-191) if type is "eip4361-eip191".

"eip4361-eip1271" mandates that we go to an ethereum contract complying with [EIP1271](https://eips.ethereum.org/EIPS/eip-1271) interface to verify the signature.

### Serialization

As a proper IPLD object, it can be deterministically serialized using [CBOR](https://ipld.io/docs/codecs/known/dag-cbor/) into bytes.
Performance is almost as fast as vanilla JSON serialization. For transport purposes we propose that a CACAO is passed inside a base64url-serialized [CAR](https://ipld.io/specs/transport/car/) file,
with root of the CAR file set to a tip of capability chain. Here and now we use [CARv1](https://ipld.io/specs/transport/car/carv1/) format, as [CARv2](https://ipld.io/specs/transport/car/carv2/) is still being worked on.

We propose, that all the necessary parent CACAOs are passed there as well. This way, even if a referenced CACAO is not yet available over IPFS, both consumer and presenter of CACAO still can access it.

## Rationale

- As a chain-agnostic standard, a capability should identify chain-specific signature methods.
- While "Sign-in with Ethereum" standardizes payload format, the payload could be extended in future.
- The standard should be usable for DID-based signing methods as well as blockchain based ones.
- The format we are creating here should be uniquely serialized as an IPLD object; we expect it to be identified by CID.
- A capability format described here should allow chaining capabilities together.
- We should standardize on a url-safe serialization format of a capability chain suitable for well-established non-binary transport protocols.

## Backwards Compatibility

Not applicable.

## Test Cases

Below you could find a CACAO, along with its serialized presentation in CAR file.

CACAO:
```
{
  "h": { "t": "eip4361-eip191" },
  "p": {
    "aud": "http://localhost:3000",
    "iss": "did:pkh:eip155:1:0xBAc675C310721717Cd4A37F6cbeA1F081b1C2a07",
    "uri": "http://localhost:3000/login",
    "version": 1,
    "nonce": 328917,
    "iat": "2022-01-13T16:46:36.839+03:00",
    "nbf": "2022-01-13T16:46:36.839+03:00",
    "exp": "2022-01-13T17:46:36.839+03:00",
    "statement": "I accept the ServiceOrg Terms of Service: https://service.org/tos",
    "requestId": "request-id-random",
    "resources": [
      "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq",
      "https://example.com/my-web2-claim.json"
    ]
  },
  "s": {
    "s": "4acb0cb4bd4868ddb76c2d425225d3b0b708e1b69f61ec1c74c3f9616ad5d12a3875841541342c3c0552230e43272a2f0cec61917fb79d7df6c346a33501cb0f1c"
  }
}
```

CACAO Serialized: base64url-encoded CARv1 file with the IPFS block of the CACAO above:

```
uOqJlcm9vdHOB2CpYJQABcRIgzqJ6pR0g80ruHWVDkryw1P5ye62QjZpUSmgy1R8knstndmVyc2lvbgHdBAFxEiDOonqlHSDzSu4dZUOSvLDU_nJ7rZCNmlRKaDLVHySey6NhaKFhdG5laXA0MzYxLWVpcDE5MWFwq2NhdWR1aHR0cDovL2xvY2FsaG9zdDozMDAwY2V4cHgdMjAyMi0wMS0xM1QxNzo0NjozNi44MzkrMDM6MDBjaWF0eB0yMDIyLTAxLTEzVDE2OjQ2OjM2LjgzOSswMzowMGNpc3N4O2RpZDpwa2g6ZWlwMTU1OjE6MHhCQWM2NzVDMzEwNzIxNzE3Q2Q0QTM3RjZjYmVBMUYwODFiMUMyYTA3Y25iZngdMjAyMi0wMS0xM1QxNjo0NjozNi44MzkrMDM6MDBjdXJpeBtodHRwOi8vbG9jYWxob3N0OjMwMDAvbG9naW5lbm9uY2UaAAUE1Wd2ZXJzaW9uAWlyZXF1ZXN0SWRxcmVxdWVzdC1pZC1yYW5kb21pcmVzb3VyY2VzgnhCaXBmczovL2JhZnliZWllbXhmNWFiandqYmlrb3o0bWMzYTNkbGE2dWFsM2pzZ3BkcjRjanIzb3ozZXZmeWF2aHdxeCZodHRwczovL2V4YW1wbGUuY29tL215LXdlYjItY2xhaW0uanNvbmlzdGF0ZW1lbnR4QUkgYWNjZXB0IHRoZSBTZXJ2aWNlT3JnIFRlcm1zIG9mIFNlcnZpY2U6IGh0dHBzOi8vc2VydmljZS5vcmcvdG9zYXOhYXNYQUrLDLS9SGjdt2wtQlIl07C3COG2n2HsHHTD-WFq1dEqOHWEFUE0LDwFUiMOQycqLwzsYZF_t5199sNGozUByw8c
```

## Links

- [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md)
- [did:pkh Method Specification](https://github.com/spruceid/ssi/blob/main/did-pkh/did-pkh-method-draft.md)
- [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
