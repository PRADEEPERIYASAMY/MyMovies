# Contributing to MyMovies

Thank you for taking the time to contribute! This document walks through the process for reporting issues, proposing changes, and submitting pull requests.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [How to Report a Bug](#how-to-report-a-bug)
4. [How to Request a Feature](#how-to-request-a-feature)
5. [Development Workflow](#development-workflow)
6. [Pull Request Guidelines](#pull-request-guidelines)
7. [Coding Style](#coding-style)
8. [Commit Message Format](#commit-message-format)

---

## Code of Conduct

By participating in this project you agree to abide by the [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before contributing.

---

## Getting Started

1. **Fork** the repository and clone your fork locally.
2. Open the project in **Android Studio** (Electric Eel or newer recommended).
3. Let Gradle sync fully before making any changes.
4. Create a dedicated branch for your work:

```bash
git checkout -b feat/your-feature-name
# or
git checkout -b fix/your-bug-description
```

---

## How to Report a Bug

Before opening a new issue, please search the [existing issues](https://github.com/PRADEEPERIYASAMY/MyMovies/issues) to avoid duplicates.

When filing a bug report, include:

- **Android version and device model** (or emulator API level)
- **Steps to reproduce** the issue
- **Expected behavior** vs. **actual behavior**
- **Logcat output** or a stack trace if available
- A minimal code snippet or screen recording if relevant

---

## How to Request a Feature

Open a [GitHub Issue](https://github.com/PRADEEPERIYASAMY/MyMovies/issues/new) with the label `enhancement`. Describe:

- The problem you are trying to solve
- Your proposed solution
- Any alternatives you have considered

Discussing the scope in an issue *before* writing code saves everyone time.

---

## Development Workflow

This project follows an architecture outlined in [README.md](README.md). Before adding new screens or repository methods, familiarize yourself with:

- The cache-as-source-of-truth pattern in `MovieRepository`
- The `Result<T>` sealed wrapper used across repository/ViewModel boundaries
- Hilt module structure (`AppModule`, `ApiModule`, `CacheModule`)

**Running the app locally:**

```bash
./gradlew installDebug
```

**Running any available checks:**

```bash
./gradlew check
```

---

## Pull Request Guidelines

- Keep pull requests focused on a single concern. Large, sweeping PRs are hard to review.
- Reference the related issue number in the PR description (e.g. `Closes #42`).
- Make sure the project builds cleanly before submitting:

```bash
./gradlew build
```

- Write or update inline documentation when behavior changes.
- Be responsive to review feedback. PRs with no activity for 30 days may be closed.

---

## Coding Style

- Follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use `viewBinding` (already enabled at the module level) instead of `findViewById`.
- Prefer `suspend` functions over callbacks for asynchronous work.
- Keep Fragment/Activity classes thin. Business logic belongs in the ViewModel or Repository layer.

---

## Commit Message Format

Use short, imperative present-tense subject lines:

```
feat: add subtitle language filter to InfoFragment
fix: prevent crash when torrent magnet URI is null
refactor: consolidate Picasso usages into Glide
docs: update README with Room fallback explanation
```

---

Made with love by [Pradeep Periyasamy](https://github.com/PRADEEPERIYASAMY) ❤️
