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
  iat Int // Unix timestamp =issued-at
  nbf optional Int // Unix timestamp =not-before
  exp optional Int // Unix timestamp = expiration-time
  statement optional String // =statement
  requestId optional String // =request-id
  resources optional [ String ] // =resources as URIs
}
```

It is important to note, that issuer here is [did:pkh](https://github.com/spruceid/ssi/blob/main/did-pkh/did-pkh-method-draft.md), which includes both blockchain address and blockchain network information.

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

Then using [EcdsaSecp256k1RecoveryMethod2020](https://identity.foundation/EcdsaSecp256k1RecoverySignature2020/) we verify the signature of the payload above, if type is "eip4361-eip191".

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
  "h": {
    "t": "eip4361-eip191"
  },
  "p": {
    "aud": "http://localhost:3000",
    "exp": 1635517748,
    "iat": 1635514148,
    "iss": "did:pkh:eip155:1:0xfa3F54AE9C4287CA09a486dfaFaCe7d1d4095d93",
    "nbf": 1635514148,
    "uri": "http://localhost:3000/login",
    "nonce": 328917,
    "version": 1,
    "requestId": "request-id-random",
    "resources": [
      "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq",
      "https://example.com/my-web2-claim.json"
    ],
    "statement": "I accept the ServiceOrg Terms of Service: https://service.org/tos"
  },
  "s": {
    "s": "0xe56e2cdb90d070b8aa1639e1cef36f6a49da8460f86f4a51415a1c3cf0e8f9664a6669dc50250e6bcade9f58e4852da81d97676ee96c22e9ff23b4508955c68d1b" // IPLD bytes presented as hex-prefixed string
  }
}
```

CACAO Serialized: base64url-encoded CARv1 file with the IPFS block of the CACAO above:

```
uOqJlcm9vdHOB2CpYJQABcRIgwfKyc_T-HH2gEAe1ZgoJ-KAuIPXvz2U7PuelvCGEHCJndmVyc2lvbgGHBAFxEiDB8rJz9P4cfaAQB7VmCgn4oC4g9e_PZTs-56W8IYQcIqNhaKFhdG5laXA0MzYxLWVpcDE5MWFwrGNhdWR1aHR0cDovL2xvY2FsaG9zdDozMDAwY2V4cBphfAU0Y2lhdBphe_ckY2lzc3gqMHhmYTNGNTRBRTlDNDI4N0NBMDlhNDg2ZGZhRmFDZTdkMWQ0MDk1ZDkzY25iZhphe_ckY3VyaXgbaHR0cDovL2xvY2FsaG9zdDozMDAwL2xvZ2luZW5vbmNlGgAFBNVnY2hhaW5JZAFndmVyc2lvbgFpcmVxdWVzdElkcXJlcXVlc3QtaWQtcmFuZG9taXJlc291cmNlc4J4QmlwZnM6Ly9iYWZ5YmVpZW14ZjVhYmp3amJpa296NG1jM2EzZGxhNnVhbDNqc2dwZHI0Y2pyM296M2V2Znlhdmh3cXgmaHR0cHM6Ly9leGFtcGxlLmNvbS9teS13ZWIyLWNsYWltLmpzb25pc3RhdGVtZW50eEFJIGFjY2VwdCB0aGUgU2VydmljZU9yZyBUZXJtcyBvZiBTZXJ2aWNlOiBodHRwczovL3NlcnZpY2Uub3JnL3Rvc2FzoWFzWEHlbizbkNBwuKoWOeHO829qSdqEYPhvSlFBWhw88Oj5ZkpmadxQJQ5ryt6fWOSFLagdl2du6Wwi6f8jtFCJVcaNGw-3yciatpgOo4vtCSBjZ3bctBejGCW8UyNc9dQUgqNhaKFhdGdlaXA0MzYxYXCsY2F1ZHVodHRwOi8vbG9jYWxob3N0OjMwMDBjZXhwGmF7-MVjaWF0GmF76rVjaXNzeCoweGZhM0Y1NEFFOUM0Mjg3Q0EwOWE0ODZkZmFGYUNlN2QxZDQwOTVkOTNjbmJmGmF76rVjdXJpeBtodHRwOi8vbG9jYWxob3N0OjMwMDAvbG9naW5lbm9uY2UaAAUE1WdjaGFpbklkAWd2ZXJzaW9uAWlyZXF1ZXN0SWRxcmVxdWVzdC1pZC1yYW5kb21pcmVzb3VyY2VzgnhCaXBmczovL2JhZnliZWllbXhmNWFiandqYmlrb3o0bWMzYTNkbGE2dWFsM2pzZ3BkcjRjanIzb3ozZXZmeWF2aHdxeCZodHRwczovL2V4YW1wbGUuY29tL215LXdlYjItY2xhaW0uanNvbmlzdGF0ZW1lbnR4QUkgYWNjZXB0IHRoZSBTZXJ2aWNlT3JnIFRlcm1zIG9mIFNlcnZpY2U6IGh0dHBzOi8vc2VydmljZS5vcmcvdG9zYXOhYXNYQeXC_kN9a4-7cySLEe_aUkQN4dENMwUbeS416tnbFcO3FGtWMXCLjmpW4RUoi5WWH_vcgx9TCcEn3H3S00sMROQb
```

## Links

- [EIP-4361 "Sign-in with Ethereum"](https://github.com/ethereum/EIPs/blob/5e9b0fe0728e160f56dd1e4cbf7dc0a0b1772f82/EIPS/eip-4361.md)
- [did:pkh Method Specification](https://github.com/spruceid/ssi/blob/main/did-pkh/did-pkh-method-draft.md)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
