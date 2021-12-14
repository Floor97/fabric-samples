#!/usr/bin/env bash

TEST_NET="${PWD}/../../test-network"
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
    -c '{"function":"AddData","Args":["'$1'","'$2'","'$3'"]}' \
    2>&1 | sed 's/result: /result:\n/g' | sed 's/status:200 /status:200\n/g'
)

#### Pretty print the things:

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