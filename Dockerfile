FROM java:7

MAINTAINER Conjur Inc

RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y vim wget curl git maven

RUN mkdir -p /build

WORKDIR /build

ADD pom.xml pom.xml
ADD settings.xml /root/.m2/settings.xml

# Fetch all of the dependencies.
RUN mvn dependency:go-offline

# RUN possum policy load root root.yml && possum policy load cucumber cucumber.yml
