---
caip: CAIP-X <X will be changed to the PR number if accepted>
title: Node Software Release Data
author: Will White (@null-ref-ex)
discussions-to: https://github.com/ChainAgnostic/CAIPs/discussions/{TBC CAIP ID}
status: Draft
type: Standard
created: 2023-07-25
updated: 2023-07-25
requires: 2
---

## Simple Summary

CAIP-{TBC} defines a way of referencing recommended node software version(s) in a given network at a specific or current blockheight.

## Abstract

Node operators within distributed blockchain infrastructure must rely on myriad and fragmented communications channels between themselves and foundations as it relates to software releases that employ limited to no standards.
This fragmentation and lack of standard makes the development of automation processes for software release unneccessarily difficult.

## Motivation

No distributed, permissionless system currently exists that enables the dissemination of software release information which employs a known and industry standard data structure.
This creates a layer of inefficiency within the technology stack that at least creates operational costs and at worst creates node downtime within networks.

## Specification

CAIP-{TBC} builds upon CAIP-2 with an additional suffix that represents the network centric height/block-number or human friendly moniker which the requestor is asking for release information from.

### Syntax

The `height` is a case-sensitive string in the form

```
release:           chain_id + "/" + asset_namespace + "/release:" + height + ":" + type
chain_id:          Namespace+Blockchain ID as per [CAIP-2][]
height:            ([a-zA-Z0-9]{1,128}|latest)
type:              (signer|validator|beacon|execution)
```

* Height has to be generic to account for different ways that networks record block progression. It isn't true to say that they will always implementing an incrementing numeric.
* Without the specification of a `type` of release the question should be assumed to be for all known software assets of all types. If a `type` is specified then the question should be assumed to be specifically about that type of software asset.

### Output

Alongside the standard way of representing the data access request path it is vital that a standard be employed for the response so that node operations can rely on the data structure for critical automation work.
The example below is JSON but any serializable format is equally acceptable.

```json
{
    height: "latest",
    chain_id: "EIP155:1",
    known_clients: [
        {
            type: "validator",
            version: "v1.0.1",
            git_hash: "dfcsf2342fsfdfg..",
            asset_download_urls: [
                {
                    url: "https://...",
                    checksum: "df554gb...",
                    operating_system: {
                        name: "Ubuntu",
                        version: 22.04
                        architecture: "AMD64"
                    }
                },
                ...
            ],
            configuration_changes: true // this is important in a bare-metal context owing to configuration being decoupled from code version
            release_type: "nca", // This should be an enum: NCA = Non-Consensus Altering, CA = Consensus Altering
            upgrade_height: "99999" // should be a string to cater for potential non-numeric chains
        },
        ...
    ]
}
```

## Rationale

The goals of CAIP-{TBC} are:

- A standard way of requesting release information for software release on a target blockchain with generic or more specific filters.
- A standard data structure as a response to the data access request posed

The goal of this CAIP is not to be opinionated or define the implementations of either input or output mechanisms. There are still a lot of considerations to be made in terms of how this standard can be implemented in a decentralized and permissionless way, but without the foundational standard to follow an adequate solution will be much harder to design and build.
The aforementioned goals are achieved utilizing the following query parameters:

* `/release` as the immediate suffix to CAIP-2 was chosen to enable immediate recognition of the ~namespace of the data access request as this type of methodology could well be employed for other data partitions in the future.
* `height` was chosen as a generic way of specifying a point in history or indeed a simple `latest` request that can get requisite information without any prior form of blockchain data access to decipher the current state of the chain in question. `latest` will be a very common usage scenario but having the capability to reference historic information will also be very useful.
* `type` allowing for an optional specific for the software asset type allows a query to be more targetted if desirable.


## Test Cases

This is a list of manually composed examples

```
# Ethereum all clients at specific height
eip155:1/release:56745

# Ethereum all clients at latest
eip155:1/release:latest

# Ethereum validator clients at latest
eip155:1/release:latest:validator

# Ethereum beacon clients at latest
eip155:1/release:latest:beacon

# Ethereum validator clients at specific height
eip155:1/release:56745:validator

# Ethereum beacon clients at specific height
eip155:1/release:56745:beacon
```

## Changelog

- 2023-07-25: 
    - initial draft

## Links

- [CAIP-2][] - CASA Chain ID specification

[CAIP-2]: https://ChainAgnostic.org/CAIPs/caip-2

## Copyright

Copyright and related rights waived via [CC0](../LICENSE).
