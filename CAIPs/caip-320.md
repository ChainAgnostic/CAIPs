---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: 320
title: Wallet Switch Chain JSON-RPC Method
author: Anh Tu Nguyen (@tuna1207), Chaitanya Potti (@chaitanyapotti), Tai Nguyen (@tanguyenvn)
discussions-to: <URL(s); if multiple, list separated by , without " or []>
status: Draft
type: Standard
created: 2025-01-06
updated: 2025-01-06
requires: 2, 25, 171, 316
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->

## Simple Summary

<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->

CAIP-320 introduces the wallet_switchChain method for a chain agnostic wallet to switch the current active chain of the wallet using the id specified in CAIP-2.

## Abstract

<!--A short (~200 word) description of the technical issue being addressed.-->

This proposal aims to define a standard method for decentralized applications to invoke a chain agnostic wallet to switch the current active chain of the wallet using the blockchain id specified in CAIP-2.

## Motivation

<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->

The motivation comes from the lack of standardization for multichain wallet to switch current active chain between multiple blockchain and define the expected JSON-RPC methods to be used by an application through a provider connecting to a signer or other user agent.

## Specification

<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in
uppercase in this document are to be interpreted as described in [RFC
2119][]

### Definition

The JSON-RPC provider is able to invoke a single JSON-RPC request accompanied by a [CAIP-2][] compatible `chainId` authorized by a pre-existing session.
If that pre-existing session was initiated by a [CAIP-25] response containing a [sessionId][CAIP-171], this `sessionId` value should also be returned at the top level of the `wallet_switchChain` envelope (see [CAIP-316] for more context on managing sessions with and without `sessionId` keys).

### Request

The application would interface with an JSON-RPC provider to make request as follows:

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "method": "wallet_switchChain",
  "params": {
    "sessionId": "0xdeadbeef",
    "chainId": "eip155:1"
  }
}
```

The JSON-RPC method is labeled as `wallet_switchChain` and expects two parameters, **two of them required**:

- **sessionId** (conditional) - [CAIP-171][] `SessionId` disambiguates an open session in a multi-session actor; it is required in some sessions, such as [CAIP-25][] sessions created by a response containing one, and SHOULD be omitted in other sessions, such as [CAIP-25] sessions created by a response not containing one (see [CAIP-316]).
- **chainId** (required) - a valid [CAIP-2][] network identifier, previously authorized by or within a `scopeObject` in the active session

### Validation

1. A respondent SHOULD check the `scope` against active session's `scopeObject`s before executing or responding to such a request, and SHOULD invalidate a request for a scope not previously authorized.
2. The respondent SHOULD check that `request.method` is authorized for the specified scope, and SHOULD invalidate a request for a scope not previously authorized.
3. The respondent MAY check that the `request.params` are valid for `request.method`, if its syntax is known to it.
4. The respondent MAY apply other logic or validation.
5. The respondent MAY chose to drop invalid requests or return an error message.

### Response

Upon successful validation, the respondent will switch the current active chain to the targeted network.

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "result": {
    "sessionId": "0xdeadbeef",
    "chainId": "eip155:1"
  }
}
```

#### Error Handling

##### Generic Failure Code

Unless the dapp is known to the wallet and trusted, the generic/undefined error response,

```jsonc
{
  "id": 1,
  "jsonrpc": "2.0",
  "error": {
    "code": 0,
    "message": "Unknown error"
  }
}
```

##### Trusted Failure Codes

More informative error messages MAY be sent in trusted-counterparty circumstances.
The core error messages over trusted connections are as follows:

The valid error messages codes are the following:

- User rejected request
  - code = 4001
  - message = "User disapproved request"
- Unrecognized chain
  - code = 4902
  - message = "Unrecognized chain"
- Chain not supported
  - code = 4903
  - message = "Chain not supported"

## References

<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations. See CONTRIBUTING.md#style-guide . -->

- [CAIP-2]: Network identifiers
- [CAIP-25]: Authorized session definition
- [CAIP-171]: Session identifiers for Authorized Sessions
- [CAIP-217]: Scope Definitions for Authorized Sessions
- [CAIP-316]: Managing Authorized Sessions With and Without Identifiers
- [wallet_switchEthereumChain]: Metamask switch chain ethereum JSON-RPC method

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[CAIP-316]: https://chainagnostic.org/CAIPs/caip-316
[wallet_switchEthereumChain]: https://docs.metamask.io/wallet/reference/json-rpc-methods/wallet_switchethereumchain/

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
