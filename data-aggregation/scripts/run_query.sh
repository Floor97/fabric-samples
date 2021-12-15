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
      -n data-query-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"StartQuery","Args":["'$1'","'$2'","'$3'","'$4'", "'$5'", "'$6'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function add() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n data-query-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"AddResult","Args":["'$1'","'$2'","'$3'", "'$4'"]}' \
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
      -n data-query-contract \
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
      -n data-query-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"RetrieveDataQuery","Args":["'$1'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function remove() {
  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n data-query-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"RemoveDataQuery","Args":["'$1'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'\
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
      -n data-query-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"DataQueryExists","Args":["'$1'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'\
  );
}


if [ $# -eq 0 ]; then # Help menu if no args
  echo "To use this script, use one of the options:"
  echo "start    - to start a new data query             - params: queryID, modulus, ciphertext, exponent"
  echo "add      - To add the result of the data query   - params: queryID, ciphertext, exponent"
  echo "close    - To close the data query               - params: queryID"
  echo "retrieve - To retrieve and remove the data query - params: queryID"
  echo "remove   - To remove the data query              - params: queryID"
  echo "exists   - To check if an data query exists      - params: queryID"

  return 0
fi

TEST_NET="${PWD}/../../test-network"
RESULT=""

case "$1" in
  "start"   ) start $2 $3 $4 $5 $6 $7;;
  "add"     ) add $2 $3 $4 $5;;
  "close"   ) close $2;;
  "retrieve") retrieve $2;;
  "remove"  ) remove $2;;
  "exists"  ) exists $2;;
  * ) echo "Unrecognized command" && return 1 ;;
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




