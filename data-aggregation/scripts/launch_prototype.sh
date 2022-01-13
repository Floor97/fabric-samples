#!/usr/bin/env bash

function deploy() {
    #beep 2>/dev/null || true

    # Expects to work in the 'test-network' folder
    if [ "${PWD##*/}" != "test-network" ]; then
      echo "This expects to be run from the 'test-network' folder"
      return 1
    fi

    # First deploy the things
    # Can't use the network.sh function, it would deploy on channel 2 as well	
	./$SCRIPTS/deployCC.sh "participants" "aggregationprocess" \
		"../data-aggregation/paillier-contract-prototype/" "java" "$1"
	./network.sh deployCC -c asker -ccn "query" \
		-ccp "../data-aggregation/data-query-contract-prototype/" \
		-ccl java -ccv $1

    # Export the path and set globals
    export PATH=${PWD}/../bin:$PATH
    export FABRIC_CFG_PATH=$PWD/../config/
    . scripts/envVar.sh
    setGlobals 1
}

function stop() {

    # Expects to work in the 'test-network' folder
    if [ "${PWD##*/}" != "test-network" ]; then
      echo "This expects to be run from the 'test-network' folder"
      return 1
    fi

    docker stop logspout || true
    ./network.sh down

    for (( i=1; i<=$NUMBER_PEERS; i++ )); do
        ./$SCRIPTS/createPeer.sh 1 $i -r
    done
}

function start() {
    #beep 2>/dev/null || true
    stop # Stop the network
    ./network.sh up -ca                         # creates p0o1 and p0o2
    ./network.sh createChannel -c "asker"       # creates channel "asker", both peers join
    ./$SCRIPTS/createChannel.sh "participants"  # creates channel "participant", only p0o1 joins

    \cp "organizations/peerOrganizations/org1.example.com/connection-org1.yaml" "${SCRIPT_DIR}/../gateway/"
    \cp "organizations/peerOrganizations/org2.example.com/connection-org2.yaml" "${SCRIPT_DIR}/../gateway/"

    deploy 1 # Deploy both chaincodes

	for (( i=1; i<=$NUMBER_PEERS; i++ )); do
        ./$SCRIPTS/createPeer.sh 1 $i -c asker participants -n aggregationprocess query
    done
}

if [ $# -eq 0 ]; then # Help menu if no args
  echo "To use this script, use one of the options:"
  echo "start   - To start the network, and then deploy"
  echo "deploy  - To deploy CC"
  echo "stop    - To stop and clean the network"
  return 0
fi

BACKTO=$(pwd)
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR/../../test-network"
SCRIPTS="../data-aggregation/scripts"

NUMBER_PEERS="${2:-5}"

case "$1" in
  "start"  )  start ;;
  "deploy" )  deploy $3;;
  "stop"   )  stop ;;
  * ) echo "Unrecognized command" && return 1;;
esac

cd "$BACKTO"







