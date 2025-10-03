---
caip: CAIP-380
title: Portable Proof
author: Chris Leal (@cl34l)
discussion-to: https://github.com/ChainAgnostic/CAIPs/pull/380
status: Draft
type: Standard
created: 2025-10-01
updated: 2025-10-01
requires: [2,10]
---

## Simple Summary

A canonical, chain-agnostic, wallet-signed envelope that applications validate once off-chain and can reference anywhere via a deterministic `qHash` anchor.
Identities follow CAIP-10 (`did:pkh`) and chains follow CAIP-2.

## Abstract

Defines a chain-agnostic, wallet-signed proof object anchored by a SHAKE-256 digest (`qHash`).
Applications validate once off-chain; the same `qHash` may be surfaced on-chain for indexing/transport via [EIP-7683]–compatible vouchers.

## Motivation

Establish a canonical, deterministic envelope that can be validated once and referenced anywhere (off-chain or on-chain) without binding to any vendor, domain, or transport.
Align identities with CAIP-10 and chain context with CAIP-2 to avoid namespace collisions.

## Rationale

- Use [EIP-191] for broad wallet compatibility; support [EIP-1271] and detect [EIP-6492] to cover smart accounts and pre-deploy signatures.
- Keep `qHash` anchored to a canonical subset to ensure stable, cross-environment equivalence and idempotency.
- Treat vouchers as optional [EIP-7683]–compatible artifacts keyed by `qHash` to enable cross-chain transport without constraining settlement designs.

## Specification

- **Terminology:**
  - Envelope: the signed data structure defined in this document
  - Anchor: `qHash` computed over the canonical subset
  - Verifier: logic module identified by `verifierId`
  - Voucher: optional on-chain artifact ([EIP-7683]–compatible) keyed by `qHash`

- **Identifiers:**
  - Chains: `eip155:<chainId>` per CAIP-2 (numeric `chainId` on-wire)
  - Accounts: `did:pkh:eip155:<chainId>:<address>` per CAIP-10
- **Envelope (top-level):**
  - Note that the fields `signedMessage` and `signatureMethod` are OPTIONAL (see Conformance).

```json
  {
    "did": "did:pkh:eip155:1:0xabc000000000000000000000000000000000def0",
    "qHash": "0x<shake256-hex>",
    "verifierIds": ["ownership-basic"],
    "data": {},
    "signature": "0x…",
    "signedMessage": "Portable Proof Verification Request\nWallet: 0xabc000000000000000000000000000000000def0\nChain: 1\nVerifiers: ownership-basic\nData: {}\nTimestamp: 1730000000000",
    "signedTimestamp": 1730000000000,
    "chainId": 1,
    "signatureMethod": "eip191",
    "options": {},
    "meta": {}
  }
```

- **Canonical signing (MUST):** [EIP-191] (also known as `personal_sign`). Exact six-line signer string structure (line 1 is a fixed context label; default label shown):
  - Freshness: reject if older than 5 minutes or >1 minute in the future
  - Smart-accounts: support [EIP-1271];
  - detect [EIP-6492] wrappers.

```sh
  Portable Proof Verification Request
  Wallet: 0x<lowercased address>
  Chain: <numeric chain id>
  Verifiers: <comma-separated verifier ids>
  Data: <deterministic JSON of envelope.data>
  Timestamp: <unix ms>
```

- **Deterministic JSON (MUST):**
  - key-sorted objects;
  - omit `undefined`;
  - preserve `null`;
  - arrays keep order;
  - standard JSON escaping;
  - no "pretty-print" or whitespace.
- **Anchor (SHOULD):** `qHash = SHAKE-256_32(canonical_json({ did, verifierIds, data, signedTimestamp, chainId }))`.
  - Implementations MAY compute `qHash = SHAKE-256_32(canonical_json(data))` for compatibility with existing systems, provided validators reconstruct the same anchor deterministically.
  - Cross-domain portability is maximized with the canonical subset.
