# CAIPs

Chain Agnostic Improvement Proposals (CAIPs) describe standards for blockchain projects that are not specific to a single chain.

## Contributing

1.  Review [CAIP-1](CAIPs/caip-1.md).
2.  Fork the repository.
3.  Add your CAIP to your fork of the repository. There is a [template CAIP here](caip-template.md).
4.  Submit a Pull Request to Chain Agnostics's [CAIPs repository](https://github.com/ChainAgnostic/CAIPs).

Your first PR should be a first draft of the final CAIP. An editor will manually review the first PR for a new CAIP and assign it a number before merging it. Make sure you include a `discussions-to` header with the URL to a discussion forum or open GitHub issue where people can discuss the CAIP as a whole.

If your CAIP requires images, the image files should be included in a subdirectory of the `assets` folder for that CAIP as follows: `assets/caip-N` (where **N** is to be replaced with the CAIP number). When linking to an image in the CAIP, use relative links such as `../assets/caip-1/image.png`.

It is recommended that you render your PR locally to check the Jekyll syntax; to do so, run `bundle exec jekyll serve`.

## CAIP Status Terms

- **Draft** - an CAIP that is undergoing rapid iteration and changes.
- **Review** - an CAIP that is done with its initial iteration and ready for review by a wide audience.
- **Accepted** - a core CAIP that has been in Review for at least 2 weeks and any technical changes that were requested have been addressed by the author.

## CAIP Index

Visit [chainagnostic.org](https://chainagnostic.org/) for the up-to-date index of all CAIPs listed by status.

## Namespaces

Previously there were specific CAIPs for what is now referred to as *namespaces*. Chain Agnostic [Namespaces](https://github.com/chainagnostic/namespaces) describe a blockchain ecosystem or set of ecosystems as a namespace, relying as much as possible on the CAIP specifications to minimize the research needed to interact with assets, contracts, and accounts in that namespace.
