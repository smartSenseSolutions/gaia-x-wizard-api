FROM openjdk:17-alpine
LABEL org.opencontainers.image.authors="nitin.vavdiya@smartsensesolutions.com"
LABEL version="1.0"
LABEL description="Dokcer image for smartSense gaia-x MVP"
RUN mkdir /home/app
RUN adduser -D smartsense && chown -R smartsense /home
USER smartsense
COPY ./build/libs/smartsense-gaia-x-api-0.0.1-SNAPSHOT.jar /home/app
WORKDIR /home/app
CMD java -Djava.security.egd=file:/dev/./urandom $MEMORY_LIMIT -jar smartsense-gaia-x-api-0.0.1-SNAPSHOT.jar