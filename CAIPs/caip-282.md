---
caip: 282
title: Browser Wallet Discovery Interface
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/282
status: Draft
type: Standard
created: 2024-05-30
requires: 25, 27, 217, 275
---

## Simple Summary

CAIP-282 defines a standardized interface for browser wallet discovery

## Abstract

To interface with a Decentralized Application (dapp), users install browser wallets to manage their blockchain accounts, which are required to sign messages and transactions. Leveraging existing browser messaging APIs, these are used to initiate a dapp-wallet connection in a browser environment.

## Motivation

Currently, in order for Decentralized Applications to be able to support all users they need to support all browser wallet APIs. Similarly, in order for browser wallets to support all Decentralized Applications they need to support all APIs. This is not only complicated but also results in a larger bundle size of applications.

Users are already installing a wallet application on their devices, and given its presence in the browser environment, it should be unnecessary for application developers to install extra software to support these Wallet Providers. This situation is only present due to the lack of standardization and interoperability between interfaces and discovery mechanisms for different Wallet Providers.

This results not only in a degraded user experience but also increases the barrier to entry for new Wallet Providers as users are incentivized to use more popular Wallet Providers that are more widely supported in more applications.

This situation is further aggravated by differences between blockchain networks such as Ethereum, Cosmos, Solana, Tezos, etc. While some solutions attempt to solve this, such as WalletConnect, [EIP-6963][eip-6963], Solana Wallet Protocol, etc., they do not cover all wallets and are not chain-angostic.

In this proposal, we present a solution focused on optimizing interoperability for multiple Wallet Providers, fostering fairer competition by reducing the barriers to entry for new Wallet Providers, and enhancing user experience across all blockchain networks.

## Specification

The key words “MUST”, “MUST NOT”, “REQUIRED”, “SHALL”, “SHALL NOT”, “SHOULD”, “SHOULD NOT”, “RECOMMENDED”, “MAY”, and “OPTIONAL” in this document are to be interpreted as described in [RFC-2119].

### Definitions

Wallet Provider: A user agent that manages accounts and facilitates transactions with a blockchain.

Decentralized Application (dapp): A web page that relies upon one or many Web3 platform APIs which are exposed to the web page via the Wallet.

Blockchain Library: A library or piece of software that assists a dapp to interact with a blockchain and interface with the Wallet.

#### Discovery

Both Wallet Providers and blockchain libraries MUST listen to incoming messages that might be published after their initialization. Additionally both Wallet Providers and blockchain libraries MUST publish a message to both announce themselves and their intent to connect, respectively.

This discovery would use the following JSON-RPC request params:

```typescript
// for "wallet_prompt" method
interface WalletPromptRequestParams {
  chains?: string[]; // compatible with CAIP-2
  authName?: string; // compatible with CAIP-275
}

// for "wallet_announce" method
interface WalletAnnounceRequestParams {
  uuid: string;
  name: string;
  icon: string;
  rdns: string;
  target?: { type: string, value: any }[],
  scopes?: AuthorizationScopes;
}
```

Whenever a new Wallet Provider is discovered the Blockchain Library would index them in order for the Decentralized Application to display them and prompt the user for selecting their wallet of choice for this connection.

The parameters `name` and `icon` are used to display to the user to be easily recognizable while the `rdns` and `uuid` are only used internally for de-duping while they must always be unique, the `rdns` will always be the same but `uuid` is ephemeral per browser session.

The optional parameters are `scopes`, which is defined by [CAIP-217] authorization specs that enables early discoverability and filtering of wallets based on RPC methods, notifications, documents and endpoints but also optional discovery of supported chains and even accounts, and `target`, which accepts [CAIP-341] Extension ID as a valid target type for establishing connections with browser extension wallets via the [CAIP-294] `wallet_announce` wallet discovery event.

```typescript
// Defined by CAIP-217
interface AuthorizationScopes {
  [scopeString: string]: {
    scopes?: string[];
    methods: string[];
    notifications: string[];
    accounts?: string[];
    rpcDocuments?: string[];
    rpcEndpoints?: string[];
  };
}
```

#### UUIDs

The generation of UUIDs is crucial for this messaging interface to work seamlessly for the users.

A Wallet Provider MUST always generate UUIDs distinctly for each web page loaded, and they must not be re-used without a session being established between the application and the wallet with the user's consent.

A UUID can be re-used as a `sessionId` if and only if the [CAIP-25][caip-25] procedure has been prompted to the user and the user has approved its permissions to allow the application to make future signing requests.

Once established, the UUID is used as `sessionId` for the [CAIP-27][caip-27] payloads, which can verify that incoming messages are being routed through the appropriate channels.

## Rationale

Browser wallets differentiate themselves because they can be installed by users without the application developer requiring any further integration. Therefore, we optimize for a messaging interface that leverages the two-way communication available to browser wallets to make themselves discoverable, and negotiate a set of parameters that enable not only easy human readability with a clear name and icon but also machine-readability using strong identifiers with uuids and rdns.

