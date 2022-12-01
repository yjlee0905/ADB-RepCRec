mkdir -p output

for FILE in data/*.txt;

do
    echo "Running input $FILE"
    ../../../usr/bin/java -jar ADB-RepCRec.jar $FILE
    ../../../usr/bin/java -jar ADB-RepCRec.jar $FILE  > output/outfile_${FILE/\//}
done
