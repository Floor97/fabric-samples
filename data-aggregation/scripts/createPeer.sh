#!/usr/bin/env bash

# loosly from:
# https://kctheservant.medium.com/add-a-peer-to-an-organization-in-test-network-hyperledger-fabric-v2-2-4a08cb901c98

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
cd "$SCRIPT_DIR"


add_if_not_yet() {
    # Arg 1 is the file, arg2 the line
    grep -qxF "$2" "$1" || echo "$2" >> "$1"
}

add_to_etc_hosts() {
    FUNC=$(declare -f add_if_not_yet)
    # Add the ones for the base things from test network, and the new peer
    sudo bash -c "$FUNC;
        add_if_not_yet \"/etc/hosts\" \"127.0.0.1 peer0.org1.example.com\";
        add_if_not_yet \"/etc/hosts\" \"127.0.0.1 peer0.org2.example.com\";
        add_if_not_yet \"/etc/hosts\" \"127.0.0.1 orderer.example.com\";
        add_if_not_yet \"/etc/hosts\" \"127.0.0.1 peer${PEER_ID}.org${ORG_ID}.example.com\";
    "
}

exports() {
    cd ../../test-network
    export TEST_NET_PATH="$PWD"

    ORGDIR_PEER="${PWD}/organizations/peerOrganizations/org${ORG_ID}.example.com"
    ORGDIR_ORD="${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com"
    export CRYPTO_DIR="${ORGDIR_PEER}/peers/peer${PEER_ID}.org${ORG_ID}.example.com"

    export PATH="$PATH:$PWD/../bin/"

    export CORE_PEER_TLS_ENABLED=true
    export CORE_PEER_LOCALMSPID="Org${ORG_ID}MSP"
    export CORE_PEER_TLS_ROOTCERT_FILE="$CRYPTO_DIR/tls/ca.crt"
    export CORE_PEER_MSPCONFIGPATH="$ORGDIR_PEER/users/Admin@org${ORG_ID}.example.com/msp"

    if [ "$ORG_ID" = "1" ]; then export CORE_PEER_ADDRESS=localhost:7051
    elif [ "$ORG_ID" = "2" ]; then export CORE_PEER_ADDRESS=localhsot:9051
    else echo "[ Error ] unrecognized organization id"; exit 1
    fi

    export FABRIC_CFG_PATH="$PWD/../config/"
    export ORDERER_CA="$ORGDIR_ORD/msp/tlscacerts/tlsca.example.com-cert.pem"
    export FABRIC_CA_CLIENT_HOME="${PWD}/organizations/peerOrganizations/org${ORG_ID}.example.com/"
}

generate_peer_crypto() {
    if [ -d "$ORGDIR_PEER/peers/peer${PEER_ID}.org${ORG_ID}.example.com/msp/signcerts" ]; then
        echo "Crypto material exists, so lets not make it again."; return
    fi

    if [ "$ORG_ID" = "1" ]; then CA_PORT="7054"
    elif [ "$ORG_ID" = "2" ]; then CA_PORT="8054"
    else echo "[ Error ] unrecognized organization id"; exit 1
    fi
    fabric-ca-client register --caname ca-org${ORG_ID} --id.name peer${PEER_ID} \
        --id.secret peer${PEER_ID}pw --id.type peer \
        --tls.certfiles "${PWD}/organizations/fabric-ca/org${ORG_ID}/tls-cert.pem"
    mkdir -p "$ORGDIR_PEER/peers/peer${PEER_ID}.org${ORG_ID}.example.com"
    fabric-ca-client enroll \
        -u https://peer${PEER_ID}:peer${PEER_ID}pw@localhost:${CA_PORT} \
        --caname ca-org${ORG_ID} -M "${CRYPTO_DIR}/msp" \
        --csr.hosts peer${PEER_ID}.org${ORG_ID}.example.com \
        --tls.certfiles "${PWD}/organizations/fabric-ca/org${ORG_ID}/tls-cert.pem"
    cp "$ORGDIR_PEER/msp/config.yaml" "${CRYPTO_DIR}/msp/config.yaml"
    fabric-ca-client enroll \
        -u https://peer${PEER_ID}:peer${PEER_ID}pw@localhost:${CA_PORT} \
        --caname ca-org${ORG_ID} -M "${CRYPTO_DIR}/tls" --enrollment.profile tls \
        --csr.hosts peer${PEER_ID}.org${ORG_ID}.example.com --csr.hosts localhost \
        --tls.certfiles "${PWD}/organizations/fabric-ca/org${ORG_ID}/tls-cert.pem"

    # Copy files in /tls/ a folder down
    cp "${CRYPTO_DIR}/tls/tlscacerts/"* "${CRYPTO_DIR}/tls/ca.crt"
    cp "${CRYPTO_DIR}/tls/signcerts/"* "${CRYPTO_DIR}/tls/server.crt"
    cp "${CRYPTO_DIR}/tls/keystore/"* "${CRYPTO_DIR}/tls/server.key"
}

