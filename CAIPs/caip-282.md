---
caip: 282
title: Browser Wallet Messaging Interface
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/282
status: Draft
type: Standard
created: 2024-05-30
requires: 25, 27, 217, 222, 275
---

## Simple Summary

CAIP-282 defines a standardized messaging interface for browser wallets.

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

### Messaging APIs

The browser exposes two APIs that can be used for messaging across different parts of the stack. Using `window.addEventListener` and `window.postMessage` enables communication between browser window, iframes and extensions.

This provides the foundation for any Wallet Provider to interface with a Decentralized Application using a Blockchain Library which implements this standard.

Different loading times can be affected by multiple factors, which makes it non-deterministic to publish and listen to messages from different sources within the browser.

#### Discovery

Both Wallet Providers and blockchain libraries must listen to incoming messages that might be published after their initialization. Additionally both Wallet Providers and blockchain libraries must publish a message to both announce themselves and their intent to connect, respectively.

Here is the expected logic from the Blockchain Library:

```js
const wallets = {};

// Blockchain Library starts listening on init
window.addEventListener("message", (event) => {
  if (event.data.method === 'wallet_announce') {
    // when an announce message was received then the library can index it by uuid
    wallets[event.data.params.uuid] = event.data.params
  }
});

// Blockchain Library publishes on init
window.postMessage({
  id: 1,
  jsonrpc: "2.0"
  method: "wallet_prompt",
  params: {
    // optionally the Blockchain Library can prompt wallets to announce matching only the chains
    chains: []  // optional
    //  if the Blockchain Library supports CAIP-275 then it can include a name
    authName: "", // optional
  },
});
```

Here is the expected logic from the Wallet Provider:

```js
// Wallet Provider sets data on init
const walletData = {
  uuid: "";
  name: "";
  icon: "";
  rdns: "";
}


// Wallet Provider publishes on init
window.postMessage({
  id: 2,
  jsonrpc: "2.0"
  method: "wallet_announce",
  params: data
});


// Wallet Providers starts listenning on init
window.addEventListener("message", (event) => {
  if (event.data.method === 'wallet_prompt') {
    // when a prompt message was received then the wallet will announces again
    window.postMessage({
      id: 2,
      jsonrpc: "2.0"
      method: "wallet_announce",
      params: data
    });
  }
});
```

#### WalletData

Whenever a new Wallet Provider is discovered the Blockchain Library would index them in order for the Decentralized Application to display them and prompt the user for selecting their wallet of choice for this connection. Each wallet announced will share the following data:

```typescript
interface WalletData {
  uuid: string;
  name: string;
  icon: string;
  rdns: string;
  scps?: AuthorizationScopes;
}
```

The parameters `name` and `icon` are used to display to the user to be easily recognizable while the `rdns` and `uuid` are only used internally for de-duping while they must always be unique, the `rdns` will always be the same but `uuid` is ephemeral per browser session.

The only optional parameter is `scps` which is defined by CAIP-217 authorization specs that enables early discoverability and filtering of wallets based on RPC methods, notifications, documents and endpoints but also optional discovery of supported chains and even accounts.

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

#### Handshake

After the wallet has been selected by the user then the Blockchain Library MUST publish a message to share its intent to establish a connection. This can be either done as a [CAIP-25][caip-25] request or [CAIP-222][caip-222] authentication.

The communication will use the `uuid` shared by the initial Wallet Provider announcement payload, which the Wallet Provider will listen to for any incoming requests, and consequently, the Blockchain Library will also be used for publishing messages. The same will happen again the other way around but vice-versa, where the Wallet Provider will be the Blockchain Library that will be listening to any incoming responses, and consequently, the Wallet Provider will also use it for publishing messages.

Here is the expected logic from the Blockchain Library:

```js
// Blockchain Library creates a JSON-RPC request
const request = {
  id: 123,
  jsonrpc: "2.0",
  method: "wallet_request",
  params: {
    // UUID from WalletData is used as SessionId
    sessionId: walletData.uuid,
    scope: "chain:777",
    request: {
      method: "chain_signMessage",
      params: [
        "Verifying my wallet with this message",
        "0xa89Df33a6f26c29ea23A9Ff582E865C03132b140",
      ],
    },
  },
};

// Blockchain Library listens for responses
window.addEventListener("message", (event) => {
  if (event.data.id === request.id) {
    // Get JSON-RPC response
    if (event.data.error) {
      console.error(event.data.error.message);
    } else {
      console.log(event.data.result);
    }
  }
});

// Blockchain Library publishes for requests
window.postMessage(request);
```

