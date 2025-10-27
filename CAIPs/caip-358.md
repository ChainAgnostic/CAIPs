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

A standard for enabling one-interaction cryptocurrency payment experiences across wallets and dapps, allowing payment information to be transmitted in a single round-trip.

## Abstract

This CAIP standardizes a wallet <> dapp JSON-RPC method `wallet_pay` for more efficient communication about the purchase intent from the dapp to the wallet.
The method allows merchants to specify payment requirements enabling wallets to handle payment execution with minimal user interaction.

## Motivation

Current cryptocurrency payment experiences are either error-prone (manual transfers, address QR codes) or suboptimal, requiring multiple interactions from the user.
In addition to this, different payment providers implement different payment experiences, creating confusion.

Solutions like EIP-681 or `bitcoin:` url are ecosystem-specific and have not historically gotten sufficient support from the wallets. They tend to rely on a QR code scan as well, which means that they can't be batched as part of a connection-flow using protocols like WalletConnect.

By standardizing the payment experience on both the application side and the wallet side, we can reduce user errors during payment, providing the payment experience in as few clicks as possible and reducing the friction in crypto payments.

The method transmits all the acceptable payment requests so the wallet can pick the most optimal one based on the assets that user has in the account and the wallet's capabilities.

## Specification

### Method: `wallet_pay`

#### Request

```typescript
// Accepted Payment Options
type PaymentOption = {
  chain: string;
  asset: string;
  value: string;
  payto: string;
  types: string[];
};

// JSON-RPC Request Params
type RequestParams = {
  version: integer;
  orderId: string;
  timeout: number;
  accepts: PaymentOption[];
};
```

The following request parameters are defined for `version=1` as:

- `version` - this field is an integer and **MUST** be present to define which the following parameters are optional and required.
- `orderId` - this field **MUST** uniquely identify an order for which this payment request is linked to and **MUST NOT** be longer than 128 characters.
- `timeout` - this field **MUST** be an integer in seconds after which the payment request is expired. Wallets **MUST** check this timestamp before processing the payment.
- `accepts` - this field **MUST** be an array of `PaymentOption` objects with at least one entry. Each element in the array represents a payment option that the wallet can choose from to complete the payment with independent parameters.

For `PaymentOption` parameters these are defined for `version=1` as:

- `chain` - this fields **MUST** follow the chainId [CAIP-2][] spec and it will used for all fields below.
- `asset` - this field **MUST** follow the assetId [CAIP-19][] spec without the CAIP-2 prefix.
- `value` - this field **MUST** be a string representing the value of the asset to be transferred.
- `payTo` - this field **MUST** be a chain-specific address present in the chain.
- `types` - this field **MUST** be an array of strings defining different transfer authorization types (eg. `erc20-transfer`, `erc20-approve`, `spl-transfer`, `erc2612-permit`, `erc3009-authorization`).

Example Request:

```jsonc
{
  "version": 1,
  "orderId": "order-123456",
  "timeout": 300,
  "accepts": [
    {
      "chain": "eip155:1",
      "asset": "erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
      "value": "100",
      "payTo": ":0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "types": ["erc20-transfer", "erc20-approve", "erc3009-authorization"]
    },
    {
      "asset": "erc20:0x4c9edd5852cd905f086c759e8383e09bff1e68b3",
      "value": "100",
      "payTo": ":0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "types": ["erc20-transfer", "erc20-approve", "erc2612-permit"]
    },
    {
      "chain": "solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ",
      "asset": "slip44:501",
      "value": "0.5",
      "payTo": "9WzDXwBbmkg8ZTbNMqUxvQRAyrZzDsGYdLVL9zYtAWWM",
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

The following response parameters are defined for `version=1` as:

- `version` - this field is an integer and **MUST** match the same one as the request.
- `orderId` - this field is a string and **MUST** match the same one as the request.
- `payment` - this field is a `PaymentOption` object and describes which option was used to fulfill this request and **MUST** match one of the available ones in the request.
- `receipt` - this field is a `TransferReceipt` object and will include the transfer type used and corresponding data which **MUST** not be empty.

Example Response:

```jsonc
// Response (type="erc20-transfer")
// [hash = transaction id]

