mkdir -p output

FILES="data/*.txt"

for FILE in $FILES;
do
    # echo $FILE
    echo "Running input $FILE"
    removedSlash="${FILE##*/}"
    ../../../usr/bin/java -jar RepCRec.jar $FILE
    ../../../usr/bin/java -jar RepCRec.jar $FILE > output/outfile_${removedSlash}
done
