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
      -n aggregation-process-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"StartAggregation","Args":["'$KEY'","'$ARGS3'","'$ARGS4'","'$ARGS5'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
  );
}

function add() {
  echo "$KEY";
  echo "$ARGS3";
  echo "$ARGS4";

  RESULT=$(\
    peer chaincode invoke \
      -o localhost:7050 \
      --ordererTLSHostnameOverride orderer.example.com \
      --tls \
      --cafile "${TEST_NET}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem" \
      -C mychannel \
      -n aggregation-process-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"AddData","Args":["'$KEY'","'$ARGS3'","'$AGRS4'"]}' \
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
      -n aggregation-process-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"Close","Args":["'$KEY'"]}' \
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
      -n aggregation-process-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"RetrieveAggregationProcess","Args":["'$KEY'"]}' \
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
      -n aggregation-process-contract \
      --peerAddresses localhost:7051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt" \
      --peerAddresses localhost:9051 \
      --tlsRootCertFiles "${TEST_NET}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt" \
      -c '{"function":"AggregationProcessExists","Args":["'$KEY'"]}' \
      2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'\
  );
}


if [ $# -eq 0 ]; then # Help menu if no args
  echo "To use this script, use one of the options:"
  echo "start    - to start a new aggregation process             - params: key, modulus, ciphertext, exponent"
  echo "add      - To add to the aggregated data                  - params: key, ciphertext, exponent"
  echo "close    - To close the aggregation process               - params: key"
  echo "retrieve - To retrieve and remove the aggregation process - params: key"
  echo "exists   - To check if an aggregation process exists      - params: key"

  exit 0
fi

TEST_NET="${PWD}/../../test-network"
RESULT=""
KEY="$2"
ARGS3="$3"
ARGS4="$4"
ARGS5="$4"

case "$1" in
  "start"   ) start ;;
  "add"     ) add ;;
  "close"   ) close ;;
  "retrieve") retrieve ;;
  "exists"  ) exists ;;
  ? ) echo "Unrecognized command" ;;
esac

while read -r line; do
  if [[ $line == "payload:"* ]]; then
    IFS=':' read -r p payload <<< "$line"
    echo "payload:"
    # jq '. | fromjson' <<< "$payload" # JQ formats into scientific notation...
    echo "${payload:1:-1}" | sed 's/\\//g' | perl -0777 -MJSON::PP -E '
        $j=JSON::PP->new->ascii->pretty->allow_nonref->allow_bignum;
        $p=$j->decode(<>);
        say $j->encode($p)'
  else
    echo "$line"
  fi
done <<< "$RESULT"




