# syntax=docker/dockerfile:1.4.1
ARG username=worker
ARG work_dir=/home/$username/work
ARG gid=1000
ARG uid=1001

# Copy across all the dependency controlling files in a separate stage
# This will not get any layer caching if anything in the context has changed, but when we
# subsequently copy them into a different stage that stage *will* get layer caching. So if none of
# the dependency controlling files have changed, a subsequent command will also get layer caching.
FROM alpine as gradle-files
RUN --mount=type=bind,target=/docker-context \
    mkdir -p /gradle-files && \
    cd /docker-context/ && \
    find . -name "*.gradle.kts" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name "*.gradle" -exec cp --parents "{}" /gradle-files/ \; && \
    find . -name "*module-info.java" -exec cp --parents "{}" /gradle-files/ \;



FROM eclipse-temurin:17.0.2_8-jdk-focal as builder
ARG username
ARG work_dir
ARG gid
ARG uid

RUN addgroup --system $username --gid $gid && \
    adduser --system $username --ingroup $username --uid $uid

USER $username
RUN mkdir -p $work_dir
WORKDIR $work_dir

# The single use daemon will be unavoidable in future so don't waste time trying to prevent it
ENV GRADLE_OPTS='-Dorg.gradle.daemon=false'
ARG gradle_cache_dir=/home/$username/.gradle/caches

# Download gradle in a separate step to benefit from layer caching
COPY --chown=$username gradle/wrapper gradle/wrapper
COPY --chown=$username gradlew gradle.properties ./
RUN ./gradlew --version

# Do all the downloading in one step...
COPY --chown=$username --from=gradle-files /gradle-files .
RUN --mount=type=cache,target=$gradle_cache_dir,gid=$gid,uid=$uid \
    ./gradlew --no-watch-fs --stacktrace downloadDependencies

# So the actual build can run without network access. Proves no tests rely on external services.
COPY --chown=$username . .
RUN --mount=type=cache,target=$gradle_cache_dir,gid=$gid,uid=$uid \
    --network=none \
    ./gradlew --no-watch-fs --offline build || mkdir -p build



FROM scratch as build-output
ARG work_dir

COPY --from=builder $work_dir/build .



FROM builder as checker

RUN --mount=type=cache,target=$gradle_cache_dir,gid=$gid,uid=$uid \
    --network=none \
    ./gradlew --no-watch-fs --offline build
