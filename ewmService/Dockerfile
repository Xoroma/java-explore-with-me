FROM amazoncorretto:11-alpine-jdk
COPY target/*.jar ewmService.jar
ENTRYPOINT ["java","-jar","/ewmService.jar"]