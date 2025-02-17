---
caip: CAIP-345
title: Wallet Service URL property
author: Chris Smith (@chris13524)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/345
status: Draft
type: Standard
created: 2025-02-17
updated: 2025-02-17
requires: 25
---

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
This CAIP defines a mechanism for CAIP-25 wallets to advertize support for a "wallet service" which can handle requests for specific wallet RPC methods instead of apps sending these requests directly to the wallet app.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
A short (~200 word) description of the technical issue being addressed.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
In protocols such as WalletConnect it is bad UX to redirect the user to a wallet to handle requests that are non-actionable to them. Examples of this include:
- `wallet_getCapabilities` - is this real? point to EIP
- `wallet_getCallsStatus` - point to EIP
- `wallet_getAssets` - point to EIP

By defining a way for wallets to send requests to an external URL, the requests can be satisfied without needing the wallet app to be open.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

Apps SHOULD use the wallet service when available for a method, instead of calling the wallet directly.

Wallets SHOULD implement fallback handling for all wallet service methods.

Wallets MUST NOT include the same method multiple times or with different (conflicting) wallet service URLs. Apps SHOULD NOT attempt to recover from multiple or conflicting wallet service URLs, but MAY use the first URL available for the method as a convenience for implementation.

```ts
type WalletService = {
    url: string,
    methods: string[],
}[];

type Properties = {
    walletService: WalletService,
    [key: string]: any, // other properties
};
```

An easy way for apps to find the wallet service URL for a given method would be:

```javascript
walletService.find(s => s.methods.includes(method)).url
```

The `walletService` key can be used in both `sessionProperties` and `scopedProperties`, depending on what namespaces the method(s) in question are supported on.

Below is an example support for `wallet_getAssets` which is only supported on `eip155` namespaces:

```json
"scopedProperties": {
    "eip155": {
        "walletService": [{
            "url": "<wallet service URL>",
            "methods": ["wallet_getAssets"]
        }],
    }
}
```

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->

Allowing multiple URLs to be provided for different methods (useful for different endpoints, params or auth tokens). While at the same time, not duplicating the Wallet Service URL.

There was consideration for being able to specify different wallet service URLs for different accounts. However this would not make sense in the context of CAIP-25 because there is no mechanism to scope the methods themselves to particular accounts. If we provided a mechanism to scope the methods to accounts in this CAIP, the app may still try to send the method requests for non-listed accounts directly to the wallet.

There was consideraiton for defining the standard to have a unique wallet service URL for every single method. However this would cause excessive bandwidth consumption if the same URL were to be used for multiple methods.

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
The wallet service would bear the security resonsibility of responding to these wallet RPC requests. Wallets should make informed decisions about which providers they use for this.

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
Similarly, the wallet service would have visibility into the wallet RPC requests being sent to it.

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
This new property is fully backwards-compatible with CAIP-25.

## References
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-25][CAIP-25] is where this property is used

[CAIP-25]: https://ChainAgnostic.org/CAIPs/caip-25

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