Here is the expected logic from the Wallet Provider:

```js
// Wallet Provider listens for request
window.addEventListener("message", (event) => {
  if (event.data.method === "wallet_request") {
    // if incoming requests match the WalletData UUID
    if (event.data.params.sessionId === walletData.uuid) {
        console.log(event.data);
    }
  }
});

const response = {
  id: event.data.id,
  jsonrpc: "2.0",
  result: { ... }
}

// Wallet Provider publishes for reponses
window.postMessage("message", response);
```

#### Signing

This same channel `uuid` can then be used for a connected session using [CAIP-27][caip-27] which then would use the `sessionId` from the established connection to identify incoming payloads that need to be respond to, and also which `chainId` is being targetted.

#### UUIDs

The generation of UUIDs is crucial for this messaging interface to work seamlessly for the users.

A Wallet Provider MUST always generate UUIDs distinctly for each web page loaded, and they must not be re-used without a session being established between the application and the wallet with the user's consent.

A UUID can be re-used as a `sessionId` if and only if the [CAIP-25][caip-25] or [CAIP-222][caip-222] procedure has been prompted to the user and the user has approved its permissions to allow the application to make future signing requests.

Once established, the UUID is used as `sessionId` for the [CAIP-27][caip-27] payloads, which can verify that incoming messages are being routed through the appropriate channels.

## Rationale

Browser wallets differentiate themselves because they can be installed by users without the application developer requiring any further integration. Therefore, we optimize for a messaging interface that leverages the two-way communication available to browser wallets to make themselves discoverable, and negotiate a set of parameters that enable not only easy human readability with a clear name and icon but also machine-readability using strong identifiers with uuids and rdns.

The choice for using `window.postMessage` is motivated by expanding the range of Wallet Providers it can support, including browser extensions that can alternatively use `window.dispatchEvent` but instead it would also cover Inline Frames, Service Workers, Shared Workers, and more.

The use of UUID for message routing is important because while RDNS is useful for identifying the Wallet Provider, it causes issues when it comes to the session management of different webpages connected to the same Wallet Provider or even managing stale sessions, which can be out-of-sync. Since UUID generation is derived dynamically on page load, Wallet Providers can track these sessions more granularly rather than making assumptions around the webpage URL and RDNS relationship.

The existing standards around provider authorization (CAIP-25) and wallet authentication (CAIP-222) are fundamental to this experience because they create clear intents for a wallet to "connect" with a webpage url after it's been discovered. This standard does not enforce either one but strongly recommends these standards as the preferred interface for connecting or authenticating a wallet.

Finally the use of CAIP-27 leverages the work above to properly target signing requests that are intended to be prompt to wallet users which will include a `sessionId` and `chainId` in parallel with the pre-established sessions using either CAIP-25 or CAIP-222

## Test Cases

Here is a test case where we demonstrate a scenario with logic from both a Blockchain Library and a Wallet Provider.

Logic from the Blockchain Library:

