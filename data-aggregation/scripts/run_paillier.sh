#!/usr/bin/env bash

# shellcheck disable=SC2120
function start() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregationprocess \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"StartAggregation","Args":["'$1'","'$2'","'$3'","'$4'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function addop() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregationprocess \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"AddOperator","Args":["'$1'","'$2'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function adddata() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregationprocess \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"AddData","Args":["'$1'","'$2'","'$3'","'$4'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function close() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregationprocess \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"Close","Args":["'$1'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function retrieve() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregationprocess \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"RetrieveAggregationProcess","Args":["'$1'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function exists() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregationprocess \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"AggregationProcessExists","Args":["'$1'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'\
  );
}


if [ $# -eq 0 ]; then # Help menu if no args
  echo "To use this script, use one of the options:"
  echo "start    - to start a new aggregation process             - params: id, paillierModulus, postQuantumPk, nrOperators"
  echo "addop    - to add the key of an operator                  - params: id, postQuantumPk"
  echo "adddata  - To add to the aggregated data                  - params: id, ciphertext, exponent, nonces"
  echo "close    - To close the aggregation process               - params: id"
  echo "retrieve - To retrieve and remove the aggregation process - params: id"
  echo "exists   - To check if an aggregation process exists      - params: id"

  return 0
fi

TEST_NET="${PWD}/../../test-network"
RESULT=""

case "$1" in
  "start"   ) start $2 $3 $4 $5;;
  "addop"   ) addop $2 $3;;
  "adddata" ) adddata $2 $3 $4 $5;;
  "close"   ) close $2;;
  "retrieve") retrieve $2;;
  "exists"  ) exists $2;;
  * ) echo "Unrecognized command" && return 1;;
esac

while read -r line; do
  if [[ $line == "payload:"* ]]; then
    IFS=':' read -r p payload <<< "$line"
    echo "payload:"
    echo "${payload:1:-1}" | sed 's/\\//g' | sed 's/{/{\n\t/g' | sed 's/,/,\n\t/g' | sed 's/}/\n}/g' | sed 's/:/: /g'
  else
    echo "$line"
  fi
done <<< "$RESULT"




