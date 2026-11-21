# Issue tracker: Local Markdown

Issues and specs for this repo live as Markdown files in `.scratch/`.

## Conventions

- One feature per directory: `.scratch/<feature-slug>/`
- The spec is `.scratch/<feature-slug>/spec.md`
- Implementation tickets are separate files under `.scratch/<feature-slug>/issues/`, numbered from `01`.
- Comments and conversation history append under a `## Comments` heading.

## When a skill says "publish to the issue tracker"

Create a new file under `.scratch/<feature-slug>/`.

## When a skill says "fetch the relevant ticket"

Read the referenced file path. The user will normally pass the path or issue number directly.
