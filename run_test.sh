# !/usr/bin/env bash
mkdir -p outputDIR

for FILE in data/*.txt;

do
    echo "Running input $FILE"
    java -jar ADB-RepCRec.jar $FILE
    java -jar ADB-RepCRec.jar $FILE  > outputDIR/outfile_${FILE/\//}
done