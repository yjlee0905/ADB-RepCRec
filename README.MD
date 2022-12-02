
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

There are multiple ways to create a JAR file, depending
on the IDE and environment. The following is one way to create a JAR file for this project.
Download the repository and move to the project directory in your terminal.
We did not create the JAR using the below way, but used IntelliJ IDEA's feature, so be catious
of the version of ```javac``` for compiling.

``` 
cd src

// Below command will compile all *.java files to *.class
javac *.java

// here as long as you stay in the directory, Test.class could be used to run the program (must stay in this directory)
java Test ../data/test1.txt
```


Stay in the ``` src/ ``` directory for the following commands.

For convinience, the following command will recursively list all *.class file to a classes.list file that were compiled above using the ```javac *.java``` command.
```
find . -name '*.class' -print > classes.list
```

Create a jar file from all the .class files listed in classes.list file with an entry point of Test, since it is
the entry point of this project: (again we are staying in the src directory, but the point is that we are
executing the command where Test.class is located.)
```
jar cvfe RepCRec.jar Test @classes.list
```

Now you have a ``` RepCRec.jar``` file, and this could be executed wherever in the terminal as long as an input file is provided.

This assumes that the Java version on the machine is Java 19, for CIMS it would be differet to reproduce under the reprounzp directory. See below on how to run the JAR file on CIMS or on a reprounzipped-directory.

## Execution, assuming that Java verion is 19, for Reprounzip please see below commands.
```
java -jar RepCRec.jar /path/to/inputFile
```

## Re-direct the standard output to a file.
```
java -jar RepCRec.jar /path/to/inputFile > output_path
```

If you encounter an error that says that 


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
├── RepCRec.jar
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
reprozip pack adb-repcrec-final
```

Above command will create an adb-repcrec-final.rpz, and this will allow the ```./run_time.sh``` command to be reproduced as long as a system has reprounzip.

After transfering this file to a system with reprounzip, you can run the following commands. You might need to ```./reprounzip``` depending on if the system has reprounzip installed or in a binary format. The following assumes that you are running this on CIMS.

## Using Reprounzip

#### Checking the contents of .rpz file
```
./reprounzip showfiles adb-repcrec-final.rpz

./reprounzip info adb-repcrec-final.rpz
```

In case reprounzip gives an error, run 
```
export LC_ALL=C
```

#### Unzip the .rpz file and replicate the experiment

To unzip and run the experiment traced by reprozip, firstly unzip the .rpz file by and create a directory called unzipped_adb and place it inside that by
```
 ./reprounzip directory setup adb-repcrec-final.rpz ~/unzipped_adb 
```

and then to reproduce the experiment traced by reprozip, run
```
 ./reprounzip directory run ~/unzipped_adb 
```

If you want to the JAR program individually,
```
../../../usr/bin/java -jar RepCRec.jar path/to/input/file
 
../../../usr/bin/java -jar RepCRec.jar path/to/input/file > path/to/output/file

```

Or if you want to add additional test cases, add them inside the data directory and then run ```./run_test.sh```.
All the output files will be created under the ```output``` directory.

## Misc.

There was a problem while using reprounzip which is resolved, but should be documented in case such problem happend again.

I realized that when I plainly used ``` java -jar RepCRec.jar /path/to/inputFile ```, it would grab the java binary of the system, but not from the binary from the reprounzipped directory. Therefore I had to manually direct to use the java version by ```../../../usr/bin/java -jar RepCRec.jar path/to/input/file.```

