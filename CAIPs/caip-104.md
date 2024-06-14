---
caip: 104
title: Namespace Reference Purpose and Guidelines
status: Review
type: Meta
author: Bumblefudge (@bumblefudge), Wayne Chang (@wyc)
created: 2022-03-27
---

# What is a Namespace Reference?

Previously, the specifics of blockchain ecosystems and namespaces were defined
in single stand-alone CAIPs alongside cross-chain CAIPs. Now, as the system has
matured and expanded, it has become clear that namespaces are more complex and
multi-dimensional than can be efficiently governed over time in the current CAIP
single-document structure; instead, a Namespace Reference is proposed that
splits out the application of each cross-chain CAIP to a given namespace as a
distinct document with its own ratification/supersession lifecycle. This way,
long-term specifications for low-level primitives like Chain IDs and addresses
can be defined once and not be superseded by the addition or modification of
higher-level specifications for RPC interfaces or multiple asset types.

In practical terms, that means each blockchain ecosystem's namespace is defined
by a folder containing a distinct document for _each CAIP_ applied to that
namespace. At a high level, the important facts about a namespace, its
governance, and its nomenclature can be defined/introduced in a `README.md`
file, and each CAIP can be defined in a `caip{X}.md` file, where X is the number
of the ratified CAIP.

Like CAIPs, each CAIP-reference and namespace-reference is a design document
providing information to the community and/or describing an addressing scheme
with the explicit aim of further cross-chain (and more importantly,
cross-namespace) engineering. The references should provide a concise technical
specification of each feature or variant, as well as a rationale requiring
little namespace-specific context. The namespace reference should include
canonical (and ideally long-lived) links to authoritative documents, both when
relied upon by the specified behavior AND when namespace-specific context is
required to understand the inputs to or assumptions of the behavior.

The reference author is responsible for building consensus within the community
and documenting dissenting opinions or rejected alternatives.

## Namespace Reference Rationale

Cross-chain engineering is difficult and often requires one to know both sides
of a cross-namespace/cross-ecosystem interaction deeply to avoid serious
security, UX, and design problems. The primary function of defining a namespace
reference is to map the cross-chain CAIPs against the specifics of a given
ecosystem and namespace, ideally written for a reader with very little context in that namespace.

## Reference Formats and Templates

Namespace references should be written in [markdown][] format.

Image files should be included in the same namespace directory to allow for
simple relative references. Such files must be named readme-Y.ext, where “XXXX”
is the CAIP number, “Y” is a serial number (starting at 1), and “ext” is
replaced by the actual file extension (e.g. “png”).

