---
caip: 211
title: JSON-RPC Authority Negotiation
author: Hassan Malik (@hmalik88), Juan Caballero (@bumblefudge)
discussions-to: ["https://github.com/ChainAgnostic/CAIPs/pull/207", "https://github.com/ChainAgnostic/CAIPs/pull/211"]
status: Draft
type: Informational
created: 2023-02-02
updated: 2023-02-02
requires: [2, 10, 25, 171]
---

## Simple Summary

CAIP-211 defines the behavior and semantics for both implicit and explicit RPC
documents (which define methods and notifications for a given namespace or scope
within one) and RPC endpoints (i.e. preferential routing for specific nodes). 

## Abstract

Without a profile of this CAIP which defines implicit values for a namespace,
setting the `rpcDocuments` and `rpcEndpoints` values in the `scopeObject`s of a
[CAIP-25][] negotiation does not make much sense. Once those implicit values
have been set, however, the meaning of any explicit values in [CAIP-25][]
negotiations or other scope expressions can be deterministic and evolve over
time.

## Motivation

Allowing experimentation and extension within certain local contexts (like a
specific network within a namespace, or a specific community of usage of a
network) without compromising the integrity and security of the broader
community requires a flexible and layer mechanism for negotiating authorities
over routing and RPC method/notification definitions. 

## Specification

### Implicit values

Many namespaces have a single, authoritative RPC definition (whether
human-readable, machine-readable, or both) and a set of endpoints which can be
considered definitive. Where these can be referred to by a static URI and this
URI is described in a [namespace profile][namespaces] of this CAIP, these static
URIs are the "implicit" endpoint and document values for those namespaces.

For a given namespace `X`, if the implicit RPC endpoints are `Y1`, `Y2` and
`Y3`, and the implicit RPC document is `Z`, then the scope object

```jsonc
{
  "X": {
    methods: [A, B, C],
    notifications: [D, E, F]
  }
}
```

SHOULD be interpreted as equivalent to the scope object: 

```jsonc
{
  "X": {
    methods: [A, B, C],
    notifications: [D, E, F],
    rpcEndpoints: [Y1, Y2, Y3],
    rpcDocuments: [Z]
  }
}
```

### Explicit values

Additional values may be set for a given `scopeObject`. 

### Ordering 

In the case of `rpcEndpoints`, priority is contextual and not defined
universally. In the case of `rpcDocuments`, priority is more definitive: each
document after the first MUST only add (and MUST NOT re-define) any terms
already defined, iteratively through the array.

For this reason, if a namespace has defined stable URIs for default
`rpcDocuments` and `rpcEndpoints`, these SHOULD be defined in a namespace
profile. These implicit values allow extensions and variations from those
defaults to be negotiated, such as by [CAIP-25][] or other discovery protocols.

In the context of a CAIP-25 negotiation, a requesting party may define explicit
values for `rpcEndpoints` or `rpcDocuments` without including the implicit
values. Extending the example above, we could say that the request:

```jsonc
{
  "X": {
    methods: [A, B, C],
    notifications: [D, E, F],
    rpcEndpoints: [U1, U2, U3],
    rpcDocuments: [V]
  }
}
```
MUST be taken literally, as dropping the implicit values rather than appending
to them. It MUST NOT be interpreted as equivalent to:

```jsonc
{
  "X": {
    methods: [A, B, C],
    notifications: [D, E, F],
    rpcEndpoints: [Y1, Y2, Y3, U1, U2, U3],
    rpcDocuments: [Z, V]
  }
}
```

But the latter SHOULD be returned as response if dropping the implicit values in
favor of the requested ones is unacceptable and extending the implicit values is
preferred. 

## Privacy Considerations

The trust model of custom RPC endpoints and/or definition documents is complex
and reputation/discovery systems are still emerging on a per-chain basis in many
ecosystems. For this reason, negotiation protocols like [CAIP-25][] are best
constructive iteratively and progressively to avoid malicious dapps partially
deanonymizing wallets by profiling their support for custom RPCs (e.g., by
"overasking" upfront).  For this reason, as with the initial CAIP-25 exchange,
discovery requests rejected due to user input, due to security policy, and due
to non-support at the wallet software level should not be distinguished at the
RPC level.

## References

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