The choice for using `window.postMessage` is motivated by expanding the range of Wallet Providers it can support, including browser extensions that can alternatively use `window.dispatchEvent` but instead it would also cover Inline Frames, Service Workers, Shared Workers, and more.

The use of UUID for message routing is important because while RDNS is useful for identifying the Wallet Provider, it causes issues when it comes to the session management of different webpages connected to the same Wallet Provider or even managing stale sessions, which can be out-of-sync. Since UUID generation is derived dynamically on page load, Wallet Providers can track these sessions more granularly rather than making assumptions around the webpage URL and RDNS relationship.

The existing standards around wallet session creation (CAIP-25) are fundamental to this experience because they create clear intents for a wallet to "connect" with a webpage url after it's been discovered. This standard does not enforce either one but strongly recommends these standards as the preferred interface for connecting or authenticating a wallet.

Finally the use of CAIP-27 leverages the work above to properly target signing requests that are intended to be prompt to wallet users which will include a `sessionId` and `chainId` in parallel with the pre-established sessions using either CAIP-25.

## Test Cases

Here are some illustrative examples for both JSON-RPC request params:

```typescript
// Example for wallet_prompt
{
  id: 1,
  jsonrpc: "2.0"
  method: "wallet_prompt",
  params: {
    chains: ["chain:777", "chain:888", "chain:999"]  // optional
    //  if the Blockchain Library supports CAIP-275 then it can include a name
    authName: "johndoe.chain", // optional
  }
}


// Example for wallet_announce
{
  id: 2,
  jsonrpc: "2.0"
  method: "wallet_announce",
  params: {
    uuid:  "350670db-19fa-4704-a166-e52e178b59d2",
    name: "Example Wallet",
    icon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==",
    rdns: "com.example.wallet",
  }
}
```

## Security Considerations

Regarding security considerations it's important to consider that the WalletData can be imitated and/or manipulated by cross-site scripting and additionally we must consider the following:

### Wallet Imitation and Manipulation

Application developers are expected to actively detect for misbehavior of properties or functions being modified in order to tamper with or modify other wallets. One way this can be easily achieved is to look for when the uuid property within two WalletData objects match. Applications and Libraries are expected to consider other potential methods that the WalletData objects are being tampered with and consider additional mitigation techniques to prevent this as well in order to protect the user.

### Prevent SVG Javascript Execution

The use of SVG images introduces a cross-site scripting risk as they can include JavaScript code. This JavaScript executes within the context of the page and can modify the page or the contents of the page. So, when considering the experience of rendering the icons, dapps need to take into consideration how they’ll approach handling these concerns in order to prevent an image from being used as an obfuscation technique to hide malicious modifications to the page or to other wallets.

## Privacy Considerations

Any form of wallet discoverability must always take into consideration wallet fingerprinting that can happen by malicious webpages or extensions that attempt to capture user information. Wallet Providers can abstain from publishing "Announce" messages on every page load and wait for incoming "Prompt" messages. Yet this opens the possibility for race conditions where Wallet Providers could be initialized after the "Prompt" message was published and therefore be undiscoverable. It is recommended that Wallet Providers offer this more "private connect" feature that users only enable optionally, rather than set by default.

## Backwards Compatibility

It's important to note that existing blockchain ecosystems already have standards that overlap with the scope of this standard and backwards-compatibility must be considered for a smooth adoption by both wallet and application developers.

For the EIP155 (Ethereum) ecosystem there are already interfaces for the discoverability of browser wallets through either legacy `window.ethereum` or [EIP-6963][eip-6963] events. These existing mechanisms should be supported in parallel without conflict with this new discovery interface.

Similarly the Solana and BIP122 (Bitcoin) ecosystems have used similar patterns around `window.solana` and `window.bitcoin` respectively plus the wallet-standard events. Yet these can also be supported in parallel without conflict with this new discovery interface.

The WalletData exposed in this messaging interface is also compatible with EIP-6963 and wallet-standard interfaces therefore Wallet Providers can re-use the same identifiers and assets already being used in these existing integrations.

## Links

- [EIP-6963][eip-6963] - Multi Injected Provider Discovery
- [CAIP-27][caip-27] - Blockchain ID Specification
- [CAIP-25][caip-25] - Blockchain ID Specification
- [CAIP-217][caip-217] - Provider Authorization Scopes
- [CAIP-275][caip-275] - Domain Wallet Authentication

[eip-6963]: https://eips.ethereum.org/EIPS/eip-6963
[caip-27]: https://chainagnostic.org/CAIPs/caip-27
[caip-25]: https://chainagnostic.org/CAIPs/caip-25
[caip-217]: https://chainagnostic.org/CAIPs/caip-217
[caip-275]: https://chainagnostic.org/CAIPs/caip-275

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
