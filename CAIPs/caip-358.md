---
caip: CAIP-358
title: Universal Payment Request Method
author: Luka Isailovic (@lukaisailovic), Derek Rein (@arein)
discussions-to: []
status: Draft
type: Standard
created: 2025-05-26
updated: 2025-05-26
requires: 2, 10, 19
replaces: 
---


## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
A standard for enabling one-interaction cryptocurrency payment experiences across wallets and dapps, allowing payment information to be transmitted in a single round-trip.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
This CAIP standardizes a wallet <> dapp JSON-RPC method `wallet_pay` for more efficient communication about the purchase intent from the dapp to the wallet.
The method allows merchants to specify payment requirements enabling wallets to handle payment execution with minimal user interaction.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Current cryptocurrency payment experiences are either error-prone (manual transfers, address QR codes) or suboptimal, requiring multiple interactions from the user.
In addition to this, different payment providers implement different payment experiences, creating confusion.

Solutions like EIP-681 or `bitcoin:` url are ecosystem specific and didn't get much support from the wallets. They rely on the QR code scan as well, which means that they can't be batched during connection with protocols like WalletConnect.

By standardizing the payment experience both dapp side and wallet side, we can reduce user errors during payment, provide the payment experience in as few clicks as possible reducing the friction in crypto payments.
 
The method transmits all the possible payment requests so the wallet can pick the most optimal one based on the assets that user has in the account.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
### Method: `wallet_pay`
#### Request

```typescript
type Hex = `0x${string}`;

type PaymentOption = {
  asset: string; 
  amount: Hex;
  recipient: string;
}

// JSON-RPC Request
type PayRequest = {
  orderId: string;
  acceptedPayments: PaymentOption[]; 
  expiry: number;
}

```

The application **MUST** include:
- An `orderId` that uniquely identifies this payment request. `orderId` **MUST NOT** be longer than 128 characters.
- At least one entry in the `acceptedPayments` array
- `expiry` timestamp for the payment request


The `orderId` field **MUST** be a string that uniquely identifies the payment request. Implementations **SHOULD** ensure this ID is unique across their system to prevent collisions.

The `acceptedPayments` field **MUST** be an array of `PaymentOption` objects. Each element in the array represents a payment option that the wallet can choose from to complete the payment.

For `PaymentOption` options:
- The `recipient` field **MUST** be a valid [CAIP-10][] account ID.
- The `asset` field **MUST** follow the [CAIP-19][] standard.
- The `amount` field **MUST** be a hex-encoded string representing the amount of the asset to be transferred.
- The [CAIP-2][] chainId component in the [CAIP-19][] `asset` field **MUST** match the [CAIP-2][] chainId component of the [CAIP-10][] `recipient` account ID.

The `expiry` field **MUST** be a UNIX timestamp (in seconds) after which the payment request is considered expired. Wallets **SHOULD** check this timestamp before processing the payment.


Request example:
```json
{
    "orderId": "order-123456",
    "acceptedPayments": [
      {
        "recipient": "eip155:1:0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
        "asset": "eip155:1/erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
        "amount": "0x5F5E100" 
      },
      {
        "recipient": "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp:9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
        "asset": "solana:5eykt4UsFv8P8NJdTREpY1vzqKqZKvdp/slip44:501",
        "amount": "0x6F05B59D3B20000"
      }
    ],
    "expiry": 1709593200 
  }
```

#### Response
```typescript
type PayResult = {
  orderId: string; 
  txid: string; 
  recipient: string;  
  asset: string;  
  amount: Hex; 
}
```

The wallet's response MUST include:
- `orderId` that matches the original request
- `txid` with the transaction identifier on the blockchain
- `recipient` that received the payment. It **MUST** be a valid [CAIP-10][] account ID.
- `asset` that was used for payment. It **MUST** follow the [CAIP-19][] standard.
- `amount` that was paid. It **MUST** be represented in hex string

`txid` **MUST** be a valid transaction identifier on the blockchain network specified in the asset's chain ID.

 `recipient`, `asset`, and `amount` **MUST** match those specified in the selected direct payment option in the `acceptedPayments` array.


Example response:
```json
{
    "orderId": "order-123456",
    "txid": "0x8a8c3e0b1b812182db4cabd81c9d6de78e549fa3bf3d505d6e1a2b25a15789ed",
    "recipient": "eip155:1:0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
    "asset": "eip155:1/erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
    "amount": "0x5F5E100"
}
```

#### Idempotency
The `wallet_pay` method **MUST** be idempotent for the same `orderId`. This ensures robustness in case of connection failures or timeout scenarios.

Requirements:
- If a payment with the same `orderId` has already been completed successfully, the wallet **MUST** return the original `PayResult` without executing a new payment
- If a payment with the same `orderId` is currently pending, the wallet **SHOULD** return the result of the original payment attempt
- If a payment with the same `orderId` has failed previously, the wallet **MAY** attempt the payment again or return the previous error
- Wallets **SHOULD** maintain payment status for completed transactions for at least 24 hours after completion
- If connection is lost during payment execution, dapps **MAY** retry the same request to query the payment status

#### Error Handling
If the payment process fails, the wallet **MUST** return an appropriate error message:
```typescript
type PayError = {
  code: number;
  message: string;
  data?: any;
}
```
The wallet **MUST** use one of the following error codes when the pay request fails:

- When user rejects the payment
    - code = 8001
    - message = "User rejected payment"
- When no matching assets are available in user's wallet
    - code = 8002
    - message = "No matching assets available"
- When the payment request has expired
    - code = 8003
    - message = "Payment request expired"
- When there are insufficient funds for the payment
    - code = 8004
    - message = "Insufficient funds"


If a wallet does not support the `wallet_pay` method, it **MUST** return an appropriate JSON-RPC error with code -32601 (Method not found).

Example error response:
```json
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": 8001,
    "message": "User rejected payment"
  }
}
```

## Rationale

This specification evolved through multiple iterations to address fundamental usability issues in cryptocurrency payment flows. Initial exploration began as a CAIP alternative to EIP-681, but analysis of existing payment service provider (PSP) implementations revealed significant friction in current user experiences.

Existing cryptocurrency payment flows typically require users to:
- Select a token type
- Choose a blockchain network
- Wait for address/QR code generation
- Complete the transfer manually

This multi-step process creates excessive friction, often requiring 4-6 user interactions for a simple payment.

The `wallet_pay` method addresses these limitations by:
- Moving choice to the wallet rather than forcing merchants to pre-select payment methods, wallets can filter available options based on user account balances and preferences
- All payment options are transmitted in one request, eliminating the need for multiple user interactions
- The response includes transaction ID and execution details, providing immediate confirmation
- Can be batched with connection establishment, enabling "connect + pay" flows in protocols like WalletConnect

### Alternative Approaches Considered

An intermediate solution involved encoding multiple payment addresses in a single QR code, allowing merchants to present all payment options simultaneously. However, this approach proved impractical for dapp implementations because:

- PSPs cannot determine which payment option was selected
- Monitoring requires polling up to 20+ addresses simultaneously
- No confirmation mechanism exists for payment completion

## Test Cases
<!--Please add diverse test cases here if applicable. Any normative definition of an interface requires test cases to be implementable. -->
TODO
## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
TODO
## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
TODO
## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.

## References 
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-1][CAIP-1] defines the CAIP document structure

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).