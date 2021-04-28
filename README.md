# World Clock - A JavaFX World Clock
Welcome to the World Clock application! This project is for a series of blog entries at https://foojay.io  

- https://foojay.io/today/creating-a-javafx-world-clock-from-scratch-part-1
- https://foojay.io/today/creating-a-javafx-world-clock-from-scratch-part-2
- https://foojay.io/today/creating-a-javafx-world-clock-from-scratch-part-3
- https://foojay.io/today/creating-a-javafx-world-clock-from-scratch-part-4

![A JavaFX World Clock](https://github.com/carldea/worldclock/blob/main/world-clock-part1_1.png?raw=true)
![A JavaFX World Clock Config](https://github.com/carldea/worldclock/blob/main/world-clock-part3_1.png?raw=true)

This is a standard Maven JavaFX modular (JPMS) application.

## Requirements:
- Maven 3.6.3 or greater (Optional)
- Java 16+
- Bach (https://github.com/sormuras/bach)

## Build project using Bach

`$ .bach/bin/bach build`

To simplify add to your .bashrc or .bash_profile as the following:

`export PATH=$PATH:.bach/bin`

Open a new terminal session to be able to run Bach build tool.

On Windows you'll add to your environment variables as the following:

`set PATH=%PATH%;.bach\bin`

## Run World Clock as a module 

`$ java --add-modules worldclock --module-path .bach/workspace/modules/:.bach/external-modules/ com.carlfx.worldclock.Launcher`

## Run World Clock using a custom image

```bash
# Linux/MacOS
$ .bach/workspace/image/bin/worldclock
```

```bash
# Windows 
$ .bach\workspace\image\bin\worldclock
```

## Clean project using Maven

`$ mvn clean`

## Run World Clock using Maven plugin

`$ mvn javafx:run`

## Create a MacOS package to run allow user to install onto desktop

```bash
$ jpackage --verbose \
      --name "JFX World Clock" \
      --description "JavaFX World Clock Application" \
      --vendor "Carl Dea" \
      --runtime-image .bach/workspace/image \
      --module worldclock/com.carlfx.worldclock.Launcher \
      --dest .bach/workspace/package
```

# Outstanding issues
1. The map doesn't load properly when building and running the app as a built image.
2. Map uses MapBox and Leaflet.js an access_token needs to be created. See https://www.mapbox.com/studio/account/tokens/ for details.
3. Weather is still under construction. This may need an access_token also.

