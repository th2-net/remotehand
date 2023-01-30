FROM gradle:7.6-jdk11 AS build
ARG release_version
ARG artifactory_user
ARG artifactory_password
ARG artifactory_deploy_repo_key
ARG artifactory_url
ARG nexus_url
ARG nexus_user
ARG nexus_password

COPY ./ .
RUN gradle --no-daemon clean build publish artifactoryPublish \
    -Prelease_version=${release_version} \
    -Partifactory_user=${artifactory_user} \
    -Partifactory_password=${artifactory_password} \
    -Partifactory_deploy_repo_key=${artifactory_deploy_repo_key} \
    -Partifactory_url=${artifactory_url} \
    -Pnexus_url=${nexus_url} \
    -Pnexus_user=${nexus_user} \
    -Pnexus_password=${nexus_password}
