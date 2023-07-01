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

# Read args from cmd line
DEMO_NAME=$1
VARIANT=$2
DISPLAY_NAME=$3

# Utility placeholder string
PLACEHOLDER_NAME="new_demo_template"
PLACEHOLDER_NAME_CAMEL="NewDemoTemplate"
ANCHOR_DEMO_1="NEW_DEMO_TEMPLATE ANCHOR1"
ANCHOR_DEMO_2="NEW_DEMO_TEMPLATE ANCHOR2"
ANCHOR_DEMO_3="NEW_DEMO_TEMPLATE ANCHOR3"
ANCHOR_DEMO_4="NEW_DEMO_TEMPLATE ANCHOR4"

# Check args format
if [ -z "$DEMO_NAME" ] || [[ $DEMO_NAME = *" "* ]]; then
  echo "Specify a demo name (no space, lowercase, with _ between words)"
  exit 1
fi

if [ -z "$VARIANT" ]; then
  echo "Specify a variant between: [xml compose]"
  exit 1
fi

if [ -z "$DISPLAY_NAME" ]; then
  echo "Specify the display name for this demo"
  exit 1
fi

# Check if module with same name already exist
if [ -d "../st_$DEMO_NAME" ]; then
  echo "Directory st_$DEMO_NAME already exists."
  exit 1
fi

# Set _ as delimiter
IFS='_'
# Read the split words into an array based on _ delimiter
read -ra strarr <<<"${DEMO_NAME,,}"

# Prettify demo name string CamelCaseNameExample and camel_case_name_example
DEMO_NAME_CAMEL=''
DEMO_NAME_UNDERSCORE=''
for i in "${strarr[@]}"; do
  DEMO_NAME_CAMEL+="${i^}"
  DEMO_NAME_UNDERSCORE+="${i}_"
done
DEMO_NAME_UNDERSCORE=${DEMO_NAME_UNDERSCORE::-1}

# Add to private settings.gradle the new module
echo "include ':st_$DEMO_NAME_UNDERSCORE'" >>../settings.gradle

# Add to demo showcase
DEP_NUM=$(sed -n '$=' ../st_demo_showcase/st_dependencies.gradle)
sed -i "$DEP_NUM i implementation project(path: ':st_$DEMO_NAME_UNDERSCORE')" ../st_demo_showcase/st_dependencies.gradle

