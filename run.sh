#!/usr/bin/env bash

set -eo

export TZ=Europe/Madrid
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "Build"
./gradlew clean build

echo "Run"
java -jar ./sportcalendars-web-scraper/build/libs/sportcalendars-web-scraper-all.jar -pd=./ -gr

UPDATED_CALENDARS=0
for calendar in $(find calendars -name *.ics); do
  if ! git diff --unified=0 -- ${calendar}| grep -e "^[+-][^-+]" | grep -ve "^.DTSTAMP\|^.UID"; then
    echo "No changes in ${calendar}."
  else
    echo "Adding calendar with changes: ${calendar}"
    git add ${calendar}
    UPDATED_CALENDARS=$(expr ${UPDATED_CALENDARS} + 1)
  fi
done

if [ ${UPDATED_CALENDARS} -gt 0 ]; then
  echo "${UPDATED_CALENDARS} calendars updated!"
  echo "Configure git"
  git config --global user.name 'github-actions[bot]'
  git config --global user.email 'github-actions[bot]@users.noreply.github.com'

  echo "Commit"
  git add README.md || true
  git commit -m "[AUTOMATION] ${TIMESTAMP} execution"
  git push origin master
  git tag "${TIMESTAMP}"
  git push origin --tags
fi

echo "Done!"
