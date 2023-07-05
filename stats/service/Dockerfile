FROM amazoncorretto:17-alpine-jdk
COPY target/*.jar  stats.jar
ENTRYPOINT ["java","-jar","/stats.jar"]