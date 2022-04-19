#!/usr/bin/env bash

set -euo pipefail

if ! docker buildx inspect builder > /dev/null 2>&1; then
  docker buildx create --use --name builder
  docker buildx install
fi
