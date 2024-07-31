---
caip: 27
title: Wallet Invoke Method JSON-RPC Method
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/27
status: Draft
type: Standard
created: 2020-12-12
updated: 2024-07-02
requires: 2, 25, 171, 217
---

## Simple Summary

CAIP-27 defines a JSON-RPC method for a wallet-connected application to invoke
a wallet invoke an JSON-RPC method in a specified context defined by a valid
[scopeObject][CAIP-217] and tagged with a [sessionId][CAIP-171] for maintaining session continuity.

## Abstract

This proposal has the goal of defining a standard method for decentralized
applications to invoke JSON-RPC methods from user agents (such as
cryptocurrency wallets) directed to a given, previously-authorized target
chain (such as nodes of a specific blockchain or consensus community within a
protocol). It requires a valid [scopeObject][CAIP-217] and a valid
[sessionId][CAIP-171] for interoperability and composability. These two
properties MAY be inherited from a persistent session created by [CAIP-25][],
but could also be used as part of other session management mechanisms.

## Motivation

The motivation comes from the ambiguity that comes from interfacing with a
multi-chain agent (e.g. a cryptocurrency wallets which supports the same
method on multiple chains in a namespace, or supports methods with the same name
on multiple namespaces).

## Specification

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in
uppercase in this document are to be interpreted as described in [RFC
2119][]

### Definition

The JSON-RPC provider is able to invoke a single JSON-RPC request accompanied
by a [CAIP-2][] compatible `chainId` scoped by the [sessionId][CAIP-171] of
a pre-existing session.

### Request

The application would interface with an JSON-RPC provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_invokeMethod",
  "params": {
    "sessionId": "0xdeadbeef",
    "scope": "eip155:1",
    "request": {
      "method": "eth_sendTransaction",
      "params": [
        {
          "to": "0x4B0897b0513FdBeEc7C469D9aF4fA6C0752aBea7",
          "from": "0xDeaDbeefdEAdbeefdEadbEEFdeadbeefDEADbEEF",
          "gas": "0x76c0",
          "value": "0x8ac7230489e80000",
          "data": "0x",
          "gasPrice": "0x4a817c800"
        }
      ]
    }
  }
}
```

The JSON-RPC method is labeled as `wallet_invokeMethod` and expects
three **required parameters**:

- **sessionId** - [CAIP-171][] `SessionId` referencing a known, open session
- **scope** - a valid `scopeObject` previously authorized to the caller and persisted in
  the session identified by `sessionId`
- **request** - an object containing the fields:
  - **method** - JSON-RPC method to invoke
  - **params** - JSON-RPC parameters to invoke (may be empty but must be set)

### Validation

1. A respondent MUST check the `scope` against the identified session object
   before executing or responding to such a request, and invalidate a request
   for a scope not already authorized and persisted.
2. The respondent SHOULD check that `request.method` is authorized in the
   session object for that specific scope.
3. The respondent MAY check that the `params` are valid for that method, if its
   syntax is known to it.
4. The respondent MAY apply other logic or validation.
5. The respondent MAY chose to drop invalid requests or return an error message,
   but it MUST NOT route or submit them.

### Response

Upon successful validation, the respondent will submit or route the request to
the targeted chain. If the targeted chain returns a response to the
respondent, the respondent MAY forward this response to the caller. Constraints
on, metadata about, or envelopes for response-forwarding MAY be set by
[namespace][namespaces] profiles of this CAIP.

Similarly, error messages depend on the design of a given namespace, and MAY be
defined by a [namespace][namespaces] profile of this CAIP.

## Links

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[namespaces]: https://namespaces.chainagnostic.org/
[RFC 2119]: https://www.ietf.org/rfc/rfc2119.txt

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
