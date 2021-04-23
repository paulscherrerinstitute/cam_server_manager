# Overview

__csm__ is a managemet console for CamServer. 



# Requirements

 * Java 11. 



# Building

Before being able to use gradle either comment the uploadArchives task in the build.gradle or create a ~/.gradle/gradle.properties file with following contents:
```
artifactoryUser=xxx
artifactoryPwd=xxx
artifactoryUrlRel=xxx
artifactoryUrlLibSnap=xxx
```

The JAR file csm-<version>-fat.jar can be built executing:
 ```
 ./gradlew build
 ```  

After executing the build command, the file csm-<version>-fat.jar is located in the folder  ./build/libs. 


## RPM
To build the RPM Java is required to be installed on your build machine (as the compilation of the Java code is not done inside the docker build container). 

To build the RPM, generate the fat jar first:
 ```
 ./gradlew clean build
 ```

Afterwards run the docker rpm build container as follows (for RHEL7):
```
docker run -it --rm -v ~/.ssh:/root/.ssh -v `pwd`:/data paulscherrerinstitute/centos_build_rpm:7 package csm.spec
```

The resulting rpm will be placed in the `rpm` folder.

For SL6 use following command to build the RPM:

```
docker run -it --rm -v ~/.ssh:/root/.ssh -v `pwd`:/data paulscherrerinstitute/centos_build_rpm:6 package csm.spec
```


# Launching

Launch the application typing:
 ```
 java -jar csm-<version>-fat.jar <startup options...>
 ```  

# Startup Options

The most relevant options are:

 * `-srv_url=<...> : Set the URL for the pipeline proxy server`
 * `-cam_srv_url=<...> : Set the URL for the camera proxy server`

