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

> Conformance Language. The key words MUST, MUST NOT, SHOULD, MAY are to be interpreted as described in RFC 2119 and RFC 8174 when, and only when, they appear in all capitals. Unless otherwise stated, bullets under a normative heading inherit the heading’s conformance level.
The key words "MUST", "MUST NOT", "REQUIRED", "SHALL", "SHALL NOT", "SHOULD", "SHOULD NOT", "RECOMMENDED", "NOT RECOMMENDED", "MAY", and "OPTIONAL" written in uppercase in this document are to be interpreted as described in [RFC 2119] and [RFC 8174].

### Terminology

- Envelope: The top-level, signed data structure defined by this CAIP.
- Canonical Subset: The exact set of Envelope properties that MUST be serialized deterministically (see Deterministic JSON) and hashed to produce the Anchor.
- Anchor: The 32-byte `qHash` (SHAKE-256, 32 bytes) computed over the canonical serialization of the Canonical Subset.
- Verifier: logic module identified by `verifierId`.
- Voucher: optional on-chain artifact ([EIP-7683]–compatible) keyed by `qHash`.

### Canonical Subset (Normative)

The Canonical Subset MUST contain exactly the following top-level properties and MUST NOT contain any others:

1. "did" (string) — CAIP-10 account identifier of the signer (`did:pkh:eip155:<chainId>:<address>`).
2. "verifierIds" (array of string) — MUST be a non-empty array of ASCII identifiers.
3. "data" (object) — application payload object.
4. "signedTimestamp" (integer) — Unix epoch milliseconds when the user signed.
5. "chainId" (integer) — numeric EVM chain ID.

`chainId` is a JSON integer. The signer DID MUST map to CAIP-10 `did:pkh:eip155:<chainId>:<address>`, where `<chainId>` is the same integer rendered in decimal.

Extensibility. Any additional top-level properties MUST be outside the Canonical Subset and therefore excluded from the Anchor. Producers MAY add such properties (e.g., `signature`, `signedMessage`, `signatureMethod`, `options`, `meta`), but validators MUST ignore unknown non-canonical properties when computing/validating the Anchor, while they MAY apply additional local validation policies to them.

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

- **Canonical signing (MUST):** All sub-requirements in this section are MUST unless marked otherwise.

### Canonical Signing (Normative)

Signatures MUST use [EIP-191] (`personal_sign`) over the exact six-line message below.

Six-line signer message (ABNF):

```abnf
signer-msg = line1 LF line2 LF line3 LF line4 LF line5 LF line6

line1 = "Portable Proof Verification Request" ; fixed context label
line2 = "Wallet: " eth-addr
line3 = "Chain: " 1*DIGIT               ; numeric chain id
line4 = "Verifiers: " verifier-id *("," verifier-id)
line5 = "Data: " 1*VCHAR                ; canonical JSON of envelope.data (no spaces)
line6 = "Timestamp: " 1*DIGIT           ; unix ms

eth-addr = "0x" 40HEXDIG               ; MUST be lowercased in practice
verifier-id = 1*(ALPHA / DIGIT / "-" / "_" / ".")
LF = %x0A
```

Binding rules.

- `line2` address MUST equal the lowercased address component of `did`.
- `line3` MUST equal `chainId`.
- `line4` MUST equal the ASCII join of `verifierIds` with `","` (no spaces).
- `line5` MUST equal the deterministic (canonical) JSON bytes of `data` as defined in Deterministic JSON. It MUST be the exact byte sequence used inside the Canonical Subset's `data` portion.
- `line6` MUST equal `signedTimestamp`.
- The recovered EOA address (for EOA signers) MUST match the address component of `did`.

Address Case. Implementations MUST compare addresses case-insensitively. For canonicalization and binding, the address component of `did` MUST be normalized to lowercase. Examples in this CAIP show lowercase addresses.

Freshness window.

- Verifiers MUST reject if `signedTimestamp` is older than 5 minutes from verification time or more than 60 seconds in the future (clock-skew allowance). Implementations SHOULD make this window configurable, but MUST default to these values.
- Verification time is the validator’s local wall clock; implementations SHOULD use a synchronized time source.

- **Deterministic JSON (MUST):** See the following normative rules.

### Deterministic JSON (Normative)

All canonicalization in this CAIP follows a JCS-style profile.

- Scope. The rules apply to: (1) the Canonical Subset object, and (2) the `"data"` object contained within it. Non-canonical, top-level extension properties are not included in the Anchor and MUST NOT be fed into the Canonical Subset digest.

