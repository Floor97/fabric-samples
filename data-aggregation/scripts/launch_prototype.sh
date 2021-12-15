#!/usr/bin/env bash

function deploy() {
    cd ../../test-network
    # Expects to work in the 'test-network' folder
    if [ "${PWD##*/}" != "test-network" ]; then
      echo "This expects to be run from the 'test-network' folder"
      exit 1
    fi

    # First deploy the thing
    CONTRACT_NAME="aggregation-process-contract"
    if [[ $CONTRACT_NAME == "aggregation-process-contract" ]]; then
      ./network.sh deployCC -ccn "$CONTRACT_NAME"  -ccp "../data-aggregation/paillier-contract-prototype/" -ccl java
    else
      ./network.sh deployCC -ccn "$CONTRACT_NAME"  -ccp "../data-aggregation/data-query-contract-prototype/" -ccl java
    fi

    # Export the path to some binaries
    export PATH=${PWD}/../bin:$PATH
    export FABRIC_CFG_PATH=$PWD/../config/

    # Environment variables for Org1
    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID="Org1MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
    export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
    export CORE_PEER_ADDRESS=localhost:7051
}

function stop() {
    # Expects to work in the 'scripts' folder
    if [ "${PWD##*/}" != "scripts" ]; then
      echo "This expects to be run from the 'scripts' folder"
      exit 1
    fi

    docker stop logspout || true
    cd ../../test-network
    ./network.sh down
}

function start() {
    stop # Stop, which also changes folder
    ./network.sh up createChannel
}

if [ $# -eq 0 ]; then # Help menu if no args
  echo "To use this script, use one of the options:"
  echo "start   - To start the network, and then deploy"
  echo "deploy  - To deploy CC"
  echo "stop    - To stop and clean the network"
  exit 0
fi

case "$2" in
  "agg"   ) CONTRACT_NAME="aggregation-process-contract";;
  "query" ) CONTRACT_NAME="query-process-contract";;
  * ) echo "Unrecognized contract" && exit 1;;
esac

case "$1" in
  "start"  )  start && deploy  ;;
  "deploy" )  deploy ;;
  "stop"   )  stop ;;
  * ) echo "Unrecognized command" && exit 1;;
esac

cd ../data-aggregation/scripts







