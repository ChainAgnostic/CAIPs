---
caip: 300
title: Wallet Connect JSON-RPC Method
author: Lukas Rosario (@lukasrosario), Conner Swenberg (@ilikesymmetry), Pedro Gomes (@pedrouid), Luka Isailovic (@lukaisailovic), Jake Moxey (@jxom)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/300
status: Draft
type: Standard
created: 2023-06-28
requires: 2, 10,
---

## Simple Summary

This CAIP defines a JSON-RPC method to request a batch of RPC methods to be resolved when connecting a wallet in a single roundtrip.

## Abstract

This proposal enables a one-click experience to fulfill several RPC requests in parallel during the connection approval when it's established when the user is requested to connect its wallet. It gives the ability for a wallet to not only establish a session, but also authenticate the user, expose capabilities or features, define some onchain permissions, etc.

## Motivation

Currently connecting a wallet establishes a session which will be used for resolving future RPC requests which are scoped in a session with [CAIP-25][caip-25] scopes which results in an experience where user is redirected multiple times to the wallet for approving different signatures.

However there are several scenarios where you are required to resolve RPC requests that require user approval but are contextually required to happen when you first connect your wallet. This essentially ends up creating a friction point for users on their first few seconds when they connect their wallet and are redirected to the wallet two or three times in a row.

One of the most common scenarios is the requirement to authenticate a user right after the session is established which means the user is redirected first to approve a [CAIP-25][caip-25] session and immediatelly is redirected to sign an authentication request with [CAIP-122][caip-122] message to be signed. This specific use-case is addressed by the introduction of `wallet_authenticate` as standardized by [CAIP-222][caip-222].

Yet there are many other requests that can benefit from this pattern but it's not feasible to create several replicas of specialized batches of requests on a single round-trip similarly to [CAIP-222][caip-222]. Also these batches are sometimes not chain-agnostic and require ecosystem specific requests which are specified for blockchains like Ethereum, Solana, Cosmos, Polkadot, etc.

Therefore this proposal creates a generalized approach to batching requests to be resolved in a single roundtrip which would be fullfilled by redirecting the user only once when they connect their wallet and more importantly, it will support RPCs that are useful for ecosystem specifically.

## Specification

This JSON-RPC method can be requested to a wallet provider without prior knowledge of any blockchain accounts, chains, methods or other features.

### Request

The application would interface with a wallet to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_connect",
  "params": {
      "requests?": {
        "method": string,
        "params": unknown
      }[],
  }
}
```

The JSON-RPC method is labelled as `wallet_connect` and expects the following parameters:

- requests - is an OPTIONAL array of rpc requests that would be batched togehter when requesting the user for approval
  - method - the RPC method being requested
  - params - will include params specific to the RPC method

The methods that are allowed to be batched together in the `wallet_connect` request is going to be going to dependant on each ecosystem that must be defined under the [Namespaces][namespaces] repo to define a CAIP-300 profile

### Response

The wallet will prompt the user with a dedicated UI to display the app requesting the authentication and allow the user to select which blockchain account to sign with.

#### Success

If approved, the wallet will return a list of signed, valid CACAOs for each account authorized on the networks requested by the `chains` property.

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": { "result": unknown } || { error: { message: string, code: number } }[]
}
```

The JSON-RPC response will include an array of requests which are going to be ordered in the same order as provided in the request.

#### Failure

Request will fail if rejected by the user or if parameters fail validation.

The following Error responses MUST be used:

- User Rejected Request
  - code = 7000
  - message = "User Rejected Connect"
- Invalid Request Params
  - code = 7001
  - message = "Invalid Method Requested"

### Fallback Behavior

For backwards-compatibility, there is going to be a fallback behavior that is expected but is going to dependant on each ecosystem that must be defined under the [Namespaces][namespaces] repo to define a CAIP-300 profile.

