---
caip: X
title: Wallet Scope
author: Alex Donesky (@adonesky1), Jiexi Luan (@jiexi), Shane Jonas (@shanejonas), Vandan Parikh (@vandan)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/131 
status: Draft
type: Standard
created: 2024-07-05
updated: 2024-07-05
---

## Simple Summary
This document defines a standardized method for applications (e.g., dapps) to reference functions of user-agents (e.g., "wallets") that are not necessarily part of a specific network protocol. This provides a uniform way for applications to reference these functions and supports interoperability.

## Abstract
Applications require a standardized method to reference wallet functions that, while related, may not be directly part of the network protocols referenced by [CASA namespaces][namespaces]. The `wallet_scope` offers a standardized scheme to delineate such wallet functions.

## Motivation
The process of implementing [CAIP-25][], [CAIP-27][], and [CAIP-217][] for Ethereum Wallets highlighted the need for an authorization scope that covers functions that are not directly part of Ethereum network protocols, but do serve as standard wallet interface definitions (e.g. [Interface EIPs][]). Furthermore, some wallet functions, such as [wallet_scanQRcode](https://github.com/ethereum/EIPs/issues/945) are not even related to any particular protocol or ecosystem. This pattern of non-protocol wallet functions is anticipated to be present across other ecosystems, which would benefit from standard ways to reference such functions.

## Specification
A wallet scope is a string designed to delineate the wallet functions by namespace. Wallet functions are prefixed with `wallet` and can be optionally paired with a `namespace` parameter.

### Syntax
A `wallet_scope` is represented as a case-sensitive string in the form:

```
wallet_scope:   ("wallet" | ("wallet:" + namespace))
namespace:      [-a-z0-9]{3,8}
```

## Test Cases
This is a list of manually composed examples

```
# A scope string for general wallet functions
wallet

# A scope string for Ethereum wallet functions
wallet:eip155

# A scope string for Solana wallet functions
wallet:solana
```
### Semantics
The `wallet_scope` standard specifies how to delineate wallet functions that can be either general or specific to a particular blockchain namespace, facilitating interoperability and consistency across different blockchain ecosystems.

1. **General Wallet Functions**:
   - The string `wallet` alone refers to general wallet functions not specific to any `namespace`, and may be applicable across wallets.

2. **Namespace-Specific Wallet Functions**:
   - Appending a `namespace` to the `wallet` prefix (e.g., `wallet:eip155`) specifies wallet functions related to a particular `namespace`.

3. **Use Cases and Examples**:
   - A general function such as `wallet` might include basic operations like triggering a scan of a QR code.
   - A namespace-specific function like `wallet:eip155` might include operations like `wallet_addEthereumChain` or interacting with Ethereum-specific features not directly part of the Ethereum network protocols but common in Ethereum wallets.
   - Another example, `wallet:solana`, might include Solana-specific wallet operations such as `signMessage`, which commonly used to interact with Solana-based applications and services.

## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [Namespaces][namespaces] Chain Agnostic Namespaces
- [CAIP-25][]
- [CAIP-27][]
- [Interface EIPs][] Ethereum Interface Improvement Proposals

[namespaces]: https://namespaces.chainAgnostic.org/
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-27]: https://chainagnostic.org/CAIPs/caip-27
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[Interface EIPs]: https://eips.ethereum.org/interface

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
