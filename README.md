[//]: # (SPDX-License-Identifier: CC-BY-4.0)

## Description
This repository is a fork of the [fabric-samples repository](https://github.com/hyperledger/fabric-samples), and contains the prototype result of a Research Project. It gives a prototype for general use case data aggregation using Homomorphic Encryption and Hyperledger Fabric. It is an adaption of the Ethereum design proposed in Regueiro et al. (2021) to fit the Hyperledger Fabric blockchain.


## Relevant Changes
 - The paillier-contract-prototype folder was added and contains a smart contract using the Paillier homomorphic encryption scheme.


## Details Paillier Contract
The library [Javallier](https://github.com/n1analytics/javallier) was used to implement the Paillier homomorphic encryption scheme. It contract contains the following transactions:
 - StartAggregation: starts a new aggregation process.
 - AddData: adds the given data into the existing aggregation process.
 - Closed: sets the status of the aggregation processes to closed.
 - RetrieveAggregationProcess: retrieves the aggregation process and removes it from the ledger.
 - AggregationProcessExists: returns a boolean indicating if the aggregation process with key input exists.
 

## Details AggregationProcess object
The object has the following properties:
 - String key: the unique key of the aggregation process.
 - BigInteger modulus: the modulus of the public key encrypting the data in the aggregation process.
 - BigInteger cData: the ciphertext of the encrypted aggregated data.
 - int expData: the exponent of the ciphertext of the encrypted aggregated data.
 - int nrParticipants: the number of participants in the aggregation process.
 - String status: the status of the aggregation process, which is either AGGREGATING or CLOSED.

## Available scripts
- launch_paillier.sh - launches the test-network and deploys the aggregation-process-contract on the network.
- run_paillier.sh    - enables the use of the aforementioned transactions with the correct flags.

## Credit
This project employs the test network from the [Hyperledger Fabric Samples repository](https://github.com/hyperledger/fabric-samples) to test smart contracts.
Hyperledger Project source code files are made available under the Apache License, Version 2.0 (Apache-2.0), located in the [LICENSE](LICENSE) file.
Hyperledger Project documentation files are made available under the Creative Commons Attribution 4.0 International License (CC-BY-4.0), available at http://creativecommons.org/licenses/by/4.0/.

Regueiro, C., Seco, I., de Diego, S., Lage, O., & Etxebarria, L. (2021). Privacy-enhancing distributed protocol for data aggregation based on blockchain and homomorphic encryption. <em>Information Processing & Management, 58(6)</em>, 102745. https://doi.org/https://doi.org/10.1016/j.ipm.2021.102745


