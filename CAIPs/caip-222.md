---
caip: 222
title: Wallet Authenticate JSON-RPC Method
author: Pedro Gomes (@pedrouid), Gancho Radkov (@ganchoradkov)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/222
status: Draft
type: Standard
created: 2023-04-07
updated: 2023-10-19
requires: 2, 10, 74, 122
---

## Simple Summary

This CAIP defines a JSON-RPC method to authenticate wallet ownership/control of one or more blockchain account(s), on one or more specific chains.

## Abstract

This proposal provides a simpler user experience by bundling two wallet requests into a single method to provide wallet authentication when exposing blockchain accounts with a cryptographic signature

## Motivation

Prior this standard Wallets were required to establish a JSON-RPC connection with an application (Dapp) to expose blockchain accounts but this required a secondary request to verify ownership of each account to return a cryptographic signature.

There are challenges regarding the flexibility of what can be signed or not without prior knowledge of the blockchain accounts but with the adoption of standards like SIWx ([CAIP-122][caip-122]) we have the ability to define specific parameters that can be signed securely without the accounts being exposed beforehand.

This will accelerate wallet adoption by social media applications which do not require persistent sessions and only need a single cryptographic signature to verify ownership of blockchain accounts.

## Specification

This JSON-RPC method can be requested to a wallet provider without prior knowledge of the blockchain accounts.

The requester will provide parameters required by [CAIP-122][caip-122] plus a CACAO header type as specified by [CAIP-74][caip-74]

The responder will return zero, one, or more signed CACAO(s) with a header type and payload matching the requested parameters.

### Request

The application would interface with a provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_authenticate",
  "params": {
      "cacaov": string,
      "type": string,
      "chains": string[],
      "domain": string,
      "aud": string,
      "version": string,
      "nonce": string,
      "iat": string,
      "exp": string, // optional
      "nbf": string, // optional
      "statement": string, // optional
      "requestId": string, // optional
      "resources": string[], // optional
      "signatureTypes": Record<string, string[]> // optional
  }
}
```

The JSON-RPC method is labelled as `wallet_authenticate` and expects the following parameters:

- cacaov - Cacao version number
- type - Cacao header message type
- chains - List of [CAIP-2][]-defined `chains` to identify one or more chains or networks.
- domain - [RFC 4501][rfc 4501] `dnsauthority` that is requesting the signing.
- aud - [RFC 3986][rfc 3986] URI referring to the resource that is the subject of the signing.
- version - Current version of the message.
- nonce - Randomized token to prevent signature replay attacks.
- iat - [RFC 3339][rfc 3339] `date-time` that indicates the issuance time.
- exp (optional) - [RFC 3339][rfc 3339] `date-time` that indicates when the signed authentication message is no longer valid.
- nbf (optional) - [RFC 3339][rfc 3339] `date-time` that indicates when the signed authentication message starts being valid.
- statement (optional) - Human-readable ASCII assertion that the user will sign. It _MUST NOT_ contain `\n`.
- requestId (optional) - System-specific identifier used to uniquely refer to the authentication request.
- resources (optional) - List of information or references to information the user wishes to have resolved as part of the authentication by the relying party; express as [RFC 3986][rfc 3986] URIs and separated by `\n`.
- signatureTypes (optional) - Object specifying the supported signing algorithms by the application for each namespace. The namespace MUST be defined in the key, while the supported signing algorithms as `string[]` and MUST contain one or more values if present.

Example of `signatureTypes`

```
signatureTypes: {
  "eip155": ["eip191", "eip1271" ],
  "cosmos": ["amino"]
}
```

### Response

The wallet will prompt the user with a dedicated UI to display the app requesting the authentication and allow the user to select which blockchain account to sign with.

#### Success

If approved, the wallet will return a list of signed CACAOs for each of the requested chains and/or selected blockchain account(s).

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": [{
    "h": {
      "t": string,
    },
    "p": {
      "iss": string,
      "aud": string,
      "exp": string,
      "iat": string,
      "nbf": string,
      "nonce": string,
      "domain": string,
      "version": string,
      "requestId": string,
      "resources": string[],
      "statement": string,
    },
    "s": {
      "t": string,
      "s": string
    }
  }]
}
```

All payload parameters MUST match the same parameters as the ones provided in the request and the following will be added:

- h - header object according to [CAIP-74][caip-74]
  - t - cacao message type (matching `type` in request)
- p - payload object according to [CAIP-74][caip-74]
  - iss - [did:pkh][did:pkh] method identifying [CAIP-10][caip-10] blockchain account
- s - signature object according to [CAIP-74][caip-74]
  - t - signature type
  - s - signature bytes

#### Failure

Request will fail if rejected by the user or if parameters fail validation.

The following Error responses MUST be used:

- User Rejected Request
  - code = 6000
  - message = "User Rejected Request"
- Invalid Request Params
  - code = 6001
  - message = "Invalid Request Params"

#### Not supported chains

The wallet SHOULD ignore all unsupported chains and SHOULD NOT auto reject the request if there are supported chains to sign without explicit user rejection.

## Rationale

This standard provides both benefits to users and developers by compiling two widely adopted patterns into a single request: connecting a wallet and signing an authentication message.

Users have the ability to quickly authenticate into applications without establishing a persistent session before they have decided to actually expose a two-way communication channel to sign more messages and/or transactions.

Developers have the ability to verify ownership of users' blockchain accounts similarly to how other authentication standards like OAuth and OIDC using also a more familiar interface that is commonly found in many social applications today.

This also incentives more applications to prevent impersonation by verifying ownership of blockchain accounts upfront without needing to trust the wallet provider interface that optimistically exposes account on connection.

