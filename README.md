[//]: # (SPDX-License-Identifier: CC-BY-4.0)

## 1. Description
This repository is a fork of the [fabric-samples repository](https://github.com/hyperledger/fabric-samples), and contains the prototype result of a Research Project, in the data-aggregation folder. It gives a prototype for general use case data aggregation using Homomorphic Encryption and Hyperledger Fabric. It is an adaption of the Ethereum design proposed in Regueiro et al. (2021) to fit the Hyperledger Fabric blockchain.


## 2. Relevant Changes
 - The paillier-contract-prototype folder was added and contains a smart contract using the Paillier homomorphic encryption scheme.
 - The data-query-contract-prototype contains a smart contract and corresponding DataType enabling the launch of data queries.  

## 3. Details Paillier Contract
The library [Javallier](https://github.com/n1analytics/javallier) was used to implement the Paillier homomorphic encryption scheme. The contract contains the following transactions:
 - StartAggregation: starts a new aggregation process.
 - AddData: adds the given data into the existing aggregation process.
 - Closed: sets the status of the aggregation processes to closed.
 - RetrieveAggregationProcess: retrieves the aggregation process and removes it from the ledger.
 - AggregationProcessExists: returns a boolean indicating if the aggregation process with key input exists.
 

### 3.1 Details AggregationProcess object
The object has the following properties:
 - String key: the unique key of the aggregation process.
 - BigInteger modulus: the modulus of the public key encrypting the data in the aggregation process.
 - BigInteger cData: the ciphertext of the encrypted aggregated data.
 - int expData: the exponent of the ciphertext of the encrypted aggregated data.
 - int nrParticipants: the number of participants in the aggregation process.
 - String status: the status of the aggregation process, which is either AGGREGATING or CLOSED.

## 4. Details Data Query Contract
No encryption libraries were used in this contract. The contract contains the following transactions:
 - StartQuery: starts a new data query.
 - AddResult: adds the result of the data query.
 - Close: closes the data query.
 - RetrieveDataQuery: retrieves the data query.
 - RemoveDataQuery: removes the data query.
 - DataQueryExists: checks if the data query with the queryID exists.

### 4.1 Details DataQuery object
The object has the following properties:
 - String queryID: the unique ID of the data query.
 - String result: the result of the data query.
 - String expResult: the exponent of the encrypted result.
 - String timeLimit: the time limit the data query can have.
 - String nrParticipants: the number of wanted participants in the data query.
 - String status: the status of the data query, which can be WAITING, DONE or CLOSED.

## 5 Available scripts
 - launch_prototype.sh - launches the test-network and deploys the aggregation-process-contract on the network.
 - run_paillier.sh     - enables the use of the aforementioned transactions with the correct flags.
 - run_query.sh        - enables the use of the aforementioned transactions with the correct flags.

## 6 Credit
This project employs the test network from the [Hyperledger Fabric Samples repository](https://github.com/hyperledger/fabric-samples) to test smart contracts.
Hyperledger Project source code files are made available under the Apache License, Version 2.0 (Apache-2.0), located in the [LICENSE](LICENSE) file.
Hyperledger Project documentation files are made available under the Creative Commons Attribution 4.0 International License (CC-BY-4.0), available at http://creativecommons.org/licenses/by/4.0/.

Regueiro, C., Seco, I., de Diego, S., Lage, O., & Etxebarria, L. (2021). Privacy-enhancing distributed protocol for data aggregation based on blockchain and homomorphic encryption. <em>Information Processing & Management, 58(6)</em>, 102745. https://doi.org/https://doi.org/10.1016/j.ipm.2021.102745