# Create module folder
mkdir -p ../st_"$DEMO_NAME_UNDERSCORE"/
# Copy template to module folder
cp -r ./st_"$PLACEHOLDER_NAME"_"${VARIANT,,}"/* ../st_"$DEMO_NAME_UNDERSCORE"/

# Rename path and empty androidTest package
mv ../st_"$DEMO_NAME_UNDERSCORE"/src/androidTest/java/com/st/"$PLACEHOLDER_NAME" ../st_"$DEMO_NAME_UNDERSCORE"/src/androidTest/java/com/st/"$DEMO_NAME_UNDERSCORE"
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/src/androidTest/java/com/st/"$DEMO_NAME_UNDERSCORE"/ExampleInstrumentedTest.kt

# Rename path and empty unitTest package
mv ../st_"$DEMO_NAME_UNDERSCORE"/src/test/java/com/st/"$PLACEHOLDER_NAME" ../st_"$DEMO_NAME_UNDERSCORE"/src/test/java/com/st/"$DEMO_NAME_UNDERSCORE"
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/src/test/java/com/st/"$DEMO_NAME_UNDERSCORE"/ExampleUnitTest.kt

# Rename path and Fragment
mv ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$PLACEHOLDER_NAME" ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"
mv ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$PLACEHOLDER_NAME_CAMEL"Fragment.kt ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$DEMO_NAME_CAMEL"Fragment.kt
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$DEMO_NAME_CAMEL"Fragment.kt
sed -i "s/$PLACEHOLDER_NAME_CAMEL/$DEMO_NAME_CAMEL/g" ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$DEMO_NAME_CAMEL"Fragment.kt

# Rename ViewModel
mv ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$PLACEHOLDER_NAME_CAMEL"ViewModel.kt ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$DEMO_NAME_CAMEL"ViewModel.kt
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$DEMO_NAME_CAMEL"ViewModel.kt
sed -i "s/$PLACEHOLDER_NAME_CAMEL/$DEMO_NAME_CAMEL/g" ../st_"$DEMO_NAME_UNDERSCORE"/src/main/java/com/st/"$DEMO_NAME_UNDERSCORE"/"$DEMO_NAME_CAMEL"ViewModel.kt

if [ "$VARIANT" = "xml" ] || [ "$VARIANT" = "XML" ]; then
  # Rename layout
  mv ../st_"$DEMO_NAME_UNDERSCORE"/src/main/res/layout/"$PLACEHOLDER_NAME"_fragment.xml ../st_"$DEMO_NAME_UNDERSCORE"/src/main/res/layout/"$DEMO_NAME_UNDERSCORE"_fragment.xml
fi

mv ../st_"$DEMO_NAME_UNDERSCORE"/src/main/res/drawable/ic_demo.xml ../st_"$DEMO_NAME_UNDERSCORE"/src/main/res/drawable/"$DEMO_NAME_UNDERSCORE"_icon.xml

# Rename namespace package
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/build.gradle

# Rename publish lib detail
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/publish.gradle

# Rename loco tag
sed -i "s/$PLACEHOLDER_NAME/$DEMO_NAME_UNDERSCORE/g" ../st_"$DEMO_NAME_UNDERSCORE"/build.gradle

lineNum=$(awk "/$ANCHOR_DEMO_1/{ print NR ; exit }" ../st_demo_showcase/src/main/java/com/st/demo_showcase/models/Demo.kt)
text=",${DEMO_NAME_CAMEL}(displayName=\"${DISPLAY_NAME}\",icon=com.st.${DEMO_NAME_UNDERSCORE}.R.drawable.${DEMO_NAME_UNDERSCORE}_icon,features=listOf())"
sed -i "$lineNum i $text" ../st_demo_showcase/src/main/java/com/st/demo_showcase/models/Demo.kt

lineNum=$(awk "/$ANCHOR_DEMO_2/{ print NR ; exit }" ../st_demo_showcase/src/main/java/com/st/demo_showcase/models/Demo.kt)
text="${DEMO_NAME_CAMEL} -> DemoListFragmentDirections.actionDemoListTo${DEMO_NAME_CAMEL}Fragment(nodeId)"
sed -i "$lineNum i $text" ../st_demo_showcase/src/main/java/com/st/demo_showcase/models/Demo.kt

lineNum=$(awk "/$ANCHOR_DEMO_3/{ print NR ; exit }" ../st_demo_showcase/src/main/res/navigation/demo_showcase_nav_graph.xml)
text="<action android:id=\"@+id/action_demoList_to_${DEMO_NAME_CAMEL,}Fragment\" app:destination=\"@id/${DEMO_NAME_CAMEL,}Fragment\" />"
sed -i "$lineNum i $text" ../st_demo_showcase/src/main/res/navigation/demo_showcase_nav_graph.xml

lineNum=$(awk "/$ANCHOR_DEMO_4/{ print NR ; exit }" ../st_demo_showcase/src/main/res/navigation/demo_showcase_nav_graph.xml)
text="<fragment android:id=\"@+id/${DEMO_NAME_CAMEL,}Fragment\" android:name=\"com.st.${DEMO_NAME_UNDERSCORE}.${DEMO_NAME_CAMEL}Fragment\" android:label=\"${DEMO_NAME_CAMEL}Fragment\"> <argument android:name=\"nodeId\" app:argType=\"string\" /> </fragment>"
sed -i "$lineNum i $text" ../st_demo_showcase/src/main/res/navigation/demo_showcase_nav_graph.xml

echo "Module st_$DEMO_NAME_UNDERSCORE created. sync to load it"
