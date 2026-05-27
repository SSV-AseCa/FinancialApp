#!/bin/sh

echo "Configuring git hooks..."

chmod +x .githooks/pre-push
chmod +x .githooks/pre-commit
git config core.hooksPath api/.githooks

echo "Hooks configured successfully."