rm registry.jar 
rm -r fake-oidc-server
rm oidc.jar
rm Bootstrap.class

wget -O registry.jar 'https://jenkins-2.sse.uni-hildesheim.de/view/Teaching/job/Teaching_Service-Registry/lastBuild/net.ssehub.sparky$registry/artifact/net.ssehub.sparky/registry/1.0.0/registry-1.0.0.jar'
git clone https://github.com/CESNET/fake-oidc-server && mvn -f fake-oidc-server/pom.xml package && mv fake-oidc-server/target/fake_oidc_server.jar oidc.jar
javac Bootstrap.java

docker build -t sparky-test-setup .
