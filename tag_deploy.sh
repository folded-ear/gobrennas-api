#!/usr/bin/env zsh
set -e

tag="deploy-$(date "+%Y%m%d_%H%M%S")"

reset=no
if ! git diff --quiet || ! git diff --quiet --cached; then
    reset=yes
    git commit -am "WIP before $tag"
fi

git tag $tag
git branch --force auto-deploy
git tag --list 'deploy-*' \
    | sort -r \
    | tail -n +10 \
    | xargs git tag -d

if [ $reset = "yes" ]; then
    git reset --soft HEAD^
fi
