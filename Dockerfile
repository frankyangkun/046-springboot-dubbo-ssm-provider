FROM openjdk:11
MAINTAINER yangkun yangkun@sefon.com
#WORKDIR /ROOT
VOLUME /tmp
ADD target/046-springboot-dubbo-ssm-provider-v1.0.jar 046-springboot-dubbo-ssm-provider-1.0.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "046-springboot-dubbo-ssm-provider-1.0.jar"]

