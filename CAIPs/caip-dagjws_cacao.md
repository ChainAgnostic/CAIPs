---
caip: <to be assigned>
title: DagJWS CACAO
author: Zach Ferland <zachferland>, Joel Thorstensson (@oed)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/162
status: Draft
type: Standard
created: 2022-10-25
updated: 2022-10-25
requires: CAIP-74
---

## Simple Summary

Create and verify IPLD based JWS payloads using chain-agnostic Object Capabilities and blockchain accounts.

## Abstract

This specification describes the construction and verification of DagJWS objects with CACAO. DagJWS being a subset of the [DAG-JOSE codec in IPLD](https://ipld.io/specs/codecs/dag-jose/spec/) to describe the serialization and signing of JWS objects in IPLD. CACAO being chain-agnostic Object Capabilities (OCAP) described in [CAIP-74](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md). Being a CAIP, the scope of this specification applies to using blockchain accounts by way of [did:pkh](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md), [did:key](https://w3c-ccg.github.io/did-method-key/) and [CAIP-74](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md). General specifications of DagJWS with CACAO would not necessarily rely on did:pkh or DIDs. 

## Motivation

With the building blocks of [did:pkh](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md), [IPLD DAG-JOSE](https://ipld.io/specs/codecs/dag-jose/spec/), [CAIP-74](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md), and [CAIP-122](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-122.md) we can describe an additional building block towards building a rich capability-based authorization system on an authenticated DAG based data structure.

Initial practical use cases include the ability to make multiple writes to an authenticated DAG based data structure (IPLD with dag-jose) using a blockchain account by way of a temporary key authorized with a CACAO. Allowing a user to only sign once with blockchain based account and/or wallet and then continue to sign many payloads for an authorized amount of time (session).

## Specification

### JWS with CACAO Construction

JWS CACAO support includes adding a `cap` parameter to the JWS Protected Header and specifying the correct `kid` parameter string. 

**“cap” Header Parameter**

The `cap` parameter maps to a URI string. ln the scope here this is expected to be an IPLD CID resolvable to a CACAO object. 

**“kid” Header Parameter**

The `kid` parameter references the key used to secure the JWS.  In the scope here this is expected to be a DID with reference to any key in the DID verification methods.  The parameter MUST match the `aud` target of the CACAO object for both the CACAO and corresponding signature to be valid together.

**Protected Header**

With `cap` currently not being a registered header parameter name in the IANA "JSON Web Signature and Encryption Header Parameters" registry, we treat this as a “Private Header Parameter Name” for now with additional meaning provided by the CACAO for implementations that choose to use this specification.  

Example 1: Protected JWS header with CACAO
```tsx
{ 
  "alg": "EdDSA",
  "cap": "ipfs://bafyreidoaclgf2ptbvflwalfrr6d4iqehkzyidwbzaouprdbjjfb4yim6q"
  "kid": "did:key:z6MkrBdNdwUPnXDVD1DCxedzVVBpaGi8aSmoXFAeKNgtAer8#z6MkrBdNdwUPnXDVD1DCxedzVVBpaGi8aSmoXFAeKNgtAer8"
 }
```

NOTE: Ignoring the `cap` header during validation still results in a valid JWS payload by the key defined in the ‘kid’. It just has no additional meaning by what is defined in the CACAO. The `cap` header parameter could also have support added as an extension by using the `crit` (Critical) Header Parameter in the JWS, but there is little reason to invalidate the JWS based on a consumer not understanding the `cap` header given it is still valid. 

### DagJWS with CACAO Construction

Given JWS with CACAO described in prior section, follow the DAG-JOSE specification and implementations for the steps to construct a given JWS with CACAO header and payload into a DagJWS. DagJWS is very similar to any JWS, except that the payload is a base64url encoded IPLD CID that references the JSON object payload. 

### DagJWS with CACAO Verification

The following algorithm describes the steps required to determine if a given DagJWS with CACAO is valid. 

1) Follow DAG-JOSE specification to transform a given DagJWS into a JWS. 

2) Follow JWS specifications to determine if the given JWS is valid. Verifying that the given signature paired with `alg` and `kid` in the protected header is valid over the given payload. If invalid, an error MUST be raised. 

3) Resolve the given URI in `cap` parameter of the projected JWS header to a CACAO JSON object. Follow the [CAIP-74 CACAO](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md) specification to determine if the given CACAO is valid. If invalid, an error MUST be raised. 

4) Ensure that the `aud` parameter of the CACAO payload is the same target as the `kid` parameter in the JWS protected header. If they do not match, an error MUST be raised.

### Example DagJWS with CACAO

Example IPLD dag-jose encoded block, strings abbreviated. 

```tsx
{ 
  cid: "bagcqcera2mews3mbbzs...quxj4bes7fujkms4kxhvqem2a",
  value: {
    jws: { 
      link: CID("bafyreidkjgg6bi4juwx...lb2usana7jvnmtyjb4xbgwl6e"),
      payload: "AXESIGpJjeCjiaWv...LKw6pIDQfTVrJ4SHlwmsvx", 
      signatures: [
        {
          protected: "eyJhbGciOiJFZERTQSIsImNh...GU2djZEpLTmhYSDl4Rm9rdEFKaXlIQiJ9"
          signature: "6usTYvu5KN0LFTQsWE9U-tqx...h60EgfvjL_rlAW7_tnQUl84sQyogpkLAQ"
        }
      ]
    }
  }
}
```

If `block.value.jws.signatures[0].protected` is decoded, you would see the following object, a JWS protected header as described above:

```tsx
{
  "alg": "EdDSA",
  "cap": "ipfs://bafyreidoaclgf...yidwbzaouprdbjjfb4yim6q",
  "kid": "did:key:z6Mkq2ZyjGV54ev...hXH9xFoktAJiyHB#z6Mkq2ZyjGV54ev...hXH9xFoktAJiyHB"
}
```

## Links

- [CAIP-74 CACAO - Chain Agnostic CApability Object](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md)
- [CAIP-122 "Sign-in with X"](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-122.md)
- [IPLD](https://ipld.io/)
- [DAG-JOSE codec](https://ipld.io/specs/codecs/dag-jose/spec/) 
- [did:pkh](https://github.com/w3c-ccg/did-pkh/blob/main/did-pkh-method-draft.md)
- [did:key](https://w3c-ccg.github.io/did-method-key/)
- [js-did implementation with CACAO](https://github.com/ceramicnetwork/js-did/tree/main/packages/dids)
- [js-dag-jose implementation](https://github.com/ceramicnetwork/js-dag-jose)

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
