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
It should contain meta-information, payload and signatures. For reference let's call such container _CACAO_.
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

For now, we expect this to be "eip4361" only. In the future, we anticipate creating a specialized registry for payload formats.
For "eip4361" the payload structure must be presented as follows:

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

It is important to note, that issuer here is [did:pkh](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md), which includes both blockchain address and blockchain network information.
Also, as per [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md) specificaction,
`iat`, `nbf`, and `exp` are encoded as [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6) `date-time`, which could include milliseconds precision.

The signature in essence is just bytes, but we have to give a hint on how the signature verification should work.
At the moment, we limit the signature verification by two types:
- `eip191` indicates that that signature is made by an Ethereum [externally owned account](https://www.ethdocs.org/en/latest/contracts-and-transactions/account-types-gas-and-transactions.html#externally-owned-accounts-eoas) (EOA),
- `eip1271` indicates that the signature is made by an Etereum [contract account](https://www.ethdocs.org/en/latest/contracts-and-transactions/account-types-gas-and-transactions.html#contract-accounts) (like Gnosis Safe or Argent); the verification should be done according to [EIP-1271](https://eips.ethereum.org/EIPS/eip-1271).

In the future, we anticipate creating a specialized registry for signature types.

```
type Signature struct {
  t String //= "eip191" or "eip1271"
  m optional SignatureMeta
  s Bytes
}

type SignatureMeta struct {
}
```

This construction allows a dApp to uniformly request a SIWE signature regardless of the user's account nature. The user's wallet determines if it should use an EOA or a contract account.

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

Signature verification goes according to `t` in `SignatureMeta`:
- `eip191`: use [EIP-191](https://eips.ethereum.org/EIPS/eip-191),
- `eip1271`: use [EIP1271](https://eips.ethereum.org/EIPS/eip-1271).

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
  "h": {
    "t": "eip4361"
  },
  "p": {
    "aud": "http://localhost:3000",
    "exp": "2022-02-21T14:06:33.940+03:00",
    "iat": "2022-02-21T13:06:33.933+03:00",
    "iss": "did:pkh:eip155:1:0xBAc675C310721717Cd4A37F6cbeA1F081b1C2a07",
    "nbf": "2022-02-21T13:06:33.940+03:00",
    "uri": "http://localhost:3000/login",
    "nonce": "328917",
    "version": 1,
    "requestId": "request-id-random",
    "resources": [
      "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq",
      "https://example.com/my-web2-claim.json"
    ],
    "statement": "I accept the ServiceOrg Terms of Service: https://service.org/tos"
  },
  "s": {
    "s": "65dd132e71a84e726736977474e7fe2d1286f24d78f0b3775791feaff91e850f408daa0ac689a25c16ab2d1102e1b998d2eda066332a816c35bb82414459da5a1b", // bytes
    "t": "eip191"
  }
}
```

CACAO Serialized: base64url-encoded CARv1 file with the IPFS block of the CACAO above:

```
uOqJlcm9vdHOB2CpYJQABcRIgdsRdr45d6rrcN57pvH9FTZsWFLnk5-mx7jiWry80BRdndmVyc2lvbgHhBAFxEiB2xF2vjl3qutw3num8f0VNmxYUueTn6bHuOJavLzQFF6NhaKFhdGdlaXA0MzYxYXCrY2F1ZHVodHRwOi8vbG9jYWxob3N0OjMwMDBjZXhweB0yMDIyLTAyLTIxVDE0OjA2OjMzLjk0MCswMzowMGNpYXR4HTIwMjItMDItMjFUMTM6MDY6MzMuOTMzKzAzOjAwY2lzc3g7ZGlkOnBraDplaXAxNTU6MToweEJBYzY3NUMzMTA3MjE3MTdDZDRBMzdGNmNiZUExRjA4MWIxQzJhMDdjbmJmeB0yMDIyLTAyLTIxVDEzOjA2OjMzLjk0MCswMzowMGN1cml4G2h0dHA6Ly9sb2NhbGhvc3Q6MzAwMC9sb2dpbmVub25jZWYzMjg5MTdndmVyc2lvbgFpcmVxdWVzdElkcXJlcXVlc3QtaWQtcmFuZG9taXJlc291cmNlc4J4QmlwZnM6Ly9iYWZ5YmVpZW14ZjVhYmp3amJpa296NG1jM2EzZGxhNnVhbDNqc2dwZHI0Y2pyM296M2V2Znlhdmh3cXgmaHR0cHM6Ly9leGFtcGxlLmNvbS9teS13ZWIyLWNsYWltLmpzb25pc3RhdGVtZW50eEFJIGFjY2VwdCB0aGUgU2VydmljZU9yZyBUZXJtcyBvZiBTZXJ2aWNlOiBodHRwczovL3NlcnZpY2Uub3JnL3Rvc2FzomFzWEFl3RMucahOcmc2l3R05_4tEobyTXjws3dXkf6v-R6FD0CNqgrGiaJcFqstEQLhuZjS7aBmMyqBbDW7gkFEWdpaG2F0ZmVpcDE5MQ
```

## Links

- [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md)
- [did:pkh Method Specification](https://github.com/spruceid/ssi/blob/main/did-pkh/did-pkh-method-draft.md)
- [RFC 3339](https://datatracker.ietf.org/doc/html/rfc3339#section-5.6)
- [EIP-191: Signed Data Standard](https://eips.ethereum.org/EIPS/eip-191)
- [EIP-1271: Standard Signature Validation Method for Contracts](https://eips.ethereum.org/EIPS/eip-1271)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