create_peer() {
    CORE_PEER_ADDRESS=localhost:$PEER_PORT

    echo "Creating peer crypto"
    exports
    generate_peer_crypto

    echo "Preparing docker file and bringing up peer"
    sed -i "s/  peer[0-9]\+\.org[0-9]\+\.example\.com/  peer$PEER_ID.org$ORG_ID.example.com/g"\
        "$HERE/docker-compose-peerNorgN.yaml"
    docker-compose -f "$HERE/docker-compose-peerNorgN.yaml" up -d
    echo "Waiting a second for peer processing..."
    sleep 8s

    add_to_etc_hosts
    export CORE_PEER_ADDRESS=localhost:$PEER_PORT

    echo "Joining channels ($CHANNELS)"
    for channel in $CHANNELS; do
        echo " ... joining $channel"
        if peer channel list | grep "$channel" >/dev/null; then
            echo "     peer already in channel $channel"
            continue
        fi
        peer channel join -b channel-artifacts/$channel.block
        echo "Waiting a second for peer processing..."
        sleep 10s
        # Check if the ledger is the same now...
        REAL_LEDGER_H=$(CORE_PEER_ADDRESS=localhost:7051 peer channel getinfo -c $channel \
            | grep --color=never -o '{.*}' | jq .height)
        NEW_LEDGER_H=$(peer channel getinfo -c $channel \
            | grep --color=never -o '{.*}' | jq .height)
        echo "Real ledger height: $REAL_LEDGER_H"
        echo "New ledger heigth:  $NEW_LEDGER_H"
        if [ $REAL_LEDGER_H -ne $NEW_LEDGER_H ]; then
            echo "Ledger height did not match"
            exit 1
        fi
    done

    echo "Deploying chaincodes ($CHAINCODES)"
    for chaincode in $CHAINCODES; do
        echo " ... deploying $chaincode"
        CC_CONTAINER="dev-peer$PEER_ID.org$ORG_ID.example.com-$chaincode"
        if docker ps --filter="name=dev-peer" | grep "$CC_CONTAINER" >/dev/null; then
            echo "     peer already has chaincode $channel (container at least)"
            continue
        fi
        peer lifecycle chaincode install $chaincode.tar.gz
    done
}

cleanup_peer() {
    sudo chmod -R 777 "$TEST_NET_PATH"
    docker stop "peer${PEER_ID}.org${ORG_ID}.example.com" || true
    docker rm "peer${PEER_ID}.org${ORG_ID}.example.com" || true
    #\rm -r "$ORGDIR_PEER/peers/peer${PEER_ID}.org${ORG_ID}.example.com" || true
    \rm -r "$TEST_NET_PATH/peer${PEER_ID}.org${ORG_ID}.example.com" || true
}



#########################################################################################
######################################################### Script start: Process arguments

#Notes:
# Starting network should become:
# ./network.sh up createChannel -ca

set -e
HERE="$PWD"
if [ "$#" -lt 2 ]; then
    echo "usage: $0 <ORG_ID> <PEER_ID> -c <CHANNELS...> -n <CHAINCODES...>"
fi

export ORG_ID="$1"
export PEER_ID="$2"
# Peer port will start at 8060, then +100 for each org, and +5 for each id
export PEER_PORT=$(( 8060 + (100 * $ORG_ID) + (5 * $PEER_ID) ))
export PEER_CHAINCODE_PORT=$(( $PEER_PORT + 1 ))
shift; shift

CHANNELS=""
CHAINCODES=""
LASTDASH=""
CLEAN_FILES=0

while [[ $# -gt 0 ]]; do
  case $1 in
    -r|--clean)      CLEAN_FILES=1; shift ;;
    -c|--channels)   LASTDASH="-c"; shift ;;
    -n|--chaincodes) LASTDASH="-n"; shift ;;
    *) case "$LASTDASH" in
          -c) CHANNELS+=" $1" ;;
          -n) CHAINCODES+=" $1" ;;
          *) echo "[ Error ] weird argument without recognized dash"; exit 1 ;;
       esac
       shift ;;
  esac
done


if [ $CLEAN_FILES -eq 1 ]; then
    echo "Cleaning up peer files (peer$PEER_ID.org$ORG_ID)"
    exports
    cleanup_peer
else
    echo "Adding peer with arguments:"
    echo "Org id    : $ORG_ID"
    echo "Peer id   : $PEER_ID"
    echo "Channels  : $CHANNELS"
    echo "Chaincodes: $CHAINCODES"
    echo "Peer port : $PEER_PORT"
    create_peer
fi

