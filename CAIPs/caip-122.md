---
caip: 122
title: Sign in With X (SIWx)
author: Haardik (@haardikk21), Sergey Ukustov (@ukstv)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/122
status: Review
type: Standard
created: 2022-06-23
updated: 2022-07-06
---

## Abstract

Sign in With X describes how blockchain accounts should authenticate and authorize with off-chain services by signing a chain-agnostic message parameterized by scope, session details, and security mechanisms (e.g. a nonce).

The goal of this specification is to define a chain-agnostic data model.
When accompanied with chain-specific message forms and signing algorithms, along with chain-agnostic serialization format, this would allow for a self-custodied alternative to centralized identity providers, and improve interoperability across off-chain services for blockchain-based authentication.

## Motivation

As specified in [EIP-4361][], Sign in With Ethereum defined an Ethereum-focused workflow to authenticate Ethereum accounts on non-blockchain services. 
This work is meant to generalize and abstract the Sign in With Ethereum specification, thereby making EIP-4361 a specific implementation of this specification, to work with any cryptographic system's namespace expressible in [CAIP-10][] and [CAIP-2][] formats.

Additionally, [CAIP-74][] specified a way to represent a chain-agnostic capability object (OCAP) by placing an EIP-4361 message into a CACAO container.

With this specification, we hope to extend [CAIP-74][] to support blockchains other than Ethereum and allow for the creation of Object Capabilities (OCAPs) in a chain-agnostic way.

## Specification

### Abstract Data Model

We start by declaring an abstract data model, which contains all the requisite information, metadata, and security mechanisms to authenticate and authorize with a blockchain account securely. 
We call this data model _SIWX_.

The data model _MUST_ contain the following fields:

| Name              | Type            | Mandatory | Description                                                                                                                                                                                       |
| ----------------- | --------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `domain`          | string          | ✓         | [RFC 4501][rfc 4501] `dnsauthority` that is requesting the signing.                                                                                                                               |
| `account_address`         | string          | ✓         | Blockchain address performing the signing, expressed as the `account_address` segment of a [CAIP-10][caip-10] address; should NOT include [CAIP-2][caip-2] `chain_id`.| namespace                                                                  |
| `uri`             | string          | ✓         | [RFC 3986][rfc 3986] URI referring to the resource that is the subject of the signing i.e. the subject of the claim.                                                                              |
| `version`         | string          | ✓         | Current version of the message.                                                                                                                                                                   |
| `statement`       | string          |           | Human-readable ASCII assertion that the user will sign. It _MUST NOT_ contain `\n`.                                                                                                               |
| `nonce`           | string          |           | Randomized token to prevent signature replay attacks.                                                                                                                                             |
| `issued-at`       | string          |           | [RFC 3339][rfc 3339] `date-time` that indicates the issuance time.                                                                                                                                |
| `expiration-time` | string          |           | [RFC 3339][rfc 3339] `date-time` that indicates when the signed authentication message is no longer valid.                                                                                        |
| `not-before`      | string          |           | [RFC 3339][rfc 3339] `date-time` that indicates when the signed authentication message starts being valid.                                                                                        |
| `request-id`      | string          |           | System-specific identifier used to uniquely refer to the authentication request.                                                                                                                  |
| `chain_id`        | string          | ✓         | The `chain_id` segment of a [CAIP-10][caip-10], i.e., a [CAIP-2][caip-2] identifier for locating the address listed separately above.| 
| `resources`       | List of strings |           | List of information or references to information the user wishes to have resolved as part of the authentication by the relying party; express as [RFC 3986][rfc 3986] URIs and separated by `\n`. |
| `signature`       | bytes           | ✓         | Signature of the message signed by the wallet.                                                                                                                                                    |
| `type`            | string          | ✓         | Type of the signature to be generated, as defined in the namespaces for this CAIP.                                                                                                                |

### Namespace Specification

A [CAIP-104][] namespace that enables SIWX for that namespace needs to define an implementation profile for this specification which _MUST_ provide:

1. a signing algorithm, or a finite set of these, where multiple different signing interfaces might be used,
2. a `type` string(s) that designates each signing algorithm, for inclusion in the `signatureMeta.t` value of each signed response
3. a procedure for creating a signing input from the data model specified in this document for each signing algorithm

The signing algorithm _MUST_ cover:

1. how to sign the signing input,
2. how to verify the signature.

### Examples

As a general suggestion for authors and implementers, the signing input should be based on a string. 
The string should be human-readable, so that the signing represents the fully-informed consent of a user.

The proposed string representation format, adapted from [EIP-4361][eip-4361], should be as such:

```
${domain} wants you to sign in with your **blockchain** account:
${account_address(address)}

${statement}

URI: ${uri}
Version: ${version}
Nonce: ${nonce}
Issued At: ${issued-at}
Expiration Time: ${expiration-time}
Not Before: ${not-before}
Request ID: ${request-id}
Chain ID: ${chain_id(address)}
Resources:
- ${resources[0]}
- ${resources[1]}
...
- ${resources[n]}
```

Here:
- `**blockchain**` represents a human-readable name of the ecosystem the user can recognize its account as belonging to,
- `account_address(address)` is the `account_address` segment of a [CAIP-10][] `address` of the data model,
- `chain_id(address)` is a `chain_id` part of [CAIP-10][caip-10] `address` of the data model.

