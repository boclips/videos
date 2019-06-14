#!/usr/bin/env bash

set -e

if [ -z "$domain" ]
then
    echo "Must set domain"
    exit 1
fi

get_subject_link() {
curl "https://api.$domain.com/v1/subjects"  | jq "._embedded.subjects[] | select(.name == \"$1\") | ._links.self.href"
}

create_discipline() {
subjects=$3
subjectsUrl=$(curl -X POST  \
     -d "{\"code\":\"$1\", \"name\":\"$2\"}" \
     -H "Authorization: Bearer $(create-keycloak-token)" "https://api.$domain.com/v1/disciplines" \
     -H "Content-Type: application/json" \
  | jq ._links.subjects.href)

for i in "${#subjects[@]}"
do
    subjects[i]=$(sed -e 's/^"//' -e 's/"$//' <<<$(get_subject_link subjects[i]))
done

subjectsUrl=$(sed -e 's/^"//' -e 's/"$//' <<<"$subjectsUrl")
curl -X PUT \
     -d "$(printf -v var "%s\n" "${subjects[@]}")" \
     -H "Authorization: Bearer $(create-keycloak-token)" ${subjectsUrl} \
     -H "Content-Type: text/uri-list"
}

subjects=('Art' 'Art History' 'Music' 'Performing Arts' 'Performing Arts (Technical Support)' 'Design and technology' 'Film Studies')
create_discipline arts Arts subjects

subjects=('History' 'Reading and writing' 'Literature' 'Politics and International Relations' 'Philosophy and Religion' 'Economics and business' 'Law' 'Sociology' 'Psychology' 'Tourism' 'Classics')
create_discipline humanities Humanities subjects

subjects=('Science' 'Computing' 'Mathematics' 'Engineering' 'Biology' 'Physics' 'Chemistry' 'Geography and Earth' 'Science')
create_discipline stem STEM subjects

subjects=('Social and emotional aspects of learning' 'Food and Health' 'Sports and Sport Science' 'Careers and employability' 'Independent living' 'Early Childhood' 'Horticulture')
create_discipline life-skills "Life Skills" subjects

subjects=('Teacher Training' 'Social Care' 'Special Education Needs')
create_discipline pedagogy Pedagogy subjects

subjects=('Spanish' 'French' 'Arabic' 'English as a foreign language' 'German' 'Mandarin' 'Other languages')
create_discipline languages Languages subjects