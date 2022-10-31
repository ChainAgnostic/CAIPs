---
# Every document starts with a front matter in YAML enclosed by triple dashes.
# See https://jekyllrb.com/docs/front-matter/ to learn more about this concept.
caip: <to be assigned>
title: StreamSync
author: Joel Thorstensson <joel@3box.io>
discussions-to: <URL>
status: Draft
type: Standard
created: 2022-10-31
updated: 2022-10-31
requires: 162
---

<!--You can leave these HTML comments in your merged EIP and delete the visible duplicate text guides, they will not appear and may be helpful to refer to if you edit it again. This is the suggested template for new EIPs. Note that an EIP number will be assigned by an editor. When opening a pull request to submit your EIP, please use an abbreviated title in the filename, `eip-draft_title_abbrev.md`. The title should be 44 characters or less.-->

## Simple Summary
<!--"If you can't explain it simply, you don't understand it well enough." Provide a simplified and layman-accessible explanation of the CAIP.-->
A libp2p protocol for synchronizing hash linked event streams.

## Abstract
<!--A short (~200 word) description of the technical issue being addressed.-->
This CAIP describes a protocol that enables peers in a network to synchronize self-certifying data structures. This is achived though creating a libp2p protocol which allows peers to query and gossip updates and synchronize event data. Furthermore the libp2p kademlia DHT is used to establish the network topology.

