ARG java_version
ARG maven_version

FROM maven:${maven_version}-eclipse-temurin-${java_version}

# Fetched via ADD (the docker engine's own HTTP client) rather than a RUN-step
# curl: RUN-step processes on this AmznDocker host consistently fail HTTPS
# handshakes with "curl: (35) Insufficient randomness", regardless of builder
# (BuildKit or legacy), while the engine's own image-layer pulls succeed —
# so route this download through the engine instead of a sandboxed curl.
ADD https://github.com/mikefarah/yq/releases/download/v4.18.1/yq_linux_amd64 /usr/bin/yq
RUN chmod +x /usr/bin/yq

RUN apt-get update && apt-get install -y gpg
