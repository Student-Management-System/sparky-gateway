FROM openjdk:17-jdk-bullseye
RUN mkdir -p /opt/gateway
WORKDIR /opt/gateway

ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.9.0/wait /wait
RUN chmod +x /wait

ADD https://jenkins-2.sse.uni-hildesheim.de/view/Teaching/job/Teaching_Sparky-Gateway/lastSuccessfulBuild/artifact/target/sparky-gateway.jar sparky-gateway.jar
RUN echo "java -Dspring.profiles.active=docker,prod -jar sparky-gateway.jar" >> start.sh
RUN chmod +x start.sh

EXPOSE 8080
CMD /wait && ./start.sh
