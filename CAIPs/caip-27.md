---
caip: 27
title: JSON-RPC Provider Request
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/27
status: Draft
type: Standard
created: 2020-12-12
updated: 2023-03-02
requires: ["2", "25", "171", "217"]
---

## Simple Summary

CAIP-27 defines a generic JSON-RPC method for routing method calls to a context
defined by a valid [scopeObject][CAIP-217] and a tagged with a
[sessionId][CAIP-171] for maintaining session continuity. 

## Abstract

This proposal has the goal to define a standard method for decentralization
applications to request JSON-RPC methods from user agents (such as
cryptocurrency wallets) directed to a given previously-authorized target network
(such as a specific blockchain or consensus community within a protocol). It
requires a valid [scopeObject][CAIP-217] and a valid [sessionId][CAIP-171] for
interoperability and composability. These two properties MAY be inherited from a
persistent session created by [CAIP-25][], but also supports other kinds of
sessions.

## Motivation

The motivation comes from the ambiguity that comes from interfacing with a
multi-network agent (e.g. a cryptocurrency wallets which supports the same
method on multiple chains in a namespace, or supports methods with the same name
on multiple namespaces). 

## Specification

The JSON-RPC provider is able to make one or more JSON-RPC requests accompanied
by a [CAIP-2][] compatible `chainId` and a keyed to the [sessionId][CAIP-171] of
a pre-existing session. 

### Request

The application would interface with an RPC provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "provider_request",
  "params": {
    "sessionId": "0xdeadbeef",
    "scope": "eip155:1",
    "request": {
      "method": "personal_sign",
      "params": [
        "0x68656c6c6f20776f726c642c207369676e2074657374206d65737361676521",
        "0xa89Df33a6f26c29ea23A9Ff582E865C03132b140"
      ]
    }
  }
}
```

The JSON-RPC method is labeled as `caip_request` and expects 
three **required parameters**:

- **sessionId** - [CAIP-171][] `SessionId` referencing a known, open session
- **scope** - a valid `scopeObject` previously authorized to the caller and persisted in
  the session identified by `sessionId`
- **request** - an object containing the fields:
  - **method** - JSON-RPC method to request
  - **params** - JSON-RPC parameters to request (may be empty but must be set)

### Validation

1. A respondent MUST check the `scope` against the identified session object
before executing or responding to such a request.
2. The respondent SHOULD check that `request.method` is authorized in the
session object.
3. The respondent MAY check that the `params` are valid for that method, if its
   syntax is known to it.
4. The respondent MAY apply other logic or validation.
5. The respondent MAY chose to drop invalid requests or return an error message.

### Response

Upon succesful validation, the respondent will submit or route the request to the targeted network. If the targeted network returns a response to the respondent, the respondent MAY forward this response to the caller.

## Links

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