```js
// 1. Blockchain Library initializes by listening to announceWallet messages and
// also by posting a prompt message
const wallets = {};

window.addEventListener("message", (event) => {
  if (event.data.method === 'wallet_announce') {
    // when an announce message was received then the library can index it by uuid
    wallets[event.data.params.uuid] = event.data.params
  }
});

window.postMessage({
  id: 1,
  jsonrpc: "2.0"
  method: "wallet_prompt",
  params: {},
});

// 2. User presses "Connect Wallet" and the library display the discovered wallets

// 3. User selects a Wallet with UUID = "350670db-19fa-4704-a166-e52e178b59d2" and
// Blockchain Library will send a CAIP-25 request to establish a wallet connection

const request = {
  id: 123,
  jsonrpc: "2.0",
  method: "wallet_createSession",
  params: {
    optionalScopes: {
      eip155: {
        scopes: ["chain:777"],
        methods: ["chain_signMessage", "chain_sendTransaction"],
        notifications: ["accountsChanged"],
      },
    },
    sessionProperties: {
      expiry: "2024-06-06T13:10:48.155Z",
    },
  },
};

let session = {}

window.addEventListener("message", (event) => {
  if (event.data.id === request.id) {
    // Get JSON-RPC response
    if (event.data.error) {
      console.error(event.data.error.message);
    } else {
      console.log(event.data.result);
    }
  }
});

window.postMessage(request);

let session = {};
window.addEventListener("message", "caip282:respond:350670db-19fa-4704-a166-e52e178b59d2", (event) => {
  if (event.data.error) throw new Error(event.data.error.message);
  session = event.data.result;
});
window.postMessage("message", {
  event: "caip282:request:350670db-19fa-4704-a166-e52e178b59d2",
  data: {
    id: 1,
    jsonrpc: "2.0",
    method: "provider_authorize",
    params: {
      optionalScopes: {
        eip155: {
          scopes: ["eip155:1", "eip155:10"],
          methods: ["eth_sendTransaction", "personal_sign"],
          notifications: ["accountsChanged", "chainChanged"],
        },
      },
      sessionProperties: {
        expiry: "2024-06-06T13:10:48.155Z",
      },
    },
  },
});

// 4. After the response was received by the Blockchain Library from the wallet
// provider then the session is established with a sessionId matchin the UUID
// thus signing requests can be using a CAIP-27 request to the wallet user
let result = {};
window.addEventListener("message", "caip282:respond:350670db-19fa-4704-a166-e52e178b59d2", (event) => {
  if (event.data.error) throw new Error(event.data.error.message);
  result = event.data.result;
});
window.postMessage("message", {
  event: "caip282:request:350670db-19fa-4704-a166-e52e178b59d2",
  data: {
    {
      id: 2,
      jsonrpc: "2.0",
      method: "provider_request",
      params: {
        sessionId: "350670db-19fa-4704-a166-e52e178b59d2",
        scope: "eip155:10",
        request: {
          method: "eth_sendTransaction",
          params: [
            {
              type: "0x2",
              nonce: "0x01",
              value: "0x00",
              maxFeePerGas: "0x9143798a4",
              maxPriorityFeePerGas: "0x59682f00",
              from: "0x43e3ca49c7be4f429abce408da6b738f879d02a0",
              to: "0x0b2C639c533813f4Aa9D7837CAf62653d097Ff85",
              data: "0xa9059cbb000000000000000000000000677d6d2747955ecf1e9fad3521d29512fb599e7b0000000000000000000000000000000000000000000000000de0b6b3a7640000",
            }
          ]
        }
      }
    }
  },
});

```

Logic from the Wallet Provider:

```js
// 1. Wallet Provider sets their WalletData and then listens to promptWallet message
// and also immediatelly posts a message with the WalletData as announceWallet type
const walletData = {
  uuid: generateUUID(); // eg. "350670db-19fa-4704-a166-e52e178b59d2"
  name: "Example Wallet",
  icon: "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==",
  rdns: "com.example.wallet",
}
window.addEventListener("message", "caip282:promptWallet", (event) => {
  // when a prompt message was received then the wallet will announces again
  window.postMessage("message", {
    event: "caip282:announceWallet",
    data,
  });
});
window.postMessage("message", {
  event: "caip282:announceWallet",
  data,
});

// 2. User presses "Connect Wallet" on the application webpage which will select UUID

// 3. Wallet Provider receives a CAIP-25 request to establish a wallet connection
// prompts the user to approve and once its approved it can respond back to app
// Wallet Provider listens for request
const request = {}
window.addEventListener("message", "caip282:request:350670db-19fa-4704-a166-e52e178b59d2", (event) => {
  request = event.data
});
// Wallet Provider publishes for reponses
window.postMessage("message", {
  event: "caip282:respond:350670db-19fa-4704-a166-e52e178b59d2",
  data: {
    id: request.id, // 1
    jsonrpc: "2.0",
    result: {
      sessionId: "350670db-19fa-4704-a166-e52e178b59d2",
      sessionScopes: {
        eip155: {
          scopes: ["eip155:1", "eip155:10"],
          methods: ["eth_sendTransaction", "personal_sign"],
          notifications: ["accountsChanged", "chainChanged"],
          accounts: [
            "eip155:1:0x43e3ca49c7be4f429abce408da6b738f879d02a0",
            "eip155:10:0x43e3ca49c7be4f429abce408da6b738f879d02a0"
          ]
        },
      },
      sessionProperties: {
        expiry: "2024-06-06T13:10:48.155Z",
      }
    }
  },
});

// 4. Once the connection is established then the Wallet Provider can receive
// incoming CAIP-27 requests which will be prompted to the user to sign and
// once signed the response is sent back to the dapp with the expected result
const request = {}
window.addEventListener("message", "caip282:request:350670db-19fa-4704-a166-e52e178b59d2", (event) => {
  request = event.data
});
// Wallet Provider publishes for reponses
window.postMessage("message", {
  event: "caip282:respond:350670db-19fa-4704-a166-e52e178b59d2",
  data: {
    id: request.id, // 2
    jsonrpc: "2.0",
    result: "0xe670ec64341771606e55d6b4ca35a1a6b75ee3d5145a99d05921026d1527331"
  },
});
```

