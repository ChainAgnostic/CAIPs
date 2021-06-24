---
caip: X
title: Blockchain Reference for the Radicle Namespace
author: Sebastian Martinez (@sebastinez)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/52, https://github.com/ChainAgnostic/CAIPs/pull/53
status: Draft
type: Standard
created: 2020-06-24
updated: 2020-06-24
requires: X
---

## Simple Summary

This document is about the details of the Radicle namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general identification scheme is defined. This is the
implementation of CAIP-2 for rad (Radicle).

## Motivation

See CAIP-2.

## Specification

### Radicle Namespace

The namespace is called "rad" as in [Radicle URNs](https://docs.radicle.xyz/docs/understanding-radicle/how-it-works#radicle-urns)

#### Reference Definition

The definition is delegated to Radicle. The format corresponds to the commonly used abbreviation for each VCS that will be supported by the Radicle protocol.

### Resolution Method

To resolve a reference for the rad namespace, visit [Radicle Docs](https://docs.radicle.xyz/docs/understanding-radicle/how-it-works) and check the supported VCS protocols.

## Rationale

We delegate the definition of the reference to be used to Radicle, since they are the maintainers and creators of the [Radicle URNs](https://docs.radicle.xyz/docs/understanding-radicle/how-it-works#radicle-urns).

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# git
rad:git

# Mercurial 
rad:mercurial

# pijul
rad:pijul
```

## Links

- [Radicle URNs](https://docs.radicle.xyz/docs/understanding-radicle/how-it-works#radicle-urns)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
