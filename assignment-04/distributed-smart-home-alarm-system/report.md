# Clustered Smart Home Alarm System Implementation (Pekko Cluster/Scala)

## 1. Problem Analysis
Transitioning the smart home alarm system to a distributed network via Apache Pekko Cluster alters location transparency, actor discovery and lifecycle management. The system must maintain identical state machine logic while operating across a dynamic network topology where keypad, sensors, and the control unit reside on separate JVMs.

Additional challenges include the fact that:

* Peripherals cannot rely on local actor paths or constructor-injected references. They must dynamically discover the active control unit across the cluster network.
* Inter-node communication protocol messages must cross network boundaries.
* A control unit crash/restart results in complete state loss. In such a case it should restart in a `SafeRecovery` state that can be escaped from only by inputting the correct PIN.

## 2. General Strategy and Implementation Details

### Protocol Adaptation
The core protocol was updated to extend a `CborSerializable` marker trait, allowing all inter-node cluster messages to be serialized using **Jackson CBOR**. Additionally, a `ForceRestart` message was added to the `AlarmControlUnit` input protocol to trigger the recovery mechanism on demand.

### Cluster Topology
The monolithic guardian has been redesigned. The new `AlarmSystemGuardian` is capable of spawning independent actor nodes based on cluster configuration roles, allowing the deployment of the control unit, keypad and sensors onto separate nodes rather than a single monolithic system.

* As an intentional implementation choice, the `AlarmControlUnitActor` and `SirenActor` are located on the same cluster node. Because of this, the `SirenActor` logic remains completely unchanged from the single-node implementation.
* Peripheral nodes like keypad and sensors run on separate cluster nodes and leverage the Pekko Cluster **Receptionist** subscription system to locate the `AlarmControlUnit`.

### Peripheral Node States
To handle network latency and control unit restarts, both the `KeypadActor` and `SensorActor` implement a dual-behavior state machine:

* **Standby Behavior:** This is the initial state when the peripheral lacks a reference to the central control unit. The actor subscribes to the cluster `Receptionist` and listens for its `Listing` protocol updates. While in standby, the peripherals can still perform local actions such as buffering typed digits on the keypad or executing local intrusion detection, but they cannot interact with the control unit.
* **Active Behavior:** Triggered automatically once the `Receptionist` delivers a valid `Listing` containing the `AlarmControlUnitActor` reference. Upon transitioning to active, the peripheral can perform normal cluster interactions.

### Control Unit Lifecycle
Upon creation, the `AlarmControlUnitActor` immediately registers itself with the cluster `Receptionist` by providing its designated `ServiceKey`.

To thoroughly test the recovery architecture, the control unit actor constructor includes a `simulateCrash` configuration option. If enabled, the actor sends itself a `ForceRestart` message once a given time mark has been reached. This message is handled in such a way that it intentionally forces the actor to crash, prompting the guardian's supervisor strategy to restart it.

Upon a supervisor-driven restart, the actor loses its current state and boots directly into the newly added `SafeRecovery` state:

* In this state, the control unit is powerless and cannot assume the home is safely armed or disarmed.
* It must receive a valid `PinEntered` message containing the correct PIN code to clear the recovery mode and return to the `Disarmed` state.

## 3. Demo
A complete demonstration of this distributed alarm system is provided via a **Docker Compose** setup; simply run `docker compose up --build` in the root folder to quickly spin up the entire cluster environment.

The test showcases identical functional state transitions to the monolithic version, but executes them in a distributed scenario. Additionally, the demo demonstrates Pekko Cluster node discovery and the lifecycle recovery procedure following a forced control unit restart.