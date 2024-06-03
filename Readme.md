
![](./OreCZML.jpg)


OreCZML

===========

### Global information ###

This JAVA project was created in order to assess the matter of display in the library Orekit using the library Cesium.
The aim was to create an interface with some outputs of Orekit and to display them with Cesium. 
Cesium understands CZML file as inputs. Hence, this project provides a library to build a CZML file to input into Cesium.

===========

### DEPENDENCIES ###

- Orekit 12.0.2 available here : https://mvnrepository.com/artifact/org.orekit/orekit
- CesiumLanguageWriter 3.0.0, available here : https://github.com/AnalyticalGraphicsInc/czml-writer

===========

### Input Accepted ###

This converter accept several inputs to write a CZML file :

- An OEM file (txt or xml file are supported)
- A list of Orekit SpacecraftStates
- An Orekit Orbit defined with orbital parameters, the orbital parameters supported are : 
    - Keplerian parameters
    - Cartesian parameters
    - Equinoctial parameters
    - Circular parameters

===========

### Output ###

The output obtained will be a .czml file that you can directly enter in cesium to display.

===========

### Get Started ###

* How to build your CZML file

* With an OEM file 

You will need first to build an OEMFile object with a given path

```
OEMFile oemFile = new OEMFile(inputPath);
```

You will then be able to build the header of your CZML file and to write it

```
Header headerOEM = oemFile.getHeader();
headerOEM.write();
```

If you want to see a satellite you can then build a Sattelite Object, write it and end the file

```
Satellite satelliteCZML = new Satellite(oemFile,header);
satelliteCZML.write();
satelliteCZML.endFile();
```

Finally you create your output file with a path and the path with the name of your file 

```
CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
CZMLFile.write(headerOEM);
CZMLFile.write(satelliteCZML);
```

* With a list of Orekit SpacecraftStates

It is the same method : Creation of a header, write it, creation of a satellite, write it.
You will just need to create a SpacecraftStateListInput object

```
// Object
SpacecraftStateListInput input = new SpacecraftStateListInput(spacecraftStateList);

// Header
Header header = input.getHeader();
header.write();

// Satellite
Satellite satelliteSpacecraftStates = new Satellite(input,header);
satelliteSpacecraftStates.write();
satelliteSpacecraftStates.endFile();

// Output File
CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
CZMLFile.write(header);
CZMLFile.write(satelliteSpacecraftStates);
```

* With an Orekit Orbit

You will need to first create an Orekit Orbit, then to create an OrbitInput object.
You will then need to enter the Type of Orbit with the Orekit OrbitType (here KEPLERIAN is used), and the timescale supported is UTC.

```
// Object
OrbitInput orbitInput = new OrbitInput(orbit, OrbitType.KEPLERIAN,UTC);

// Header
Header header = orbitInput.getHeader();
header.write();

// Satellite
Satellite satelliteOrbit = new Satellite(orbitInput,header);
satelliteOrbit.write();
satelliteOrbit.endFile();

// Output File
CZMLFile CZMLfile = new CZMLFile(pathName,outputPath);
CZMLFile.write(header);
CZMLFile.write(satelliteOrbit);
```

===========

### Wiki and more ###

Check out the [wiki](https://github.com/AnalyticalGraphicsInc/czml-writer/wiki), including the [quick start](https://github.com/AnalyticalGraphicsInc/czml-writer/wiki/Quick-Start). Also browse the [forum](https://community.cesium.com/) for questions on Cesium.