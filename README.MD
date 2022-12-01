
### Requirements

The program requires Java 19 for compilation and execution.

The following sequence of commands worked for WSL2 Ubuntu under Windows 10.

``` 
wget https://download.oracle.com/java/19/latest/jdk-19_linux-x64_bin.deb

sudo apt-get -qqy install ./jdk-19_linux-x64_bin.deb

sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-19/bin/java 1919
```
Installation of Java 19 depends on the machine and platform.


If you have previously installed Java 19 before, you could switch versions by the following command and select the number.

```
update-alternatives --config java
```
![screenshot1](https://user-images.githubusercontent.com/15973953/204946271-9d2ed1c3-1ff5-496b-b6ae-fd4acddce6ec.png)

Then confirm the version by

```
java -verison

javac -version
```

should output

```
java 19.0.1 2022-10-18
Java(TM) SE Runtime Environment (build 19.0.1+10-21)
Java HotSpot(TM) 64-Bit Server VM (build 19.0.1+10-21, mixed mode, sharing)
```


### Compilation to JAR

There are multiple ways to create a JAR file, depending
on the IDE and environment. The following way is one way
to create a JAR file for this project. Download the repository
and move to the project directory.


``` 
cd src

// Below command will compile all *.java files to *.class
javac *.java

// here as long as you are in the directory, Test.class could be used to compile the code (must be in the same directory)
java Test ../data/test1.txt

```

Stay in the ``` src/ ``` directory for the following commands.

For convinience, the following command will recursively all names of *.class file to a classes.list file.

```
find . -name '*.class' -print > classes.list
```

Create a jar file from all the .class files listed in classes.list file with an entry point of Test, since this is
the entry point for this project by: (again we are staying in the src directory, but the point is that we are
executing the command where Test.class is located.)

```
jar cvfe adb-repcrec.jar Test @classes.list
```

Now you have a ``` adb-repcrec.jar``` file.


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

### Running Reprozip

We will package by using ```reprozip``` in a directory called ADB-RepCRec that has the following structure and files for submission. There are many more test cases, but abbreviated.

```
~/ADB-RepCRec$ tree
.
├── adb-repcrec.jar
├── data
│   ├── test1.txt
│   ├── test10.txt
│   ├── test11.txt
│   ├── test12.txt
│   ├── test13.txt
│   ├── test14.txt
│   ├── ...
└── run_test.sh
```

Run an experiment and make reprozip to trace that experiement.

```
reprozip trace ./run_time.sh
```

Then,
```
reprozip pack adb
```

Above command will create an adb.rpz, and this will allow the ./run_time.sh command to be reproduced as long as a system has reprounzip.

After transfering this file to a system with reprounzip, you can run the following commands. You might need to ```./reprounzip``` depending if your system has reprounzip installed or in a binary format. The following assumes that you are running this on CIMS.

```
./reprounzip showfiles adb.rpz

./reprounzip info adb.rpz
```

To unzip and run the experiment traced by reprozip, firstly unzip the .rpz file by and create a directory called unzipped_adb and place it inside that by

```
 ./reprounzip directory setup adb.rpz ~/unzipped_adb 
```

and then to reproduce the experiment traced by reprozip, run

```
 ./reprounzip directory run ~/unzipped_adb 
```