{
  "version": 1,
  "orderId": "order-123456",
  "payment":  {
    "chain": "eip155:1",
    "asset": "erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
    "value": "100",
    "payTo": ":0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
    "types": ["erc20-transfer", "erc3009-authorization"]
   },
  "receipt": {
    "type": "erc20-transfer",
    "hash": "0x8a8c3e0b1b812182db4cabd81c9d6de78e549fa3bf3d505d6e1a2b25a15789ed",
    "data": {
      "from": "0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb",
      "to": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "value": "100",
    }
  },
}


// Response (type="erc3009-authorization")
// [hash = signature]

{
  "version": 1,
  "orderId": "order-123456",
  "paymentSelected": {
    "chain": "eip155:1",
    "asset": "erc20:0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
    "value": "100",
    "payTo": ":0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
    "types": ["erc20-transfer", "erc3009-authorization"]
   },
  "transferConfirmation": {
    "type": "erc3009-authorization",
    "hash": "0x8f3d1a72c9e54b60a7f2d98e41b3c75a9d04f68e2c71b95f3a0e6d2b4c89f17a5b3e90c47d61f2a8e9c5b4d73a1e06f298d3b57c40f9e1a62b84d5c7f03a9b6e81d24f5b70a39c8e4d26f1a05b7c9d3e8f42a",
    "data": {
      "from": "0xab16a96D359eC26a11e2C2b3d8f8B8942d5Bfcdb",
      "to": "0x71C7656EC7ab88b098defB751B7401B5f6d8976F",
      "value": "100",
      "nbf": 1740672089,
      "exp": 1740672389,
      "nonce": "0xf3746613c2d920b5fdabc0856f2aeb2d4f88ee6037b8cc5d04a71a4462f13480"
    }
  },
}
```

#### Idempotency

The `wallet_pay` method **MUST** be idempotent for the same `orderId` as his ensures robustness in case of connection failures or timeout scenarios:

- If a payment with the same `orderId` has already been completed successfully, the wallet **MUST** return the original `PayResult` without executing a new payment
- If a payment with the same `orderId` is currently pending, the wallet **SHOULD** return the result of the original payment attempt
- If a payment with the same `orderId` has failed previously, the wallet **MAY** attempt the payment again or return the previous error
- Wallets **SHOULD** maintain payment status for completed transactions for at least 24 hours after completion
- If connection is lost during payment execution, dapps **MAY** retry the same request to query the payment status

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

This specification evolved through multiple iterations to address fundamental usability issues in cryptocurrency payment flows. Initial exploration began as a CAIP alternative to EIP-681/Solana Pay, but analysis of existing payment service provider (PSP) implementations revealed significant friction in current user experiences.

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

Wallets are encouraged to utilize transaction privacy protocols to prevent payment data from leaking browsing history onchain.
A complete transaction privacy protocol can be defined as one that prevents manual or automated analysis of transaction data on-chain (e.g. on a block explorer) being enough to identify the sender and/or the payTo of a given transaction.
A protocol which protects the sender's privacy will prevent leaking of purchase data being used to build a behavioral profile through purchase history of an onchain account.
A protocol which focuses only on payTo (e.g. merchant) privacy will prevent leaking real-time transaction data of businesses which may constitute "business intelligence" that enables reverse engineering of business practices, intellectual
property, trade secrets, etc.
Depending on the use-case, either or both may be necessary to prevent this RPC's on-chain records creating damaging externalities.

## Backwards Compatibility

TODO

<!-- All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright. -->

## References

- [CAIP-1] defines the CAIP document structure
- [EIP-681] is ethereum-specific prior art that also includes gas information in the URI

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[CAIP-19]: https://ChainAgnostic.org/CAIPs/caip-19
[EIP-20]: https://eips.ethereum.org/EIPS/eip-20
[EIP-681]: https://eips.ethereum.org/EIPS/eip-681
[EIP-2612]: https://eips.ethereum.org/EIPS/eip-2612
[EIP-3009]: https://eips.ethereum.org/EIPS/eip-3009

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
