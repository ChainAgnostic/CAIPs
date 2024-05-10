---
caip: CAIP-X
title: Domain Wallet Authentication
author: Chris Cassano (@glitch003), David Sneider (@davidlsneider), Federico Amura (@FedericoAmura), Gregory Markou (@GregTheGreek)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/275
status: Draft
type: Standards Track
Standard created: 2024-04-29
updated: 2024-04-29
---

# Abstract

The Domain Wallet Authentication describes a method for linking a crypto domain with authentication methods or providers by adding an authenticator: JSON/URL field to the metadata of a crypto domain NFT. The standard also describes a method for application developers and web3 login modal providers to enable users to login with their domain name.

The goal of this specification is to define a chain-agnostic identity standard for domain-based authentication.

# Motivation

Current blockchain authentication methods primarily rely on connecting wallets via providers like Metamask. However, this requires users to remember both their wallet provider and their wallet address.  As more wallets, signers, and chains come online, this problem will only get worse.

Crypto domains provide a human-readable, user-friendly way to represent wallet addresses. By enabling authentication directly with crypto domains, this standard aims to improve usability and adoption of web3 logins.

Additionally, standardizing the way domain NFT metadata specifies its supported authentication mechanisms allows any compatible domain NFT to abstract out authentication methods and key management. This abstraction allows both login modals and dApps to easily integrate domain-based logins.

# Specification

## Storage Format

Any system capable of resolving text records can be used for the name in this system.  However, we have chosen to focus this specification on Crypto domain NFTs.  Crypto domain NFTs that are compatible with this authentication standard MUST include an authenticator text record entry with the following properties:
authenticator (string, required): A URL that dereferences to a JSON objection containing configuration information, in particular information about how to authenticate the domain's subject.  e.g. 

`http://www.authprovider.com/auth/examplename.tld`
The application will craft the final URL to get the configuration, where `exampledomain.tld` will be substituted for the user's whole crypto domain name, so for an ENS domain like `chrisc.eth`  the final URL would be `http://www.authprovider.com/auth/chrisc.eth`

The actual standard used to store this "authenticator" text record will vary depending on the Crypto Domain NFT system used.  For example, on ENS, a ENSIP-5 record should be created, with a "key" of "authenticator".  You can lookup the authenticator on an ENS domain using the text(bytes32 node, string key) function where "node" is the ENS domain being queried and "key" is the string "authenticator".

Example of a response to retrieve the "authenticator" text record:

"https://www.authprovider.com/auth/{}"

## Authentication flow definition

The User can provide their authentication flow by providing a URL where Web3 applications can fetch it.
The user is responsible for choosing what to record in his Crypto Domain NFT system, based on his subjective opinion of paying enough gas for storing it and taking care of keeping that record updated, or delegating that responsibility to an authentication flow provider. Due to the simplicity of the flow definition, it can be self hosted to get the best of both worlds

