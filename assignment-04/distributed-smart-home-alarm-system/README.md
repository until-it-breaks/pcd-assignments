# Pekko Cluster Smart Home Alarm System

This repository contains a distributed Pekko Cluster simulation of a smart home alarm system. The system consists of three distinct node types cooperating over a local cluster network: a **Control Unit**, a **Keypad**, and a **Sensors Node**.

The demo showcases real-time cluster node discovery and interaction, state transitions (Disarmed, Exit Delay, Armed, Entry Delay, Alarm, Recovery) and fault recovery.

## Requirements

To run this demo, you must have the following installed on your machine:

*   **Docker**

## Getting Started

### 1. Start the Cluster

To compile the Scala code, package the application jar, and spin up the three-node cluster, run the following command in your terminal from the root directory of this project:

```bash
docker compose up --build