#!/bin/sh

mvn -B clean package -Pprod,native oc:build oc:push \
          -Dquarkus.native.container-build=true \
          -Dquarkus.container-image.build=true \
          -Djkube.docker.push.username=${QUAY_MANUFACTURING_USERNAME} \
          -Djkube.docker.push.password=${QUAY_MANUFACTURING_PASSWORD} 
