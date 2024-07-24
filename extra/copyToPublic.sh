#!/bin/bash

#
# Copyright (c) 2022(-0001) STMicroelectronics.
# All rights reserved.
# This software is licensed under terms that can be found in the LICENSE file in
# the root directory of this software component.
# If no LICENSE file comes with this software, it is provided AS-IS.
#

# Verify bash version. macOS comes with bash 3 preinstalled.
if [[ ${BASH_VERSINFO[0]} -lt 4 ]]; then
  echo "You need at least bash 4 to run this script."
  exit 1
fi

# Exit when any command fails
set -e

# Create public app folder
mkdir -p ../../Android_App_STBLESensors_Pub/

# Copy root files
cp ./filesPublic/* ../../Android_App_STBLESensors_Pub/

cp ../.gitignore ../../Android_App_STBLESensors_Pub/
cp ../settings.gradle.kts ../../Android_App_STBLESensors_Pub/
cp ../build.gradle.kts ../../Android_App_STBLESensors_Pub/
cp ../detekt-config-compose.yml ../../Android_App_STBLESensors_Pub/
cp ../License.md ../../Android_App_STBLESensors_Pub/
cp ../README.md ../../Android_App_STBLESensors_Pub/

## declare an array variable
declare -a arr=("gradle")

# Read list of module from public settings.gradle
input="../settings.gradle.kts"
VAR1="include"
VAR2=0
while IFS= read -r line; do
  if [ "$VAR1" = "${line:0:7}" ]; then
    VAR2=$((${#line} - 12))
    arr+=("${line:10:VAR2}")
  fi
done <"$input"

re='^[0-9]+$'

lineNum=""
for i in "${arr[@]}"; do
  # Copy module and remove build, gradle folder and st_dependencies.gradle file
  cp -r ../"$i" ../../Android_App_STBLESensors_Pub/
  rm -f ../../Android_App_STBLESensors_Pub/"$i"/st_dependencies.gradle
  rm -f ../../Android_App_STBLESensors_Pub/"$i"/publish.gradle
  rm -fr ../../Android_App_STBLESensors_Pub/"$i"/build
  rm -fr ../../Android_App_STBLESensors_Pub/"$i"/.gradle
  rm -fr ../../Android_App_STBLESensors_Pub/"$i"/.idea
  # Strip publish plugin
  if [ -f ../../Android_App_STBLESensors_Pub/"$i"/build.gradle.kts ]; then
    lineNum=$(awk "/publish.gradle/{ print NR ; exit }" ../../Android_App_STBLESensors_Pub/"$i"/build.gradle.kts)
    if [[ $lineNum =~ $re ]] && [ $lineNum -gt 0 ]; then
      sed -i "$lineNum"d "../../Android_App_STBLESensors_Pub/$i/build.gradle.kts"
    fi
  fi
  # Strip loco plugin
  if [ -f ../../Android_App_STBLESensors_Pub/"$i"/build.gradle.kts ]; then
    lineNum=$(awk "/Loco/{ print NR ; exit }" ../../Android_App_STBLESensors_Pub/"$i"/build.gradle.kts)
    if [[ $lineNum =~ $re ]] && [ $lineNum -gt 0 ]; then
      sed -i "$lineNum",$(($lineNum + 7))d "../../Android_App_STBLESensors_Pub/$i/build.gradle.kts"
      lineNum=$(awk "/appswithlove/{ print NR ; exit }" ../../Android_App_STBLESensors_Pub/"$i"/build.gradle.kts)
      sed -i "$lineNum"d "../../Android_App_STBLESensors_Pub/$i/build.gradle.kts"
      lineNum=$(awk "/st_dependencies/{ print NR ; exit }" ../../Android_App_STBLESensors_Pub/"$i"/build.gradle.kts)
      if [[ $lineNum =~ $re ]] &&  [ "$lineNum" -gt 0 ]; then
        sed -i "$lineNum"d "../../Android_App_STBLESensors_Pub/$i/build.gradle.kts"
      fi
    fi
  fi
  echo "$i folder copied"
done

for file in ../../Android_App_STBLESensors_Pub/st_login/src/main/res/raw/*.json; do
  echo "" > $file
done

echo "Public project folder created."
