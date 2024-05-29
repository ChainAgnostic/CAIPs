### Chain Agnostic Improvement Proposal (CAIP) for Wallet-to-DApp Communication Standard

#### Simple Summary

This CAIP aims to standardize the method by which extension-based wallets can announce their canonical extension ID to dApps. The goal is to enable dApps to communicate with wallets using the `externally_connectable` API in browsers, facilitating a secure and standardized interaction pattern across different blockchain platforms. This proposal builds upon EIP-6963, adapting its framework to the unique requirements of browser extension-based wallets and the external communication APIs provided by browsers.

### Motivation

Given the benefits of `externally_connectable` as described in CAIP-X (TODO ADD LINK HERE WHEN AVAILABLE), we need to solve for a key area where it fails to match the injected provider pattern. `Externally_connectable` requires that the webpage attempting to connect with the wallet know the extension's canonical extension ID. Currently, there is no standard method for these wallets to provide their extension IDs to dApps. A standardized approach will improve interoperability, security, and user experience by ensuring that dApps can reliably identify and communicate with the intended wallet.

Ideally, browsers would eventually provide an API by which wallets can register the capabilities that they offer which webpages could access and choose to communicate with in a way that avoids the need for this additional discovery step.

#### Why not a centralized registry?

Using a centralized repository to manage wallet extension IDs creates potential security vulnerabilities, a central point of failure and a another layer of central authority to the wallet stack which we ought to avoid. By allowing wallets to directly announce their IDs to dApps, we eliminate reliance on a single source, enhancing security and adhering to blockchain's decentralization principles. This method minimizes risks such as data tampering and spoofing, ensuring safer and more reliable dApp interactions.

### Specification

**Provider Info**:

Each wallet provider will be announced with the following interface. The values in the `ProviderInfo` object must be included. The `ProviderInfo` object may also include additional properties. If a dApp does not recognize these properties, it should ignore them.

- **extensionId**: The canonical extension ID of the wallet provider for the active browser. This value is used by dApps to establish a connection with the wallet using the `externally_connectable` API.
- **name**: A human-readable alias of the Wallet Provider to be displayed to the user on the dApp (e.g., "Example Wallet Extension" or "Awesome Example Wallet").
- **icon**: A URI pointing to an image, recommended to be a square with a minimum resolution of 96x96px.
- **rdns**: A reverse DNS domain name identifying the wallet provider (e.g., `com.example.wallet`). This value should be stable throughout the development of the wallet.

```typescript
interface ProviderInfo {
  extensionId: string;
  name: string;
  icon: string;
  rdns: string;
}
```

**Extension ID Announcement Protocol**:

Each wallet extension must implement a mechanism to announce its extension ID along with the provider info to a dApp. This should be done using a JavaScript event emitted by the wallet and listened for by the dApp. The event should contain the `ProviderInfo` object as its payload.

**Modification to dApp Listening Mechanisms**:

dApps must modify their initialization routines to listen for the wallet's `ProviderInfo` announcement event. Upon capturing the event, the dApp can store the extension ID and use it to establish a connection using the `externally_connectable` API.


**Request Provider Mechanism**:

To ensure dApps can discover wallets even if they start listening after wallets have emitted their initial events, a request mechanism is essential.

- **Active Request Capability**: Wallets must listen for a specific event from dApps that requests the announcement of available wallet providers. Upon receiving this event, wallets should re-announce their presence.

- **Event Handling**: Wallets should listen for a `requestWalletExtensionId` event and respond by re-dispatching their provider information using the `walletExtensionIdAnnouncement` event. This ensures that any dApp can discover all available wallets, regardless of the script execution order.

### Example Implementation

- **Wallets**:

```javascript
function announceExtensionId() {
  const providerInfo = {
    name: 'Example Wallet',
    icon: 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg"/>',
    rdns: 'com.example.wallet',
    extensionId: 'your_extension_id_here'
  };
  document.dispatchEvent(new CustomEvent('walletExtensionIdAnnouncement', {
    detail: Object.freeze(providerInfo)
  }));
}

announceExtensionId();

window.addEventListener('requestWalletExtensionId', () => {
  announceExtensionId();
});
```

- **dApps**:

```javascript
window.addEventListener('walletExtensionIdAnnouncement', function(e) {
  const providerInfo = e.detail;
  // Use providerInfo.extensionId to establish communication
});

window.dispatchEvent(new Event("requestWalletExtensionId"));
```

**Content Script Utilization**:
While this CAIP significantly advances the way wallets interact with dApps through the `externally_connectable` API, it still necessitates the use of a content script. This script, however, requires fewer permissions compared to traditional methods and can be injected synchronously to facilitate the initial communication process. This adjustment is necessary to bridge the current browser limitations and set the stage for future enhancements.

- **Synchronous Injection**: The content script can be injected synchronously with the page load, ensuring that the wallet's extension ID is available as soon as the dApp begins its initialization process. This method avoids the delays and potential conflicts associated with asynchronous script loading, leading to a smoother and more predictable interaction pattern.
    
- **Security and Efficiency**: Although using a content script still presents a minimal footprint on the user's browsing experience, this approach is a compromise that balances current browser capabilities with the need for enhanced security and performance. By minimizing the script's functionality to only what is necessary for ID transmission, the risks associated with broader access permissions are mitigated.

**Browser Manifest Adjustments**:

Wallets can configure the `externally_connectable` section of their browser extension manifest to specify which domains are allowed to communicate with the extension. To avoid restricting communication to a pre-defined list of sites, wallets may use wildcards (e.g., `*.example.com`).

**Backward Compatibility**:

This protocol should be implemented in such a way that it does not interfere with existing implementations of EIP-1193 providers. Wallets may need to support both the new extension ID-based communication protocol and the traditional EIP-1193 provider interface.

#### Conclusion

This proposal enhances how decentralized applications interact with browser-based wallets by standardizing the announcement of wallet extension IDs using the `externally_connectable` API. This approach mitigates security risks associated with broad permissions and fingerprinting, and improves performance by eliminating blocking scripts. The proposal provides a detailed implementation guide for both wallets and dApps, ensuring compatibility and ease of integration. By adopting this CAIP, the blockchain community moves towards a more secure, efficient, and user-friendly ecosystem, fostering greater interoperability and adoption across various blockchain platforms.


#### References
- EIP-6963: https://eips.ethereum.org/EIPS/eip-6963