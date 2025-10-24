![Logo OreCzml](https://github.com/Zudokakikuto/OreCZML/blob/master/images/OreCZML.png?raw=true)

# Introduction

The OreCZML project aims to create an interface between the [Orekit][orekit]
space dynamics library and the [Cesium][cesium] 3D visualization library. Like
Orekit, OreCZML is developed in Java. It delegates calculations to Orekit and
converts the results into [CZML][czml] files. These files can then be used by
Cesium to visualize the trajectories and other orbit and attitude parameters
of satellites.

[![](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Quality Gate Status](https://sonar.orekit.org/api/project_badges/measure?branch=master&project=orekit%3Aoreczml&metric=alert_status)](https://sonar.orekit.org/dashboard?id=orekit%3Aoreczml)
[![Coverage](https://sonar.orekit.org/api/project_badges/measure?project=orekit%3Aoreczml&metric=coverage)](https://sonar.orekit.org/component_measures?metric=Coverage&id=orekit%3Aoreczml)
[![Security Rating](https://sonar.orekit.org/api/project_badges/measure?project=orekit%3Aoreczml&metric=security_rating)](https://sonar.orekit.org/component_measures?metric=security_review_rating&id=orekit%3Aoreczml)
![Latest release](https://gitlab.orekit.org/orekit/oreczml/-/badges/release.svg)

<p align="center">
  <img src="https://github.com/Zudokakikuto/OreCZML/blob/master/images/sinusoidalAttitudeFovGIF.gif?raw=true" alt=""/>
</p>

## Dependencies

* [Orekit](https://gitlab.orekit.org/orekit/orekit)
* [CesiumLanguageWriter](https://github.com/AnalyticalGraphicsInc/czml-writer)
* Java 17 minimum version is required

# Installation

## With Maven

Orekit and Junit can be installed with Maven using a pom.xml with dependencies:
* [Orekit](https://mvnrepository.com/artifact/org.orekit/orekit)
* [junit-jupiter](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter)

The CZML writer must be installed differently, as the package is not available
in any Maven repository. Two methods can be used:

1. Download the `.jar` file and install it directly.

   To begin, specify the groupId (`com.agi`), artifactId (`czml-writer`) and
   version (`3.0.0`) in the `pom.xml` file by adding the following lines:

    ```xml
    <dependency>
      <groupId>com.agi</groupId>
      <artifactId>czml-writer</artifactId>
      <version>3.0.0</version>
    </dependency>
    ```

   Then, install this dependency using the command below:

    ```bash
    mvn install:install-file \
        -Dfile=.\cicd\cesiumlanguagewriter-3.0.0.jar \
        -DgroupId=com.agi \
        -DartifactId=czml-writer \
        -Dversion=3.0.0 \
        -Dpackaging=jar \
        -DgeneratePom=true
    ```

   If Maven doesn't find the path with `.\`, replace the dot with your local
   path.

2. Directly add the `.jar` file locally in your IDE, here are some methods for
   the most used IDE:

    * [Eclipse](https://stackoverflow.com/questions/3280353/how-to-import-a-jar-in-eclipse)
    * [IntelliJ](https://www.geeksforgeeks.org/how-to-add-external-jar-file-to-an-intellij-idea-project/)
    * [VS Code](https://www.codeproject.com/Questions/5281024/How-to-import-jar-file-in-vs-code)
    * [Net Beans](https://stackoverflow.com/questions/17693040/adding-external-jar-to-maven-project-in-netbeans)

## Clone the repository

Because the project depends on a certain version of the Orekit-Data for test,
it is recommended to clone the repository by including the submodules. To do
so you can write:

```git
git clone --recurse-submodules https://gitlab.orekit.org/orekit/oreczml.git
```

If you cloned the repository without the submodules, you can still do:

```git
git submodule update --init
```

# Deployment

To use a deployed version of OreCzml, you will need to download the `.jar`
file of the core module. You can find the last version in the
[releases](https://gitlab.orekit.org/orekit/oreczml/-/releases). Then, run
this Maven command:

```bash
mvn install:install-file \
    -Dfile=[PATH]\oreczml-core-1.0.jar \
    -DgroupId=org.orekit \
    -DartifactId=OreCzml \
    -Dversion=1.0 \
    -Dpackaging=jar \
    -DgeneratePom=true
```

Replace `[PATH]` with your local path where you stored the `.jar` file.

# Tutorials

If you want to launch a tutorial, you will need to edit the source file and
modify following Java instruction:

```java
final String pathToJSFolder = TutorialUtils.generateJSPath(
                System.getProperty("user.dir"));
```

Replace the `System.getProperty("user.dir")` with the path of the file where
you want your external resources to be taken from if you use some.

The CZML file will be outputted in the `Output` folder.

# Linting

This project uses [checkstyle](https://checkstyle.sourceforge.io/) to check
the code style. The rules are the same as used in Orekit and are located in
[checkstyle.xml](./checkstyle.xml). You can run the check with the command:

```bash
mvn checkstyle:check
```

The Eclipse code formatting rules, copied over from Orekit, are provided in
[orekit-eclipse-formatter.xml](./orekit-eclipse-formatter.xml).
[Spotless](https://github.com/diffplug/spotless/tree/main/plugin-maven) is
used to check and apply these formatting rules to the codebase, and can be run
using:

```bash
mvn spotless:check
mvn spotless:apply
```

# Wiki and more

Check out the [wiki](https://gitlab.orekit.org/orekit/oreczml/-/wikis/home),
including the [Get Started](https://gitlab.orekit.org/orekit/oreczml/-/wikis/Get-Started).
Also visit the [Orekit Forum](https://https://forum.orekit.org) or the
[Cesium Forum](https://community.cesium.com/) for related questions.

# Input accepted

This converter accepts several inputs to write a CZML file:

* An OEM (Orbit Ephemeris Message, CCSDS) file (TXT and XML formats are
  supported)
* A list of Orekit SpacecraftStates
* A TLE (Two-Line Elements message) file
* An Orekit Orbit defined with orbital parameters, the orbital parameters
  supported are:
    * Keplerian parameters
    * Cartesian parameters
    * Equinoctial parameters
    * Circular parameters

# Output

The output obtained will be a `.czml` file that you can directly enter in
Cesium to display.

# Examples

## Solar system display

<p style="text-align=center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExY2I4NGc2aWtkOHU2dGhrajZnYWh5cnlud3lqbjB6eG83N3g0bzk3MyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/uV5Ke9trgpX0sQt5rp/giphy.gif alt="Solar system display"/>
</p>

## Covered surface

<p style="text-align=center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbnkyNGdpcnZidzBjaXJ5cjEwcWFlNHJpOGlocTllMmZ3NWkycGt3aCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/QWsSZDpUdEXCeXIo6U/giphy-downsized-large.gif alt="Covered surface"/>
</p>

## Field of view with sinusoidal attitude

<p style="text-align=center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExdWhiN2R2aDI3dXNkaHBjeDFtMGw4OTBxODMzYW54eGY1cGdkZDZnMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/02iHrLPwHaXnWME0XC/giphy.gif alt="Field of view with sinusoidal attitude"/>
</p>

## Attitude path along the orbit

<p style="text-align=center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbzFpb2F2Ym9tdTRtcGhmOHl2cGZvenRzM2k1bHhwZW9sY3RicTRtMyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/2E8NLtxAs2FIfSMgk3/giphy-downsized-large.gif alt="Attitude path along the orbit"/>
</p>

## Ground track

<p style="text-align=center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExcW4wb3NuMjdnZGxzczR4ajNmbXNqNjFwc2Nua3NrejljNmFmZW82OSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/H75MxF2ReiOjM4gUvN/giphy.gif alt="Ground track"/>
</p>

## Inter-satellite constellation visualisation

<p style="text-align=center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExMTI5M21jc2tvcmFxYm5iMGdhZXFwd3ZwYTI2aGt3MTk2eTBleXUxZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/X4zRZIzjea8V58uuH3/giphy.gif alt="Inter-satellite constellation visualisation"/>
</p>

# Copyright and license

Copyright © 2024–2025, [CS GROUP][csgroup] and other contributors

All Rights Reserved.

Permission to modify and redistribute OreCZML is granted under the terms of
the Apache 2.0 license. See the [LICENSE.txt](LICENSE.txt) file for the full
license.


[orekit]: https://orekit.org/
[cesium]: https://cesium.com/platform/cesiumjs/
[czml]: https://github.com/AnalyticalGraphicsInc/czml-writer/wiki/CZML-Guide
[csgroup]: https://www.cs-soprasteria.com/
