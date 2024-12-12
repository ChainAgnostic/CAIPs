---
caip: X
title: Extension ID Target Type Specification
author: [Joao Tavares] (@ffmcgee725)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/X
status: Draft
type: Standard
created: 2024-12-12
requires: 294
---

## Simple Summary

CAIP-X defines the `extensionId` type as a valid target type for establishing connections with browser extension wallets.

## Abstract

This proposal introduces a new target type `extensionId` for the `target` field in the `walletData` interface. This target type is used to specify the extension ID of a browser extension wallet, allowing Decentralized Applications (dapps) to establish connections with the wallet using the `externally_connectable` API.

## Motivation

Currently, Decentralized Applications need to support different messaging standards for each namespace, such as Ethereum's [EIP-6963](https://eips.ethereum.org/EIPS/eip-6963), Solana Wallet Protocol, etc. This fragmentation increases complexity and reduces interoperability. By defining a standardized target type for browser extension wallets, we aim to simplify the connection process and enhance interoperability across different blockchain ecosystems.

## Specification

The key words “MUST”, “MUST NOT”, “REQUIRED”, “SHALL”, “SHALL NOT”, “SHOULD”, “SHOULD NOT”, “RECOMMENDED”, “MAY”, and “OPTIONAL” in this document are to be interpreted as described in [RFC-2119](https://www.rfc-editor.org/rfc/rfc2119).

### Definitions

Wallet Provider: A user agent that manages accounts and facilitates transactions with a blockchain.

Decentralized Application (dapp): A web page that relies upon one or many Web3 platform APIs which are exposed to the web page via the Wallet.

Blockchain Library: A library or piece of software that assists a dapp to interact with a blockchain and interface with the Wallet.

### Target Type

The `target` field in the `walletData` interface is used to specify the connection method for the wallet. This CAIP introduces the `extensionId` type as a valid target type.

This field MAY be included in the `walletData`, and if included, SHOULD be an object containing `extensionId` type used to connect to wallets using `externally_connectable`.

```typescript
interface WalletData {
  // Required properties
  uuid: string;
  name: string;
  icon: string;
  rdns: string;
  // Optional properties
  target?: {
    type: <caip-id-for-extension-id>,
    value: <extension_id>
  }
  scopes?: Caip217AuthorizationScopes;
}
```

### Usage

The `extensionId` target type is used to specify the extension ID of a browser extension wallet. This allows the dapp to establish a connection with the wallet using the `externally_connectable` API. The `externally_connectable` API documentation can be found [here](https://developer.chrome.com/docs/extensions/reference/manifest/externally-connectable).

```ts
const walletData = {
  uuid: "350670db-19fa-4704-a166-e52e178b59d2",
  name: "Example Wallet",
  icon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==",
  rdns: "com.example.wallet",
  target: {
    type: "caip-x",
    value: "abcdefghijklmnopqrstuvwxyz"
  },
  scopes: {
    "eip155:1": {
      methods: ["eth_signTransaction", "eth_sendTransaction"],
      notifications: ["accountsChanged", "chainChanged"]
    }
  }
}
```

### Establishing Connection

When the target type is `extensionId`, the dapp MUST use the `extensionId` to establish a connection with the wallet using the `externally_connectable` browser API. All subsequent communication with the wallet SHOULD be conducted over the `externally_connectable` API using `runtime.connect()` and `runtime.sendMessage()`.

Example of establishing a connection and sending a message:

```ts
const port = chrome.runtime.connect(walletData.target.value);

port.onMessage.addListener((message) => {
  // Handle incoming messages
});

port.postMessage({
  id: 1,
  jsonrpc: "2.0",
  method: "wallet_createSession",
  params: {
    // ... session parameters ...
  }
});
```

## Rationale

By defining the `extensionId` target type, we provide a standardized way for dapps to connect with browser extension wallets. This reduces complexity and enhances interoperability across different blockchain ecosystems. The use of the `externally_connectable` API ensures secure and efficient communication between the dapp and the wallet.

## Backwards Compatibility

This CAIP is fully compatible with existing standards and does not introduce any breaking changes. It extends the `walletData` interface to include the `target` field, which is optional and does not affect existing implementations.

## Links

- [EIP-6963](https://eips.ethereum.org/EIPS/eip-6963) - Multi Injected Provider Discovery
- [CAIP-294](https://chainagnostic.org/CAIPs/caip-294) - Browser Wallet Messaging for Extensions
- [externally_connectable API documentation](https://developer.chrome.com/docs/extensions/reference/manifest/externally-connectable)

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
