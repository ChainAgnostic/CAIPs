---
namespace-identifier: <{name of folder}-caip{X}> where X = the CAIP being applied to this namespace 
title: <{namespace common name} [, aka ecosystem name] - {common name for type of entity identified by Caip-X}>
author: <["FirstName1 LastName1 (@GitHubUsername1)", "AnonHandle2 <foo2@bar.com>"]>
discussions-to: <URL of PR, mailing list, etc>
status: Draft
type: <Standard | Meta | Informational>
created: <date created on, in ISO 8601 (yyyy-mm-dd) format>
requires (*optional): <["CAIP-X", "CAIP-Y"]>
replaces (*optional): <CAIP-Z>
---

<!--You can leave these HTML comments in your merged EIP and delete the 
 visible duplicate text guides, they will not appear and may be helpful to 
 refer to if you edit it again. This is the suggested template for new EIPs.
 Note that an EIP number will be assigned by an editor. When opening a pull
 request to submit your EIP, please use an abbreviated title in the 
 filename, `caipX.md`, all lowercase, no `-` between the CAIP and its 
 number.-->
This is the suggested template for new CAIPs.

# CAIP-X

*For context, see the [CAIP-X][] specification.*

<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
As the old saying goes, "If you can't explain it simply, you don't understand it
well enough." Here is where you can provide a simplified and layman-accessible
explanation of what is particular to this namespace or how it differs from EVM
chains, where the CAIPs are easiest to apply/understand, IN THE SPECIFIC CONTEXT
OF THE CAIP YOU ARE APPLYING.  Assume the reader has read the /README.md
already.

Note the `[CAIP-X][]` link above; this should be defined below in the `##
References` section with a definition of the type: `[CAIP-X]:
https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-X.md`

## Rationale
<!--A short (~200 word) description of the technical issue being addressed.-->
A short (~200 word) description of any technical issues being addressed by the
application of this CAIP to this namespace.  In particular, call out any
relevant CAIP versioning such as not-yet-ratified changes to the applied CAIP
that are required for what you are specifying here to conform, as the tooling
relied upon by some interpreters may validate according to the CAIP as ratified,
or an earlier CAIP replaced by it, etc.

## Semantics

Explain (and refer to/add links in the `## References` section) any inputs or
namespace-specific constructs needed to generate or interpret this CAIP.

## Syntax

Explain the actual algorithm or transformation needed to transform inputs into a
conformant and unique CAIP deterministically.  Consider including a regular
expression for validation as well, as some consumers or toolmakers may want to
support this CAIP without a deep understanding of any specifications, devdocs,
or improvement proposals on which this specification depends.

### Resolution Mechanics

Many blockchain systems allow for transactions, asset-states, etc. to be
validated against the chain they are targeting or depending to to avoid replay
attacks or other unintended outcomes. This is often done by an API or RPC call
to a node to validate the targetted chain or network. Include a sample
request/response and add the relevant documentation to the `## References`
section below if possible, as well as an explanation of any steps needed to
validate the results, calculate checksums, etc.

### Backwards Compatibility

If earlier CAIPs or earlier stages in the governance of the namespace created
legacy addresses that break or extend the specification above, please add a
section for "Legacy" compatibility and an explanation of what contexts and/or
what time-frames would require catching those cases.

## Test Cases

A list of manually-composed and validated examples is the most important
section, and the most read!

## Additional Considerations (*OPTIONAL)

Future topics? Upcoming protocol upgrades that will require new specifications,
in the namespace and/or in the CAIPs?

## References
<!--Links to external resources that help understanding the CAIP better. This can e.g. be links to existing implementations.-->
Links to external resources that help understanding the namespace or the
specification/applied-CAIP better in this context. This can also include links
to existing implementations.

The preferred format, for browser-rendering and long-term maintenance, is a
bulletted list of [Name][] links (rather than classical [Name](referent) links),
followed by ` - ` and a summary or explanation of the content.  In a separate
section below, add the name-referent pairs in the `[Name]: https://{referent} `
format-- this will be invisible in any Github-flavored Markdown rendering
(including jekyll/github pages, aka github.io, but also docusaurus and many
dev-docs rendering engines).

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
