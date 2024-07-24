#!/bin/bash
#
# Copyright (c) 2022(-0001) STMicroelectronics.
# All rights reserved.
# This software is licensed under terms that can be found in the LICENSE file in
# the root directory of this software component.
# If no LICENSE file comes with this software, it is provided AS-IS.
#

# Verify jq is installed.
if ! which jq > /dev/null; then
    echo "jq is not installed. Please install it and try again. (brew install jq)"
    exit 1
fi

# Verify GitHub token is export as env variable.
if [ -z "$GPR_API_KEY" ]; then
    echo "GPR_API_KEY is empty. Please set with your GitHub token to use GitHub API."
  exit 1
fi

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
ANCHOR_DEMO="NEW_DEMO_ANCHOR"
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

# Add to private settings.gradle.kts the new module
echo "include(\":st_$DEMO_NAME_UNDERSCORE\"" >>../settings.gradle.kts

# Add to pul settings.gradle.kts the new module
echo "include(\":st_$DEMO_NAME_UNDERSCORE\"" >>../settings.gradle.kts

# Add to demo showcase
lineNum=$(awk "/$ANCHOR_DEMO/{ print NR ; exit }" ../st_demo_showcase/build.gradle.kts)
sed -i "$lineNum i implementation(project(\":st_$DEMO_NAME_UNDERSCORE\"))" ../st_demo_showcase/build.gradle.kts

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

# -------------------------------------------------------------------------------------------------
#                                  CREATE ISSUE ST VESPUCCI
# -------------------------------------------------------------------------------------------------

# Variables
TOKEN=$GPR_API_KEY
PROJECT_NUMBER=3
OWNER="PRG-SWP"
REPO="Android_App_STVespucci"
TITLE="New Demo Module created: $DEMO_NAME_UNDERSCORE"
BODY="Add the new demo module dependency to **Vespucci** \`libs.version.toml\`.\n\nAdd this line in \`[libraries]\` section:\n\`st-$DEMO_NAME_UNDERSCORE = { module = \\\"com.st.$DEMO_NAME_UNDERSCORE:st-$DEMO_NAME_UNDERSCORE\\\", version.ref = \\\"stLibs\\\" }\`\n\nAdd this line in the \`stLibs\` bundle in \`[bundles]\` section:\n\`\\\"st-$DEMO_NAME_UNDERSCORE\\\"\`"
LABELS="@high"
ASSIGNEE="AlbyST"

create_issue_response=$(curl -s -H "Authorization: token $TOKEN" -X POST -d "{ \"title\": \"$TITLE\", \"body\": \"$BODY\", \"labels\": [\"$LABELS\"], \"assignees\": [\"$ASSIGNEE\"] }" https://api.github.com/repos/$OWNER/"${REPO}"/issues)

# Get Issue Number / Issue Node Id
ISSUE_NUMBER=$(echo "$create_issue_response" | jq '.number')
ISSUE_NUMBER=${ISSUE_NUMBER//\"/}
ISSUE_NODE_ID=$(echo "$create_issue_response" | jq -r '.node_id')
ISSUE_NODE_ID=${ISSUE_NODE_ID//\"/}

echo "Issue number $ISSUE_NUMBER has been created."

## -------------------------------------------------------------------------------------------------

get_project_id_response=$(curl -s -H "Authorization: bearer $TOKEN" -X POST -d '{
"query": "query { organization(login: \"'$OWNER'\") { projectV2(number: '$PROJECT_NUMBER') { id title } } }"
}' https://api.github.com/graphql)

# Get Project Id / Project Name
PROJECT_ID=$(echo "$get_project_id_response" | jq '.data.organization.projectV2.id')
PROJECT_ID=${PROJECT_ID//\"/}
PROJECT_NAME=$(echo "$get_project_id_response" | jq '.data.organization.projectV2.title')
PROJECT_NAME=${PROJECT_NAME//\"/}

echo "Fetch $PROJECT_NAME project id succeeded."

## -------------------------------------------------------------------------------------------------

add_to_project_response=$(curl -s -H "Authorization: bearer $TOKEN" -X POST -d '{
  "query": "mutation { addProjectV2ItemById(input: { contentId: \"'"${ISSUE_NODE_ID}"'\", projectId: \"'"${PROJECT_ID}"'\" }) { item { id project { title } } } }"
}' https://api.github.com/graphql)

# Get Card ID
CARD_ID=$(echo "$add_to_project_response" | jq '.data.addProjectV2ItemById.item.id')
CARD_ID=${CARD_ID//\"/}

echo "Card has been created."

# -------------------------------------------------------------------------------------------------

#get_column_id_response=$(curl -s -H "Authorization: bearer $TOKEN" -X POST -d '{
#"query": "query { organization(login: \"'$OWNER'\") { projectV2(number: '$PROJECT_NUMBER') { fields(first: 100) { nodes { ... on ProjectV2SingleSelectField { id dataType name } ... on ProjectV2SingleSelectField { options { id name } } } } } } }"
#}' https://api.github.com/graphql)

STATUS_FIELD_ID=PVTSSF_lADOB0ylPc4AP4pnzgKJChg
STATUS_TODO_OPTION_ID=f75ad846

# -------------------------------------------------------------------------------------------------

move_card_response=$(curl -s -H "Authorization: bearer $TOKEN" -X POST -d '{
  "query": "mutation { updateProjectV2ItemFieldValue(input: { value: {singleSelectOptionId: \"'"${STATUS_TODO_OPTION_ID}"'\" } ,fieldId: \"'"${STATUS_FIELD_ID}"'\", itemId: \"'"${CARD_ID}"'\", projectId: \"'"${PROJECT_ID}"'\"}) { clientMutationId } }"
}' https://api.github.com/graphql)

echo "Card has been moved in TODO column."

# -------------------------------------------------------------------------------------------------