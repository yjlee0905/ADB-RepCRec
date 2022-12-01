mkdir -p output

for FILE in data/*.txt;

do
    echo "Running input $FILE"
    ../../../usr/bin/java -jar adb_repcrec.jar $FILE
    ../../../usr/bin/java -jar adb_repcrec.jar $FILE  > output/outfile_${FILE/\//}
done
