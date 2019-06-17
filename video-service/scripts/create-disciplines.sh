#!/usr/bin/env bash

set -e

if [ -z "$domain" ]
then
    echo "Must set domain"
    exit 1
fi

get_subject_link() {
curl -s "https://api.$domain.com/v1/subjects"  | jq -r "._embedded.subjects[] | select(.name == \"$1\") | ._links.self.href"
}

create_discipline() {
declare -a subjectLinks
subjectsUrl=$(curl -s -X POST  \
     -d "{\"code\":\"$1\", \"name\":\"$2\"}" \
     -H "Authorization: Bearer $(create-keycloak-token)" "https://api.$domain.com/v1/disciplines" \
     -H "Content-Type: application/json" \
  | jq -r ._links.subjects.href)

echo "Processing ${#subjects[@]} subjects"
i=0
for subjectName in "${subjects[@]}"
do
    subjectLinks[i]=$(get_subject_link "$subjectName")
    i=$(bc <<< "$i + 1")
done


echo "Adding subjects to $2"

printf -v delimitedSubjects "%s\n" "${subjectLinks[@]}"
delimitedSubjects=${delimitedSubjects%?}
echo "$delimitedSubjects"

curl -s -X PUT \
     -d "$delimitedSubjects" \
     -H "Authorization: Bearer $(create-keycloak-token)" ${subjectsUrl} \
     -H "Content-Type: text/uri-list"
}

subjects=('Art' 'Art History' 'Music' 'Performing Arts' 'Performing Arts (Technical Support)' 'Design and technology' 'Film Studies')
create_discipline arts Arts

subjects=('History' 'Reading and writing' 'Literature' 'Politics and International Relations' 'Philosophy and Religion' 'Economics and Business' 'Law' 'Sociology' 'Psychology' 'Tourism' 'Classics')
create_discipline humanities Humanities

subjects=('Science' 'Computing' 'Mathematics' 'Engineering' 'Biology' 'Physics' 'Chemistry' 'Geography and Earth Science' 'Science')
create_discipline stem STEM

subjects=('Social and emotional aspects of learning' 'Food and Health' 'Sports and Sport Science' 'Careers and Employability' 'Independent Living' 'Early Childhood' 'Horticulture')
create_discipline life-skills "Life Skills"

subjects=('Teacher Training' 'Social Care' 'Special Education Needs')
create_discipline pedagogy Pedagogy

subjects=('Spanish' 'French' 'Arabic' 'English as a Foreign Language' 'German' 'Mandarin' 'Other Languages')
create_discipline languages Languages