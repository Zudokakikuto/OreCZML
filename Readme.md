![Logo OreCzml](https://github.com/Zudokakikuto/OreCZML/blob/master/src/main/resources/OreCZML.png?raw=true)


# Global information

This JAVA project was created in order to assess the matter of display in the library Orekit using the library Cesium.
The aim was to create an interface with some outputs of Orekit and to display them with Cesium. 
Cesium understands CZML file as inputs. Hence, this project provides a library to build a CZML file to input into Cesium.

![Presentation](https://gitlab.orekit.org/Zudo/oreczml/-/raw/master/src/main/resources/sinusoidalAttitudeFovGIF.gif)

## DEPENDENCIES

- Orekit 12.0.2 available here : https://mvnrepository.com/artifact/org.orekit/orekit
- CesiumLanguageWriter 3.0.0, available here : https://github.com/AnalyticalGraphicsInc/czml-writer

### Installation

#### With maven

Orekit and junit can be installed with maven using a pom.xml with dependencies :
* [Orekit](https://mvnrepository.com/artifact/org.orekit/orekit)
* [junit-jupiter](https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter)

For the czml writer you will need to install it differently, because no maven repository exists, two methods can be used : 

* You download the .jar and you install it directly. Let name the groupId `com.custom`, the artifactId `cesium` and the version iwll be `1.0`. You will need to add in the pom.xml the following lines to do so :
```xml
 <dependency>
      <groupId>com.custom</groupId>
      <artifactId>cesium</artifactId>
      <version>1.0</version>
 </dependency>
```
Now that the dependency is added you can now use the following command in maven to install it :

`mvn install:install-file -Dfile=[path]\cesiumlanguagewriter-3.0.0.jar -DgroupId=com.custom -DartifactId=cesium -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true`

* You can directly add the file locally in your IDE, here are some methods for the most used IDE :

  * [Eclipse](https://stackoverflow.com/questions/3280353/how-to-import-a-jar-in-eclipse)
  * [IntelliJ](https://www.geeksforgeeks.org/how-to-add-external-jar-file-to-an-intellij-idea-project/)
  * [VS Code](https://www.codeproject.com/Questions/5281024/How-to-import-jar-file-in-vs-code)
  * [Net Beans](https://stackoverflow.com/questions/17693040/adding-external-jar-to-maven-project-in-netbeans)


## Input Accepted 

This converter accept several inputs to write a CZML file :

- An Oem file (txt or xml file are supported)
- A list of Orekit SpacecraftStates
- A Tle file
- An Orekit Orbit defined with orbital parameters, the orbital parameters supported are : 
    - Keplerian parameters
    - Cartesian parameters
    - Equinoctial parameters
    - Circular parameters


## Output

The output obtained will be a .czml file that you can directly enter in cesium to display.

===========

# Wiki and more #

Check out the [wiki](https://gitlab.orekit.org/Zudo/oreczml/-/wikis/home), including the [Get Started](https://gitlab.orekit.org/Zudo/oreczml/-/wikis/Get-Started?redirected_from=How-to-get-started). Also browse the [forum](https://community.cesium.com/) for questions on Cesium.
