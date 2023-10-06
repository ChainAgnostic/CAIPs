# Contributing

1.  Review [CAIP-1](CAIPs/caip-1.md).
2.  Fork the repository.
3.  Add your CAIP to your fork of the repository. There is a [template CAIP here](caip-template.md).
4.  Submit a Pull Request to Chain Agnostics's [CAIPs repository](https://github.com/ChainAgnostic/CAIPs).

Your first PR should be a first draft of the complete and implementable CAIP. 
An editor will manually review the first PR for a new CAIP and assign it a number before merging it.
Make sure you include a `discussions-to` header with the URL of an issue or discussion on this repository, or any other forum where you would welcome discussion.

If your CAIP requires images, the image files should be included in a subdirectory of the `assets` folder for that CAIP as follows: `assets/caip-N` (where **N** is to be replaced with the CAIP number). 
When linking to an image in the CAIP, use relative links such as `../assets/caip-1/image.png`.

It is recommended that you render your PR locally to check the Jekyll syntax; to do so, run `bundle exec jekyll serve`.

## Style Guide

Github-flavored Markdown is encouraged for all CAIP documents, with the following conventions observed:

Line breaks:
- One line per sentence (or independent clause in the case of semicolons) makes for easier parsing of diffs in PR review and is preferred but not required

Link formatting:
- All external documents cited should be defined at the end of the `## References` section of each document, one per line, in the form `[CAIP-1]: https://chainAgnostic.org/CAIPs/CAIP-1` - these link alias definitions are invisible in rendered markdown, but serves as footnotes to readers viewing the files in a text editor.
- All references to other CAIPs should refer to the rendered form on the domain controlled by CASA (e.g. `https://chainagnostic.org/CAIPs/caip-1`) rather than to the current public git repository that it renders from (currently, `https://github.com/ChainAgnostic/CAIPs/blob/main/CAIPs/caip-1.md?plain=1`, but subject to change)
- All references elsewhere in the text should be by link-alias (e.g. `[CAIP Syntax][CAIP-1]`) rather than self-contained (e.g. `[CAIP syntax](https://ChainAgnostic.org/CAIPs/caip-1)`); note that in this example, `[CAIP-1][CAIP-1]`, `[CAIP-1][]` and `[CAIP-1]` will all link to the same URL if the alias has been defined elsewhere in the document when rendered. This makes the document easier to read in a text editor.
- Links to other sections should always take the form of markdown section heading links rather than HTML anchors or any other reference, e.g. `For further detail, see the [Security Considerations section](#security-considerations)`
- Providing lists of normative and non-normative references according to, e.g., the [IETF style guide for references](https://www.ietf.org/archive/id/draft-flanagan-7322bis-07.html#section-4.8.6) is welcomed but not required; a short list of useful documents or additional resources with captions or explanations is also welcome.

## References

[CAIP-1]: https://chainagnostic.org/CAIPs/caip-1
[namespaces]: https://namespaces.chainagnostic.org/