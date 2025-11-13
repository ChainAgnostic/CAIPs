---
caip: 358
title: Universal Payment Request Method
author: Luka Isailovic (@lukaisailovic), Derek Rein (@arein), Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/358
status: Draft
type: Standard
created: 2025-05-26
updated: 2025-05-26
requires: 2, 10, 19
---

## Simple Summary

A standard for enabling one-interaction cryptocurrency payment flows across wallets and dapps, allowing all payment information to be transmitted in a single round-trip.

## Abstract

This CAIP standardizes a wallet <> dapp JSON-RPC method `wallet_pay` for more efficient communication about the purchase intent from the dapp to the wallet.
The method allows merchants to specify payment requirements enabling wallets to handle payment execution with minimal user interaction.

## Motivation

Current cryptocurrency payment experiences are either error-prone (manual transfers, address QR codes) or overly complex, often requiring multiple user interactions. In addition to this, different payment providers implement different payment experiences, creating confusion.

Solutions like [ERC-681][] or `bitcoin:` url are ecosystem-specific and have not historically gotten sufficient support from the wallets. They tend to rely on a QR code scan as well, which means that they can't be batched as part of a connection-flow using protocols like WalletConnect.

By standardizing the payment experience on both the application and wallet side, we reduce user errors and enable payments in as few interactions as possible, lowering friction across crypto payments.

The method transmits all the acceptable payment requests so the wallet can pick the most optimal one based on the assets that user has in the account and the wallet's capabilities.

## Specification

### Method: `wallet_pay`

#### Request

```typescript
// Accepted Payment Options
type PaymentOption = {
  asset: string;
  amount: string;
  recipient: string;
  types: string[];
};

// JSON-RPC Request Params
type RequestParams = {
  version: integer;
  orderId: string;
  expiry: number;
  paymentOptions: PaymentOption[];
};
```

The following request parameters are defined for `version=1` as:

- `version` - this field is an integer and **MUST** be present to define which of the following parameters are optional or required.
- `orderId` - this field **MUST** uniquely identify an order which this payment request is linked to and **MUST NOT** be longer than 128 characters. It does not require global uniqueness, but it's **RECOMMENDED** to use a UUIDv4 if global uniqueness is necessary.
- `expiry` - this field **MUST** be a UNIX timestamp (in seconds) after which the payment request is considered expired. It **SHOULD** use an expiry of at least 5 minutes (300 seconds).
- `paymentOptions` - this field **MUST** be an array of `PaymentOption` objects with at least one entry. Each element in the array represents a payment option the wallet may choose to complete the payment, each with independent parameters.

For `PaymentOption` parameters these are defined for `version=1` as:

- `asset` - this field **MUST** follow the assetId [CAIP-19][] spec which also includes the [CAIP-2][] chainId prefix.
- `amount` - this field **MUST** be a Hex string representing the amount in the smallest denomination of its asset.
- `recipient` - this field **MUST** be a chain-specific address present in the chain referred in the `asset` field.
- `types` - this field **MUST** be an array of strings defining different transfer authorization types.

The exclusive list of Transfer Types supported in `version=1` are the following:

- `native-transfer` - this is used when a native token is being used as a PUSH payment (eg. ETH, SOL).
- `erc20-transfer` - this is used when an [ERC-20][] transfer is being used as a PUSH payment.
- `erc20-approve` - this is used when an [ERC-20][] allowance is approved to be used as a PULL payment.
- `erc2612-permit` - this is used when a [ERC-2612][] permit message is being used as a PULL payment.
- `erc3009-authorization` - this is used when a [ERC-3009][] authorization message is being used as a PULL payment.
- `spl-transfer` - this is used when a [SPL][] transfer is being used as a PUSH payment.
- `spl-approve` - this is used when a [SPL][] delegation is being used as a PULL payment.

**NOTE:**

- A PUSH payment would be when the wallet user is the sender of the transaction onchain to settle the token transfer.
- A PULL payment would be when the recipient or a third-party is the sender of the transaction onchain to settle the token transfer.

Example Request:

