[//]: # (SPDX-License-Identifier: CC-BY-4.0)

## 1. Description
This repository is a fork of the [fabric-samples repository](https://github.com/hyperledger/fabric-samples), and contains the prototype result of a Research Project, in the data-aggregation folder. It gives a prototype for general use case data aggregation using Homomorphic Encryption and Hyperledger Fabric. It is an adaption of the protocol proposed in Regueiro et al. (2021) to fit the Hyperledger Fabric blockchain.
- Several scripts from the test-network folder were duplicated and then adapted to fit the use case. The createPeer.sh script was made according to the tutorial at [kctheservant.medium.com](https://kctheservant.medium.com/add-a-peer-to-an-organization-in-test-network-hyperledger-fabric-v2-2-4a08cb901c98).
- test-network/configtx/configtx.yaml was adapted to allow for a channel to have peers from only one organisation.
- The Application.java classes were created according to the tutorial of the asset-transfer-basic fabric sample.

## 2. Structure
data-aggregation
|
|_data-aggregation-shared
|_data-query-application
|_data-query-contract-prototype
|_gateway
|_libs
|_paillier-application
|_paillier-contract-prototype
|_scripts
|_wallet

From this list, the gateway and wallet folder contain files generated during the connection of the application to the network. The libs folder contains jar files of dependencies that are used in the remaining folders. The data-query-contract-prototype and data-query-application modules contain the smart contract used on the asker channel, and contains the application of the asker respectively. The paillier-contract-prototype and paillier-application modules contain the smart contract used on the participant channel, and contains the application of the participant who interacts with both contracts respectively. The data-aggregation-shared contains classes used in the four last modules.

## 3 Running the network and applications
In this section it will be described how to run the network. This prototype was tested Windows, using WSL2 running Ubuntu 18.04 on docker. 

Step 1. Follow the instructions on [Hyperledger Fabric Docs](https://hyperledger-fabric.readthedocs.io/en/latest/getting_started.html) under "Getting Started - Install". If you want to make sure this went successfully, try the instructions under "Getting Started - Run Fabric". Note that in order do so, the step to install Go in "Getting Started - Install" should also be executed.

Step 2. Launch the network using the launch_prototype.sh script. Run:
	. ./launch_prototype start <nr_peers + 1>
The nr_peers signifies how many extra participants you want to launch. By default 1 asker "peer0.org2.example.com" is launched, and 1 participant "peer0.org1.example.com". To check if this was successful run:
	docker ps -a
which will display the running peers if successful. 

Step 3. Run IPFS. If IPFS is not setup yet, install it using the guide on their [GitHub page](https://github.com/ipfs/ipfs). In data-aggregation/data-aggregation-shared/src/main/java/datatypes/values/IPFSConnection.java fill in your own IPv4 address. 127.0.0.1 could also work, but does not always work with docker. Try http://YOUR_IP:5001/api/v0/version in your browser to see if it works. You should see "403 - command not supported", if it doesn't try running: 
	netsh interface portproxy add v4tov4 listenport=5001 listenaddress=0.0.0.0 connectport=5001 connectaddress=172.20.207.9
in the powershell as administrator.

Step 4. When the network and ipfs are running, simply start the applications in your favourite IDE. Keep in mind that the applications should also be run on WSL2. When the applications start you are prompted to fill in a username, this is used in the creation of ids for data query processes.

## 4 Credit
This project employs the test network from the [Hyperledger Fabric Samples repository](https://github.com/hyperledger/fabric-samples) to test smart contracts.
Hyperledger Project source code files are made available under the Apache License, Version 2.0 (Apache-2.0), located in the [LICENSE](LICENSE) file.
Hyperledger Project documentation files are made available under the Creative Commons Attribution 4.0 International License (CC-BY-4.0), available at http://creativecommons.org/licenses/by/4.0/.

Regueiro, C., Seco, I., de Diego, S., Lage, O., & Etxebarria, L. (2021). Privacy-enhancing distributed protocol for data aggregation based on blockchain and homomorphic encryption. <em>Information Processing & Management, 58(6)</em>, 102745. https://doi.org/https://doi.org/10.1016/j.ipm.2021.102745