This interface can be used with multiple accounts by including in the response an array of signed CACAOs for each address.

## Test Cases

Here is an example request and response exchange with `wallet_authenticate` method with single chain request:

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_authenticate",
  "params": {
      "cacaov": "2",
      "type": "eip4361",
      "chains": ["eip155:1"],
      "aud": "http://localhost:3000/login",
      "exp": "2022-03-10T18:09:21.481+03:00",
      "iat": "2022-03-10T17:09:21.481+03:00",
      "nbf": "2022-03-10T17:09:21.481+03:00",
      "nonce": "328917",
      "domain": "localhost:3000",
      "version": "1",
      "requestId": "request-id-random",
      "resources": [
        "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq",
        "https://example.com/my-web2-claim.json"
      ],
      "statement": "I accept the ServiceOrg Terms of Service: https://service.org/tos",
      "signatureTypes": {
        "eip155": ["eip191", "eip1271"]
      }
  }
}


// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": [{
    "h": {
      "t": "eip4361",
    },
    "p": {
      "iss": "did:pkh:eip155:1:0xBAc675C310721717Cd4A37F6cbeA1F081b1C2a07",
      "aud": "http://localhost:3000/login",
      "exp": "2022-03-10T18:09:21.481+03:00",
      "iat": "2022-03-10T17:09:21.481+03:00",
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
      "t": "eip191",
      "s": "5ccb134ad3d874cbb40a32b399549cd32c953dc5dc87dc64624a3e3dc0684d7d4833043dd7e9f4a6894853f8dc555f97bc7e3c7dd3fcc66409eb982bff3a44671b",
    }
  }]
}
```

Example request and response with multiple chains

```jsonc
// Request
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_authenticate",
  "params": {
      "cacaov": "2",
      "type": "eip4361",
      "chains": ["eip155:1", "eip155:5"],
      "aud": "http://localhost:3000/login",
      "exp": "2022-03-10T18:09:21.481+03:00",
      "iat": "2022-03-10T17:09:21.481+03:00",
      "nbf": "2022-03-10T17:09:21.481+03:00",
      "nonce": "328917",
      "domain": "localhost:3000",
      "version": "1",
      "requestId": "request-id-random",
      "resources": [
        "ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq",
        "https://example.com/my-web2-claim.json"
      ],
      "statement": "I accept the ServiceOrg Terms of Service: https://service.org/tos",
      "signatureTypes": {
        "eip155": ["eip191", "eip1271"]
      }
  }
}


// Response
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": [{
    "h": {
      "t": "eip4361",
    },
    "p": {
      "iss": "did:pkh:eip155:1:0xBAc675C310721717Cd4A37F6cbeA1F081b1C2a07",
      "aud": "http://localhost:3000/login",
      "exp": "2022-03-10T18:09:21.481+03:00",
      "iat": "2022-03-10T17:09:21.481+03:00",
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
      "t": "eip191",
      "s": "5ccb134ad3d874cbb40a32b399549cd32c953dc5dc87dc64624a3e3dc0684d7d4833043dd7e9f4a6894853f8dc555f97bc7e3c7dd3fcc66409eb982bff3a44671b",
    }
  },
  {
    "h": {
      "t": "eip4361",
    },
    "p": {
      "iss": "did:pkh:eip155:5:0xFa2CD71C6F32EDdaA644DEa499ee0b91ebCE1E72",
      "aud": "http://localhost:3000/login",
      "exp": "2022-03-10T18:09:21.481+03:00",
      "iat": "2022-03-10T17:09:21.481+03:00",
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
      "t": "eip1271",
      "s": "0xa33c2d454407eff97d96c39b0ae5dbfd1425a96aa9a52f83c5a1ab556f17157d5496dc2ea80428c6a126ec5eb2d43c8ef0c15c24d13a7994c5a0dc2e3135d7991c",
    }
  }]
}
```

## Security Considerations

In order to provide the user guarantees of the origin for the request the wallet MUST provide domain binding by verifying the origin matches the `domain` parameter as described in [CAIP-122][caip-122] description under "Verify domain binding"

## Privacy Considerations

Users must be prompted explicitly to approve `wallet_authenticate` requests as these not only verify ownership of a specific blockchain account as specified in the `iss` param but also they are exposing it to the application which will be able to index and explore blockchain history corresponding to the blockchain account used for signing.

## Backwards Compatibility

CACAO versioning is included as a parameter for this request to allow backwards-compatibility for future changes in [CAIP-74][caip-74] standard.

## Links

- [CAIP-2][caip-2] - Blockchain ID Specification
- [CAIP-10][caip-10] - Account ID Specification
- [CAIP-74][caip-74] - CACAO: Chain Agnostic CApability Object
- [CAIP-122][caip-122] - Sign in With X (SIWx)
- [RFC 3339][rfc 3339] - Date and Time on the Internet: Timestamps
- [RFC 3986][rfc 3986] - Uniform Resource Identifier (URI): Generic Syntax
- [RFC 4501][rfc 4501] - Domain Name System Uniform Resource Identifiers
- [did:pkh][did:pkh] - did:pkh Method Specification

[caip-2]: https://chainagnostic.org/CAIPs/caip-2
[caip-10]: https://chainagnostic.org/CAIPs/caip-2
[caip-74]: https://chainagnostic.org/CAIPs/caip-74
[caip-122]: https://chainagnostic.org/CAIPs/caip-122
[rfc 3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[rfc 3986]: https://www.rfc-editor.org/rfc/rfc3986
[rfc 4501]: https://www.rfc-editor.org/rfc/rfc4501.html
[did:pkh]: https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
