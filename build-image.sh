#!/bin/bash
mkdir -p archive

unstaged=$(git diff --name-only)
uncommitted=$(git diff --cached --name-only)
untracked=$(git ls-files -o --exclude-standard)
if [[ -n "$unstaged" || -n "$uncommitted" || -n "$untracked" ]]; then
  # there are untracked files
  echo $unstaged
  echo $uncommitted
  echo $untracked
  rev=$(git stash create | cut -c1-6)
else
  rev=$(git rev-parse HEAD | cut -c1-6)
fi
echo "Creating git archive for current changes of ${rev}"
git archive -o archive/$rev.tar $rev
find archive -type f -not -name "$rev.tar" -delete

docker build . -t ember-app --build-arg ARCHIVE=archive/$rev.tar
