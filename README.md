# CAIPs
Chain Agnostic Improvement Proposals (CAIPs) describe standards for blockchain projects that are not specific to a single chain.

## Contributing

 1. Review [CAIP-1](CAIPs/caip-1.md).
 2. Fork the repository.
 3. Add your CAIP to your fork of the repository. There is a [template CAIP here](caip-template.md).
 4. Submit a Pull Request to Chain Agnostics's [CAIPs repository](https://github.com/ChainAgnostic/CAIPs).

Your first PR should be a first draft of the final CAIP. An editor will manually review the first PR for a new CAIP and assign it a number before merging it. Make sure you include a `discussions-to` header with the URL to a discussion forum or open GitHub issue where people can discuss the CAIP as a whole.

If your CAIP requires images, the image files should be included in a subdirectory of the `assets` folder for that CAIP as follows: `assets/caip-N` (where **N** is to be replaced with the CAIP number). When linking to an image in the CAIP, use relative links such as `../assets/caip-1/image.png`.


## CAIP Status Terms

* **Draft** - an CAIP that is undergoing rapid iteration and changes.
* **Last Call** - an CAIP that is done with its initial iteration and ready for review by a wide audience.
* **Accepted** - a core CAIP that has been in Last Call for at least 2 weeks and any technical changes that were requested have been addressed by the author.

## CAIP Index

* **CAIP-1** - [CAIP Purpose and Guidelines](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-1.md)
* **CAIP-2** - [Blockchain ID Specification](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md)
* **CAIP-6** - [Blockchain Reference for the LIP9 Namespace](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-6.md) - see [chainagnostic/namespaces#17](https://github.com/ChainAgnostic/namespaces/pull/17)
* **CAIP-10** - [Account ID Specification](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-10.md)
* **CAIP-19** - [Asset Type and Asset ID Specification](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-19.md)
* **CAIP-20** - [Asset Reference for the SLIP44 Asset Namespace](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-20.md)
* **CAIP-21** - [Asset Reference for the ERC20 Asset Namespace](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-21.md)
* **CAIP-22** - [Asset Reference for the ERC721 Asset Namespace](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-22.md)
* **CAIP-25** - [Chain Agnostic Provider Handshake](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-25.md)
* **CAIP-27** - [Chain Agnostic Provider Request](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-27.md)
* **CAIP-29** - [Asset Reference for the ERC1155 Asset Namespace](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-29.md)
* **CAIP-74** - [CACAO: Chain Agnostic CApability Object](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-74.md)
* **CAIP-76** - [Account Address for the Hedera namespace](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-76.md) - see [chainagnostic/namespaces#16](https://github.com/ChainAgnostic/namespaces/pull/16)
* **CAIP-104** - [Namespace Reference Purpose and Guidelines](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-104.md)

## Namespaces

Please note that CAIPs specific to a blockchain community or namespace have been moved to a separate repository, <a href=https://github.com/chainagnostic/namespaces>`namespaces`</a>, governed by the same processes.  CAIPs 
[3](https://github.com/ChainAgnostic/namespaces/tree/main/eip155), 
[4](https://github.com/ChainAgnostic/namespaces/tree/main/bip122), 
[5](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-5.md)),
[23](https://github.com/ChainAgnostic/namespaces/tree/main/eip155), 
[26](https://github.com/ChainAgnostic/namespaces/tree/main/tezos), 
[28](https://github.com/ChainAgnostic/namespaces/tree/main/stellar), and 
[30](https://github.com/ChainAgnostic/namespaces/tree/main/solana) 
have been migrated. CAIPs
[6](https://github.com/ChainAgnostic/namespaces/pull/6),
[7])(https://github.com/ChainAgnostic/namespaces/pull/5), and
[13]()
are still in process of migration.