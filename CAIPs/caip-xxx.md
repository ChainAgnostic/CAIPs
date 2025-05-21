---

# CAIP-XX: Chain-Agnostic Token Payment URI

**Title**: Chain-Agnostic Token Payment URI
**Author**: Pedro Gomes
**Status**: Draft
**Type**: Standard
**Created**: 2025-05-20

---

## Abstract

This proposal defines a URI scheme for token-based payments in a chain-agnostic format. The format encodes the target blockchain, recipient account, token asset, and optional amount using URL-safe syntax and Chain Agnostic Improvement Proposals (CAIPs):

* **CAIP-2** for chain identification
* **CAIP-19** for token identification (excluding chain prefix)
* **CAIP-10** for account identification (excluding chain prefix)

---

## Motivation

Digital payments in Web3 are fragmented across wallets, chains, and token standards. Current methods lack consistency and interoperability, especially for use in deep links and QR codes within mobile apps, websites, ecommerce checkouts, or social media posts.

A standardized URI string provides a simple, readable, and composable format for triggering token transfers across different chains using a unified scheme. This enables:

* One-click payments from mobile wallets via deep linking
* QR code generation for point-of-sale payments
* Standardized checkout links for ecommerce platforms
* Tipping or payment links embedded in social media profiles or messages
* Easy parsing and validation by wallet SDKs, apps, and browsers

---

## Specification

### URI Format

```
caip:<CHAIN>/pay?recipient=<RECIPIENT>&token=<TOKEN>&amount=<AMOUNT>
```

* `CHAIN` is **required** and follows CAIP-2
* `recipient` is **required**, follows CAIP-10 but omits chain prefix
* `token` is **required**, follows CAIP-19 but omits chain prefix
* `amount` is **optional**, denominated in the smallest unit of the token (e.g., wei, lamports, satoshi)

All parameters are URL-encoded as per RFC 3986.

### Parameter Rules

* The `token` and `recipient` fields implicitly inherit the chain from the `CHAIN` path prefix.
* The `amount` parameter is a string representing a **positive integer** in the token’s smallest unit (no decimals).
* Future extensions may support optional parameters such as `label`, `message`, or `reference`.

---

## Examples

### Example 1: Ethereum Mainnet (USDC)

```
caip:eip155:1/pay?recipient=0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb&token=erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48&amount=20000000
```

* Chain: Ethereum Mainnet (`eip155:1`)
* Recipient: `0xab16...fcdb`
* Token: USDC (ERC-20 address)
* Amount: 20 USDC (in base units: 6 decimals)

---

### Example 2: Solana Mainnet (USDC)

```
caip:solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp/pay?recipient=7S3P4HxJpyyigGzodYwHtCxZyUQe9JiBMHyRWXArAaKv&token=token:EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v&amount=20000000
```

* Chain: Solana Mainnet (`solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp`)
* Recipient: Solana address
* Token: USDC (native SPL address)
* Amount: 20 USDC (6 decimals)

---

## Use Cases

### 1. **Mobile Deep Links**

A merchant app generates a `caip:` link and opens it via `window.location` or native intent. The user’s wallet intercepts it and displays a pre-filled transaction.

### 2. **QR Codes**

POS systems display a QR with the `caip:` URI. Wallets scanning the QR can decode and present a payment confirmation instantly.

### 3. **Ecommerce Checkouts**

Online stores embed a "Pay with Wallet" button that links to a `caip:` URI. The checkout amount and token are pre-filled, allowing direct token transfer.

### 4. **Social Media Payments**

Users share a payment URI on their profile (e.g. Twitter bio or Instagram link) to receive token tips or donations across any chain.

---

## Future Extensions

The standard can be expanded to support:

* `label`: optional name for the recipient or reason for payment
* `message`: free text (e.g. invoice number, note)
* `reference`: unique ID to link payment to an order or transaction
* `memo`: for chains that require an extra tag (e.g., Cosmos, Stellar)

---

## Rationale

* Uses existing CAIP standards for maximum interoperability and composability.
* URL syntax allows universal support across browsers, mobile apps, and QR readers.
* Keeps base format minimal to promote adoption and optimize UX.

---

