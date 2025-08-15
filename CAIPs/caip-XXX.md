---
caip: XXX
title: EVM Smart Contract Call Reference Specification
author: David Furlong (@davidfurlong), Stephan Cilliers (@stephancill)
discussions-to: XXX
status: Draft
type: Standard
created: 2025-08-15
requires: 2
---

## Simple Summary

CAIP-XXX defines a standardized way to reference objects stored in EVM smart contract mappings or variables through function calls, enabling cross-application and cross-chain references to blockchain data that isn't tokenized as ERC-721s.

## Abstract

This CAIP establishes a uniform method for identifying and referencing data stored in EVM smart contracts through function calls. The specification builds upon CAIP-2 blockchain identification to create unique, resolvable references for contract data accessed via mappings, arrays, or variables, enabling applications to create portable identifiers for blockchain-stored objects.

## Motivation

Many blockchain applications store data in smart contract mappings or variables that cannot be referenced through existing token standards like ERC-721. Examples include:

1. Comments or posts stored in mapping structures (e.g., ECP protocol comments)
2. User profiles or metadata in contract storage
3. Configuration data or registry entries
4. Any contract data accessible through function calls

Currently, each application implements its own addressing scheme for such data, making it impossible to create portable references that work across different applications and chains. This specification provides a universal addressing system for contract data accessible through function calls.

## Specification

### Syntax

The `function_call_reference` is a case-sensitive string in the form:

```
function_call_reference:   chain_id + ":" + contract_address + ":" + method + ":" + function_data
chain_id:                  [-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32} (See CAIP-2)
contract_address:          0x[a-fA-F0-9]{40}
method:                    "call"
function_data:             0x[a-fA-F0-9]*
```

Note: Unlike other CAIP specifications, `function_data` has no fixed maximum length as ABI-encoded function parameters can vary significantly in size. This is a deliberate design trade-off to accommodate the full range of possible function calls.

### Semantics

- `chain_id`: Identifies the blockchain using CAIP-2 format (e.g., `eip155:1` for Ethereum mainnet)
- `contract_address`: The smart contract address containing the data (hex-encoded with 0x prefix)
- `method`: Specifies the method type - currently only "call" is supported for EVM chains
- `function_data`: The complete ABI-encoded function call including function selector and parameters

## Rationale

The goals of the function call reference format are:

- Uniqueness across the entire blockchain ecosystem
- Compatibility with existing EVM tooling and ABI encoding standards
- Extensibility to non-EVM chains through the generic "method" field
- Self-contained references that include all necessary call information

The following design decisions were made:

1. **Complete Function Encoding**: Using full ABI-encoded function call data ensures compatibility with existing EVM tooling and eliminates ambiguity in parameter encoding.

2. **No Length Restriction**: Unlike other CAIP specifications, function_data has no fixed maximum length limit to accommodate complex function calls with multiple or large parameters.

3. **Generic Method Field**: Using "method" instead of EVM-specific terminology allows for future extension to non-EVM chains while maintaining the same general structure.

## Test Cases

```
# ERC-20 total supply (no parameters)
eip155:1:0xA0b86a33E6441E9CbC2d64Ec2344E9C4Db2c4A91:call:0x18160ddd

# ERC-20 balance of specific address
eip155:1:0x6B175474E89094C44Da98b954EedeAC495271d0F:call:0x70a08231000000000000000000000000742d35cc6637c0532e3ee008ee31d49b1f7ca2f1

# ECP comment reference by ID
eip155:8453:0xb262C9278fBcac384Ef59Fc49E24d800152E19b1:call:0x8c20d587a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890
```

## Viem Implementation Example

```typescript
import { encodeFunctionData } from "viem";

// Create function_data for ERC-20 balanceOf(address)
const functionData = encodeFunctionData({
  abi: [
    {
      name: "balanceOf",
      type: "function",
      inputs: [{ type: "address" }],
      outputs: [{ type: "uint256" }],
      stateMutability: "view",
    },
  ],
  args: ["0x742d35cc6637c0532e3ee008ee31d49b1f7ca2f1"],
});
// Result: '0x70a08231000000000000000000000000742d35cc6637c0532e3ee008ee31d49b1f7ca2f1'

// Complete function call reference
const reference = `eip155:1:0x6B175474E89094C44Da98b954EedeAC495271d0F:call:${functionData}`;
```

## Implementation Guidelines

### Function Call Encoding

Function calls should be ABI-encoded according to the Ethereum ABI specification, including:

1. 4-byte function selector (first 4 bytes of keccak256 hash of function signature)
2. ABI-encoded parameters according to function signature

### Reference Resolution

Applications that need to resolve these references can:

1. Parse the reference to extract chain_id, contract_address, method, and function_data
2. Validate the chain_id exists and is accessible
3. Verify the contract_address exists on the specified chain
4. Execute the function call using the function_data
5. Process the returned data according to the function's return type

## Backwards Compatibility

This specification introduces a new standard and does not break existing implementations. Applications can choose to support this reference format alongside their existing methods.

## Future Extensions

The "method" field is designed to allow future extensions to non-EVM chains, for example:

- Solana: `solana:mainnet:program_address:invoke:instruction_data`
- Cosmos: `cosmos:cosmoshub-4:module:query:query_data`

## References

- [CAIP-2][CAIP-2] defines the blockchain identification standard
- [Ethereum ABI Specification][ETH-ABI] defines function call encoding
- [CAIP-1][CAIP-1] defines the CAIP document structure

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
