# Security Policy

## Supported Versions

Only the latest version of MyMovies on the `main` branch receives security fixes. Older tags or branches are not actively patched.

| Version / Branch | Supported |
|---|---|
| `main` (latest) | Yes |
| Older tags | No |

---

## Reporting a Vulnerability

If you discover a security vulnerability, **please do not open a public GitHub Issue**. Public disclosure before a fix is available can put users at risk.

Instead, report it privately using one of the following methods:

1. **GitHub Private Security Advisory** (preferred): Navigate to the repository's **Security** tab and select **"Report a vulnerability"**.
2. **Direct contact**: Open a regular GitHub Issue marked `[SECURITY]` in the title with as little sensitive detail as possible, and the maintainer will follow up privately.

### What to Include in Your Report

- A clear description of the vulnerability and the potential impact
- Steps to reproduce the issue
- Any proof-of-concept code, if applicable
- The Android version(s) and device(s) affected

---

## Response Timeline

| Stage | Target Time |
|---|---|
| Acknowledgement of report | Within 3 business days |
| Initial assessment and severity triage | Within 7 business days |
| Fix or mitigation released | Dependent on severity and complexity |

You will be kept informed at each stage. Once a fix is published, you are welcome to disclose the vulnerability publicly, and credit will be given in the release notes if you choose.

---

## Scope

The following are **in scope** for security reports:

- Authentication or authorization issues (e.g. biometric bypass)
- Insecure data storage (e.g. sensitive data written in plaintext to external storage)
- Unsafe handling of torrent magnet URIs or subtitle URLs
- Network traffic interception or man-in-the-middle vulnerabilities
- Dependency vulnerabilities with a direct, exploitable impact on this app

The following are **out of scope**:

- Vulnerabilities in third-party services (movie catalog API, OpenSubtitles, Firebase) that are not caused by how this app integrates with them
- Social engineering attacks
- Denial-of-service attacks that require physical device access

---

Made with love by [Pradeep Periyasamy](https://github.com/PRADEEPERIYASAMY) ❤️
