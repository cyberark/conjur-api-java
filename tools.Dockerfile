ARG java_version
ARG maven_version

FROM maven:${maven_version}-eclipse-temurin-${java_version}

RUN curl -L  https://github.com/mikefarah/yq/releases/download/v4.18.1/yq_linux_amd64 -o /usr/bin/yq &&\
    chmod +x /usr/bin/yq

RUN apt-get update && apt-get install -y gpg