- Objects. Keys MUST be UTF-8 and lexicographically sorted by Unicode code point, with no duplicate keys.

- Arrays. Order MUST be preserved as provided.

- Values.
  - The value `undefined` MUST NOT appear anywhere in the Canonical Subset or the `"data"` object. Producers MUST omit such keys entirely.
  - The value `null` MUST be preserved as a value if present.
  - Strings MUST use JSON escapes per RFC 8259; numbers SHOULD use shortest round-trip representation; booleans unchanged.

- Whitespace. No insignificant whitespace MUST be present in the serialized canonical form.

- Encoding. Canonical byte sequence MUST be UTF-8.

Canonicalization example (informative)

Input (producer view):

```text
{
  "did": "did:pkh:eip155:1:0xabc000000000000000000000000000000000def0",
  "verifierIds": ["ownership-basic", "x-bonus"],
  "data": { "owner": "0xabc000000000000000000000000000000000def0" },
  "signedTimestamp": 1738532812345,
  "chainId": 1,
  "meta": { "debug": true }    // extension, not canonical
}
```

Canonical Subset serialized (bytes fed to Anchor):

```json
{"chainId":1,"data":{"owner":"0xabc000000000000000000000000000000000def0"},"did":"did:pkh:eip155:1:0xabc000000000000000000000000000000000def0","signedTimestamp":1738532812345,"verifierIds":["ownership-basic","x-bonus"]}
```
- **Anchor (MUST):** `qHash = "0x" + hex_lower( SHAKE-256_32( canonical_json( CanonicalSubset ) ) )`, where `CanonicalSubset` is exactly `{ did, verifierIds, data, signedTimestamp, chainId }` serialized per Deterministic JSON.
  - The hex representation MUST be 64 lowercase hexadecimal characters prefixed with `0x`.
  - Cross-domain portability is maximized with the canonical subset.
- **Voucherization (SHOULD):**
  - provide exactly one [EIP-7683]–compatible voucher per target chain, keyed by `qHash`;
  - creation SHOULD be access-controlled and idempotent.
- **[EIP-712] (Future Work):** A typed-data variant may be standardized in a future revision. This document defines only the EIP-191 string for canonical signing.

### Smart-Account Support (EIP-1271) and 6492 Detection (Normative)

Implementations MUST support contract-based accounts ([EIP-1271]) and MUST detect/verify [EIP-6492] signature wrappers.

Verification algorithm:

1. Parse signature.
   - If input bytes match the EIP-6492 wrapper format, unwrap to obtain the inner signature bytes and deployment proof metadata (e.g., factory address, initCode). Record provenance metadata as needed.
2. Determine signer type.
   - If there is code at the DID’s address on `chainId` (or 6492 proves a counterfactual deployment), treat as a smart account; otherwise treat as EOA.
3. EOA path.
   - Recover address with EIP-191 over the exact six-line message. If recovered address ≠ the address component of `did`, fail.
4. Smart-account path.
   - Call `isValidSignature(<message-bytes>, <signature-bytes>)` on the contract at the DID’s address on the chain identified by `chainId` (or on the counterfactual proven by 6492). The call MUST return magic value `0x1626ba7e`; any other result or a revert MUST be treated as invalid. When using 6492, validators MUST validate the deployment proof per [EIP-6492].
5. Result.
   - On success, proceed to freshness checks and Anchor matching; on failure, reject.

Notes.

- Implementations MUST verify the same message bytes for both EOA and 1271 paths (no hashing differences).
- If both EOA recovery and 1271 succeed (unexpected), prefer 1271 and emit a warning.

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

## Appendix A — Validation Checklist (Informative)

1. Build Canonical Subset (exact keys only) and canonicalize.
2. Compute `Anchor = qHash(canonical-bytes)`.
3. Construct six-line message; ensure exact field equality.
4. Verify signature:
   - Parse 6492 wrapper if present.
   - If EOA: EIP-191 recover equals the DID’s address component.
   - If smart account: EIP-1271 `isValidSignature` returns magic value; 6492 proof valid if used.
5. Check freshness window (`signedTimestamp` within [-5m, +60s]).
6. Canonicalize `data` and ensure it matches the `Data:` line bytes.
7. Accept; otherwise reject with the first failing step recorded.

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
[RFC 2119]: https://www.rfc-editor.org/rfc/rfc2119
[RFC 8174]: https://www.rfc-editor.org/rfc/rfc8174

## References

- CAIP-2 (Chain identifiers), CAIP-10 (Account identifiers)
- EIP-191, EIP-712, EIP-1271, EIP-6492
- EIP-7683 (Cross-chain intents)

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