```jsonc
{
  "version": 1,
  "orderId": "643f31f2-67cd-4172-83cf-3176e8443ab8",
  "expiry": 1740672389,
  "paymentOptions": [
    {
      // 100 USDC on Ethereum Mainnet
      "asset": "eip155:1/erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
      "amount": "0x5F5E100",
      "recipient": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "types": ["erc3009-authorization"]
    },
    {
      // 100 USDE on Ethereum Mainnet
      "asset": "eip155:1/erc20:0x4c9edd5852cd905f086c759e8383e09bff1e68b3",
      "amount": "0x56BC75E2D63100000",
      "recipient": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "types": ["erc20-transfer", "erc20-approve", "erc2612-permit"]
    },
    {
      // 0.5 SOL on Solana Mainnet
      "asset": "solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ/slip44:501",
      "amount": "0x1DCD6500",
      "recipient": "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
      "types": ["native-transfer"]
    },
    {
      // 100 USDC on Solana Mainnet
      "asset": "solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ/spl:EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",
      "amount": "0x5F5E100",
      "recipient": "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
      "types": ["spl-transfer"]
    }
  ]
}
```

#### Response

```typescript
// Transfer Receipt Payload
type TransferReceipt = {
  type: string;
  hash: string;
  data: {
    from: string;
    to: string;
    value: string;
    nbf?: integer;
    exp?: integer;
    nonce?: string;
  };
};

// JSON-RPC Response Result
type ResponseResult = {
  version: string;
  orderId: string;
  payment: PaymentOption;
  receipt: TransferReceipt;
};
```

The following response parameters are defined for `version=1`:

- `version` - this field is an integer and **MUST** match the same value used in the request.
- `orderId` - this field is a string and **MUST** match the same valued used in the request.
- `payment` - this field is a `PaymentOption` object and describes which option was used to fulfill this request and **MUST** match one of the provided options in the request.
- `receipt` - this field is a `TransferReceipt` object and will include the transfer type used and corresponding data which **MUST** not be empty.

Example Response:

```jsonc
// Response (type="erc20-transfer")
// [hash = transaction id]

{
  "version": 1,
  "orderId": "643f31f2-67cd-4172-83cf-3176e8443ab8",
  "payment": {
    "asset": "eip155:1/erc20:0x4c9edd5852cd905f086c759e8383e09bff1e68b3",
    "amount": "0x56BC75E2D63100000",
    "recipient": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
    "types": ["erc20-transfer", "erc20-approve", "erc2612-permit"]
  },
  "receipt": {
    "type": "erc20-transfer",
    "hash": "0x8a8c3e0b1b812182db4cabd81c9d6de78e549fa3bf3d505d6e1a2b25a15789ed",
    "data": {
      "from": "0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb",
      "to": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "value": "0x56BC75E2D63100000",
    }
  },
}


// Response (type="erc3009-authorization")
// [hash = signature]

{
  "version": 1,
  "orderId": "643f31f2-67cd-4172-83cf-3176e8443ab8",
  "payment": {
    "asset": "eip155:1/erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
    "amount": "0x5F5E100",
    "recipient": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
    "types": ["erc3009-authorization"]
  },
  "receipt": {
    "type": "erc3009-authorization",
    "hash": "0x8f3d1a72c9e54b60a7f2d98e41b3c75a9d04f68e2c71b95f3a0e6d2b4c89f17a5b3e90c47d61f2a8e9c5b4d73a1e06f298d3b57c40f9e1a62b84d5c7f03a9b6e81d24f5b70a39c8e4d26f1a05b7c9d3e8f42a",
    "data": {
      "from": "0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb",
      "to": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "value": "0x5F5E100",
      "nbf": 1740672089,
      "exp": 1740672389,
      "nonce": "0xb543b4324bc37877595306a83422e70903589eb3079a0003871b5fb0a545bd8d"
    }
  },
}
```

#### Idempotency

The `wallet_pay` method **MUST** be idempotent for the same `orderId` as this ensures robustness in case of connection failures or timeout scenarios:

