---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: <to be assigned>
title: Web extension provider communication
author: Mark Stacey (@Gudahtt), Jiexi Luan (@jiexi)
discussions-to: <URL>
status: Draft
type: Standard
created: 2022-11-28
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
This CAIP discusses the motivation, specification, and rationale for a proposal aimed at improving how web extension wallets interact with websites. It outlines the current method of injecting JavaScript provider APIs into websites, its advantages, and its numerous disadvantages, such as security concerns, performance issues, and the risk of breaking websites. An alternative strategy is proposed that specifies a standard communication specification over a new transport layer which enables websites to be able to embed their own provider as a library, addressing the disadvantages of injecting providers into websites and improving web extension interoperability as a whole.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
In the current web extension wallet ecosystem, the prevalent approach involves injecting a JavaScript provider API, such as `window.ethereum`, directly into websites as a global variable. This method offers simplicity for web developers and ensures compatibility across extensions but raises significant concerns regarding security, performance, and potential disruption of website functionality due to the need for extensive permissions and the injection of additional code. An alternative approach involves websites embedding their own provider libraries, which could mitigate these issues by reducing required permissions, enhancing performance, and providing developers with greater control over provider integration. To make this approach feasible, a communication protocol must be standardized for the purpose.

This proposal addresses the challenge of maintaining interoperability and extensibility between web extensions and websites by standardizing the communication protocol through the `externally_connectable` interface. This standardization aims to facilitate seamless interaction across different web extensions, ensuring a consistent and secure method of communication. This proposal outlines a specific message format for this communication and discusses the use of `externally_connectable` to allow web extensions to send and receive messages with authorized websites and extensions.

Despite the lack of current support for `externally_connectable` in Firefox, the proposal underscores the importance of interoperability and standardized communication for the future of web extension wallets, advocating for a transition away from contentscript injection.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
Web extension wallets today will typically inject a JavaScript provider API into websites as a global variable. For example, in the Ethereum ecosystem this provider API is standardized in EIP-1193, and is conventionally injected as `window.ethereum`.

This injected API strategy has some advantages:
* For websites developers, using a global variable is simple and requires no effort on their part to setup.
* It allows websites to support any web extension following this standard with no additional effort.

However, the injected API strategy has many disadvantages:
* It depends upon the web extension having read and write access to every website the user visits, which is a scary permission that web extension authors might otherwise be able to avoid asking for.
* It slows down every website by injecting additional code to be parsed and executed. It even slows down the initial page load in most cases, because many web extensions inject the provider synchronously to maintain compatibility with websites that expect it to be available immediately.
* In some cases, injecting code into a webpage may break its original intended behaviors.
* It provides no way for web extension authors to safely make breaking changes to their provider API without having to also inject extra code for the purposes of maintaining backwards compability for dApps that may still rely on those legacy APIs.

An alternative strategy would be for a website to embed its own provider. A provider could be offered as a library, to be embedded by the website author. This strategy can address all disadvantages of the injected provider approach:
* If this strategy became widespread enough, it would allow some web extensions to stop asking for write access to all pages.
  * NOTE: Should mention discovery somewhere since that will require injection still
* The website author can control when the provider is initialized, and fine-tune performance.
* No code needs to be injected, so websites would no longer be broken by injected code.
* The provider library can be published with breaking changes, allowing changes to the API without needing to embed legacy code in each website.

A provider library can be similarly easy to use for website authors as well, requiring nothing more than a single script tag to import a library and get an equivalent experience to using an the injected provider.

Web extension inter-operability is a challenge for embedded providers though. That is what this proposal means to address. Today there is no way to write a provider such that it is compatible with any web extension. Web extensions differ today in how they communicate with wallets, from the messaging system used to the messaging format. These details often aren't publicly documented or treated as a public-facing API, so they can change without notice, making it risky even to embed support for popular conventions used today.

A standard method for providers to communicate with web extensions would allow website authors to embed their own providers without losing web extension inter-operability.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

### Language

