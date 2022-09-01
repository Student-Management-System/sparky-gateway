FROM openjdk:17-jdk-bullseye
RUN mkdir -p /opt/gateway
WORKDIR /opt/gateway

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

COPY target/*.jar sparky-gateway.jar
RUN echo "java -Dspring.profiles.active=docker,prod -jar sparky-gateway.jar" >> start.sh
RUN chmod +x start.sh

EXPOSE 8080
CMD /wait && ./start.sh
