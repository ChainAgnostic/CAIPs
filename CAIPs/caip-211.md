---
caip: 211
title: JSON-RPC Authority Negotiation
author: Pedro Gomes (@pedrouid), Hassan Malik (@hmalik88)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/207
status: Draft
type: Standard
created: 2023-02-02
updated: 2023-02-02
requires: [2, 10, 25, 171]
---

## Simple Summary

CAIP-211 extends CAIP-25 to allow callers and respondents to anchor feature
discovery in specific RPC semantics and to request the respondent route requests
to specific RPC endpoints.

## Abstract

CAIP-211 defines additional properties that enable progressive usage of CAIP-25
to layer custom or local RPC semantics and/or routing onto a session. Since
CAIP-25 respondents ignore unknown properties, respondents that conform to
CAIP-25 but not to CAIP-211 should be carefully considered and accomodated by
implementers. 

## Motivation

While some core methods and notifications are foundational to entire namespaces
and thus almost universally defined out-of-band (meaning all callers and
respondents agree to them already), others are specific to chains or even to
subsets of wallets and dapps on a given chain.  This requires scope objects in
CAIP-25 requests to negotiate semantic anchors and/or network routing
**before** authorization can occur in the confidence that both parties agree to
the syntax and semantics of a given method or notification, which may only be
available on certain RPC endpoints.

The concept of a custom RPC endpoint or definition presumes the existence of
"default" or universal endpoints and definitions, which can be hard to anchor in
explicit specifications unless a namespace profile of this CAIP has been
published. (This may be unnecessary in the case of younger namespaces without
customization built in at the per-chain or per-dapp layer). An empty array of
`rpcEndpoints` or `rpcDocuments` sent in either direction should be interpreted
differently than the absence of the array.  Namely, either array being present
but empty in a request signals that a caller is requesting that custom endpoints
or  definitions NOT be considered in CAIP-25 authorizations. Present but empty
arrays in a response confirms this behavior, while its absence can be taken to
mean the wallet either does not support custom endpoints/definitions or the user
has opted out of that degree of trust, which for privacy/fingerprinting reasons,
should not be handled by distinct codepaths (See Privacy Considerations).

## Specification

CAIP-211 adds two properties, either of which can be set in `requiredScopes` or 
in `optionalScopes` and, if authorized, optionally returned in `sessionScopes`.
These are both [ordered] strings of arrays:

1. `rpcEndpoints` is an array of zero or more URLs of RPC endpoints that
   the caller would prefer the respondent to use, ordered by preference. Each
   must be a valid URL that addresses an RPC endpoint. The respondent may
   return it empty, reordered, with less, the same, or even more conformant URLs
   than received.
2. `rpcDocuments` is an array of zero or more URLs of machine-readable OpenRPC
   documents that the caller would prefer the respondent to use, ordered by
   preference. This set of document collectively defines at least syntactically
   if not also semantically any methods and/or notifications authorized by the
   CAIP-25 authorization, in DESCENDING heirarchical authority. (For example,
   any methods or notifications defined differently by multiple authorites will
   be interpreted by whichever authority is closer to the 0-index of the array).
   Each must be a valid URL that addresses a valid openRPC document. The
   respondent may return it empty, reordered, with less, the same, or even more
   conformant URLs than received.

### Request

A CAIP-211 request is a valid CAIP-25 except for the two additional properties. 

Example:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "provider_authorization",
  "params": {
    "requiredScopes": {
      "eip155": {
        "chains": ["eip155:1", "eip155:137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "eth_sign", "get_balance", "personal_sign"],
        "notifications": ["accountsChanged", "chainChanged"]
      },
      "eip155:42069": {
        "methods": ["get_balance", "chainChanged", "42069_sEcReTbAlAnCe"],
        "rpcDocuments": ["https://openrpc.42069-chain.org/", "https://raw.githubusercontent.com/ethereum/execution-apis/assembled-spec/refs-openrpc.json"],
        "rpcEndpoints": ["https://node1.42069-chain.org/"]
      },
      "cosmos": {
        ...
      }
    },
    "optionalScopes":{
      "eip155:42161": {
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "personal_sign"],
        "events": ["accountsChanged", "chainChanged"]
    },
    "sessionProperties": {
      "expiry": "2022-12-24T17:07:31+00:00",
      "caip154-mandatory": "true"
    }         
  }
}
```

### Response

The wallet can respond to this method with either a success result or an error message.

#### Success

A succesful result will be a conformant successful result according to
[CAIP-25][], with the additional presence (if authorized) of the new properties
in one or more scope objects.

An example of a successful response follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "sessionScopes": {
      "eip155": {
        "chains": ["eip155:1", "eip155:137"],
        "methods": ["eth_sendTransaction", "eth_signTransaction", "get_balance", "eth_sign", "personal_sign"]
        "events": ["accountsChanged", "chainChanged"],
        "accounts": ["eip155:1:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb", "eip155:137:0xab16a96d359ec26a11e2c2b3d8f8b8942d5bfcdb"]
      },
      "eip155:10": {
        "methods": ["get_balance"],
        "events": ["accountsChanged", "chainChanged"],
        "accounts:" []
      },
      "eip155:42069": {
        "methods": ["get_balance", "chainChanged", "42069_sEcReTbAlAnCe"],
        "rpcDocuments": ["https://raw.githubusercontent.com/ethereum/execution-apis/assembled-spec/refs-openrpc.json", "https://openrpc.42069-chain.org/"],
        "rpcEndpoints": ["https://node1.42069-chain.org/"]
      }
        //...
    },      
    "sessionProperties": {
      "expiry": "2022-11-31T17:07:31+00:00"          
    }
  }
}
```

