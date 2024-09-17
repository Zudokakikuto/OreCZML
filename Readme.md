![Logo OreCzml](https://github.com/Zudokakikuto/OreCZML/blob/master/images/OreCZML.png?raw=true)


# Global information

This JAVA project was created in order to assess the matter of display in the library Orekit using the library Cesium.
The aim was to create an interface with some outputs of Orekit and to display them with Cesium. 
Cesium understands CZML file as inputs. Hence, this project provides a library to build a CZML file to input into Cesium.

![Presentation](https://github.com/Zudokakikuto/OreCZML/blob/master/images/sinusoidalAttitudeFovGIF.gif?raw=true)


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


# Examples 

## Solar System Display

![Solar System Display](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExbHd0enEyeWhrYnV4cDQ0Z2wzb2IxcmJlcXBocWJkeDFyN2FzaWVzeCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/YKFBmvZe9JbmbKXhWm/giphy.gif)

## Covered Surface Example

![Covered Surface](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExdXBseXp3eTFrMjNseXZoeG04M2FnOW9hdWZqYnRremM5cWpucWx4aiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/fSNucd4JHr9G9z0brG/giphy-downsized-large.gif)

## Sinusoidal Attitude Fov Example

![Sinusoidal Attitude Fov](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExdWhiN2R2aDI3dXNkaHBjeDFtMGw4OTBxODMzYW54eGY1cGdkZDZnMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/02iHrLPwHaXnWME0XC/giphy.gif)

## Moon Display

![Moon Display](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExZzJzeTByNGZ4eTZoYWp2bGd2c3cyOTJvMXFwZmMwYjJoY2FscmRvZiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/m8sNJc6JvoKMyCcXqP/giphy.gif)

## Ground Track Example

![Ground Track](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExcW4wb3NuMjdnZGxzczR4ajNmbXNqNjFwc2Nua3NrejljNmFmZW82OSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/H75MxF2ReiOjM4gUvN/giphy.gif)

## Inter-Sat Visu Constellation Example

![Inter-Sat Visu Constellation](https://i.giphy.com/media/v1.Y2lkPTc5MGI3NjExMTI5M21jc2tvcmFxYm5iMGdhZXFwd3ZwYTI2aGt3MTk2eTBleXUxZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/X4zRZIzjea8V58uuH3/giphy.gif)


# Wiki and more #

Check out the [wiki](https://gitlab.orekit.org/Zudo/oreczml/-/wikis/home), including the [Get Started](https://gitlab.orekit.org/Zudo/oreczml/-/wikis/Get-Started?redirected_from=How-to-get-started). Also browse the [forum](https://community.cesium.com/) for questions on Cesium.
