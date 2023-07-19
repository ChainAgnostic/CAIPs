---
caip: 211
title: JSON-RPC Authority Negotiation
author: Hassan Malik (@hmalik88), Juan Caballero (@bumblefudge)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/211, https://github.com/ChainAgnostic/CAIPs/pull/207 
status: Draft
type: Informational
created: 2023-02-02
updated: 2023-02-02
requires: [2, 10, 25, 171, 217]
---

## Simple Summary

The establishment of a connection between a decentralized application (web-based
or otherwise) and a wallet or other authenticator (browser-based or otherwise)
requires a shared understanding of capabilities and target networks as well as
mutual trust. This specification gives wallets and decentralized applications a
way of signalling to one another, in CAIP-25 or in other session-initiation
ceremonies, capabilities and current network information, whether for feature
discovery, user experience optimizations, or just for establishing trust.

## Abstract

This document defines a syntax for expressing JSON-RPC capabilities as documents
passed by reference and expressing networking preferences via ordered arrays of
network identifiers. Adding one or more of these URIs to the arrays defined here
to an [authentication scopes][CAIP-217] in a [CAIP-25][] exchange or equivalent
"handshake protocols" between wallet and counterparty enables that wallet and
its counterparty to mutually negotiate not just which JSON-RPC methods they will
support, but which exact versions of them (in the case of divergent, evolving,
or decentralized RPC interfaces within a given namespace), as well as
negotiating preferential routing of requests to special nodes (which may include
nodes with additional capabilities expressed in the aforementioned documents).

## Rationale

It is important to note that URIs are not constrained here, and diverse usages
of this syntax can be expected to emerge from the circumstances of each
decentralized ecosystem. For example, tightly coordinated ecosystems that share
a universal RPC interface across all ledgers/networks and wallets/clients might
simply use canonical URLs for versioning the capabilities of a given wallet
(i.e., `https://json.example.com/rpc/v1` and
`https://json.example.com/rpc/v1.1`). In other ecosystems where each network can
layer on network-specific methods to the runtimes of its nodes, each network
could be expected to document these unique capabilities at a stable address,
along with network-specific endpoints for routing traffic to them. On some
networks, these endpoints are registered with an authoritative registry like the
`ethereum_lists` registry containing network IDs for the ethereum ecosystem
(which can be queried by [CAIP-2][]).

