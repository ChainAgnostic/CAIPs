---
caip: 375
title: Wallet Sign Message
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/375
status: Review
type: Standard
created: 2025-08-25
requires: 10, 171, 217
---

## Simple Summary

Defines a JSON-RPC method, `wallet_signMessage`, to request cryptographic signatures on arbitrary messages from one or more wallet accounts, optionally tied to a session.

## Abstract

The `wallet_signMessage` RPC method is a chain-agnostic interface for signing arbitrary messages.
It supports multiple accounts and signature schemes within a single call, optionally referencing a CAIP-171 session.
This unifies how apps and wallets perform message signing for authentication, typed data, and off-chain actions.

## Motivation

Message signing today is fragmented: each wallet has its own API for personal message signing, typed data (e.g., EIP-712), or custom formats.
There's no standard to support multiple accounts or signature types in one request.
This proposal fixes that by:

- Supporting optional sessions (CAIP-171) to link signing requests to an ongoing connection.
- Allowing multiple messages and flexible signature schemes (e.g., EIP-191, EIP-1271).
- Making message type and content explicit, improving wallet UX and security.

## Specification

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in uppercase in this document are to be interpreted as described in [RFC-2119][]

### Definition

**Request:**

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_signMessage",
  "params": {
    "sessionId": "0xdeadbeef", // optional
    "messages": [
      {
        "account": "eip155:1:0xabc123...", // optional
        "signatureTypes": ["eip191", "eip1271"], // optional
        "messageType": "ethPersonalSign",
        "content": "Hello World"
      }
    ],
    "capabilities": {} // optional
  }
}
```

**Response:**

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "signatures": [
      {
        "account": "eip155:1:0xabc123...",
        "signatureType": "eip1271",
        "messageType": "ethPersonalSign",
        "signature": "0xdeadbeef..."
      }
    ],
    "capabilities": {} // optional
  }
}
```

**Rules:**

- `sessionId` is OPTIONAL but MUST follow CAIP-171 if provided.
- Each message MUST include `messageType` and `content`.
- Wallets MAY choose any of the provided `signatureTypes`.
- Response MUST include `account` and `signatureType` for each signature
- `capabilities` is an OPTIONAL metadata object, placed outside `signatures`.

## Security Considerations

- Malicious apps can trick users into signing harmful messages; wallets MUST show clear prompts including `content` and `account`.
- Including nonces or timestamps in `content` is RECOMMENDED to prevent replay attacks.
- Sessions referenced by `sessionId` SHOULD be validated to ensure scope compliance.

## Privacy Considerations

- Signing may reveal account addresses; wallets SHOULD only return requested data.
- `capabilities` could expose metadata; apps and wallets SHOULD handle them carefully.
- Multi-message requests could link identities; wallets MAY warn users.

## Links

- [CAIP-10][] - Account ID Specification
- [CAIP-104][] - Definition of Chain Agnostic Namespaces or CANs
- [CAIP-171][] - Session Identifier, i.e. syntax and usage of `sessionId`s
- [CAIP-217][] - Authorization Scopes, i.e. syntax for `scopeObject`s
- [RFC-2119][] - Key Words for use in RFS to Indicate Requirement Levels

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[RFC-2119]: https://datatracker.ietf.org/doc/html/rfc2119

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
