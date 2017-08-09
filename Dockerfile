FROM java:7

MAINTAINER Conjur Inc

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y vim wget curl git maven

RUN mkdir -p /conjurinc/api-java

WORKDIR /conjurinc/api-java

COPY . /conjurinc/api-java