---
caip: 27
title: Wallet Invoke Method JSON-RPC Method
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/27
status: Draft
type: Standard
created: 2020-12-12
updated: 2025-08-08
requires: 2, 25, 171, 217, 316
---

## Simple Summary

CAIP-27 defines a JSON-RPC method for a decentralized application to invoke a targeted JSON-RPC method, marked for a specified target previously authorized by a valid [scopeObject][CAIP-217], and tagged with a [sessionId][CAIP-171] for maintaining session continuity if applicable.

## Abstract

This proposal has the goal of defining a standard method for decentralized applications to invoke JSON-RPC methods from decentralized applications directed to a given, previously-authorized target network.
These "target networks" can include nodes of a specific blockchain (accessed via the user agent), the consensus community within a cryptographic protocol, or a user agent's network-specific state.
The JSON-RPC method is nested inside a JSON-RPC "envelope", which takes as required argument a [CAIP-2] identifier designating the target network, with an optional second argument for the [sessionId][CAIP-171] of that session if applicable (see [CAIP-316]).
These two properties MAY be inherited from a persistent session created by [CAIP-25][], but could also be used as part of other session management mechanisms.

## Motivation

This routing envelope avoids ambiguity when applications interface with a multi-chain agent (e.g. a cryptocurrency wallets which supports the same method on multiple chains in a given RPC namespace, or supports methods with the same name on multiple [namespaces]).

## Specification

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in uppercase in this document are to be interpreted as described in [RFC 2119][]

### Definition

The JSON-RPC provider is able to invoke a single JSON-RPC request accompanied by a [CAIP-2][] compatible `chainId` authorized by a pre-existing session.
If that pre-existing session was initiated by a [CAIP-25] response containing a [sessionId][CAIP-171], this `sessionId` value should also be returned at the top level of the `wallet_invokeMethod` envelope (see [CAIP-316] for more context on managing sessions with and without `sessionId` keys).

### Request

The application would interface with an JSON-RPC provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_invokeMethod",
  "params": {
    "sessionId": "0xdeadbeef",
    "chainId": "eip155:1",
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
    },
    "capabilities": {
      "atomic": {
        "status": "supported"
      },
      "paymasterService": {
        "url": "https://...",
        "optional": true
      }
    }
  }
}
```

The JSON-RPC method is labeled as `wallet_invokeMethod` and expects three parameters, **two of them required**:

- **sessionId** (conditional) - [CAIP-171][] `SessionId` disambiguates an open session in a multi-session actor; it is required in some sessions, such as [CAIP-25][] sessions created by a response containing one, and SHOULD be omitted in other sessions, such as [CAIP-25] sessions created by a response not containing one (see [CAIP-316]).
- **chainId** (required) - a valid [CAIP-2][] network identifier, previously authorized by or within a `scopeObject` in the active session
- **request** (required) - an object containing the fields:
  - **method** (required) - the JSON-RPC method to invoke (previously authorized for the targeted network)
  - **params** (required) - JSON-RPC parameters to invoke (may be empty but must be set)
- **capabilities** (optional) - an object containing metadata pertaining to a capability announced by the wallet in the [CAIP-25][] return object, to be invoked with this method

### Validation

1. A respondent SHOULD check the `chainId` against active session's `scopeObject`s before executing or responding to such a request, and SHOULD invalidate a request for a chainId not previously authorized.
2. The respondent SHOULD check that `request.method` is authorized for the specified chainId, and SHOULD invalidate a request for a chainId not previously authorized.
3. The respondent MAY check that the `request.params` are valid for `request.method`, if its syntax is known to it.
4. The respondent MAY apply other logic or validation.
5. The respondent MAY chose to drop invalid requests or return an error message, but it MUST NOT route or submit them.

### Response

Upon successful validation, the respondent will submit or route the request to the targeted network.
If the targeted network returns a response to the respondent, the respondent MAY forward this response to the caller.

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "chainId": "eip155:1",
    "result": {
      "method": "eth_sendTransaction",
      "result": "0x4e306b5a5a37532e1734503f7d2427a86f2c992fbe471f5be403b9f734e667c8"
    }
  }
}
```

Constraints on, metadata about, or envelopes for response-forwarding MAY be set by [namespace][namespaces] profiles of this CAIP.

#### Error Handling

Note that errors pertaining to the connection or session should replace the top-level `"result"` object, but cannot be matched to requests sent without a unique `id`:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": -32700,
    "message": "Parse Error"
  }
}
```

Conversely, errors specific to the method passed or its RPC namespace should be expressed INSIDE the result of a response envelope, with targeting information preserved:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "chainId": "eip155:1",
    "error": {
      "code": 4100,
      "message": "The requested account and/or method has not been authorized by the user."
    }
  }
}
```

The latter category of error depend on the design of the passed method defined within the given RPC namespace, and MAY be defined by a [namespace][namespaces] profile of this CAIP if not in the underlying documentation for that RPC community.

## Backwards Compatibility

Early drafts of this specification did not constrain `chainId` to [CAIP-2] identifiers, but rather to any [valid scopeStrings][CAIP-217] previously-authorized, including namespace-wide ones.
No known implementations in production took advantage of this affordance, as to date no RPC [namespaces] have been defined that could receive such requests regardless of [CAIP-2] network identifiers.

## Links

- [CAIP-2]: Network identifiers
- [CAIP-25]: Authorized session definition
- [CAIP-171]: Session identifiers for Authorized Sessions
- [CAIP-217]: Scope Definitions for Authorized Sessions
- [CAIP-316]: Managing Authorized Sessions With and Without Identifiers
- [Namespaces]: CASA RPC Namespaces

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[CAIP-316]: https://chainagnostic.org/CAIPs/caip-316
[namespaces]: https://namespaces.chainagnostic.org/
[RFC 2119]: https://www.ietf.org/rfc/rfc2119.txt

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
