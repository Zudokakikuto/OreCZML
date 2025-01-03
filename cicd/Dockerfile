# Use an official Maven base image
FROM maven:3.9-eclipse-temurin-17-alpine

# Custom installation of cesium writer dependency
COPY cesiumlanguagewriter-3.0.0.jar .
RUN mvn install:install-file -Dfile=cesiumlanguagewriter-3.0.0.jar -DgroupId=com.agi -DartifactId=czml-writer -Dversion=3.0.0 -Dpackaging=jar -DgeneratePom=true

# Run mvn clean install by default
CMD ["mvn", "clean", "install"]