- **Voucherization (SHOULD):**
  - provide exactly one [EIP-7683]–compatible voucher per target chain, keyed by `qHash`;
  - creation SHOULD be access-controlled and idempotent.
- **[EIP-712] option (MAY):** When `signatureMethod` is `eip712`, use [EIP-712] typed data. The envelope fields remain unchanged.

### Conformance

- **Clients MUST:**
  1. construct the six-line signer string;
  2. sign with EIP-191;
  3. include `did`, `qHash`, `verifierIds`, `data`, `signature`, `signedTimestamp`, `chainId`.
- **Clients MAY:**
  1. include `signedMessage` (diagnostic);
  2. include `signatureMethod` (default `eip191`).
- **Validators/Servers MUST:**
  1. reconstruct the signer string;
  2. enforce freshness;
  3. validate DID/`chainId`;
  4. compute/verify `qHash`;
  5. support [EIP-1271] and detect [EIP-6492].
- **Wallets SHOULD:**
  1. support [EIP-1271] and [EIP-6492].

## Security Considerations

Implementers are encouraged to consider:
  
1. Replay-window enforcement;
2. strict signer-string determinism;
3. DID/`chainId` validation;
4. controlled voucher creation;
5. deduplication by `qHash`;
6. idempotent relays.

## Privacy Considerations

Implementers are encouraged to consider:

1. Minimize `data` to what is necessary for verification; avoid including sensitive PII.
2. Public exposure SHOULD avoid revealing raw signatures; share only `qHash` and high-level verifier summaries.
3. For public artifacts, use content addressing (e.g., [IPFS]) and masking where appropriate.

## Test Cases

Canonical example envelope (must match attached test vector [`minimal-1.json`](/assets/caip-380/minimal-1.json)):

```json
{
  "did": "did:pkh:eip155:1:0x1111111111111111111111111111111111111111",
  "qHash": "0x1111111111111111111111111111111111111111111111111111111111111111",
  "verifierIds": ["ownership-basic"],
  "data": {
    "owner": "0x1111111111111111111111111111111111111111",
    "reference": { "type": "other", "id": "example-1" }
  },
  "signedMessage": "Portable Proof Verification Request\nWallet: 0x1111111111111111111111111111111111111111\nChain: 1\nVerifiers: ownership-basic\nData: {\"owner\":\"0x1111111111111111111111111111111111111111\",\"reference\":{\"type\":\"other\",\"id\":\"example-1\"}}\nTimestamp: 1730000000000",
  "signature": "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
  "signedTimestamp": 1730000000000,
  "chainId": 1,
  "signatureMethod": "eip191",
  "meta": {},
  "options": {}
}
```

## Backwards Compatibility

- The envelope uses [CAIP-10]-based DIDs and [CAIP-2] for numeric chain IDs; existing systems using EOA addresses can map directly via `did:pkh`.
- [EIP-191] signatures remain valid for EOAs; [EIP-1271] enables contract wallets without changing envelope fields.
- The `qHash` anchor is stable across environments as long as deterministic JSON and the canonical subset are followed.

## Links

[CAIP-2]: https://chainagnostic.org/CAIPs/caip-2
[CAIP-10]: https://chainagnostic.org/CAIPs/caip-10
[EIP-191]: https://eips.ethereum.org/EIPS/eip-191
[EIP-712]: https://eips.ethereum.org/EIPS/eip-712
[EIP-1271]: https://eips.ethereum.org/EIPS/eip-1271
[EIP-6492]: https://eips.ethereum.org/EIPS/eip-6492
[EIP-7683]: https://eips.ethereum.org/EIPS/eip-7683
[IPFS]: https://docs.ipfs.tech

## References

- CAIP-2 (Chain identifiers), CAIP-10 (Account identifiers)
- EIP-191, EIP-712, EIP-1271, EIP-6492
- EIP-7683 (Cross-chain intents)

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