The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD",
"SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in
uppercase in this document are to be interpreted as described in [RFC
2119](https://www.ietf.org/rfc/rfc2119.txt)

### Summary

Web extensions should expose a standard interface over [`externally_connectable`](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/manifest.json/externally_connectable) to enable the inter-operability of embedded providers.

### Message format

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "/caip-x/schemas/provider-request.schema.json",
  "title": "Provider / Web Extension message",
  "description": "A request sent between a provider and a web extension.",
  "type": "object",
  "properties": {
    "type": {
      "description": "The message type, used to identify this as a CAIP-X message",
      "const": "caip-x"
    },
    "data": {
      "description": "A CAIP-25/27/285/312/319 JSON-RPC message",
      "type": "object"
    }
  },
  "required": ["type", "data"]
}
```

### `externally_connectable`

A web extension can use the `externally_connectable` manifest field to accept messages from websites and other extensions. This permission can be configured to limit which sites and other extensions can send messages to the web extension. How this permission is configured is out-of-scope for this proposal; this proposal only concerns sites that the web extension allows messages from.


The web extension can:
* handle a connection from the website or other extensions by using `chrome.runtime.onConnectExternal.addListener((port) => {...})`
* send messages by using `port.postMessage()`
* receive messages by using `port.onMessage.addListener()`
  * incoming messages should be validated according to the message format specification above

The webpage embedded provider can:
* initiate a connection with the web extension by using `port = browser.runtime.connect()`
* send messages by using `port.postMessage()`
* receive messages by using `port.onMessage.addListener()`

### Caveats
Currently Firefox does not support `externally_connectable` yet, but they are [considering implementing it](https://bugzilla.mozilla.org/show_bug.cgi?id=1319168). Meanwhile, extension wallets on Firefox will need to continue injecting an inpage provider for the dApp.

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
While web extensions could realize the benefits of `externally_connectable` without using it with a standardized interface, this would mean that every web extension would have to ship a provider library that worked specifically for their interface and that Dapps would need to import them. It wouldn't be feasible for Dapps to import every single web extension's specific provider implementation, making this approach less viable.

Web extension inter-operability is the key to enabling generalized provider implementations. Generalized provider implementations allows for convenient adoption by Dapps, leading to more wide spread adoption of the standard as a result.

## Test Cases
<!--Please add test cases here if applicable.-->

## Security Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
`externally_connectable` has seen a decade of usage via extensions on Chrome. It has a strictly better security when compared to postMessage over contentscript.

## Privacy Considerations
<!--Please add an explicit list of intra-actor assumptions and known risk factors if applicable. Any normative definition of an interface requires these to be implementable; assumptions and risks should be at both individual interaction/use-case scale and systemically, should the interface specified gain ecosystem-namespace adoption. -->
Exposing the wallet API over `externally_connectable` opens a migration path towards a bring-your-own provider model in which web extensions can reduce their fingerprint by no longer having to inject their own provider into every webpage.

It should be noted however that this API is still fingerprintable based on the return values from a CAIP-25 request. A malicious actor could make several CAIP-25 requests with a single scope and/or account ID to determine what scopes and/or accounts the wallet supports based on whether that request returns immediately with an unsupported error or not. This can be mostly mitigated by rate liming and disallowing concurrent CAIP-25 requests from the same origin. It is no worse than the current status quo with EVM methods.

## Backwards Compatibility
<!--All CAIPs that introduce backwards incompatibilities must include a section describing these incompatibilities and their severity. The CAIP must explain how the author proposes to deal with these incompatibilities. CAIP submissions without a sufficient backwards compatibility treatise may be rejected outright.-->
This CAIP does not require discontinuing usage of contentscript. It is RECOMMENDED that wallets start implementing this alternative connection strategy and encouraging it's usage so that the ecosystem can eventually remove contentscript injection entirely. As an optional transitionary step, wallets can migrate their injected provider to start making connections over `externally_connectable` with no user-facing impact (not sure if true since this has caveats).

## Links
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->
* [externally_connectable](https://developer.mozilla.org/en-US/docs/Mozilla/Add-ons/WebExtensions/manifest.json/externally_connectable)
* [Mozilla bug report: "Implement externally_connectable from a website"](https://bugzilla.mozilla.org/show_bug.cgi?id=1319168)

## Copyright
Copyright and related rights waived via [CC0](../LICENSE).
