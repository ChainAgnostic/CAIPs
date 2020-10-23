---
caip: 24
title: Chain-Agnostic JSON-RPC Interface
author: Dan Finlay <dan@metamask.io>
discussions-to: TBA
status: Draft
type: Interface Standard
created: 2020-10-13
---

## Simple Summary

As [most Ethereum provider methods](https://eth.wiki/json-rpc/API) today use the `eth_` prefix, we could further standardize this to allow any protocol prefix within a JSON-RPC method, and allow providers to support an arbitrarily extensible interface, like `filecoin_`, `xDai_`, `starkware_`, or any other protocol identifier.

## Abstract

We will need some kind of standard protocol identifier to use as a prefix for any protocol, and I would stress that since this is only application-facing (and not user-facing) it does not need to be hotly contentious meriting a name system, and instead can even be strongly unique identifiers of a given interface. For example:

```javascript
// Arbitrary protocol-specific identifier as prefix:
const PREFIX = 'aowiehafidhashaiuwehfa45729298';

const accounts = await provider.send({
  method: `${PREFIX}_accounts`
});
```

I'll leave the definition of these prefixes as out of the scope of this proposal, as I think arbitrary but unique prefixes can be safe and useful.

Any method with a reserved prefix would then be reserved for interacting with that given protocol. For example, while today `eth_accounts` can refer to any EVM chain, we might add some more specific prefixes, like `EthMainnet_accounts` or `EthRopsten_accounts`, or wallets could even support [CAIP-2](https://github.com/ChainAgnostic/CAIPs/blob/master/CAIPs/caip-2.md) compatible prefixes, like `caip2:eip155:1_accounts`.

While some new providers are being written without protocol prefixes, it is trivial to add a prefix to all methods of any given JSON-RPC interface, making it easy for a new provider to be constructed from a provider that is compatible with this proposal, and making it trivial for this proposal to wrap any given JSON-RPC interface.

## Motivation

The Ethereum JSON-RPC protocol has established [JSON-RPC](https://www.jsonrpc.org/specification) as the standard for new blockchains, with new chains introducing their own JSON-RPC interfaces:
- [Cosmjs/json-rpc](https://www.npmjs.com/package/@cosmjs/json-rpc)
- [Starkware](https://www.npmjs.com/package/starkware-provider)
- [Filecoin](https://docs.filecoin.io/reference/lotus-api/)

For wallets looking to support multiple protocols at once, a popular approach for the short-term has been for wallets to manage multiple networks (like in [EIP-2015](https://github.com/ChainAgnostic/CAIPs.git)), but this has drawbacks:
- It puts a UX burden on users to think about networks.
- It prevents the creation of multi-chain applications, since each is connected to only a single network.
- It creates race conditions in applications where the provider's chain could change definition while a method call is in-flight.

Two other approaches would be:
- Allow the provider to return additional providers in response to methods.
- Allow the provider to handle multiple simultaneous networks at once itself.

Allowing a provider to return additional providers is a nice developer experience, but it breaks outisde the scope of JSON-RPC, and so I would argue it should be built at the convenience library layer, and instead the JSON-RPC interface itself should define how to handle multiple providers.

## Specification

The specification is very simple:

Providers that want to support multiple networks should use unique prefixes to namespace the methods for those various networks. As those prefixes and underlying interfaces are defined, they should be publicized so that they can be more widely adopted by other providers and wallets.

Additionally, the provider should allow detecting what interfaces are supported, and so I will suggest one feature detection method, which will return an array of the supported interface identifiers:

```
{
  method: 'getSupportedProtocols',
}

// returns:
['EtherMain', 'EtherRopsten', 'EtherXDai', 'Bitcoin']
```

## Rationale

Opening this CAIP as a discussion point, there are many variations that could be adopted to the proposal; Maybe this proposal itself should be behind a namespace, for example. Otherwise, I think the Motivation above covers the rationale adequatel.

## Backwards Compatibility

As long as new network identifiers do not overlap with existing prefixes, providers adopting this should be backwards compatible with older providers. This backwards compatibility safety could be enhanced by putting this proposal itself behind another namespace, like `caipN_${PROTOCOL_PREFIX}_${METHOD}`.

## Links

This proposal was spurred by [a question asked on Twitter by Philippe Castonguay](https://twitter.com/PhABCD/status/1316035409198100486?s=20).

Partly inspired by some work I was exploring with adding new protocols to MetaMask in [our Snaps beta](https://github.com/MetaMask/metamask-snaps-beta/pull/167).

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
