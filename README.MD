
## Requirements

Java 19 was used for compiling and executing the program. Although reprozip/reprounzip will help to able to make the program reproducible regardless of the Java version or system, we are just stating which Java version was used to compile and create the progrm/JAR.


#### Installing Java 19

The following commands install Java 19 on Windoes 10 / WSL2 Ubuntu, and would differ depending by machine and architecture.
``` 
wget https://download.oracle.com/java/19/latest/jdk-19_linux-x64_bin.deb

sudo apt-get -qqy install ./jdk-19_linux-x64_bin.deb

sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/jdk-19/bin/java 1919
```

#### Switching Java Versions
If you have previously installed Java 19 before, you could switch versions by the following command and select the number.
```
update-alternatives --config java
```
![image](https://user-images.githubusercontent.com/15973953/204961738-51a39d1a-f775-4ff8-9152-fc0aecfd0f8f.png)

#### Checking Java version
After installing/changing, confirm the version by
```
java -verison
```

should output
```
java 19.0.1 2022-10-18
Java(TM) SE Runtime Environment (build 19.0.1+10-21)
Java HotSpot(TM) 64-Bit Server VM (build 19.0.1+10-21, mixed mode, sharing)
```


## Compile and Creating JAR
``` 
cd src

// Below command will compile all *.java files to *.class
javac *.java

// here as long as you stay in the directory, Test.class could be used to run the program (must stay in this directory)
java Test ../data/test1.txt
```

There are multiple ways to create a JAR file, depending
on the IDE and environment. The following is one way
to create a JAR file for this project. Download the repository
and move to the project directory in your terminal.

Stay in the ``` src/ ``` directory for the following commands.

For convinience, the following command will recursively list all *.class file to a classes.list file that were compiled above using the ```javac *.java``` command.
```
find . -name '*.class' -print > classes.list
```

Create a jar file from all the .class files listed in classes.list file with an entry point of Test, since it is
the entry point of this project: (again we are staying in the src directory, but the point is that we are
executing the command where Test.class is located.)
```
jar cvfe adb_repcrec.jar Test @classes.list
```

Now you have a ``` adb_repcrec.jar``` file, and this could be executed wherever in the terminal as long as an input file is provided.

## Execution
```
java -jar adb_repcrec.jar /path/to/inputFile
```

## Re-direct the standard output to a file.
```
java -jar adb_repcrec.jar /path/to/inputFile > output_path
```

## Shell script to run all examples
Make sure that all input files are under the data directory and has the txt extension, and that this script is in the same direcetory as the JAR file for execution.

```
./run_test.sh
```

## Running Reprozip

We will package by using ```reprozip``` in a directory called ADB-RepCRec that has the following structure and files for submission. There are many more test cases, but abbreviated below.

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

#### Trace an experiement using Reprozip
```
reprozip trace ./run_time.sh
```

#### Packing using Reprozip
Then,
```
reprozip pack adb
```

Above command will create an adb.rpz, and this will allow the ./run_time.sh command to be reproduced as long as a system has reprounzip.

After transfering this file to a system with reprounzip, you can run the following commands. You might need to ```./reprounzip``` depending on if the system has reprounzip installed or in a binary format. The following assumes that you are running this on CIMS.

## Using Reprounzip

#### Checking the contents of .rpz file
```
./reprounzip showfiles adb.rpz

./reprounzip info adb.rpz
```


#### Unzip the .rpz file and replicate the experiment

To unzip and run the experiment traced by reprozip, firstly unzip the .rpz file by and create a directory called unzipped_adb and place it inside that by
```
 ./reprounzip directory setup adb.rpz ~/unzipped_adb 
```

and then to reproduce the experiment traced by reprozip, run
```
 ./reprounzip directory run ~/unzipped_adb 
```

## Misc.

There was a problem while using reprounzip which is resolved, but should be documented in case such problem happend again.

The run_test.sh file originally had a shebang ```#!/bin/sh```, and this made the system to run its native Java installed, rather than taking the binary Java file that is produced by reprounzip. Removing it alleviated the problem, but if similar problems occur, one should check if there were any files that enforced an environment.