Note:
* The response re-ordered the RPC documents array, which still allows the
  42069-chain docs to define new methods such as the namespaced secret-balance
  method example. However, since the ordinality of the array prioritizes earlier
  authorities in any case where two define the same term, the chain-specific
  authority is effectively prevented from redefining any methods defined in the
  eip155-wide standard, even in the limited scope of the 42069 chain. This is
  purely for illustrative purposes and not normative.

#### Failure States

##### TODO: 

An example of an error response should match the following format:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": 5000,
    "message": "Unknown error"
  }
}
```

The additional error messages codes to be supported in addition to CAIP-25s are the following:

##### TODO: replace following with novel errors

Possible error messages (to be discussed with WG):
- rpcDocuments not conformant syntactically (not openRPC, not served as mime type JSON, etc)

Are these error messages required at protocol level or are they implementation-specific?
- ~~rpcDocuments rejected by user input OR wallet does not support custom RPC documents~~
  - ^ no response in either case
- rpcDocuments rejected by policy/in principle
- rpcDocuments unreachable/404
- ~~rpcEndpoints rejected by user input OR wallet does not support custom RPC documents~~
  - ^ no response in either case
- rpcEndpoints rejected by policy/in principle
- rpcEndpoints unreachable/404
- rpcEndpoints URL malformed

* Unknown error OR no requested scopes were authorized
    * code = 5000
    * message = "Unknown error"
* When user disapproves accepting calls with the request methods
    * code = 5001
    * message = "User disapproved requested methods"
* When user disapproves accepting calls with the request events
    * code = 5002
    * message = "User disapproved requested events"
* When wallet evaluates requested chains to not be supported
    * code = 5100
    * message = "Requested chains are not supported"
* When wallet evaluates requested methods to not be supported
    * code = 5101
    * message = "Requested methods are not supported"
* When wallet evaluates requested events to not be supported
    * code = 5102
    * message = "Requested events are not supported"
* When a badly-formed request includes a `chainId` mismatched to scope
    * code = 5103
    * message = "Scope/chain mismatch"
* When a badly-formed request defines one `chainId` two ways
    * code = 5104
    * message = "ChainId defined in two different scopes"  
* Invalid Session Properties Object
    * code = 5200
    * message = "Invalid Session Properties requested"
* Session Properties requested outside of Session Properties Object 
    * code = 5201
    * message = "Session Properties can only be optional and global"

## Security Considerations

##### TODO
- what happens if wallet reorders Documents and dapp ignores that? should we
  make more explicit how important the ordering of those arrays is, and that
  neither party can guarantee how the other is ordering them?

## Privacy Considerations

The trust model of custom RPC endpoints and/or definition documents is complex
and reputation/discovery systems are still emerging on a per-chain basis in many
ecosystems. For this reason, iterations of CAIP-25 should be considered a
delicate negotiation best done progressively to avoid malicious dapps partially
deanonymizing wallets by profiling their support for custom RPCs (i.e.
overasking).  For this reason, as with the initial CAIP-25 exchange, discovery
requests rejected due to user input, due to security policy, and due to
non-support at the wallet software level should not be distinguished as the RPC
level.

##### TODO

## Changelog

## Links

- [CAIP-2][] - Chain ID Specification
- [CAIP-10][] - Account ID Specification
- [CAIP-25][] - JSON-RPC Provider Request
- [CAIP-75][] - Blockchain Reference for the Hedera namespace
- [CAIP-171][] - Session Identifier Specification

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-75]: https://chainagnostic.org/CAIPs/caip-75
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[namespaces]: https://namespaces.chainagnostic.org
[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6
[CAIP-170]: https://chainagnostic.org/CAIPs/caip-170

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