## Motivation
<!--The motivation is critical for CAIP. It should clearly explain why the state of the art is inadequate to address the problem that the CAIP solves. CAIP submissions without sufficient motivation may be rejected outright.-->
[Bitswap](https://docs.ipfs.tech/concepts/bitswap/) as defined by and used by [IPFS](https://ipfs.io) is a decent way to synchronize data if you know the [CID](https://github.com/multiformats/cid) of this content. [CAR Mirror](https://github.com/fission-codes/spec/blob/main/car-pool/car-mirror/SPEC.md) improves upon bitswap and enables even more optimal synchronization of an IPLD DAG. These protocols are great when you want to synchronize a known data set. However, when you want to synchronize an ever changing data set like an event stream these protocols lack some features. This specification is focused on describing a protocol for retrieving the latest events of an event stream as represented by an IPLD DAG. The retrieval of these events are ensured to be in "verification order". In a self-certifying event stream this simply implies that the events are synchornized in the order in which they were created.

## Specification
<!--The technical specification should describe the standard in detail. The specification should be detailed enough to allow competing, interoperable implementations. -->

### Protocol overview

The stream sync protocol utilizes three main mechanisms in order to synchronize event streams between nodes. First of all the libp2p DHT is used to look up other nodes that provide the relevant streams, second a protocol for querying and gossiping tips ([CID](https://github.com/multiformats/cid) of latest event), and third a protocol for synchronizing events in verification order.

### Protocol messages

The stream sync protocol has four main message types that nodes send to each other. It’s implemented as a libp2p sub-protocol, and is inspired by [ipfs bitswap](https://docs.ipfs.tech/concepts/bitswap/).

**Libp2p protocol id:**

```
/caip/streamsync/1.0.0
```

#### Synchronizing tips

The following two protocol messages are used to query and gossip tips.

**SubscribeSet**

This message is used to subscribe to a set of streams. 

```ipldsch
type SubscribeSet struct {
  separatorKey nullable String
  separatorValue nullable Bytes
  streamFilter nullable Bytes
  controllerFilter nullable Bytes
} representation tuple
```

*Properties:*

* `separatorKey` - a string key that has to be set in the *genesis event header* of a matching stream
* `separatorValue` - the value set for the `separatorKey`
* `streamFilter` - a byte array which streamids in this set must end with, e.g. if this property contains 3 bytes all streamids that ends with these three bytes are part of the set
* `controllerFilter` - A bloom filter populated with the DIDs that the set includes

*Invariants:*

* If `separatorKey` and `separatorValue` are null, `streamFilter` must be a full streamId
* If `separatorKey` and `separatorValue` are set, `shardFilter` and `ctrlFilter` are optional

**HaveTips**

A response to a *SubscribeSet* message or an update sent to known subscribed peers containing the latest known tips of the given stream set.

```ipldsch
type HaveTips struct {
  tips [Tips]
  separatorKey nullable String
  separatorValue nullable Bytes
  streamFilter nullable Bytes
  ctrlFilter nullable Bytes
} representation tuple

type Tips struct {
  streamid Bytes
  tips [CID]
} representation tuple
```

*Properties:*

* `tips` - all the tips that the node has for this set
  * `streamid` - the id of the corresponding tips
  * `tips` - a list of latest tips

* `separatorKey`, `separatorValue`, `streamFilter`, and `controllerFilter` - same as in *SubscribeSet*

#### Synchronizing Events

The following two protocol messages are used to synchronize event data between peers.

**WantStream**

Request to stream events from another node given a streamId.

```ipldsch
type WantStream struct {
  streamid Bytes
  knownTips [CID] // the CID(s) of the event to start the streaming from
  offset nullable Integer // number of bytes already received
} representation tuple
```

*Properties:*

* `streamid` - the streamId being requested
* `knownTips` - the CIDs of the tips that the requester already has locally
* `offset` - number of bytes already recieved on top of the known tip

**StreamChunk**

When a node receives a *WantStream* it sends an event chunk as a response. The event bytes are sent in verification order (starting with the oldest event, ending with the most recent event).

```ipldsch
type StreamChunk struct {
  eventData Bytes
} representation tuple
```

*Properties:*

* `eventData` - event data bytes

### DHT usage

In order to find other peers which are likely to already be subscribed to the stream sets your node wants to synchronize the libp2p DHT is utilized. The DHT key used is generated from properties in the *StreamSync* message as follows:

* If the `separatorKey/Value` properties are specified, use `hash(separatorKey | separatorValue)` 
* If `streamFilter` is a full streamid, use `hash(streamFilter)`

This gives nodes an easy way to find other nodes of either a specific stream or a set of streams as defined by the separator.

If the `streamFilter` only contains a smaller number of bytes it may be desirable to construct the key as `hash(separatorKey | separatorValue | streamFilter)`. However, since nodes are free to set the streamFilter however they want it might be hard for a large group of nodes to converge unless they coordinate out of band.

### Lifecycle

Four aspects of the protocol lifecycle are described below.

#### Tip Subscription

1. Peer `A` wants to subscribe to stream set `s`
2. `A` looks up other peers that subscribe using the libp2p DHT, it finds peer `B`
3. `A` also announces to the DHT that it is subscribed to `s`
4. `A` sends a `SubscribeSet` message for `s` to `B`
5. `B` registers that `A` is subscribed to `s` with a ttl of 12h
6. `B` sends a `HaveTips` message with the tips it has validated as a response to `A`
7. `A` registers that `B` is subscribed to `s'` (as defined in the `HaveTips` message) with a ttl of 12h

#### Publishing updates

1. A new event is created in a stream of `s`, the event creator adds this event to peer `A`
2. `A` sends a `Have` message to all peers in its registry which subscribe to `s`

#### Stream events

1. `A` sends a `WantStream` message to `B` based on `A`s current knowledge of `s`
2. `B` responds with a `StreamChunk` message
3. `A` repeats step 1 until it has caught up with the desired tip(s)

#### Maintaining connectivity

1. Every 6h nodes will send new `SubscribeSet` messages for all of its streams to all connected peers
1. Recieving peers will respond as described in *Tip Subscription* from step 5

## Rationale
<!--The rationale fleshes out the specification by describing what motivated the design and why particular design decisions were made. It should describe alternate designs that were considered and related work, e.g. how the feature is supported in other languages. The rationale may also provide evidence of consensus within the community, and should discuss important objections or concerns raised during discussion.-->
At a first glance the `SubscribeSet` and `HaveTips` messages and protocol one might consider why not to use libp2p gossipsub instead. While gossipsub is a great protocol that has been battle tested there are a few reasons why it is not optimal for this use case. 

First of all it forces all peers to agree on a particular pubsub topic. For example a topic based on the *separatorKey/Value*. This would force all nodes to listen to all events published to this stream set. With the protocol defined above peers can add additional constraints to the updates that they care about by specifying the *stream-* and *controllerFilter*. In practice this means that node size can be heterogeneous, i.e. some large nodes that subscribe to everything and many smaller nodes that only subscribe to some subset of streams. 

Another consideration is that in gossipsub messages are propagated though many peers. However, in order to synchronize the events of a stream peers need to be directly connected to the originating peer anyway. In the future peers might be disincentivized to forward `HaveTips` messages since future versions of this protocol could introduce payments for event retrieval.

The purpose of this protocol is to synchronize hash linked and signed event logs. This is the reason that event streams need to be synchronized in verification order, e.g. `event0` -> `event1` -> `event2` and so on. This is to prevent DoS attacks where if the events is sent from tip to first event an attacker can sent a lot of invalid events and the victim node would need to synchronize all data before seeing that these events are invalid.

## Implementations
<!--Please add test cases here if applicable.-->
No implementations exist as of yet. At least one implementation is required for this CAIP to be considered "Final".

## Copyright
Copyright and related rights waived via [CC0](https://creativecommons.org/publicdomain/zero/1.0/).
