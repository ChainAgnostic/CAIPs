---
caip: <to be assigned>
title: Browser Session Token Model
author: <a list of the author's or authors' name(s) and/or username(s), or name(s) and email(s), e.g. (use with the parentheses or triangular brackets): Olaf Tomalski (@ritave), Hassan BM (@hbm-88), and Juan Caballero (@bumblefudge)
discussions-to: <URL>
status: Draft
type: Standard
created: 2022-11-08
updated: 2022-11-08
requires (*optional): <CAIP number(s)>
replaces (*optional): <CAIP number(s)>
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
This data model proposes a minimal set of properties that structure a browser-based wallet<>dapp session for interoperability and security purposes.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
In order to have common security assumptions about wallet<>dapp transport across browser-based cryptocurrency wallets and native application cryptocurrency wallets, it is helpful for browser-dapp sessions be able to express sessions in a simple, shared data model.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Wallet<>dapp sessions have historically been structured on an ad hoc basis but aligning feature discovery and other protocols benefits from negotiating these in an explicit manner such that further negotations can refer back to them.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->
NOTE: the following is a very naïve straw man proposed for discussion purposes by a dog in a trenchcoat.  Comments follow each data model property

ASSUMING we can hold very close to the IPLD-based-yet-JSON-friendly schema syntax of CACAOs and define the session object as a simple subset of the CACAO data model, we could start from this straw man to define the session object

```
type Header struct {
  t String // when this CAIP goes to final status it could just be caipXX, right?
}
type Payload struct {
  domain String
  iss String // is it worth identifying the provider here, or some other actor in the browser by DOM path???
  aud String // = uri --> this would go into the CAIP-25 req and response
  version String // not sure if this is needed or just leaks metadata for fingerprinting wallets...
  nonce String // remove?
  iat String // RFC3339 date-time =issued-at
  nbf optional String // RFC3339 date-time = not-before //make mandatory?
  exp optional String // RFC3339 date-time = expiration-time //make mandatory?
  statement optional String // remove?
  requestId optional String // remove?
  resources optional [ String ] // remove?
}

//would the browser-wallet (or... the snap?!) have a way to sign/own these, or are they just unsigned blobs floating in the DOM? 
type Signature struct { 
  t String
  m optional SignatureMeta
  s Bytes
}

type SignatureMeta struct {
}
```
TODO - describe how to compact and stringify the above; I guess the
serialization depends on the desired interop targets, but if unsigned maybe just
JCS+base64 and call it a day?

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
TODO - MM team? Speaking of interop targets, maybe describe those a bit here and reference CACAO (caip-74) if it's a pertinent one?

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
TODO - I'm hoping a browser expert/historian can speak to the overlap between this an classic web app session assumptions in the browser security model...

## Test Cases
<!--Please add test cases here if applicable.-->
TODO - MM team?

## Links
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->
Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.

- [RFC 3339][] - date-time standard used for expressing times

[RFC 3339]: https://datatracker.ietf.org/doc/html/rfc3339#section-5.6


## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
