---
caip: 92
title: Chain-Agnostic Namespace Specification
status: Draft
type: Meta
author: Wayne Chang (wyc@fastmail.net), Juan Caballero (caballerojuan@pm.me)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pulls/92
created: 2022-01-18
updated: 2022-01-18
---

# CAIP-92

## What is a Chain-Agnostic Namespace?

Blockchains, DAGs, and other cryptographic systems are highly sophisticated and
complicated internally; seen from the outside, however, and particularly from
the perspective of multi-chain and chain-agnostic engineering, they can also be
viewed as simple namespaces, within which most or all entities can be
identified, addressed, and even verified. 

The CASA group focuses its efforts on three channels: 
1. definining cross-namespace specifications called CAIPs for identifying,
   addressing and verifying entities across namespaces
2. specifying each namespace according to the currently-defined CAIPs
3. tracking and in some cases maintaining reference implementations for the above

## CAIP Rationale

The purpose of this specification is to instruct documentors of existing
namespaces in how to define and maintain a single document in the
[`ChainAgnostic/namespaces`](https://github.com/chainagnostic/namespaces)
repository. See
[CAIP-2](https://github.com/chainagnostic/CAIPs/blob/master/CAIPs/caip-2.md) for
terminology and context.

## CAIP Formats and Templates

- Namespace documents should be written in [markdown][] format.
    - there should be one document per namespace, named by adding the `.md`
      suffix to the CAIP-2 name used for that namespace, i.e. `eip155.md` rather
      than `ethereum.md` or `evm.md`.
    - any specification that applies only to a subset of references within a
      namespace should occur in a section within that same file, i.e.
      `eip155.md#reference-5` rather than `eip155-görli.md`, etc.
- Image files should be included in a subdirectory of the `assets` folder of the
  `namespaces` repo following the same convention. 
    - When linking to an image in the CAIP, use relative links such as
      `../assets/caip-1/image.png`.

## Namespace Reference Sections

Care should be taken to preserve case-sensitive anchor links: give sections
short names, so as to create consistent links, i.e. `#CAIP-2` rather than
`#CAIP-2:+chainids`. 

The most complete namespace reference includes all of the following sections:

- `## Introduction` A brief introduction intended for explaining any
  idiosyncracies or sui generis traits to developers of cross-chain systems
- One section for each applicable CAIP currently ratified, i.e. `## CAIP-2` for
  namespace and references (i.e., ChainIDs), `## CAIP-10` for account
  abstraction (or equivalent) syntax, `## CAIP-19` for asset references, etc.
- `## References` for links and bibliography useful to people being introduced
  to this namespace for the first time by this document
- `## Future Considerations` may be a useful section to add if upcoming events
  in the namespace being defined (e.g. "The Merge", halvenings, etc.) are likely
  to require updates to the reference, or if any sections of the reference risk
  becoming obsolete in the foreseeable future. For smaller future
  considerations, tracking issues can be opened in the namespaces repo with a
  `[{namespace}]` prefix.

## Namespace Reference Header Preamble

Each Namespace document must begin with an [RFC
822](https://www.ietf.org/rfc/rfc822.txt) style header preamble, preceded and
followed by three hyphens (`---`). This header is also termed ["front matter" by
Jekyll](https://jekyllrb.com/docs/front-matter/). The headers must appear in the
following order. Headers marked with "*" are optional and are described below.
All other headers are required.

- ` namespace-identifier:` <unique identifier for namespace> (this is explained in the mandatory #CAIP-2 section)
- ` title:` <Namespace title> (A human-readable, broad name for the namespace)
- ` author:` <a list of the author's or authors' name(s) and/or username(s), or name(s) and email(s). Details are below.>
- ` * resolution:` \<a URL pointing to a public notice of resolution, e.g. a stable URL to a mailing list email, web-page, etc \>
- ` * discussions-to:` \<a URL pointing to the official discussion thread \>
- ` status:` \< Draft | Last Call | Accepted | Active | Abandoned | Rejected | Superseded \>
- `* review-period-end:` <date review period ends>
- ` type:` \< Standards Track (Core, Networking, Interface, ERC)  | Informational | Meta \>
- ` * category:` \< Core | Networking | Interface | ERC \>
- ` created:` \< date created on \>
- ` * updated:` \< comma separated list of dates \>
- ` * requires:` \< CAIP number(s) \>
- ` * replaces:` \< CAIP number(s) and/or namespace identifier \>
- ` * superseded-by:` \< CAIP number(s) and/or namespace identifier \>

Headers that permit lists must separate elements with commas.

Headers requiring dates will always do so in the format of ISO 8601 (yyyy-mm-dd).

#### `author` header

The `author` header optionally lists the names, email addresses or usernames of
the authors/owners of the namespace. Those who prefer anonymity may use a
username only, or a first name and a username. The format of the author header
value must be:

> Random J. User &lt;address@dom.ain&gt;

or

> Random J. User (@username)

if the email address or GitHub username is included, and

> Random J. User

if the email address is not given.

#### `resolution` header
    
A link, ideally archival/immutable, so a public statement of the resolution to
ratify and adopt this standard, e.g. from a mailing list.

#### `discussions-to` header

While an CAIP is a draft, a `discussions-to` header will indicate the mailing
list or URL where the CAIP is being discussed.

As a single exception, `discussions-to` cannot point to GitHub pull requests.

#### `type` header

The `type` header specifies the type of CAIP: Standards Track, Meta, or Informational.

#### `created` header

The `created` header records the date that the CAIP was assigned a number. Both
headers should be in yyyy-mm-dd format, e.g. 2001-08-14.

#### `updated` header

The `updated` header records the date(s) when the CAIP was updated with
"substantial" changes. This header is only valid for CAIPs of Draft and Active
status.

#### `requires` header

Namespaces may have a `requires` header, indicating the CAIP numbers that this CAIP depends on or profiles.

#### `superseded-by` and `replaces` headers

Namespaces may also have a `superseded-by` header indicating that it has been
rendered obsolete by a later document; the value is the number of the CAIP
and/or identifier of another namespace that replaces the current document. The
referred to CAIPs/namespaces must have a `replaces` header containing the number
of the namespace that it rendered obsolete.

## Auxiliary Files

Namespaces may include auxiliary files such as diagrams. Such files must be
named {namespace identifier}-Y.ext, “Y” is a serial number (starting at 1), and
“ext” is replaced by the actual file extension (e.g. “png”).

## Transferring Namespace Ownership

It occasionally becomes necessary to transfer ownership of namespaces to a new
champion. In general, we'd like to retain the original author as a co-author of
the transferred namespace specification, but that's really up to the original
author. A good reason to transfer ownership is because the original author no
longer has the time or interest in updating it or following through with the
namespace specification process, or has fallen off the face of the 'net (i.e. is
unreachable or isn't responding to email). A bad reason to transfer ownership is
because you don't agree with the direction of the namespapce. We try to build
consensus around a namespace specification, but if that's not possible, you can
always submit a competing PR for the same namespace.

If you are interested in assuming ownership of a namespace reference, send a
message asking to take over, addressed to both the original author and the
namespace editor. If the original author doesn't respond to email in a timely
manner, the current namespace editor will make a unilateral decision (all
decisions are tracked by git and reverseable if needed, with CASA consensus).

## Namespace Editors

The current Namespace editors are

 - `@ligi <ligi@ligi.de>`
 - `@bumblefudge <juan.caballero@spruceid.com>`
    
## Namespace Editor Responsibilities

For each new namespace reference that comes in, an editor does the following:

- Read the specification to check if it is ready: sound and complete. The ideas
  must make technical sense, even if they don't seem likely to get to final
  status.
- The title should accurately describe the content.
- Check the reference for language (spelling, grammar, sentence structure,
  etc.), markup (Github flavored Markdown), code style

If the namespace reference isn't ready, the editor will send it back to the
author for revision, with specific instructions.

Once the namespace reference is ready for the repository, the namespace  editor will:

- Merge the corresponding pull request

- Send a message back to the namespace reference author with the next steps.

The editors don't pass judgment on namespace references. We merely do the administrative & editorial part.

### Bibliography

[markdown]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
[Bitcoin's BIP-0001]: https://github.com/bitcoin/bips
[Python's PEP-0001]: https://www.python.org/dev/peps/

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).