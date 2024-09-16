# Use an official Maven base image
FROM maven:3.9-eclipse-temurin-17-alpine

# Custom installation of cesium writer dependency
COPY cesiumlanguagewriter-1.0.jar .
RUN mvn install:install-file -Dfile=cesiumlanguagewriter-1.0.jar -DgroupId=com.custom -DartifactId=cesium -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

# Run mvn clean install by default
CMD ["mvn", "clean", "install"]
