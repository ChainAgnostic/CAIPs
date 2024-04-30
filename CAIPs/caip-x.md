
### Chain Agnostic Improvement Proposal (CAIP) for Wallet-to-DApp Communication Standard

  

#### Abstract

This CAIP aims to standardize the method by which extension based wallets can announce their canonical extension ID to decentralized applications (dApps). The goal is to enable dApps to communicate with wallets using the `externally_connectable` API in browsers, facilitating a secure and standardized interaction pattern across different blockchain platforms. This proposal builds upon EIP-6963, adapting its framework to the unique requirements of browser extension-based wallets and the external communication APIs provided by browsers.

### Motivation

First specifically why switch from an injected provider to `externally_connectable`

#### Enhanced Security Benefits

 **Reduced Permissions Requirement**: Utilizing the `externally_connectable` API allows wallet extensions communicate with websites, without requiring broad permissions to read and modify all data on the websites a user visits as is currently the case with the typical injected provider used by most extension Wallets. For instance the `externally_connectable` method does not allow modification of web page content. This reduces the level of trust and permission users must grant to the extension, minimizing potential security risks if the extension or its updates are compromised.
    
**Reduced Fingerprinting Risk**: The traditional method of injecting a JavaScript provider into every webpage a user visits exposes the user to increased fingerprinting risks. Each script injection can potentially be detected by malicious websites aiming to track users by identifying unique behaviors in their browser environments.

#### Enhanced Performance Benefits

**Fewer Blocking Scripts**: Injected scripts often modify the DOM or execute operations that can block or slow down page loading, especially if they are not efficiently optimized. By using the `externally_connectable` approach, extensions do not inject any JavaScript into the pages, thereby avoiding the introduction of blocking scripts that can degrade the performance of web pages.
    
 **Optimized Resource Usage**: The `externally_connectable` method focuses on direct communication through message passing, which is generally more resource-efficient than script injection. This means less CPU and memory usage as the communication is handled by the browser’s built-in mechanisms rather than through additional JavaScript execution on the page.

**Faster Page Load Times**: Without the overhead of injecting and running additional scripts on every page, pages load faster, providing a smoother and more responsive user experience. This performance improvement is critical for user retention and satisfaction, especially on resource-intensive applications or slower devices.

#### So Why Wallet Discovery?

Given the benefits of `externally_connectable` we need to solve for a key area where it fails to match the injected provider pattern.  
`externally_connectable` requires that the webpage attempting to connect with the wallet know the extension's canonical extension id. Currently, there is no standard method for these wallets to provide their extension IDs to dApps. A standardized approach will improve interoperability, security, and user experience by ensuring that dApps can reliably identify and communicate with the intended wallet.

Ideally browsers would eventually provide an API by which wallets can register the capabilities that they offer which webpages could access and choose to communicate with in a way that avoids the need for this additional discovery step.
  
### Specification

**Extension ID Announcement Protocol:**

Each wallet extension must implement a mechanism to announce its extension ID to a dApp. This should be done using a JavaScript event emitted by the wallet and listened for by the dApp. The event should contain the canonical extension ID as its payload.

**Modification to dApp Listening Mechanisms:**

dApps must modify their initialization routines to listen for the wallet's extension ID announcement event. Upon capturing the event, the dApp can store the extension ID and use it to establish a connection using the `externally_connectable` API.

**Content Script Utilization**:

While this CAIP significantly advances the way wallets interact with dApps through the `externally_connectable` API, it still necessitates the use of a content script. This script, however, requires fewer permissions compared to traditional methods and can be injected synchronously to facilitate the initial communication process. This adjustment is necessary to bridge the current browser limitations and set the stage for future enhancements.

1.  **Reduced Permission Scope**: The content script deployed under this new standard does not need the extensive permissions typically required for full page content access and manipulation. Instead, its role is limited to emitting and listening for the specific event that carries the wallet's extension ID, thereby reducing the risk and scope of permissions required.
    
2.  **Synchronous Injection**: The content script can be injected synchronously with the page load, ensuring that the wallet's extension ID is available as soon as the dApp begins its initialization process. This method avoids the delays and potential conflicts associated with asynchronous script loading, leading to a smoother and more predictable interaction pattern.
    
3.  **Security and Efficiency**: Although using a content script still presents a minimal footprint on the user's browsing experience, this approach is a compromise that balances current browser capabilities with the need for enhanced security and performance. By minimizing the script's functionality to only what is necessary for ID transmission, the risks associated with broader access permissions are mitigated.
    

It worth acknowledging the transitional nature of the proposed solution, aiming for a future where browsers might offer more direct APIs for wallet capability registration and discovery, eliminating the need for any content script altogether. Meanwhile, the approach detailed in this CAIP ensures a more secure and efficient method of wallet-to-dApp communication than previously employed techniques, setting a foundation for further innovations in this space.

 **Example Implementation:**

- Wallets:

```javascript

document.dispatchEvent(new CustomEvent('walletExtensionIdAnnouncement', {

detail: {

extensionId: 'your_extension_id_here'

}

}));

```

- dApps:

```javascript

document.addEventListener('walletExtensionIdAnnouncement', function(e) {

const walletExtensionId = e.detail.extensionId;

// Use walletExtensionId to establish communication

});

```
**Browser Manifest Adjustments:**

 Wallets can configure the `externally_connectable` section of their browser extension manifest to specify which domains are allowed to communicate with the extension. To avoid restricting communication to a pre-defined list of sites, wallets may use wildcards (e.g., `*.example.com`). 
 

**Backward Compatibility:**
This protocol should be implemented in such a way that it does not interfere with existing implementations of EIP-1193 providers. Wallets may need to support both the new extension ID-based communication protocol and the traditional EIP-1193 provider interface.

#### Conclusion

This proposal enhances how decentralized applications interact with browser-based wallets by standardizing the announcement of wallet extension IDs using the `externally_connectable` API. This approach mitigates security risks associated with broad permissions and fingerprinting, and improves performance by eliminating blocking scripts. The proposal provides a detailed implementation guide for both wallets and dApps, ensuring compatibility and ease of integration. By adopting this CAIP, the blockchain community moves towards a more secure, efficient, and user-friendly ecosystem, fostering greater interoperability and adoption across various blockchain platforms.