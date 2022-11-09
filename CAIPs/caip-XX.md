---
caip: 171
title: Session tokens
author: Olaf Tomalka (@ritave)
discussions-to: <URL>
status: Draft
type: Standard
created: 2022-11-09
---

## Simple Summary

This CAIP defines a token that is used to represent an open session when communicating with a wallet

## Motivation

Currently, wallets use different ways to manage open sessions, MetaMask maintains connection based on origin, WalletConnect uses topics, etc. This becomes hard to implement and has different edge cases depending on the source. One way to identify an open session no matter the source of the session is required.

## Specification

> Such sections are considered non-normative.

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT",
"SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and
"OPTIONAL" written in uppercase in this document are to be interpreted as described in [RFC 2119](https://www.ietf.org/rfc/rfc2119.txt)

### Definition

Whenever a CAIP uses a name `SessionToken` and has this CAIP in the `required` front-matter property, it SHALL be interpreted as reference to this specification.

> Notice that there are no code constraints on the token value itself. This is by design and the value is implementation dependent.

```typescript
type SessionToken = string;
```

`SessionToken` value MUST uniquely identify an open session. It MUST become invalid after a session is closed.

It MUST be serializable into JSON. Serialization and later deserialization using JSON MUST result in the same value.

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
