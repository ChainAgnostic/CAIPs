---
caip: X
title: CAIP-10 for the GPG Namespace
author: Sebastian Martinez (@sebastinez)
discussions-to: https://github.com/ChainAgnostic/CAIPs/issues/65, https://github.com/ChainAgnostic/CAIPs/pull/53
status: Draft
type: Standard
created: 2021-09-02
updated: 2021-09-02
requires: X
---

## Simple Summary

This document is about the details of the GPG namespace and reference for CAIP-2.

## Abstract

In CAIP-2 a general identification scheme is defined. This is the
implementation of CAIP-2 for gpg (GnuPG).

## Motivation

See CAIP-2.

## Specification

### GPG Namespace

The namespace is called "gpg" referring to the [The GNU Privacy Guard](https://www.gnupg.org/) (aka GnuPG or GPG).

#### Reference Definition

I would suggest to use the different primitives as reference definitions.

### Resolution Method

To resolve a reference for the gpg namespace, check the [The GNU Privacy Guard](https://www.gnupg.org/).

## Rationale

We delegate the definition of the reference to the feature set of the GPG CLI, as a first step we should allow the GPG fingerprint.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# fpr 
gpg:fpr
```

## Links

- [The GNU Privacy Guard](https://www.gnupg.org/)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
