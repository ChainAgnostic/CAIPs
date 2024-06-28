---
caip: 50
title: Multi-Chain Account ID Specification
author: Joel Torstensson (@oed), Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/50
status: Draft
type: Standard
created: 2020-06-10
updated: 2020-06-11
requires: 2
---

## Simple Summary

CAIP-50 defines a way to identify blockchain account addresses uniquely across multiple blockchains

## Abstract

This proposal aims to facilitate using unique address for accounts on multiple blockchain systems using a multi-codec format that encodes variable integer for chain identifiers complaint with CAIP-2 blockchain id specification. This is useful for both decentralized applications and wallets to communicate user accounts for multiple chains using unique identifiers which are machine veriable and can be decoded to identify the original address and chainId encoded. This proposal aims to standardize these identifiers for accounts to allow inteoperability for multi-chain applications.

## Motivation

The motivation for this proposal came from different feedback received from the adoption of CAIP-10 and tackles these to provide a significant improvement in more efficient communication of these identifiers using smaller byte footprint while preserving uniqueness and interoperability.

## Specification

In this specifiation we define the Multi-Chain Account Id or MACI for short which is a compactly encoded account identifier that is contextual to a blockchain using a CAIP-2 chainId.

### Syntax

```js
mcai ::= <multibase_prefix><mcai_code><chain_namespace><id_size><chain_id><address_size><address><parity_byte>
```

### Semantics

- `multibase_prefix` - the prefix which defines which multibase is used to encode the bytes, `z` for `base58btc`
- `mcai_code` - a number registered on the [multicodec table](https://github.com/multiformats/multicodec/blob/master/table.csv), makes the multi-chain account id upgradable, encoded as varint
- `chain_namespace` - see table below, encoded as varint
- `id_size` - the length in bytes of the `chain_id`, encoded as varint
- `chain_id` - the chain id, encoding is defined by the chain namespace
- `address_size` - the length of the address, encoded as varint
- `address` - the address itself, encoding is defined by the chain namespace
- `parity_byte` - a checksum byte, see section below

### MCAI multicodec

Should be a number registered on the [multicodec table](https://github.com/multiformats/multicodec/blob/master/table.csv). In the examples below we use `0xCA` but this is subject to change.

### Chain namespaces

Each blockchain namespace needs to be properly defined with a registry table:

#### Registry Table

| Namespace | code |
| --------- | ---- |
| bip122    | 0x00 |
| eip155    | 0x01 |
| cosmos    | 0x02 |
| polkadot  | 0x03 |
| filecoin  | 0x04 |
| lip9      | 0x05 |
| eosio     | 0x06 |
| tezos     | 0x07 |

#### BIP122 Namespace (CAIP-4)

**Chain ID:** Convert from hex to bytes

**Address:** Convert from base58btc to bytes

**Example:**

In the exammple below we encode `128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6` on bitcoin mainnet.
This means that we use `chain_id = 000000000019d6689c085ae165831e93`

```
z2gNan4mLV1vvkzpmEi2kzHR8wUyFd5SpsuLcFDGbbyXTxbR8HL5hmqf7DhCEx5Lwt
```

#### EIP155 Namespace (CAIP-3)

**Chain ID:** Convert from integer to bytes

**Address:** Convert from hex to bytes

**Example:**

In the exammple below we encode `0xde30da39c46104798bb5aa3fe8b9e0e1f348163f` on ethereum mainnet.
This means that we use `chain_id = 1`

```
zUJWDxUnc8pZCfUtVKcAsRgxijaVqHyuMgeKKF
```

#### Cosmos Namespace (CAIP-5)

- TODO

#### Polkadot Namespace (CAIP-13)

- TODO

#### Filecoin Namespace (CAIP-23)

- TODO

#### LIP9 Namespace (CAIP-6)

- TODO

#### EOSIO Namespace (CAIP-7)

- TODO

#### Tezos Namespace (CAIP-26)

- TODO

### Parity byte

Using the algorithm described on [Wikipedia: checksums](https://en.wikipedia.org/wiki/Checksum). XOR each byte word in the mcai, the resulting byte is the parity byte.

### Making MCAI human readable

We could easily build tools and UIs that decompose the encoded mcai similar to this: https://cid.ipfs.io/#bagcqcera6wh5laey5njuo2weun46wv4cn2jlbn6qio6mt3bwian4kbp76tdq

### Implementation

Below is a PoC implementation in javascript

```js
const varint = require('varint')
const u8a = require('uint8arrays')

const mcai_code = 0xca

const namespaces = {
  bip122: 0x00,
  eip155: 0x01,
  cosmos: 0x02,
  polkadot: 0x03,
  filecoin: 0x04
}

function checksum(bytes) {
  let result = u8a.xor([bytes[0]], [bytes[1]])
  for (let i = 2; i < bytes.length; i++) {
    result = u8a.xor(result, [bytes[i]])
  }
  return result
}

function encodeMCAI(namespace, chain_id, address) {
  const bytes = u8a.concat([
    varint.encode(mcai_code),
    varint.encode(namespace),
    varint.encode(chain_id.length),
    chain_id,
    varint.encode(address.length),
    address
  ])
  const checksummedBytes = u8a.concat([bytes, checksum(bytes)])
  return 'z' + u8a.toString(checksummedBytes, 'base58btc')
}

function encodeBtcMainnet() {
  const chain_id = '000000000019d6689c085ae165831e93'
  const chain_id_bytes = u8a.fromString(chain_id, 'base16')
  const address = '128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6'
  const address_bytes = u8a.fromString(address, 'base58btc')
  return encodeMCAI(namespaces['bip122'], chain_id_bytes, address_bytes)
}

function encodeEthMainnet() {
  const chain_id = 0x01
  const chain_id_bytes = Uint8Array.from([chain_id])
  const address = '0xde30da39c46104798bb5aa3fe8b9e0e1f348163f'
  const address_bytes = u8a.fromString(address.slice(2), 'base16')
  return encodeMCAI(namespaces['eip155'], chain_id_bytes, address_bytes)
}


console.log('btc mainnet:', encodeBtcMainnet())
console.log('eth mainnet:', encodeEthMainnet())
```

### Test Cases

This is a list of manually composed examples comparing CAIP-10 and CAIP-50 identifiers

```
# Bitcoin mainnet
CAIP10 = 128Lkh3S7CkDTBZ8W7BbpsN3YYizJMp8p6@bip122:000000000019d6689c085ae165831e93
CAIP50 = z2gNan4mLV1vvkzpmEi2kzHR8wUyFd5SpsuLcFDGbbyXTxbR8HL5hmqf7DhCEx5Lwt

# Ethereum mainnet
CAIP10 = 0xde30da39c46104798bb5aa3fe8b9e0e1f348163f@eip155:1
CAIP50 = zUJWDxUnc8pZCfUtVKcAsRgxijaVqHyuMgeKKF
```

## Links

- [CAIP-2][caip-2] - Blockchain ID Specification
- Multicodec - https://github.com/multiformats/multicodec/

[caip-2]: https://chainagnostic.org/CAIPs/caip-2

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
