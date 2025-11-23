# HMCTS API Versioning Strategy

## Overview

HMCTS Common Platform follows an API versioning strategy based on GitHub's approach, where **media type versioning** is handled through the HTTP `Accept` header. This method allows us to evolve our APIs while maintaining backwards compatibility and keeping URLs clean.

We use **Semantic Versioning (SemVer)** to express the version number:

```
MAJOR.MINOR.PATCH
```

Learn more at [semver.org](https://semver.org/).

Versioning is embedded in the `Accept` header — **not** in the URL path.

---

## Recommended Usage

Clients should specify the **MAJOR version only** in the `Accept` header:

```bash
curl -H "Accept: application/vnd.hmcts.cp.v1+json" https://api.hmcts.service.gov.uk/cases
```

This tells the server:
- The client is requesting the latest compatible version within **v1**.
- The response should be in **JSON format**.
- The request is for a resource defined by the HMCTS Common Platform API.

### Server Response

```http
HTTP/1.1 200 OK
Content-Type: application/vnd.hmcts.cp.v1.4.2+json

{
  "case_id": "ABC123",
  "status": "submitted"
}
```

---

## Media Type Format

We use a vendor-specific media type with semantic versioning:

```
application/vnd.hmcts.cp.v<MAJOR>[.<MINOR>[.<PATCH>]]+json
```

- `application/vnd.hmcts.cp`: Indicates the HMCTS Common Platform API.
- `v<MAJOR>[.<MINOR>[.<PATCH>]]`: A [Semantic Version](https://semver.org) indicating the version requested.
- `+json`: The response format (currently JSON).

> Example: `application/vnd.hmcts.cp.v2.3.1+json`

---

## Versioning Behaviour

- If a client specifies only `v1`, the server returns the latest compatible `v1.x.x` version.
- All changes within a **MAJOR** version are guaranteed to be **non-breaking**.
- Minor and patch changes may introduce **new optional fields**, improvements, or bug fixes.

---

## Default Version

If the `Accept` header is missing or does not include a version, the API will respond with the **latest stable MAJOR version**.

```bash
# Without version
curl -H "Accept: application/json" https://api.hmcts.service.gov.uk/cases
```

> ⚠️ Clients relying on the default version may be affected by future MAJOR version changes.

---

## Advanced Versioning (Optional)

Advanced clients can choose to specify more precise versions:

| Accept Header                                | Behaviour                              |
|----------------------------------------------|----------------------------------------|
| `application/vnd.hmcts.cp.v1+json`           | Latest version within `v1.x.x`         |
| `application/vnd.hmcts.cp.v1.2+json`         | Latest patch version within `v1.2.x`   |
| `application/vnd.hmcts.cp.v1.2.3+json`       | Specific version `v1.2.3`              |

> We recommend using only the **MAJOR version** unless strict version control is required.

---

## Future Versions

As new versions are introduced:

- **PATCH** updates include backwards-compatible bug fixes.
- **MINOR** updates add backwards-compatible features.
- **MAJOR** updates introduce breaking changes.

Clients will only be affected by breaking changes if they upgrade to a new MAJOR version.

---

## Caching Notes

API responses include the following header:

```http
Vary: Accept
```

This ensures intermediaries (like CDNs or proxies) treat different `Accept` headers as distinct cache entries.

---

## Further Reading

- [GitHub API Media Types](https://docs.github.com/en/rest/overview/media-types)
- [Semantic Versioning](https://semver.org)
- [RFC 6838 – Media Type Naming](https://datatracker.ietf.org/doc/html/rfc6838)
- [RFC 7231 – HTTP Content Negotiation](https://datatracker.ietf.org/doc/html/rfc7231)

---

For questions or implementation help, please contact the HMCTS API Platform team.
