---
caip: 28
title: Blockchain Reference for Stellar Namespace
author: Gleb Pitsevich (@pitsevich)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/44#pullrequestreview-594204708
status: Superseded
type: Standard
created: 2021-02-17
requires: 2
superseded-by: https://github.com/ChainAgnostic/namespaces/tree/main/stellar
---

## Simple Summary

This CAIP defines the Stellar network namespace and reference for CAIP-2.

## Abstract
In CAIP-2 a general blockchain identification scheme is defined. This is the
implementation of CAIP-2 for the Stellar network.

## Motivation
See CAIP-2.

## Specification

### Stellar Namespace

The namespace "stellar" refers to the wider Stellar ecosystem.

#### Reference Definition

The reference relies on Stellar's current designation of addresses belonging to test or main networks by prefixing them
with `testnet` or `pubnet` correspondingly.

Reference could only be populated with `testnet` or `pubnet` symbols.

Each Stellar network has its own unique passphrase, which is used when validating signatures on a given transaction.

The current passphrases for the Stellar pubnet and testnet are:
- Pubnet: 'Public Global Stellar Network ; September 2015'
- Testnet: 'Test SDF Network ; September 2015'


### Resolution Method

To resolve a blockchain reference for the Stellar namespace, make a REST GET request to the Stellar Horizon node with endpoint `/` or REST GET request to the Stellar Core node with endpoint `/info`, for example:

```jsonc
// Request
curl -X GET "https://horizon.stellar.org/" -H "accept: application/json"

// Response
{
  "_links": {"...": "..."},
  "horizon_version": "2.0.0-rc-89ef5f86ac784d35e29845496e8e1bceac31298a",
  "core_version": "stellar-core 15.2.0 (54b03f755ae5d5aa12a799c8f1ee4d87fc9d1a1d)",
  "ingest_latest_ledger": 34073932,
  "history_latest_ledger": 34073932,
  "history_latest_ledger_closed_at": "2021-02-19T15:50:02Z",
  "history_elder_ledger": 2,
  "core_latest_ledger": 34073932,
  "network_passphrase": "Public Global Stellar Network ; September 2015",
  "current_protocol_version": 15,
  "core_supported_protocol_version": 15
}
```
The response will return a JSON object which will include network information. 

The blockchain reference can be retrieved from `network_passphrase` response of Horizon or from `network` response of Stellar Core.


## Rationale

Blockchains in the "stellar" namespace are [two Stellar public networks](https://developers.stellar.org/docs/glossary/network-passphrase/) - pubnet and testnet.

## Backwards Compatibility

Not applicable

## Test Cases

This is a list of manually composed examples

```
# Testnet (Test SDF Network ; September 2015)
stellar:testnet

# Pubnet (Public Global Stellar Network ; September 2015)
stellar:pubnet
```

## Links

- [Stellar Specification](https://developers.stellar.org/docs)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