As an example, [EIP-4361][eip-4361] directly conforms to this data model. 
Since EIP-155 chains can request personal signatures ([EIP-191][eip-191]) or contract signatures ([EIP-1271][eip-1271]) in plaintext, an example message to be signed could be

```
service.org wants you to sign in with your Ethereum account:
0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2

I accept the ServiceOrg Terms of Service: https://service.org/tos

URI: https://service.org/login
Version: 1
Nonce: 32891756
Issued At: 2021-09-30T16:25:24Z
Chain ID: 1
Resources:
- ipfs://bafybeiemxf5abjwjbikoz4mc3a3dla6ual3jsgpdr4cjr3oz3evfyavhwq/
- https://example.com/my-web2-claim.json
```

Other chains, however, for example Solana, cannot do plaintext signatures and require signing over raw bytes. 
As such, signing input for the data model could be created as a string, similar to [EIP-4361][eip-4361] example above, that is represented as raw bytes. The signing input is then passed to Solana-specific signing algorithm. 
This should be defined in the Solana namespace for this CAIP specification.

Below is an example of the data model represented as a plain text similar Ethereum, and then converted to raw bytes.

Plain text representation:

```
service.org wants you to sign in with your Solana account:
GwAF45zjfyGzUbd3i3hXxzGeuchzEZXwpRYHZM5912F1

I accept the ServiceOrg Terms of Service: https://service.org/tos

URI: https://service.org/login
Version: 1
Nonce: 32891757
Issued At: 2021-09-30T16:25:24.000Z
Chain ID: 5eykt4UsFv8P8NJdTREpY1vzqKqZKvdpKuc147dw2N9d
Resources:
- ipfs://Qme7ss3ARVgxv6rXqVPiikMJ8u2NLgmgszg13pYrDKEoiu
- https://example.com/my-web2-claim.json
```

Raw bytes (encoded as base64url for brevity).

```
c2VydmljZS5vcmcgd2FudHMgeW91IHRvIHNpZ24gaW4gd2l0aCB5b3VyIFNvbGFuYSBhY2NvdW50OgpHd0FGNDV6amZ5R3pVYmQzaTNoWHh6R2V1Y2h6RVpYd3BSWUhaTTU5MTJGMQoKSSBhY2NlcHQgdGhlIFNlcnZpY2VPcmcgVGVybXMgb2YgU2VydmljZTogaHR0cHM6Ly9zZXJ2aWNlLm9yZy90b3MKClVSSTogaHR0cHM6Ly9zZXJ2aWNlLm9yZy9sb2dpbgpWZXJzaW9uOiAxCk5vbmNlOiAzMjg5MTc1NwpJc3N1ZWQgQXQ6IDIwMjEtMDktMzBUMTY6MjU6MjQuMDAwWgpDaGFpbiBJRDogMQpSZXNvdXJjZXM6Ci0gaXBmczovL1FtZTdzczNBUlZneHY2clhxVlBpaWtNSjh1Mk5MZ21nc3pnMTNwWXJES0VvaXUKLSBodHRwczovL2V4YW1wbGUuY29tL215LXdlYjItY2xhaW0uanNvbg
```

## Rationale

- As a chain-agnostic standard, SIWx should allow for authentication via blockchain wallet across non-blockchain applications regardless of which chain/wallet the user is using.
- The application server _MUST_ be able to implement fully functional authentication for as many users as possible without forcing a change to wallets
- The model should be abstract enough to allow individual namespaces to represent the signing message as suitable for their chain, while allowing conformance with [CAIP-74](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md).

## Backwards Compatibility

Not applicable.

## Wallet Implementer Steps

### Verifying `domain` binding
- Wallet implementers MUST prevent phishing attacks by matching on the `domain` term when processing a signing request. For example, when processing the message beginning with `"service.invalid wants you to sign in..."`, the wallet checks that the request actually originated from `service.invalid`.
- The domain is expected to be read from a trusted data source such as the browser window.

## References

[caip-2]: https://chainagnostic.org/CAIPs/caip-2
[caip-10]: https://chainagnostic.org/CAIPs/caip-10
[caip-74]: https://chainagnostic.org/CAIPs/caip-74
[caip-104]: https://chainagnostic.org/CAIPs/caip-104
[eip-191]: https://eips.ethereum.org/EIPS/eip-191
[eip-1271]: https://eips.ethereum.org/EIPS/eip-1271
[eip-4361]: https://eips.ethereum.org/EIPS/eip-4361
[rfc 3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[rfc 3986]: https://www.rfc-editor.org/rfc/rfc3986
[rfc 4501]: https://www.rfc-editor.org/rfc/rfc4501.html

- [CAIP-2][]: Blockchain ID Specification
- [CAIP-10][]: Account ID Specification
- [CAIP-74][]: CACAO: Chain Agnostic CApability Object
- [CAIP-104][]: Account ID Specification
- [EIP-191][]: Signed Data Standard
- [EIP-1271][]: Standard Signature Validation Method for Contracts
- [EIP-4361][]: Sign-In with Ethereum
- [RFC 4501][]: Domain Name System Uniform Resource Identifiers
- [RFC 3986][]: Uniform Resource Identifier (URI): Generic Syntax
- [RFC 3339][]: Date and Time on the Internet: Timestamps

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
