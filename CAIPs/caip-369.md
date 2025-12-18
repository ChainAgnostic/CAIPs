---
caip: 369
title: Wallet Session Property for Personal Information
author: Pedro Gomes (@pedrouid)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/369
status: Draft
type: Standard
created: 2025-08-04
updated: 2025-08-04
requires: CAIP-25
---

## Simple Summary

This CAIP introduces a new session property for use within CAIP-25 session negotiations, enabling dApps to request structured personal information from users through wallets.

## Abstract

This standard defines the `personalInfo` session property for CAIP-25-compatible providers and clients. It allows for a consistent method to request and return personally identifiable information (PII) during a session, such as name, contact details, and addresses. This enables use cases like e-commerce, KYC, and identity-bound onchain interactions within the WalletConnect ecosystem and other CAIP-25 implementations.

## Motivation

Many decentralized applications require basic personal data for tasks such as order fulfillment, customer support, regulatory compliance, or shipping logistics. Currently, CAIP-25 supports arbitrary `sessionProperties`, but lacks standardization around identity-related fields.

By defining a common format for `personalInfo`, this CAIP ensures interoperability across wallets and apps, reduces implementation ambiguity, and improves user experience when disclosing PII in Web3 contexts.

## Specification

This proposal defines a new top-level session property key:

- **Key**: `personalInfo`
- **Value Type**: Object
- **Fields**:
  - `firstName`: The user's given name.
  - `lastName`: The user's family or surname.
  - `emailAddress`: A valid email address for communication or identification.
  - `phoneNumber`: A phone number with country code for verification or contact.
  - `shippingAddress`: A postal address used for product delivery or identity matching.
  - `billingAddress`: A postal address used for billing or invoicing.

All six fields are expected to be populated by the wallet or client, though implementations MAY allow partial or optional support depending on context or jurisdiction.

Wallets SHOULD prompt the user clearly to approve the transmission of this information and MUST only share it with user consent.

## Rationale

Standardizing personal information fields within session negotiations allows WalletConnect and other CAIP-25-based protocols to:

- Enable e-commerce-style dApps to collect shipping or billing info without custom integrations
- Reduce friction in user onboarding for apps requiring identity context
- Support regulatory or financial compliance (e.g. KYC-lite scenarios)

By using `sessionProperties` rather than `sessionScopes`, the information is treated as contextual metadata relevant to the session lifecycle, rather than a permissioned capability tied to accounts or chains.

## Backwards Compatibility

This extension is fully backward compatible with CAIP-25. Clients that do not recognize the `personalInfo` key may ignore it without causing any session errors. Similarly, wallets that do not support this key may omit it from the session response.

## Reference Implementation

- [Session Properties Specification (CAIP-25)](https://github.com/ChainAgnostic/CAIPs/blob/main/CAIPs/caip-25.md)

## Copyright

Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
