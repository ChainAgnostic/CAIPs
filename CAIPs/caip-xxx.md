---
caip: xxx
title: Browser Wallet Messaging Interface
author: Pedro Gomes (@pedrouid)
discussions-to: TODO
status: Draft
type: Standard
created: 2023-05-28
requires: 2, 10, 25, 27, 222, 275
---

## Simple Summary

Standardized messaging for wallet interface in browser environments.

## Abstract

When interfacing with decentralized applications users install wallets in their browser to manage their blockchain accounts which apps require to sign messages and transactions. Leveraging existing browser messaging APIs these are are used for messaging to initiate a dapp-wallet connection in a browser environment.

## Motivation

Currently, decentralized applications are required to install a multitude of SDKs to support many different wallet providers to broaden its userbase with different wallets of choice. However this creates a big overhead for applications to support more and more proprietrary SDKs that increase its size and laso creates vendor lockin for major wallet providers that can allocate resources to reach more applications.

Users are already installing a wallet applicationg in their devices and given its presence in the browser environment it should be unnecessary for application developers to also have to install extra software to support these wallet providers. This situation is only present due to lack of standardization and interoperability between the interfaces and also discovery mechanisms for different wallet providers.

This results not only in a degraded user experience but also increases the barrier to entry for new wallet providers as users are incentivized to use more popular wallet providers that are more widely supported in more applications.

This situation is further aggravated by differences between blockchain networks such as Ethereum, Cosmos, Solana, Tezos, etc. While some solutions amttept to solve this such as WalletConnect, EIP-6963, Solana Wallet Protocol, etc. They are not covering all wallets and are not chain-angostic.

In this proposal, we present a solution that focused on optimizing interoperability for multiple Wallet Providers and fostering fairier competition by reducing the barriers to entry for new Wallet Providers, along with enhancing the user experience across all blockchain networks.

## Specification

The key words “MUST”, “MUST NOT”, “REQUIRED”, “SHALL”, “SHALL NOT”, “SHOULD”, “SHOULD NOT”, “RECOMMENDED”, “MAY”, and “OPTIONAL” in this document are to be interpreted as described in [RFC-2119].

### Definitions

Wallet Provider: A user agent that manages accounts and facilitates transactions with a blockchain.

Decentralized Application (DApp): A web page that relies upon one or many Web3 platform APIs which are exposed to the web page via the Wallet.

Blockchain Library: A library or piece of software that assists a DApp to interact with blockchain and interface with the Wallet.

### Messaging APIs

The browser has available two APIs that can be used for messaging across different parts of the stack. Using `window.addEventListener` and `window.postMessage` enables communication between browser window, iframes and extensions.

This provides the foundation for any wallet provider to interface with a decentralized application using a blockchain library which implements this standard.

There are different loading times that can be affected by multiple factors which makes it non-deterministic to publish and listen messages from different sources within the browser.

#### Discovery

Both wallet providers and blockchain libraries must listen to incoming messages that might be published after their initialization. Additionally both wallet providers and blockchain libraries must publish a message to both announce themselves and their intent to connect, respectively.

Here is the expected logic from the blockchain library:

```js
const wallets = {};

// blockchain library starts listening on init
window.addEventListener("caipxxx:announceWallet", (event) => {
  // when an announce message was received then the library can index it by uuid
  wallets[event.data.uuid] = event.data;
});

// blockchain library publishes on init
window.postMessage("message", {
  event: "caipxxx:promptWallet",
  data: {
    //  if the blockchain library supports CAIP-275 then it can include a name in its prompt
    name: "", // optional
  },
});
```

Here is the expected logic from the wallet provider:

```js
// wallet provider sets data on init
const data = {
  uuid: "";
  name: "";
  icon: "";
  rdns: "";
}


// wallet provider publishes on init
window.postMessage("message", {
  event: "caipxxx:announceWallet",
  data,
});


// wallet providers starts listenning on init
window.addEventListener("caipxxx:promptWallet", (event) => {
  // when a prompt message was received then the wallet will announces again
  window.postMessage("message", {
    event: "caipxxx:announceWallet",
    data,
  });
});


```

Whenever a new wallet provider is discovered the blockchain library would index them in order for the decentralized application to display them and prompt the user for selecting their wallet of choice for this connection. Each wallet announced will share the following data:

```typescript
interface CAIPXXXWalletData {
  uuid: string;
  name: string;
  icon: string;
  rdns: string;
}
```

The parameters `name` and `icon` are used to display to the user to be easily recognizable while the `rdns` and `uuid` are only used internally for de-duping while they must always be unique, the `rdns` will always be the same but `uuid` is ephemeral per browser session.

#### Connection

After the wallet has been selected by the user then the blockchain library MUST publish a message to share its intent to establish a connection. This can be either done as a [CAIP-25][caip-25] request or [CAIP-222][caip-222] authentication.

The communication will use the `uuid` shared by the initial wallet provider announcement payload which the wallet provider will be listening to for any incoming requests and consequently the blockchain library will also use for publishing messages. The same will happen again the other way around but vice-versa where the wallet provider will be the blockchain library will be listening to for any incoming responses and consequently the wallet provider will also use for publishing messages.

Here is the expected logic from the blockchain library:

```js
// blockchain library listens for responses
window.addEventListener("caipxxx:respond:<wallet_provider_uuid>", (event) => {
  console.log(event.data);
});

// blockchain library publishes for requests
window.postMessage("message", {
  event: "caipxxx:request:<wallet_provider_uuid>",
  data: { name: "test" },
});
```

Here is the expected logic from the wallet provider:

```js
// wallet provider listens for request
window.addEventListener("caipxxx:request:<wallet_provider_uuid>", (event) => {
  console.log(event.data);
});

// wallet provider publishes for reponses
window.postMessage("message", {
  event: "caipxxx:respond:<wallet_provider_uuid>",
  data: { name: "test" },
});
```

#### Signing

This same channel uuid can then be used for a connected session using the [CAIP-27][caip-27] which then would use the sessionId from the established connection to identify incoming payloads that need to be respond and also which chainId is being targetted.

## Rationale

TODO

## Test Cases

TODO

## Security Considerations

TODO

## Privacy Considerations

TODO

## Backwards Compatibility

TODO

## Links

- [CAIP-2][caip-2] - Blockchain ID Specification
- [CAIP-10][caip-10] - Account ID Specification
- [CAIP-27][caip-27] - Blockchain ID Specification
- [CAIP-25][caip-25] - Blockchain ID Specification
- [CAIP-222][caip-222] - Account ID Specification

[caip-2]: https://chainagnostic.org/CAIPs/caip-2
[caip-10]: https://chainagnostic.org/CAIPs/caip-10
[caip-27]: https://chainagnostic.org/CAIPs/caip-27
[caip-25]: https://chainagnostic.org/CAIPs/caip-25
[caip-222]: https://chainagnostic.org/CAIPs/caip-222
[caip-275]: https://chainagnostic.org/CAIPs/caip-275

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
