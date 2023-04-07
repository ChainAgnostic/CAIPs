---
caip: 222
title: Wallet Authenticate JSON-RPC Method
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/222
status: Draft
type: Standard
created: 2022-04-07
requires: ["2", "10", "74", "122"]
---

## Simple Summary

This CAIP defines a JSON-RPC method to authenticate wallet ownership/control of a blockchain account.

## Abstract

This proposal provides a simpler user experience by bundling two wallet requests into a single method to provide wallet authentication when exposing blockchain accounts with a cryptographic signature

## Motivation

Prior this standard Wallets were required to establish a JSON-RPC connection with an application (Dapp) to expose blockchain accounts but this required a secondary request to verify ownership of each account to return a cryptographic signature.

There are challenges regarding the flexibility of what can be signed or not without prior knowledge of the blockchain accounts but with the adoption of standards like SIWx ([CAIP-122][caip-122]) we have the ability to define specific parameters that can be signed securely without the accounts being exposed beforehand.

This will accelerate wallet adoption by social media applications which do not require persistent sessions and only need a single cryptographic signature to verify ownership of blockchain accounts.

## Specification

This JSON-RPC method can be requested to a wallet provider without prior knowledge of the blockchain accounts.

The requester will provide parameters required by [CAIP-122][caip-122] plus a CACAO header type as specified by [CAIP-74][caip-74]

The responder will return a signed CACAO with a header type and payload matching the requested parameters

### Request

The application would interface with a provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_authenticate",
  "params": {
      "type": string,
      "chainId": string,
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
  }
}
```

The JSON-RPC method is labelled as `wallet_authenticate` and expects the following parameters:

- type - cacao header message type
- chainId - [CAIP-2][]-defined `chainId` to identify specific chain or network.
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

### Response

The wallet will prompt the user with a dedicated UI to display the app requesting the authentication and allow the user to select which blockchain account to sign with.

#### Success

If approved, the walllet will return a signed CACAO with the selected blockchain account.

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "h": {
      "t": string,
    },
    "p": {
      "aud": string,
      "exp": string,
      "iat": string,
      "iss": string,
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
  }
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

Request will fail if rejected by the user or if paramaters fail validation.

The following Error responses MUST be used:

- User Rejected Request
  - code = 6000
  - message = "User Rejected Request"
- Invalid Request Params
  - code = 6001
  - message = "Invalid Request Params"

## Rationale

This standard provides both benefits to users and developers by compiling two widely adopted patterns into a single request: connecting a wallet and signing an authentication message.

Users have the ability to quickly authenticate into applications without establishing a persistent session before they have decided to actually expose a two-way communication channel to sign more messages and/or transactions.

Developers have the ability to verify ownership of users' blockchain accounts similarly to how other authentication standards like OAuth and OIDC using also a more familiar interface that is commonly found in many social applications today.

This also incentives more applications to prevent impersonation by verifying ownership of blockchain accounts upfront without needing to trust the wallet provider interface that optimistically exposes account on connection.

This interface can be used with multiple accounts by simply calling it multiple times with a difference nonce and prompting the waller user to authenticate each account individually without establishing a persistent connection.

## Test Cases

TODO

## Security Considerations

TODO

## Privacy Considerations

TODO

## Backwards Compatibility

TODO

## Links

[caip-2]: https://chainagnostic.org/CAIPs/caip-2
[caip-10]: https://chainagnostic.org/CAIPs/caip-2
[caip-74]: https://chainagnostic.org/CAIPs/caip-74
[caip-122]: https://chainagnostic.org/CAIPs/caip-122
[rfc 4501]: https://www.rfc-editor.org/rfc/rfc4501.html
[rfc 3986]: https://www.rfc-editor.org/rfc/rfc3986
[rfc 3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[did:pkh]: https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
