---
caip: 1
title: CAIP Purpose and Guidelines
status: Active
type: Meta
author: ligi <ligi@ligi.de>
created: 2019-08-31
---

## What is an CAIP?

CAIP stands for Chain Agnostic Improvement Proposal. An CAIP is a design document providing information to the community or describing a standard to be used across multiple chains. The CAIP should provide a concise technical specification of the feature and a rationale for the feature. The CAIP author is responsible for building consensus within the community and documenting dissenting opinions.

## CAIP Rationale

Currently it is often the case that a standard defined in one chain is also used in another chain. E.g. the usage of BIP39 in Ethereum applications. Also there is no real place to propose a standard that can be used for multiple chains (like mnemonics) currently. CAIPs are intended to fill this gap and be a place where such standards can live.

## CAIP Formats and Templates

CAIPs should be written in [markdown] format.
Image files should be included in a subdirectory of the `assets` folder for that CAIP as follows: `assets/caip-N` (where **N** is to be replaced with the CAIP number). When linking to an image in the CAIP, use relative links such as `../assets/caip-1/image.png`.

## CAIP Header Preamble

Each CAIP must begin with an [RFC 822](https://www.ietf.org/rfc/rfc822.txt) style header preamble, preceded and followed by three hyphens (`---`). This header is also termed ["front matter" by Jekyll](https://jekyllrb.com/docs/front-matter/). The headers must appear in the following order. Headers marked with "*" are optional and are described below. All other headers are required.

` caip:` <CAIP number> (this is determined by the CAIP editor)

` title:` <CAIP title>

` author:` <a list of the author's or authors' name(s) and/or username(s), or name(s) and email(s). Details are below.>

` * discussions-to:` \<a url pointing to the official discussion thread\>

` status:` <Draft | Last Call | Accepted | Active | Abandoned | Rejected | Superseded>

`* review-period-end:` <date review period ends>

` type:` <Standards Track (Core, Networking, Interface, ERC)  | Informational | Meta>

` * category:` <Core | Networking | Interface | ERC>

` created:` <date created on>

` * updated:` <comma separated list of dates>

` * requires:` <CAIP number(s)>

` * replaces:` <CAIP number(s)>

` * superseded-by:` <CAIP number(s)>

Headers that permit lists must separate elements with commas.

Headers requiring dates will always do so in the format of ISO 8601 (yyyy-mm-dd).

#### `author` header

The `author` header optionally lists the names, email addresses or usernames of the authors/owners of the CAIP. Those who prefer anonymity may use a username only, or a first name and a username. The format of the author header value must be:

> Random J. User &lt;address@dom.ain&gt;

or

> Random J. User (@username)

if the email address or GitHub username is included, and

> Random J. User

if the email address is not given.

#### `resolution` header

#### `discussions-to` header

While an CAIP is a draft, a `discussions-to` header will indicate the mailing list or URL where the CAIP is being discussed.

As a single exception, `discussions-to` cannot point to GitHub pull requests.

#### `type` header

The `type` header specifies the type of CAIP: Standards Track, Meta, or Informational.

#### `created` header

The `created` header records the date that the CAIP was assigned a number. Both headers should be in yyyy-mm-dd format, e.g. 2001-08-14.

#### `updated` header

The `updated` header records the date(s) when the CAIP was updated with "substantial" changes. This header is only valid for CAIPs of Draft and Active status.

#### `requires` header

CAIPs may have a `requires` header, indicating the CAIP numbers that this CAIP depends on.

#### `superseded-by` and `replaces` headers

CAIPs may also have a `superseded-by` header indicating that an CAIP has been rendered obsolete by a later document; the value is the number of the CAIP that replaces the current document. The newer CAIP must have a `replaces` header containing the number of the CAIP that it rendered obsolete.

## Auxiliary Files

CAIPs may include auxiliary files such as diagrams. Such files must be named CAIP-XXXX-Y.ext, where “XXXX” is the CAIP number, “Y” is a serial number (starting at 1), and “ext” is replaced by the actual file extension (e.g. “png”).

## Transferring CAIP Ownership

It occasionally becomes necessary to transfer ownership of CAIPs to a new champion. In general, we'd like to retain the original author as a co-author of the transferred CAIP, but that's really up to the original author. A good reason to transfer ownership is because the original author no longer has the time or interest in updating it or following through with the CAIP process, or has fallen off the face of the 'net (i.e. is unreachable or isn't responding to email). A bad reason to transfer ownership is because you don't agree with the direction of the CAIP. We try to build consensus around an CAIP, but if that's not possible, you can always submit a competing CAIP.

If you are interested in assuming ownership of an CAIP, send a message asking to take over, addressed to both the original author and the CAIP editor. If the original author doesn't respond to email in a timely manner, the CAIP editor will make a unilateral decision (it's not like such decisions can't be reversed :)).

## CAIP Editors

The current CAIP editors are

` * ligi <ligi@ligi.de>`

## CAIP Editor Responsibilities

For each new CAIP that comes in, an editor does the following:

- Read the CAIP to check if it is ready: sound and complete. The ideas must make technical sense, even if they don't seem likely to get to final status.
- The title should accurately describe the content.
- Check the CAIP for language (spelling, grammar, sentence structure, etc.), markup (Github flavored Markdown), code style

If the CAIP isn't ready, the editor will send it back to the author for revision, with specific instructions.

Once the CAIP is ready for the repository, the CAIP editor will:

- Assign an CAIP number (generally the PR number or, if preferred by the author, the Issue # if there was discussion in the Issues section of this repository about this CAIP)

- Merge the corresponding pull request

- Send a message back to the CAIP author with the next step.

The editors don't pass judgment on CAIPs. We merely do the administrative & editorial part.

## History

This document was derived heavily from [Bitcoin's BIP-0001] written by Amir Taaki which in turn was derived from [Python's PEP-0001]. In many places text was simply copied and modified. Although the PEP-0001 text was written by Barry Warsaw, Jeremy Hylton, and David Goodger, they are not responsible for its use in the Ethereum Improvement Process, and should not be bothered with technical questions specific to CAIPs. Please direct all comments to the CAIP editors.

### Bibliography

[markdown]: https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet
[Bitcoin's BIP-0001]: https://github.com/bitcoin/bips
[Python's PEP-0001]: https://www.python.org/dev/peps/

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
