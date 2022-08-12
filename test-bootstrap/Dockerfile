FROM openjdk:17-jdk-bullseye
RUN mkdir setup
WORKDIR setup

RUN apt-get update && apt-get install -y netcat
COPY Bootstrap.class .
COPY registry.jar .
COPY oidc.jar .

EXPOSE 8761
EXPOSE 8090

ENTRYPOINT ["java", "Bootstrap", "registry.jar", "oidc.jar"] 
