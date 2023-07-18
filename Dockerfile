FROM gradle:8.1.1-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/app
WORKDIR /home/app
RUN gradle clean build --no-daemon -i -x test -x javadoc

FROM openjdk:17-alpine
RUN mkdir /home/app
COPY --from=build /home/app/wizard-web/build/libs/wizard-web-0.0.1-SNAPSHOT.jar /home/app/gaia-x/gaia-x-wizard.jar
WORKDIR /home/app/gaia-x
EXPOSE 8080
ENTRYPOINT ["java","-jar","gaia-x-wizard.jar"]