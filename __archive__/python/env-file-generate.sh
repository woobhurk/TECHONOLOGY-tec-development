#!/bin/env bash

BASE_DIR="$(dirname "$0")"

ENV_STR=""
while read -r DIR; do
    [[ -z "$ENV_STR" ]] && ENV_STR="$DIR" || ENV_STR="$ENV_STR;$DIR"
done < <(find "$BASE_DIR" -iname "*" -type d)

echo "$ENV_STR" > "$BASE_DIR/.env"
