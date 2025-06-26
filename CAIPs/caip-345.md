---
caip: CAIP-345
title: Wallet Service URL property
author: Chris Smith (@chris13524)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/345
status: Draft
type: Standard
created: 2025-02-17
updated: 2025-06-24
requires: 25
---

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
Handling of wallet JSON-RPC requests by wallet-provided HTTP endpoint.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
This proposal defines the wallet service property, `caip345`, for use in CAIP-25. Wallets set this to indicate that certain methods, instead of being handled by the CAIP-25 session, will instead be sent to a JSON-RPC HTTP endpoint. Compatible apps that support this will be able to call these RPC methods, without interactivity with the actual wallet application or user.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
It is sub-optimal UX to redirect the user to their wallet in order to handle RPC requests that are non-actionable to them. This is especially relevant for protocols such as WalletConnect which is used in more distributed environments such as mobile wallets or custodial solutions. In these contexts, actioning a wallet RPC request can involve significant effort.

Examples of non-actionable wallet requests include:
- [EIP-5792 `wallet_getCapabilities`](https://eips.ethereum.org/EIPS/eip-5792#wallet_getcapabilities)
- [EIP-5792 `wallet_getCallsStatus`](https://eips.ethereum.org/EIPS/eip-5792#wallet_getcallsstatus-rpc-specification)
- [ERC-7836](https://github.com/ethereum/ERCs/pull/758) `wallet_prepareCalls` and `wallet_sendPreparedCalls`
- [ERC-7811](https://eips.ethereum.org/EIPS/eip-7811) `wallet_getAssets`

By defining a way for wallets to send requests to an out-of-band endpoint, the requests can be satisfied without needing the wallet app to be open.

## Specification

### Wallet Service

A "wallet service" is a JSON-RPC-compatible HTTP endpoint that can be used to satisfy certain wallet RPC methods. This service may be developed, hosted, and maintained by the same organization developing the wallet app, or by a third-party. It is up to the wallet to determine what server should be responsible for handling wallet RPCs.

The wallet service can be specified as a URL, and a list of methods for which to use the URL. `methods` SHOULD NOT be empty. The endpoint MUST be JSON-RPC compatible and support `POST` requests.

```ts
type WalletService = {
    methods: string[],
    url: string,
};
```

Wallets MAY provide query params as part of the URL. These params could be useful for many things such as providing an authentication token, connection ID, identifying the chain being used, or providing other necessary details to fulfil the request.

The endpoint SHOULD enable CORS (Cross-Origin-Resource-Sharing) to allow arbitrary app domains to access the endpoint.

The wallet service MAY respond with a `Cache-Control` header, indicating the cacheability of the response. Apps SHOULD set the JSON-RPC `id` field to `0`, increasing the chance of a cache-hit.

Wallets MUST NOT set multiple wallet service entries for the same method. Apps SHOULD NOT attempt to recover from multiple or conflicting wallet service URLs, but MAY use the first URL available for the method as a convenience for implementation.

Apps SHOULD use the wallet service when available for a method, instead of calling the wallet directly.

Here is an example implementation:

```javascript
const jsonRpc = { ... };

const handler = walletService.find(s => s.methods.includes(jsonRpc.method));
if (handler) {
    jsonRpc.id = 0; // optional
    return fetch(handler.url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(jsonRpc)
    });
} else {
    // fallback to sending directly to wallet
}
```

### Usage in CAIP-25

The `caip345` property can be used in both `sessionProperties` and `scopedProperties`, depending on what scopes the methods are supported on.

```ts
// scoped or session properties
type Caip25Properties = {
    caip345: WalletService[],
    [key: string]: any, // other properties
};
```

If a method is included in the `caip345` property, then the same method MUST be included in the CAIP-25 session for the same scopes. Wallets MUST implement fallback handling for all wallet service methods, in case the app does not implement this CAIP.

Below is an unverified, non-normative, example for `wallet_getAssets` which is only supported in the `eip155` scope:

```json
"scopedProperties": {
    "eip155": {
        "caip345": [{
            "methods": ["wallet_getAssets"],
            "url": "https://wallet-service.example.com/rpc"
        }],
    }
}
```

Below is an unverifed, non-normative, example for ERC-7836, which is also only valid in the `eip155` scope:

```json
"scopedProperties": {
    "eip155": {
        "caip345": [{
            "methods": ["wallet_prepareCalls", "wallet_sendPreparedCalls"],
            "url": "https://wallet-service.example.com/rpc"
        }],
    }
}
```

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->

### Support for multiple wallet service URLs

Supporting an array of wallet services increases flexibility. Allowing different URLs (servers or parameters) to be used for different methods or use cases.

There must still be 1 canonical wallet service URL for a given method (if available at all).

### Different wallet service depending on account

There was consideration for being able to specify different wallet service URLs for different accounts. However, this would not make sense in the context of CAIP-25 because there is no mechanism to scope the methods to particular accounts. If this CAIP provided a way to scope the methods to particular accounts, the app may still try to send the method requests for non-listed accounts directly to the wallet.

### Map methods to wallet services, instead of methods in array

There was consideration for defining the standard to have a separate wallet service URL for every single method. However, this would cause excessive bandwidth consumption if the same URL were to be used for multiple methods which we think is the more likely case.

### Not supporting custom headers

Specifying custom headers to use in the wallet service request is not supported. This is because in browsers, custom headers will create pre-flight `OPTIONS` requests, increasing bandwidth and server load. Instead, the necessary parameters should be passed via query params.

### Web apps using `connect-src` in Content-Security-Policy

Many web apps specify `connect-src` in their CSP which prevents the app and its libraries from connecting to URLs that aren't pre-specified. Since each wallet may use their own wallet service hosted on unique origins, it's not possible/advisable to dynamically set the `connect-src` value as-necessary.

However to support this use case, such apps can consume a minimal proxy service (such as a server function) which will forward the wallet RPC request to the destination wallet service. This proxy service is known by the app, and has a fixed origin URL, which allows placing it in `connect-src`. The proxy service could be implemented by the app, or by a third-party.

The mechanism by which this proxy service is implemented or consumed is outside the scope of this CAIP.

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->
Valid wallet service:
```json
{
    "methods": ["wallet_prepareCalls", "wallet_sendPreparedCalls"],
    "url": "https://wallet-service.example.com/rpc"
}
```

Invalid wallet service:
```json
{
    "url": "https://wallet-service.example.com/rpc"
}
```

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
The wallet service would bear the security responsibility of responding to these wallet RPC requests. Wallets should make informed decisions about which providers they use for this.

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
Similarly, the wallet service would have visibility into the wallet RPC requests being sent to it.

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
This new property is fully backwards-compatible with CAIP-25.

As also mentioned above, wallets MUST implement fallback handling for all wallet service methods, in case the app does not implement this CAIP.

## References
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-25][CAIP-25] is where this property is used

[CAIP-25]: https://ChainAgnostic.org/CAIPs/caip-25

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
