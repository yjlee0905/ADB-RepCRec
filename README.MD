
### Requirements

The program requires Java 19 for compilation and execution.

The following sequence of commands worked for WSL2 Ubuntu under Windows 10.

``` 
wget https://download.oracle.com/java/19/latest/jdk-19_linux-x64_bin.deb

sudo apt-get -qqy install ./jdk-19_linux-x64_bin.deb

sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-19/bin/java 1919
```

Then confirm the version by

```
java -verison

javac -version
```

Installation of Java 19 depends on the machine and platform.

### Compilation to JAR

There are multiple ways to create a JAR file, depending
on the IDE and environment. The following way is one way
to create a JAR file for this project.

``` 
javac /src/*.java

// TODO
jar cfe ADB-RepCRec.jar Test
```


### Execution

```
java -jar adb_repcrec.jar /path/to/inputFile
```

### Re-direct the standard output to a file.

```
java -jar adb_repcrec.jar /path/to/inputFile > output_path
```

### Shell script to run all examples

Make sure that all input files are under the data directory and has the txt 
extension to work.

```
./run_test.sh
```


