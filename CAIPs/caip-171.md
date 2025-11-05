---
caip: 171
title: Session Identifiers
author: Olaf Tomalka (@ritave)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/176
status: Review
type: Standard
created: 2022-11-09
---

## Simple Summary

This CAIP defines an common identifier for representing an open session with a
wallet, including both in-browser session tokens and API-based connection IDs.

## Motivation

Currently, sessions with wallet clients are tracked differently across different
architectures: in-browser MetaMask sandboxes instance-specific connections based
on origin which are referred to by tokens in the browser, while WalletConnect
uses a topic-based pub/sub protocol keyed to a unique topic string, etc.
Aligning specific aspects of session state can be hard without shared
assumptions about session boundaries and tracking. A minimal assumption is that
sessions are tracked by identifiers, and that different actors can update the
session accordingly.

## Specification

> Such sections are considered non-normative.

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in
uppercase in this document are to be interpreted as described in [RFC
2119](https://www.ietf.org/rfc/rfc2119.txt)

### Definition

Whenever a CAIP uses the name `SessionIdentifier` and has this CAIP in the
`required` front-matter property, it SHALL be interpreted as reference to this
specification.

> Notice that there are no code constraints on the token value. This is by
> design and the value is implementation-dependent.

```typescript
type SessionIdentifier = string;
```

Properties of the `SessionIdentifier` are as follows:

1. It MUST uniquely identify an open and stateful session.
2. It MUST identify a closeable session, and it MUST become invalid after a
   session is closed.
3. It MUST remain the same as the identified session's state changes.
4. It MUST be serializable into JSON. Serialization and later deserialization
   using JSON MUST result in the same value.
5. It MUST be generated from a cryptographically random source and include at
   least 96 bits of entropy for security.

## Copyright

Copyright and related rights waived via
[CC0](https://creativecommons.org/publicdomain/zero/1.0/).
