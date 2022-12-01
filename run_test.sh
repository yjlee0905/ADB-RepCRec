mkdir -p output

FILES="data/*.txt"

for FILE in $FILES;
do
    # echo $FILE
    echo "Running input $FILE"
    removedSlash="${FILE##*/}"
    java -jar adb_repcrec.jar $FILE
    java -jar adb_repcrec.jar $FILE > output/outfile_${removedSlash}
done
