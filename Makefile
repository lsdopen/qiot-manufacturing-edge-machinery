.PHONY: package image push
all: package image push

package:
	./mvnw clean package -DskipTests
image:
	docker build -f src/main/docker/Dockerfile.jvm -t quay.io/jules0/lsdqiot-manufacturing-edge-machinery:latest .
push:
	docker push quay.io/jules0/lsdqiot-manufacturing-edge-machinery:latest
