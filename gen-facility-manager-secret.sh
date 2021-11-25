#!/bin/sh

#0 - 
oc logout;oc login --server=${QIOT_SNO_SERVER} --username=${QIOT_SNO_USERNAME} --password=${QIOT_SNO_PASSWORD}
mkdir -p src/main/resources/certs/bootstrap

#1 - 
oc extract secret/facility-manager-service-secret --namespace factory --keys=truststore.p12 --to=- > src/main/resources/certs/bootstrap/truststore.p12