- If a payment with the same `orderId` has already been completed successfully, the wallet **MUST** return the original `PayResult` without executing a new payment
- If a payment with the same `orderId` is currently pending, the wallet **SHOULD** return the result of the original payment attempt
- If a payment with the same `orderId` previously failed, the wallet MAY attempt it again or return the same error.
- Wallets **SHOULD** maintain payment status for completed transactions for at least 24 hours after completion
- If the connection is lost during payment execution, dapps **MAY** retry the same request to query the payment status

#### Error Handling

If the payment process fails, the wallet **MUST** return an appropriate error message:

```typescript
type ResponseError = {
  code: number;
  message: string;
  data?: any;
};
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

This specification evolved through multiple iterations to address fundamental usability issues in cryptocurrency payment flows. Initial exploration began as a CAIP alternative to ERC-681/Solana Pay, but analysis of existing payment service provider (PSP) implementations revealed significant friction in current user experiences.

Existing cryptocurrency payment flows typically require users to:

- Select a token
- Choose a chain
- Wait for address/QR code generation
- Complete the transfer manually

This multi-step process creates excessive friction, often requiring 4-6 user interactions for a simple payment.

The `wallet_pay` method addresses these limitations by:

- Moving choice to the wallet rather than forcing merchants to pre-select payment methods, wallets can filter available options based on user account balances and preferences
- All payment options are transmitted in one request, eliminating the need for multiple user interactions
- The response includes transaction ID and execution details, providing immediate confirmation
- Can be batched with connection establishment, enabling "connect-and-pay" flows in protocols like WalletConnect

### Alternative Approaches Considered

An intermediate solution involved encoding multiple payment addresses in a single QR code, allowing merchants to present all payment options simultaneously.
However, this approach proved impractical for dapp implementations because:

- PSPs cannot determine which payment option was selected
- Monitoring requires polling up to 20+ addresses simultaneously
- No confirmation mechanism exists for payment completion

## Test Cases

TODO

## Security Considerations

`wallet_pay` does not try to address various cases of merchant fraud that end-users are exposed to today.
Specifically it does not try to tackle merchant fraud insurance in case the sold good is not delivered.
It also does not attempt to provide dispute functionality. These present ideas for future work.

## Privacy Considerations

### Wallet Address Sharing

Wallet addresses were intentionally omitted here both for the purpose of UX simplicity as well as for privacy.
By opting to limit the usage of a wallet address, we make this API implementable without first needing to request permission for the user's wallet address.
The wallet address acts as a cross-origin identifier which can be used to link a user's financial transactions across sites.
Since the wallet address is not needed, we can leave it up to the wallet which address to use.
Furthermore, it is also the responsibility of the wallet to determine if possible which token they wish to make a payment from, if multiple are accepted.
This may be done automatically to improve the user experience or allowing the user to select and override assumed defaults.

### Transaction Privacy

Wallets are encouraged to utilize transaction-privacy protocols to prevent payment data from leaking browsing behavior onchain.
A complete transaction privacy protocol can be defined as one that prevents manual or automated analysis of transaction data onchain (e.g. on a block explorer) being enough to identify the sender and/or the recipient of a given transaction.
A protocol which protects the sender's privacy will prevent leaking of purchase data being used to build a behavioral profile through purchase history of an onchain account.
A protocol focused only on recipient (e.g., merchant) privacy will prevent leaking real-time transaction data of businesses, which may constitute “business intelligence” enabling reverse engineering of business practices, intellectual property, trade secrets, etc.
Depending on the use-case, either or both may be necessary to prevent this RPC's onchain records creating damaging externalities.

## Backwards Compatibility

TODO

## References

- [CAIP-1] defines the CAIP document structure

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-19]: https://ChainAgnostic.org/CAIPs/caip-19
[ERC-20]: https://eips.ethereum.org/EIPS/eip-20
[ERC-681]: https://eips.ethereum.org/EIPS/eip-681
[ERC-2612]: https://eips.ethereum.org/EIPS/eip-2612
[ERC-3009]: https://eips.ethereum.org/EIPS/eip-3009
[SPL]: https://github.com/solana-program/token

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
