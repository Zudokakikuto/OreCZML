![Logo OreCzml](https://github.com/Zudokakikuto/OreCZML/blob/master/images/OreCZML.png?raw=true)

# Global information

This JAVA project was created in order to assess the matter of display in the library Orekit using the library Cesium.
The aim was to create an interface with some outputs of Orekit and to display them with Cesium. 
Cesium understands CZML file as inputs. Hence, this project provides a library to build a CZML file to input into Cesium.

[![](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Quality Gate Status](https://sonar.orekit.org/api/project_badges/measure?project=Zudo_oreczml_AZKVgjr8GI6o9WSLa4d5&metric=alert_status&token=sqb_ec26b77ffc69a7f8da60eab4b3c73ec9b1b851bf)](https://sonar.orekit.org/dashboard?id=Zudo_oreczml_AZKVgjr8GI6o9WSLa4d5)
[![Coverage](https://sonar.orekit.org/api/project_badges/measure?project=Zudo_oreczml_AZKVgjr8GI6o9WSLa4d5&metric=coverage&token=sqb_ec26b77ffc69a7f8da60eab4b3c73ec9b1b851bf)](https://sonar.orekit.org/dashboard?id=Zudo_oreczml_AZKVgjr8GI6o9WSLa4d5)
[![Security Rating](https://sonar.orekit.org/api/project_badges/measure?project=Zudo_oreczml_AZKVgjr8GI6o9WSLa4d5&metric=security_rating&token=sqb_ec26b77ffc69a7f8da60eab4b3c73ec9b1b851bf)](https://sonar.orekit.org/dashboard?id=Zudo_oreczml_AZKVgjr8GI6o9WSLa4d5)

<p align="center">
  <img src=https://github.com/Zudokakikuto/OreCZML/blob/master/images/sinusoidalAttitudeFovGIF.gif?raw=true alt=""/>
</p>

## DEPENDENCIES

- Orekit available here : https://gitlab.orekit.org/orekit/orekit
- CesiumLanguageWriter available here : https://github.com/AnalyticalGraphicsInc/czml-writer
- Java 8 minimum version is required
### Installation

#### With maven

Orekit and junit can be installed with maven using a pom.xml with dependencies :
* [Orekit](https://mvnrepository.com/artifact/org.orekit/orekit)
* [junit-jupiter](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter)

For the czml writer you will need to install it differently, because no maven repository exists, two methods can be used : 

* You download the .jar, and you install it directly. Let name the groupId `com.custom`, the artifactId `czml-writer` and the version will be `3.0.0`. You will need to add in the pom.xml the following lines to do so :
```xml
 <dependency>
      <groupId>com.agi</groupId>
      <artifactId>czml-writer</artifactId>
      <version>3.0.0</version>
 </dependency>
```
Now that the dependency is added you can now use the following command in maven to install it :

(Replace [PATH] with the path that you used)

`mvn install:install-file -Dfile=[PATH]\cicd\cesiumlanguagewriter-3.0.0.jar -DgroupId=com.custom -DartifactId=czml-writer -Dversion=3.0.0 -Dpackaging=jar -DgeneratePom=true`

* You can directly add the file locally in your IDE, here are some methods for the most used IDE :

  * [Eclipse](https://stackoverflow.com/questions/3280353/how-to-import-a-jar-in-eclipse)
  * [IntelliJ](https://www.geeksforgeeks.org/how-to-add-external-jar-file-to-an-intellij-idea-project/)
  * [VS Code](https://www.codeproject.com/Questions/5281024/How-to-import-jar-file-in-vs-code)
  * [Net Beans](https://stackoverflow.com/questions/17693040/adding-external-jar-to-maven-project-in-netbeans)


## Deployment

To deploy OreCzml, you will need to download the .jar. You can find the last version in the [releases](https://gitlab.orekit.org/Zudo/oreczml/-/releases).
Then you run this maven command :

`mvn install:install-file -Dfile=[PATH]\OreCzml-1.0.jar -DgroupId=org.orekit -DartifactId=OreCzml -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true`

## Wiki and more #

Check out the [wiki](https://gitlab.orekit.org/Zudo/oreczml/-/wikis/home), including the [Get Started](https://gitlab.orekit.org/Zudo/oreczml/-/wikis/Get-Started?redirected_from=How-to-get-started). Also browse the [forum](https://community.cesium.com/) for questions on Cesium.

## Input Accepted 

This converter accepts several inputs to write a CZML file :

- An Oem file (TXT or XML file are supported)
- A list of Orekit SpacecraftStates
- A Tle file
- An Orekit Orbit defined with orbital parameters, the orbital parameters supported are : 
    - Keplerian parameters
    - Cartesian parameters
    - Equinoctial parameters
    - Circular parameters


## Output

The output obtained will be a .czml file that you can directly enter in cesium to display.


# Examples 

<h1 style="text-align: center;">Solar System Display</h1>

<p align="center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExY2I4NGc2aWtkOHU2dGhrajZnYWh5cnlud3lqbjB6eG83N3g0bzk3MyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/uV5Ke9trgpX0sQt5rp/giphy.gif alt=""/>
</p>

<h1 style="text-align: center;">Covered Surface</h1>

<p align="center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbnkyNGdpcnZidzBjaXJ5cjEwcWFlNHJpOGlocTllMmZ3NWkycGt3aCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/QWsSZDpUdEXCeXIo6U/giphy-downsized-large.gif alt=""/>
</p>

<h1 style="text-align: center;">Field of view with sinusoidal attitude </h1>

<p align="center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExdWhiN2R2aDI3dXNkaHBjeDFtMGw4OTBxODMzYW54eGY1cGdkZDZnMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/02iHrLPwHaXnWME0XC/giphy.gif alt=""/>
</p>

<h1 style="text-align: center;">Attitude Path along the orbit</h1>

<p align="center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbzFpb2F2Ym9tdTRtcGhmOHl2cGZvenRzM2k1bHhwZW9sY3RicTRtMyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/2E8NLtxAs2FIfSMgk3/giphy-downsized-large.gif alt=""/>
</p>

<h1 style="text-align: center;">Ground Track</h1>

<p align="center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExcW4wb3NuMjdnZGxzczR4ajNmbXNqNjFwc2Nua3NrejljNmFmZW82OSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/H75MxF2ReiOjM4gUvN/giphy.gif alt=""/>
</p>

<h1 style="text-align: center;">Inter-Sat Visu Constellation</h1>

<p align="center">
  <img src=https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExMTI5M21jc2tvcmFxYm5iMGdhZXFwd3ZwYTI2aGt3MTk2eTBleXUxZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/X4zRZIzjea8V58uuH3/giphy.gif alt=""/>
</p>
