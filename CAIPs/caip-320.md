---
caip: CAIP-320
title: Chain Agnostic State Transition Protocol
author: Layer-3 Foundation (@layer-3), Louis Bellet (@mod)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/X
status: Draft
type: Standard
created: 2024-11-22
updated: 2024-11-22
requires: [1]
---

## Simple Summary

A protocol for defining and executing state transitions across diverse Layer 1 (L1) and Layer 2 (L2) blockchain platforms using a chain-agnostic state channel model. This protocol enables interoperability and efficient off-chain state execution, leveraging Nitro Protocol principles while abstracting from EVM-specific dependencies.

## Abstract

The Chain Agnostic State Transition Protocol defines a universal standard for state channels that operate across multiple blockchain ecosystems. It specifies the structure of states, execution rules, and outcomes to facilitate the locking, updating, and finalization of assets within state channels. By abstracting execution rules from the EVM, this protocol ensures compatibility with a variety of consensus mechanisms and cryptographic standards.

## Motivation

Existing state channel implementations are tightly coupled to specific blockchain architectures, such as the Ethereum Virtual Machine (EVM). This limits their adaptability to emerging L1 and L2 ecosystems with different consensus and execution models. This CAIP proposes a universal framework to enable interoperability, reduce operational costs, and encourage ecosystem-wide adoption of state channels.

## Specification

### 1. States & Channels

A **state channel** is an off-chain mechanism for securely updating states between participants, represented by:

#### Fixed Part:
- **Participants:** A list of participant identifiers (e.g., public keys).
- **ChannelNonce:** A unique identifier for the channel.
- **AppDefinition:** The contract or ruleset defining application-specific logic.
- **ChallengeDuration:** The dispute resolution window, expressed in seconds.

#### Variable Part:
- **Outcome:** Specifies how assets are distributed.
- **AppData:** Optional application-specific data.
- **TurnNum:** Monotonically increasing version number.
- **IsFinal:** A boolean to indicate immediate finalization.

States are committed by participants through cryptographic signatures.

### 2. Execution Rules

Channels operate by updating states according to the following principles:
1. **Higher Turn Numbers Precede Lower:** States with a greater `TurnNum` take precedence.
2. **Application Rules:** Defined by the `AppDefinition` logic, enabling custom execution logic.
3. **Finalization Escape Hatch:** Channels marked `IsFinal` bypass other rules for immediate finalization.

#### Example Application Rules
Application rules can include:
- Unanimous consensus (e.g., `ConsensusApp`).
- Round-robin updates.
- Complex rules for virtual payment channels (e.g., `VirtualPaymentApp`).

### 3. Outcomes

An **outcome** determines the redistribution of assets upon channel finalization. Outcomes are composed of:
- **Asset:** The resource type (e.g., token or native cryptocurrency).
- **Allocations:** Destination and amount pairs indicating how assets are distributed.
- **Allocation Types:** 
  - **Simple:** Direct asset transfers.
  - **Guarantee:** Conditional transfers based on secondary channels.

Outcomes ensure interoperability with various asset types, leveraging flexible allocation mechanisms.

## Rationale

This protocol decouples state channel execution from any specific blockchain infrastructure, making it adaptable to diverse environments. By leveraging modular components, it allows:
- Customizable execution rules for different use cases.
- Universal support for asset types, reducing integration overhead.
- Efficient resolution of disputes across heterogeneous networks.

## Test Cases

1. **Multi-Chain State Transition:**
   - Test state updates between participants on chains A and B.
2. **Outcome Redistribution:**
   - Validate the correctness of simple and guaranteed allocations.
3. **Dispute Resolution:**
   - Simulate on-chain adjudication of a disputed state.

## Security Considerations

- **Signature Validity:** Ensure signatures are cryptographically secure and tamper-proof.
- **Replay Attacks:** Protect against reuse of states across different channels.
- **Finalization Safety:** States marked `IsFinal` must prevent further updates.

## Privacy Considerations

The protocol minimizes on-chain data exposure by operating primarily off-chain, reducing the risk of metadata leakage. However, participants should consider the implications of revealing outcomes during disputes.

## Backwards Compatibility

This protocol is designed to be forward-compatible with existing state channel implementations. However, some adaptations may be required to integrate with chains that lack programmable execution environments.

## References
- [Nitro Protocol Overview]: [https://statechannels.org](https://docs.statechannels.org/protocol-tutorial/0010-states-channels/)
- [Exit Format Specification]: https://github.com/statechannels/exit-format

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