The Authentication flow definition JSON MUST conform to the following Draft 7 [JSON Schema
`]: ``
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "address": {
      "type": "string",
      "description": "The Ethereum address requested by the user, prefixed by 0x."
    },
    "authFlows": {
      "type": "array",
      "description": "List of authentication flows supported for different platforms and connections. At least one value must be provided.",
      "items": {
        "type": "object",
        "properties": {
          "platform": {
            "type": "string",
            "description": "The platform type, e.g., 'browser' or 'mobile'. If not specified, it should support all platforms"
          },
          "connection": {
            "type": "string",
            "description": "The method of connection, e.g., 'extension', 'wc' (wallet connect), or 'mwp' (mobile wallet protocol)."
          },
          "URI": {
            "type": "string",
            "description": "The Uniform Resource Identifier used to initiate the authentication process. This can be a template URL or a deep link."
          }
        },
        "required": ["connection", "URI"]
      }
    }
  },
  "required": ["address", "authFlows"]
}
```

For example:
```json
{
  "address": "0xd8da6bf26964af9d7eed9e03e53415d37aa96045",
  "authFlows": [
    {
      "platform": "browser",
      "connection": "extension",
      "URI": "injected"
    },
    {
      "platform": "mobile",
      "connection": "mwp",
      "URI": "https://mobile.wallet.universal.link/"
    },
    {
      "connection": "wc",
      "URI": "http://www.authprovider.com/auth/{}"
    }
  ]
}
```

Application will filter the authentication flows that are not supported and then try to execute in order. In the example:
When used on a browser, it will first try to execute the authentication flow that claims for an injected extension like metamask. In this case we are not defining any wallet rdns so it will use `window.ethereum` instead of looking at a specific EIP-6963 compliant wallet. If it cannot find the extension, it will try with the next flow and open another browser tab pointing to the corresponding URI, automatically passing it the wallet connect URI as query param to establish a connection upon user confirmation
When used on mobile it will verify support to use mobile wallet protocol with the specified universal link. When it is not supported, it will resolve the same way as browser using the last authentication flow

# Login With Name Flow

Web3 applications and login modal providers can implement the Login With Name flow as follows:
The user enters a crypto domain address for a wallet they control and want to authenticate with
Check that the domain has a compatible authenticator JSON/URL text record
If the authenticator text record is a URL, client application sends an HTTP GET request to it in order to obtain users authentication flows. This is not needed if the authenticator text record already has a JSON in it
Validate the response is for the same address resolved by the Crypto Domain NFT.
Initiate the authentication flow of the selected domain's authenticator which could be by opening a new window in the user's browser to the authenticator URL, triggering the browser wallet, etc. If it is not supported due to platform or some other requirement, it can continue with the next one
Upon successful authentication, dApp matches the domain name, the user's wallet address, and an authenticated session to the application
If no authentication flow can be processed by the application, then display this situation accordingly to the user.

How the dApp actually requests signatures and talks to the signer will vary depending on the authenticator and the chain being used.  One option for session management is WalletConnect, where the "authenticated session" returned is actually a WalletConnect session.  dApps may also choose to directly integrate signer SDKs, providing a more streamlined signing flow.

For example, here is how an application would integrate with the Login With Name login process:
Install the wagmi library and @domain-wallet/loginwithname-wagmi-sdk package
Import the LoginWithName connector from the package
Configure the wagmi config with the LoginWithName connector
Wrap the app with WagmiProvider, pass it the configuration
Use standard wagmi hooks like useAccount, useConnect etc. to manage connection state

Refer to the Wallet Implementer Steps section for detailed code samples.

## Other domain name or name resolution systems

For the purposes of this document, we've detailed a flow based on ENS domains.  But this standard is extensible to any domain resolution system.  For example, to use Solana domains, you could just replace the ENS resolver component with a Solana resolver component.

The standard functions that must be implemented by any resolver are of the format:

### Function to Resolve a Domain Name to an Address

> Function: resolveName
> Description: Resolves a given domain name to a blockchain address.
> Input: name (String) - The domain name to be resolved.
> Output: Address (String | null) - The blockchain address associated with the domain name, or null if no address is found.

This function takes a string input representing the domain name and returns either the associated blockchain address as a string or null if the address cannot be found. This function must handle various types of blockchain addresses and be adaptable to different blockchain technologies.

### Function to Resolve a Domain Name to an authenticator URL or JSON

> Function: resolveAuthenticator
> Description: Resolves a given domain name to a URL or a JSON object that can be used for authentication or further information retrieval.
> Input: name (String) - The domain name to be resolved.
> Output: Authenticator (String | JSON | null) - A URL or JSON object providing authentication details or additional information, or null if no data can be found.

This function accepts a domain name as a string and returns a URL or a JSON object. The output is intended to provide authentication details or additional information related to the domain name. If no relevant data can be found, the function returns null. This function should be flexible enough to support different formats and data structures, adapting to the needs of various blockchain platforms and applications.

#### ENS example implementation using Typescript and Viem:

```typescript
import { type Address, type Chain, createPublicClient, http, PublicClient } from "viem";
import { mainnet } from "viem/chains";
import { normalize } from "viem/ens";

export interface NameResolver {
 resolveName(name: string): Promise<Address | null>;
 resolveAuthenticator(name: string): Promise<string | null>;
}

export interface ENSOptions {
 chain?: Chain;
 jsonRpcUrl?: string;
}

export class ENS implements NameResolver {
 private readonly client: PublicClient;

 constructor(options: ENSOptions) {
   this.client = createPublicClient({
     chain: options.chain ?? mainnet,
     transport: http(options.jsonRpcUrl),
   });
 }

 async resolveName(domainName: string): Promise<Address | null> {
   return this.client.getEnsAddress({
     name: normalize(domainName),
   });
 }

 async resolveKey(domainName: string, key: string): Promise<string | null> {
   return this.client.getEnsText({
     name: normalize(domainName),
     key,
   });
 }

 async resolveAuthenticator(domainName: string): Promise<string | null> {
   return this.resolveKey(domainName, "authenticator");
 }
}
```

# Rationale

Specifying the authenticator URL as a domain name NFT text record allows applications to easily discover and integrate with compatible login methods in a standard way
Having a chain-agnostic standard enables interoperability between different crypto domain providers and authentication methods
Providing clear wallet implementer steps and code samples makes it easy for developers to adopt this standard

# Backwards Compatibility

This standard is fully backwards compatible as it proposes an additional metadata field for crypto domain NFTs. Existing NFTs and applications will continue to function normally.

# dApp Implementer Steps
Here are the detailed steps for dApps to integrate Login With Name @domain/loginwithname-wagmi-sdk connector:
[Login With Name WAGMI SDK](https://github.com/FedericoAmura/login-with-name-wagmi-sdk)

# References

[EIP-4361: Sign-In with Ethereum](https://github.com/ethereum/ercs/blob/master/ERCS/erc-4361.md)
[Login With Name WAGMI SDK](https://github.com/FedericoAmura/login-with-name-wagmi-sdk)

# Copyright

Copyright and related rights waived via [CC0](https://github.com/ChainAgnostic/CAIPs/blob/main/LICENSE).
