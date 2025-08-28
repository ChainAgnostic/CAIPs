---
caip: 373
title: EVM Smart Contract Call Reference Specification
author: David Furlong (@davidfurlong), Stephan Cilliers (@stephancill)
discussions-to: https://github.com/ChainAgnostic/CAIPs/pull/373
status: Draft
type: Standard
created: 2025-08-15
requires: [2, 10]
---

## Simple Summary

CAIP-373 extends the [CAIP-10] Account ID Specification to reference specific data within EVM smart contracts fetched through "on-chain" function calls, enabling cross-application and cross-chain references to blockchain data.

## Abstract

This CAIP extends the [CAIP-10] Account ID Specification by adding function call parameters to reference specific data stored within EVM smart contracts.
While CAIP-10 identifies contract addresses, this specification allows referencing specific data within those contracts through function calls.
The specification builds upon [CAIP-2] blockchain identification and [CAIP-10] account addressing to create unique, resolvable references for contract data accessed via mappings, arrays, or variables.

## Motivation

Many blockchain applications store data in smart contract mappings or variables that cannot be referenced through existing token standards like ERC-721. Examples include:

1. Comments or posts stored in mapping structures (e.g., ECP protocol comments)
2. User profiles or metadata in contract storage
3. Configuration data or registry entries
4. Any contract data accessible through function calls

While CAIP-10 provides a way to identify contract addresses across chains, there is no standardized way to reference specific data within those contracts
Currently, each application implements its own addressing scheme for such data, making it impossible to create portable references that work across different applications and chains.
This specification extends CAIP-10 to provide a universal addressing system for contract data accessible through function calls.

## Specification

### Syntax

The `function_call_reference` is a case-sensitive string in the form:

```
function_call_reference:   account_id + ":" + method + ":" + function_data + [":" + block_number]
account_id:                chain_id + ":" + account_address (See CAIP-10)
chain_id:                  [-a-z0-9]{3,8}:[-_a-zA-Z0-9]{1,32} (See CAIP-2)
account_address:           0x[a-fA-F0-9]{40}
method:                    "call"
function_data:             0x[a-fA-F0-9]*
block_number:              [0-9]+ (optional)
```

Note: Unlike other CAIP specifications, `function_data` has no fixed maximum length as real-world usage of on-chain function calls (e.g. ABI-encoded function parameters, in EVM-based contexts) can vary significantly in size.
This is a deliberate design trade-off to accommodate the full range of possible function calls.

The `block_number` is optional and when omitted, the function call will be executed against the latest block.
When specified, the function call will be executed against the specified block number for historical data access in VMs/namespaces where this is supported. See per-namespace profiles for syntax, validation rules, etc. of this segment.

### Semantics

- `account_id`: Identifies the smart contract using CAIP-10 format (e.g., `eip155:1:0x6B175474E89094C44Da98b954EedeAC495271d0F`)
- `method`: Specifies the method type - currently only "call" is supported for EVM chains
- `function_data`: The complete parameters passed to the target address, exactly as passed (i.e., in EVM contexts, fully ABI-encoded, including the function's selector and concatenated parameters)
- `block_number`: Optional block number for historical data access (e.g., `12345678`). When omitted, the function call executes against the latest block.

This extends CAIP-10 by appending `:method:function_data[:block_number]` to a standard CAIP-10 account identifier.

## Rationale

The goals of the function call reference format are:

- Uniqueness across the entire blockchain ecosystem
- Compatibility with existing EVM tooling and ABI encoding standards
- Extensibility to non-EVM chains through the generic "method" field
- Self-contained references that include all necessary call information

The following design decisions were made:

1. **Extension of CAIP-10**: Building on the established account identification standard ensures compatibility and follows the logical progression from identifying contracts to identifying data within contracts.

2. **Complete Function Encoding**: Using full ABI-encoded function call data ensures compatibility with existing EVM tooling and eliminates ambiguity in parameter encoding.

3. **No Length Restriction**: Unlike other CAIP specifications, function_data has no fixed maximum length limit to accommodate complex function calls with multiple or large parameters.

4. **Generic Method Field**: Using "method" instead of EVM-specific terminology allows for future extension to non-EVM chains while maintaining the same general structure.

## Test Cases

```
# ERC-20 total supply (no parameters)
eip155:1:0xA0b86a33E6441E9CbC2d64Ec2344E9C4Db2c4A91:call:0x18160ddd

# ERC-20 balance of specific address
eip155:1:0x6B175474E89094C44Da98b954EedeAC495271d0F:call:0x70a08231000000000000000000000000742d35cc6637c0532e3ee008ee31d49b1f7ca2f1

# ECP comment reference by ID
eip155:8453:0xb262C9278fBcac384Ef59Fc49E24d800152E19b1:call:0x8c20d587a1b2c3d4e5f6789012345678901234567890123456789012345678901234567890

# Historical ERC-20 balance at specific block
eip155:1:0x6B175474E89094C44Da98b954EedeAC495271d0F:call:0x70a08231000000000000000000000000742d35cc6637c0532e3ee008ee31d49b1f7ca2f1:12345678

# Historical total supply at specific block
eip155:1:0xA0b86a33E6441E9CbC2d64Ec2344E9C4Db2c4A91:call:0x18160ddd:98765432
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

1. Parse the reference to extract account_id, method, function_data, and optional block_number (where account_id follows CAIP-10 format)
2. Validate the `chain_id` exists and is accessible
3. Verify the contract_address exists on the specified chain
4. Execute the function call using the function_data at the specified block_number (or latest block if not specified)
5. Process the returned data according to the function's return type

## Backwards Compatibility

This specification introduces a new standard and does not break existing implementations. Applications can choose to support this reference format alongside their existing methods.

## Future Extensions

The "method" field is designed to allow future extensions to non-EVM chains, for example:

- Solana: `solana:mainnet:program_address:invoke:instruction_data`
- Cosmos: `cosmos:cosmoshub-4:module:query:query_data`

## References

- [CAIP-10][CAIP-10] defines the account identification standard that this specification extends
- [CAIP-2][CAIP-2] defines the blockchain identification standard
- [Ethereum ABI Specification][ETH-ABI] defines function call encoding
- [CAIP-1][CAIP-1] defines the CAIP document structure

## Links

[CAIP-1]: https://ChainAgnostic.org/CAIPs/caip-1
[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2
[CAIP-10]: https://ChainAgnostic.org/CAIPs/caip-10
[ETH-ABI]: https://docs.soliditylang.org/en/latest/abi-spec.html

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