Templates for both the [namespace
reference](https://github.com/ChainAgnostic/namespaces/blob/main/_template/README.md)
and for each [namespace-caip
reference](https://github.com/ChainAgnostic/namespaces/blob/main/_template/caipX.md)
are included in the namespaces repo folder for easy cloning.

# Specification

## Header Preamble Values

Each reference document must begin with an [RFC
822](https://www.ietf.org/rfc/rfc822.txt) style header preamble, preceded and
followed by three hyphens (`---`). This header is also termed ["front matter" by
Jekyll](https://jekyllrb.com/docs/front-matter/).

Please Note:

- The headers must appear in the following order.
- Headers marked with "\*" are optional and are described below.
  - All other headers are required.
- Lists/arrays in RFC822 must be encoded in the form `key: ["str1", "str2"]`,
  NOT `key: Str1, Str2`, even though single strings can be encoded in the form
  `key: Str1`
- Similarly, headers requiring dates must use the format of ISO 8601 (yyyy-mm-dd).

` namespace-identifier:` <{unique lowercase alphanumeric string}[-caip{X}],
where the optional suffix replaces X with the number of the applied CAIP unless
the reference is a base namespace reference>

` title:` <{string1}[ - {string2}]>

` author:` <a string or array of strings, each consisting of the author's public
name and github username or email>

` * resolution:` \<a string or array of strings, each consisting of an archival url>

` * discussions-to:` \<a string or array of string, each consisting of a url\>

` status:` <Draft | Last Call | Accepted | Active | Abandoned | Rejected | Superseded>

`* review-period-end:` <date review period ends>

` type:` <Standards Track (Core, Networking, Interface, ERC) | Informational | Meta>

` * category:` <Core | Networking | Interface | ERC>

` created:` <date created on>

` * updated:` <comma separated list of dates>

` * requires:` <CAIP number(s), i.e. `CAIP-XX` >

` * replaces:` <CAIP number(s) | namespace reference(s), i.e. `eip155` >

` * superseded-by:` <namespace reference(s) | URL of non-namespace standard>

#### `title` header

{string1} should be the natural-language spelling/name of the namespace, e.g.
"EIP-155" for `eip155`; if this name is technical or otherwise counterintuitive,
an optional clarification may be affixed, e.g. ", aka EVM Chains." If the
reference in question is a per-CAIP reference, a common name for the specified
referent can be added for further clarity, i.e. " - Assets" (for CAIP-19) or " -
Addresses" (for CAIP-10), as these markdown files will double as page-titles if
rendered in a browser.

#### `author` header

The `author` header optionally lists the names, email addresses or usernames of
the authors/owners of the CAIP. Those who prefer anonymity may use a username
only, or a first name and a username. The format of the author header value must
be:

> Random J. User &lt;address@dom.ain&gt;

or

> Random J. User (@username)

if the email address or GitHub username is included, and

> Random J. User

if the email address is not given.

#### `resolution` header

If ratification of this document was recorded at a permanent URL (e.g. the
recorded minutes of a CASA meeting or mailing list), that URL can be placed here
for additional context.

#### `discussions-to` header

While an CAIP is a draft, a `discussions-to` header will indicate the mailing
list or URL where the CAIP is being discussed.

#### `type` header

The `type` header specifies the type of CAIP: Standards Track, Meta, or
Informational.

#### `created` header

The `created` header records the date that the CAIP was assigned a number. Both
headers should be in yyyy-mm-dd format, e.g. 2001-08-14.

#### `updated` header

The `updated` header records the date(s) when the CAIP was updated with
"substantial" changes. This header is only valid for CAIPs of Draft and Active
status.

#### `requires` header

Namespace-CAIPs may have a `requires` header, indicating the CAIP number(s) that
this reference depends on.

#### `superseded-by` and `replaces` headers

Namespace-CAIPs may also have a `superseded-by` header indicating that an CAIP
has been rendered obsolete by a later document; the value is the `title` that
replaces the current document, i.e., if "eip155-caip10" gets superceded, it
should rename in the directory `eip155` directory but contain a link to the
superseding specification(s), like `[caip10v2](caip10v2.md)`. The newer
Namespace-CAIP must have a `replaces` header containing the number of the
Namespace-CAIP that it rendered obsolete.

## Auxiliary Files

Namespace-CAIPs may include auxiliary files such as diagrams. Such files must be
named CAIP-XXXX-Y.ext, where “XXXX” is the CAIP number, “Y” is a serial number
(starting at 1), and “ext” is replaced by the actual file extension (e.g.
“png”).

## Transferring Reference Document Ownership

It occasionally becomes necessary to transfer ownership of references to a new
champion. In general, we'd like to retain the original author as a co-author of
the transferred reference document, but that's really up to the original author.
A good reason to transfer ownership is because the original author no longer has
the time or interest in updating it or following through with the CASA process,
or has fallen off the face of the 'net (i.e. is unreachable or isn't responding
to email). A bad reason to transfer ownership is because you don't agree with
the direction of the document. We try to build consensus around each document,
but if that's not possible, you can always submit an alternate/competing
document through the same PR/consensus process.

If you are interested in assuming ownership of an document, send a message
asking to take over, addressed to both the original author and the CAIP editor
(this can be done in a new github issue or email). If the original author
doesn't respond in a timely manner, CASA editor(s) may make a unilateral but
reversible decision in the interest of keeping things moving.

## History

This document was derived heavily from [CAIP-1][], which was in turn influenced by [Bitcoin's BIP-0001] written by Amir Taaki which in turn was derived from [Python's PEP-0001]. In many places text was simply copied and modified. Although the PEP-0001 text was written by Barry Warsaw, Jeremy Hylton, and David Goodger, they are not responsible for its use in the Ethereum Improvement Process, and should not be bothered with technical questions specific to CAIPs. Please direct all comments to the CAIP editors.

### Bibliography

[caip-1]: caip-1.md
[markdown]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
[bitcoin's bip-0001]: https://github.com/bitcoin/bips
[python's pep-0001]: https://www.python.org/dev/peps/

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
