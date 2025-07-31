Title: Chain-Agnostic Wallet Address Request URI Scheme
Type: Standard
Status: Draft
Authors: Tim Robinson
Created: 2025-07-06

## Abstract

This CAIP introduces a chain-agnostic URI command (`web3://wallet_getAccounts`) designed to request an array of wallet addresses from any wallet application or service. It returns an array of standardised CAIP-10 format wallet addresses, which clearly specifies the blockchain namespace, chain reference, and wallet address.

## Motivation

Currently there is no way for a service to request an address from a wallet in a chain agnostic way. This makes chain agnostic payments hard because the requester does not know what chains/tokens the payer supports before sending out a payment request. By requesting a wallet address the requester can look up the payers available tokens out of band (or via something like [EIP-7811][]) and then send them an appropriate chain-specific payment request. 

## Specification

The wallet URI scheme is defined as follows:

`web3://wallet_getAccounts`

When invoked by a wallet, this intent will return all wallet addresses in the CAIP-10 format:

`[namespace:reference:address]`

Example response for Ethereum mainnet:

`[eip155:1:0xabc123abc123abc123abc123abc123abc123abc1, eip155:1:0xdef456def456def456def456def456def456def4]`

Example response for Solana mainnet:

`[solana:mainnet:7YhWuwZfYExVt1WkdFdHEbvhb7LgPCz4vNYBrH1N8eYQ]`

If the wallet has a preferred chain for a multi-chain address it could specify that in the response, to guide the requester towards the chain the wallet would prefer. 

## Rationale

This allows a user to use NFC or other simple intent methods to provide their wallet address without manual copying, scanning, or complex integrations. The [CAIP-10][] standard is chosen as it clearly indicates the blockchain namespace and reference.

## Backwards Compatibility

This standard is new and has no backward compatibility issues. It does not conflict with existing CAIPs.

## Security Considerations

Wallets should ensure user consent when responding to this URI.

Wallets should implement warnings and confirmation dialogs to prevent inadvertent exposure of wallet addresses.

## Implementation

Implementations should:

Register `web3://wallet_getAccounts` URI handlers.

Return addresses strictly formatted according to [CAIP-10][].

## References

[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[ERC-7811]: https://eips.ethereum.org/EIPS/eip-7811