If we take for example Ethereum this would fallback to the legacy behavior of `eth_requestAccounts` as defined by ERC-1102

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_connect",
  "params": {} // fallback to ERC-1102 request
}
```

Which then will result in a single item in the array that will match the response of the fallback method defined for each Namespace profile for CAIP-300.

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": [
    {
      "result": ["0xa...", "0xb..."] // fallback to ERC-1102 request
    }
  ]
}
```

## Rationale

An RPC method was choosen for batching because it enabled a smoother upgrade path for existing dapps and wallets to support it without much breaking changes. While the alternative would be to batch RPC requests at the transport layer this is not possible today without a breaking change.

Therefore batching inside a single RPC request makes it easier to re-use the existing developer tooling and will enable both dapps and wallet to progressively support this new "connection" standard for wallets.

The rationale for making this as generic as possible is to enable profiles to be defined for different ecosystems such as Bitcoin, Ethereum, Solana, Cosmos, Polkadot, etc. The alternative would be to create specific fields for "session", "authentication", "capabilities", "permissions", etc. But this would introduce a bias from one chain to another and harm chain-agnosticism.

Finally it was intentional that batch requests do not have `id` in the schema because all requests are intended to be resolved together in the same round-trip and they must be ordered as requested.

## Test Cases

Let's take for example the Ethereum ecosystem which currently has popularized the session creation with CAIP-25 being followed by an authentication request with CAIP-222. Yet there are new standards being developed where other functionality is defined pre-session and requires being batched together. This example will include capability expsosure with ERC-5792 and permission granting with ERC-7715.

```jsonc
{
  "id": 123,
  "jsonrpc": "2.0",
  "method": "wallet_connect",
  "params": {
    "requests?": [
      {
          "method": "wallet_authenticate",  // CAIP-222 request
          "params": {...}
      },
      {
          "method": "wallet_createSession", // CAIP-25 request
          "params": {...}
      },
      {
          "method": "wallet_getCapabilities", // ERC-5792 request
          "params": {...}
      },
      {
          "method": "wallet_grantPermissions", // ERC-7715 request
          "params": {...}
      },

    ]
  }
}
```

This would then resolve with an array of responses that are going to be ordered exactly as requested

```jsonc
{
  "id": 123,
  "jsonrpc": "2.0",
  "result": [
    {
        "result": {...} // CAIP-222 response
    },
    {
        "result": {...} // CAIP-25 response
    },
    {
        "result": {...} // ERC-5792 response
    },
    {
        "result": {...} // ERC-7715 response
    },

  ]
}
```

## Security Considerations

TODO

## Privacy Considerations

TODO

## Backwards Compatibility

The backwards compability is addressed by the fallback behavior therefore legacy patterns can be supported in parallel by simply replacing the method name to `wallet_connect`.

As dapps and wallets progressively support this new RPC method then they can eventually leverage the batching by including requests in the params to override the fallback behavior.

There standard can also be used in parallel where a wallet can respond both to the legacy pattern, the fallback behavior or the batching behavior in order to support dapps on different stages of the upgrade path.

## Links

- [CAIP-2][caip-2] - Blockchain ID Specification
- [CAIP-10][caip-10] - Account ID Specification
- [CAIP-25][caip-25] - Wallet Create Session RPC Method
- [CAIP-122][caip-122] - Sign in With X (SIWx)
- [CAIP-222][caip-222] - Wallet Authenticate JSON-RPC Method
- [Namespaces][namespaces]: https://namespaces.chainAgnostic.org/

[caip-2]: https://chainagnostic.org/CAIPs/caip-2
[caip-10]: https://chainagnostic.org/CAIPs/caip-10
[caip-25]: https://chainagnostic.org/CAIPs/caip-25
[caip-122]: https://chainagnostic.org/CAIPs/caip-122
[caip-222]: https://chainagnostic.org/CAIPs/caip-222
[namespaces]: https://namespaces.chainAgnostic.org/

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
