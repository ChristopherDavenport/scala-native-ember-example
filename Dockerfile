FROM debian:11-slim as build-env

ARG SBT_VERSION=1.9.6
ARG S2N_VERSION=1.3.55

# We don't use https://github.com/sbt/docker-sbt so that we can chose specific base image
# https://hub.docker.com/_/eclipse-temurin
ENV JAVA_HOME=/opt/java/openjdk
COPY --from=eclipse-temurin:17 $JAVA_HOME $JAVA_HOME
ENV PATH="${JAVA_HOME}/bin:${PATH}"

WORKDIR /build

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl ca-certificates unzip git \
    clang llvm \
    # for building s2n + http4s native
    libssl-dev cmake build-essential

# Install sbt
RUN \
    curl -sL "https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.zip" > /tmp/sbt.zip && \
    unzip /tmp/sbt.zip -d /opt
ENV PATH="/opt/sbt/bin:${PATH}"

# Install s2n.
# We don't use linuxbrew because
# 1. linuxbrew doesn't support ARM64 yet https://docs.brew.sh/Homebrew-on-Linux#arm-unsupported
# 2. sbt doesn't work on qemu (so we can't use --platform) because of https://github.com/docker/for-mac/issues/6174
# Therefore we build s2n from source https://github.com/aws/s2n-tls/blob/e4f5bf6e779c153d9063805be81c547584398f7a/docs/BUILD.md
RUN git clone https://github.com/aws/s2n-tls.git --branch "v$S2N_VERSION"
RUN cd s2n-tls && \
    cmake . -Bbuild \
        -DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_INSTALL_PREFIX=./s2n-tls-install && \
    cmake --build build -j $(nproc) && \
    cmake --install build

# Build the native image.
# WE don't use sbt-docker or sbt-native-packger because we'd like to run nativeLink
# in the target runtime platform, instead of the host platform.
COPY . /build
RUN S2N_LIBRARY_PATH=/build/s2n-tls/s2n-tls-install/lib sbt nativeLink

FROM debian:11-slim
WORKDIR /app
COPY --from=build-env /build/target/scala-3.3.1/scala-native-ember-example-out ./app
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
EXPOSE 8080
ENV S2N_DONT_MLOCK=1
ENTRYPOINT ["./app"]
