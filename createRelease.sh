#! /bin/bash

if [ -n "$1" ]; then 

	echo "Preparing Release" $1.

else

	echo "Run with Argument Release Version "
	exit

fi

versionName=BlueMS_$1



if [ ! -f ../release.keystore ]; then
	./CreateSignerKey.sh
fi

export PATH=~/AppData/Local/Android/Sdk/build-tools/29.0.3/:/c/Program\ Files/Java/jre1.8.0_141/bin:$PATH

export JAVA_HOME=/c/Program\ Files/Android/Android\ Studio/jre
export JRE_HOME=/c/Program\ Files/Android/Android\ Studio/jre


mkdir -p ../$versionName

echo "---------------"
echo "| Zip Project |"
echo "---------------"
/c/Program\ Files/7-Zip/7z.exe a -t7z ../$versionName/src_$versionName.7z . -mx0  -xr!.git -xr!build -xr!.gradle -xr!release

echo "-----------------"
echo "| Clean Project |"
echo "-----------------"
./gradlew clean

echo "-------------------"
echo "| Compile Project |"
echo "-------------------"
./gradlew assembleRelease

echo "-------------------"
echo "| sig/unsigned APK |"
echo "-------------------"
cp BlueMS/build/outputs/apk/release/BlueMS-release-unsigned.apk ../$versionName/$versionName-unsigned.apk

zipalign -v -p 4 ../$versionName/$versionName-unsigned.apk ../$versionName/$versionName-unsigned-aligned.apk

apksigner.bat sign --v1-signing-enabled  --v2-signing-enabled   --ks ../release.keystore --ks-key-alias MyReleaseKey --ks-pass pass:MyPassword --out ../$versionName/$versionName-release.apk --in ../$versionName/$versionName-unsigned-aligned.apk
apksigner.bat verify ../$versionName/$versionName-release.apk
