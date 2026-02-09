FROM maven:3.9-eclipse-temurin-17-alpine

RUN apk add --no-cache git openssh-client

WORKDIR /opt/czml
COPY cesiumlanguagewriter-3.0.0.jar /opt/czml/cesiumlanguagewriter-3.0.0.jar

RUN mvn -q install:install-file \
  -Dfile=/opt/czml/cesiumlanguagewriter-3.0.0.jar \
  -DgroupId=com.agi \
  -DartifactId=czml-writer \
  -Dversion=3.0.0 \
  -Dpackaging=jar \
  -DgeneratePom=true