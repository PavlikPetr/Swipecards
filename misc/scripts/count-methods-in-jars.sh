#!/bin/bash
pathToDir="$1"

if [ ! $pathToDir ]; then
pathToDir="../../libs/"
fi

jars=$( ls $pathToDir*jar )

if [ ${#jars[@]} -eq 0 ]; then
echo "No jars found"
exit
fi


for i in $jars; do
~/work/adt-bundle-linux-x86_64-20140321/sdk/build-tools/android-4.4.2/dx --dex --output=temp.dex $i
echo $i" "
cat temp.dex | head -c 92 | tail -c 4 | hexdump -e '1/4 "%d\n"'
done

rm -rf temp.dex