## Security Considerations

The advantage of using `window.postMessage` over existing standards that leverage `window.dispatchEvent` is the prevention of prototype pollution, but that still does not mean that there aren't existing attacks that must be considered:

### Wallet Imitation and Manipulation

Application developers are expected to actively detect for misbehavior of properties or functions being modified in order to tamper with or modify other wallets. One way this can be easily achieved is to look for when the uuid property within two WalletData objects match. Applications and Libraries are expected to consider other potential methods that the WalletData objects are being tampered with and consider additional mitigation techniques to prevent this as well in order to protect the user.

### Prevent SVG Javascript Execution

The use of SVG images introduces a cross-site scripting risk as they can include JavaScript code. This JavaScript executes within the context of the page and can modify the page or the contents of the page. So, when considering the experience of rendering the icons, dapps need to take into consideration how they’ll approach handling these concerns in order to prevent an image from being used as an obfuscation technique to hide malicious modifications to the page or to other wallets.

## Privacy Considerations

Any form of wallet discoverability must alwasys take in consideration wallet fingerprinting that can happen by malicious webpages or extensions that attempt to capture user information. Wallet Providers can abstain from publishing `announceWallet` messages on every page load and wait for incoming `promptWallet` messages. Yet this opens the possibility for race conditions where Wallet Providers could be initialized after the `promptWallet` message was published and therefore be undiscoverable. It is recommended that Wallet Providers offer this more "private connect" feature that users only enable optionally, rather than set by default.

## Backwards Compatibility

It's important to note that existing blockchain ecosystems already have standards that overlap with the scope of this standard and backwards-compatibility must be considered for a smooth adoption by both wallet and application developers.

For the EIP155 (Ethereum) ecosystem there are already interfaces for the discoverability of browser wallets through either legacy `window.ethereum` or [EIP-6963][eip-6963] events. These existing mechanisms should be supported in parallel in order to receive incoming requests either through this new messaging interface or legacy ones.

Similarly the Solana and BIP122 (Bitcoin) ecosystems have used similar patterns around `window.solana` and `window.bitcoin` respectively plus the wallet-standard events. Yet these can also be supported in parallel without conflict with this new messaging interface.

The WalletData exposed in this messaging interface is also compatible with EIP-6963 events and wallet-standard events therefore Wallet Providers can re-use the same identifiers and assets already being used in these existing integrations.

## Links

- [EIP-6963][eip-6963] - Multi Injected Provider Discovery
- [CAIP-27][caip-27] - Blockchain ID Specification
- [CAIP-25][caip-25] - Blockchain ID Specification
- [CAIP-217][caip-217] - Provider Authorization Scopes
- [CAIP-222][caip-222] - Account ID Specification
- [CAIP-275][caip-275] - Domain Wallet Authentication

[eip-6963]: https://eips.ethereum.org/EIPS/eip-6963
[caip-27]: https://chainagnostic.org/CAIPs/caip-27
[caip-25]: https://chainagnostic.org/CAIPs/caip-25
[caip-217]: https://chainagnostic.org/CAIPs/caip-217
[caip-222]: https://chainagnostic.org/CAIPs/caip-222
[caip-275]: https://chainagnostic.org/CAIPs/caip-275

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
