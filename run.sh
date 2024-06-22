#!/usr/bin/env bash

set -eo

TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "Build"
./gradlew clean build

echo "Run"
java -jar ./sportcalendars-web-scraper/build/libs/sportcalendars-web-scraper-all.jar -pd=./ -gr

echo "Commit"
git config --global user.name 'github-actions[bot]'
git config --global user.email 'github-actions[bot]@users.noreply.github.com'
git add calendars
git commit -m "[AUTOMATION] ${TIMESTAMP} execution"
git tag "${TIMESTAMP}"
git push origin master --follow-tags

echo "Done!"