In today's decentralized wallet/dapp ecosystems, these values are often set
manually in the case of network routing and assumed mutually by developers of
both sides of the handshake in the case of RPC capabilities. Progressively more
competitive and decentralized development, however, is increasingly making
feature discovery and explicit authority negotiation necessary to security
across low-trust or high-fraud contexts. For this reason, the specification
below assumes a progression from "**assumed**" values (passed out of band, hard
to validate), "**implicit**" values (universal to all parties in a namespace as
a well-known default) and "**explicit**" values (explicitly set by the two
counterparties negotiating authorities and routes). Implicit values get much
easier to negotiate (or make explicit in negotiation) once a [namespace-wide
profile][namespaces] has been written for this document; see the [Security
Considerations](#security-considerations) section below for more on how to
handle previously-unknown values, namespaces with no consensus on implicit
authorities, etc.

## Motivation

This deterministic expression of variations in RPC routing and behavior is
motivated by both security and user experience, since informed user consent and
reactive rendering in the web context are both made much easier by explicit
negotiation between user agent and counterparty. This expression syntax,
extending other negotiation protocols for user agent/counterparty negotiations
over the web, allows flexible, progressive, and explicit protocols to evolve
over time. It could also combine with out-of-band coordination to enable
experimentation and extension to develop within certain local contexts (like a
specific network within a namespace, or a specific community of usage of a
network) without compromising the integrity and security of the broader
community.

## Specification

### Syntax

The properties `rpcDocuments` and `rpcEndpoints` in an Authorization Scope
object (defined in [CAIP-217][]) are both defined as an ordered array of
strings. 

Each of the members of the `rpcDocuments` array MUST be a valid URI
that dereferences to a stable document. It is RECOMMENDED that URLs be
canonicalized and expressed in all lowercase to minimize risk of false
mismatches.

Each of the members of the `rpcEndpoints` array MUST be a valid URI that can be
used to connect to an RPC endpoint.

### Assumed values

Many namespaces have a single, authoritative RPC definition (whether
human-readable, machine-readable, or both). Switching between networks is either
done by wallets and consumers of the network being pre-configured to know a set
of definitive endpoints per network, or getting those values from a definitive
registry at first connection to a registered network (See [CAIP-2][]). 

Arriving at definitive versions of these RPC documents, archival publication
methods for them, and canonical forms for the URIs by which they should be
referred to all take a degree of consensus or authority within a namespace's
ecosystem. Once this consensus exists, these assumed values can be encoded as a
[namespaces profile][namespaces] of this document. Until such consensus,
however, assumed values are hard to test and should be negotiated explicitly and
validated if unfamiliar.

### Implicit values

Assuming a [namespaces profile of this document][namespaces] exists for a given
namespace `X`, which defines the implicit RPC endpoints `Y1`, `Y2` and `Y3`, and
the implicit RPC document `Z`, then the scope object

```jsonc
{
  "X": {
    methods: [A, B, C],
    notifications: [D, E, F]
  }
}
```

SHOULD be interpreted as functionally equivalent to the scope object: 

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

because the values `[Y1, Y2, Y3]` are implicit in any `X` [Authorization
Scope][CAIP-217] that has not set `rpcEndpoints`, just as `Z` is the only member
of the implicit `rpcDocuments` unless any other members are set.

### Explicit values

Additional values may be set for a given `scopeObject`. 

### Ordering & Parsing

In the case of the `rpcEndpoints` array, the semantics of the ordering is
contextual to the namespace are not defined universally, as networking models
vary between namespaces. 

In the case of `rpcDocuments`, however, the ordering MUST be reflected by a
strictly heirarchical parsing of the documents: each document after the first
MUST only add (and MUST NOT re-define) any methods or constants already defined,
iteratively through the array. I.e., once a user-agent and counterparty have
negotiated and persisted an authorization scope enumerating multiple
`rpcDocuments`, both parties SHOULD interpret the authorization of any `methods`
for that scope (see [CAIP-25][] and [CAIP-217][]) as refering to the FIRST
definition of that method's name which appears in the list of documents.

#### Parsing Implicit Values

In the context of a [CAIP-25][] negotiation, a requesting party may define
explicit values for `rpcEndpoints` or `rpcDocuments` without including the
implicit values. Extending the example above, we could say that the request:

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
MUST be interpreted as replacing rather than appending to the implicit values.
It MUST NOT be interpreted as equivalent to:

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
## CAIP-25 Example

Since [CAIP-25][] obligates both parties in an authorization negotiation to
persist and honor whatever `scopeObject`s they agree to, including the ordering
of arrays, it is important to resolve all implicit and explicit members at the
same negotiation step where `requiredScopes` and `optionalScopes` get merged
into the `sessionScopes` both parties will persist. We can illustrate this
resolution and all the possibilities open to a respondent by extending the
examples above, in which a request omits implicit values Y1, Y2, Y3, and Z:

```jsonc
{
  "requiredScopes": {
    "X": {
      methods: [A, B, C],
      notifications: [D, E, F],
    }
  },
  "optionalScopes": {
    "X": {
      methods: [A, B, C],
      notifications: [D, E, F],
      rpcEndpoints: [U1, U2, U3],
      rpcDocuments: [V]
    }
  },
}
```

Since the ordering of arrays expresses authority or priority in case of
conflicts, we can illustrate three different security postures, explained below:

```jsonc

// Security Posture 1: Implicit value prioritized, requested values treated as extensions
{
  ...
  "sessionScopes": {
    "X": {
      methods: [A, B, C],
      notifications: [D, E, F],
      rpcEndpoints: [Y1, Y2, Y3, U1, U2, U3],
      rpcDocuments: [Z, V]
    }
  }
  ...
}

// Security Posture 2: Explicit prioritized, but implicit values preserved
{
  ...
  "sessionScopes": {
    "X": {
      methods: [A, B, C],
      notifications: [D, E, F],
      rpcEndpoints: [U1, U2, U3, Y1, Y2, Y3],
      rpcDocuments: [V, Z]
    }
  }
  ...
}

//Security Posture 3: Requested values only, explicit deauthorization of implicit values.
{
  ...
  "sessionScopes": {
    "X": {
      methods: [A, B, C],
      notifications: [D, E, F],
      rpcEndpoints: [Y1, Y2, Y3],
      rpcDocuments: [Z]
    }
  }
  ...
}

//Security Posture 4: Implicit values only, explicit deauthorization of requested values.
{
  ...
  "sessionScopes": {
    "X": {
      methods: [A, B, C],
      notifications: [D, E, F],
      rpcEndpoints: [U1, U2, U3],
      rpcDocuments: [V]
    }
  }
  ...
}


```

Option 1 would be the overwhelmingly most common, since implicit values are
essentially the baseline consensus of an entire namespace once they have been
set namespace-wide (i.e. a canonical document defining all network-wide basic
RPC functionality). Any possible conflict between this and an extension should
be sandboxed by not allowing an extension to override baseline except in
high-trust contexts.

Option 2 would likely be quite rare, as a respondent and/or its controlling
end-user would have to have a lot of out-of-band trust in an extension or local
authority to allow it to override systemwide defaults. This could be thought of
as "advanced mode" or "high-trust mode", and likely only enabled for a subset of
end-users, perhaps negotiated by progressive re-authorization over time.

Similarly, Option 3 would only make sense in special cases with meaningful
consent of an end-user that knows they are entering a special-case and familiar
UX and security assumptions. It could be thought of as "developer mode" or
"alternate network configuration", and would getting meaningful consent from
the end-user.

Note that these three options are not exhaustive, just illustrative, and many
combinations are also possible, i.e. mixing and matching the `rpcDocuments`
values and `rpcEndpoints` values of different security postures.

## Security Considerations

The main security risk in accepting multiple authorities (expressed as URIs,
which may be mutable) is that ambiguity and undesired behavior can arise from
multiple RPC documents defining the same term, or different endpoints producing
different outcomes. The solution to this is to assign priority by array ordering
in the context of negotiation. In a protocol like [CAIP-25][], this means
achieving consensus between parties on the exact priority before initiating a
session. Progressive authorization of extensions or additional authorities may
also be deferred to later in the session to minimize de-anonymization or
fingerprinting risks at initial connection.

### Previously-Unknown Values

When a user-agent like a wallet receives a request for a connection that
includes unknown RPC endpoints, it can validate those unknown RPC endpoints by
testing them or querying a trusted authority; it can drop the request; or it can
counterpropose a connection defined by [Authorization Scopes][] explicitly
declaring only endpoints known to it. Validation of unknown RPC endpoints or
getting explicit user consent would rarely be justified in a namespace like the
[EIP155 namespace][EIP155 namespace] of EVM-compatible networks where 
[CAIP-2][]-style chain identifiers have RPC endpoints definitively associated
with them by an authoritative registry.

The same three options apply when presented with an unknown RPC document, but in
this case validating the document by fetching and parsing it is less of a
security risk. Furthermore, fetching an unknown document "live" (at time of
connection request) is more likely to justify the compute and delay, since
comparing it to the union of all RPC documents known to the wallet (and/or to
its own capabilities) could show the request to be a subset of these and
permissible, in some cases even without user input.

### Trust Infrastructure & Out-of-Band Trust

For better or for worse, a dominant pattern in decentralized user experience is
instructing end-users to open the "advanced settings" of their agents and
manually enter custom network information, exceptions to security policies, etc.
This "manual" and user-initiated flow is one way of supporting wallets not
compiled with support for (i.e. knowledge of) these networks or capabilities.
Another option is centralizing trust in namespace-wide authorities queried in
realtime, periodically publishing verifiable trust-list and allow-list documents
in registries, etc.

### Self-attested Capabilities and Dunning-Kruger Risks

It is important to explicitly recognize that wallets and their callers may
overstate their capabilities or their conformance to specific versions of those
codified into stable documents, just as the URI identifiers used to point to
those document may prove stabler than their referents (and have little way to
enforce their counterparty's expectations of archival immutability and
availability). Identifying malicious wallets impersonating other wallets or
falsifying their capabilities is beyond the scope of this specification and will
likely require orthogonal mechanisms.

## Privacy Considerations

The trust model of custom RPC endpoints and/or definition documents is complex
and reputation/discovery systems are still emerging on a per-chain basis in many
ecosystems. For this reason, negotiation protocols like [CAIP-25][] are best
implemented iteratively and progressively to avoid malicious dapps partially
deanonymizing wallets by profiling their support for custom RPCs (e.g., by
"overasking" upfront).  For this reason, as with the initial CAIP-25 exchange,
discovery requests rejected due to user input, due to security policy, and due
to non-support at the wallet software level should not be distinguished at the
RPC level by verbose or explanatory responses. To the same ends, in the
[extended CAIP-25 example](#caip-25-example) a caller should not be able to tell
whether a wallet responded to the maximum of its capabilities or a subset of
those defined by security policy and security posture.

## References

- [CAIP-2][] - Chain ID Specification
- [CAIP-10][] - Account ID Specification
- [CAIP-25][] - JSON-RPC Provider Request
- [CAIP-75][] - Blockchain Reference for the Hedera namespace
- [CAIP-171][] - Session Identifier Specification
- [CAIP-217][] - Authorization Scopes

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[CAIP-25]: https://chainagnostic.org/CAIPs/caip-25
[CAIP-75]: https://chainagnostic.org/CAIPs/caip-75
[CAIP-104]: https://chainagnostic.org/CAIPs/caip-104
[CAIP-170]: https://chainagnostic.org/CAIPs/caip-170
[CAIP-171]: https://chainagnostic.org/CAIPs/caip-171
[CAIP-217]: https://chainagnostic.org/CAIPs/caip-217
[namespaces]: https://namespaces.chainagnostic.org
[EIP155 namespace]: https://namespaces.chainagnostic.org/eip155/README
[RFC3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
