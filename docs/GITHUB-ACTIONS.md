## 📋 Overview of GitHub Actions Workflows

| Workflow Name                        | Purpose                                                                                                       | Trigger |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|---------|
| `ci-draft.yml`                        | Builds the project, updates OpenAPI spec with draft version, and publishes artefact for pull requests. (*)    | `pull_request` |
| `ci-released.yml`                     | On release, updates OpenAPI spec with release version, builds, and publishes artefact to GitHub Packages. (*) | `release` |
| `code-analysis.yml`                   | Run PMD CLI for static code analysis and upload an HTML report if violations are found.                       | `pull_request`, `push` |
| `codeql.yml`                          | Run GitHub CodeQL to analyse Java code for security and quality issues                                        | `pull_request`, `schedule` |
| `lint-openapi.yml`                    | Lints and validates OpenAPI documents and JSON schema example responses on pull requests.                     | `pull_request` |
| `publish-released-openapi-spec.yml`   | Pushes the released OpenAPI spec to APIHub, validates it, and sets it as the default version.                 | `release` |
| `push-draft-openapi-spec.yml`         | Pushes the draft OpenAPI spec to APIHub in an unpublished state.                                              | `push` (to `main` or `master`) |

* Generates a CycloneDX Software Bill of Materials (SBOM) and uploads it as a build artefact. SBOM is also embedded in the JAR for draft and release builds to support supply chain integrity. 
* See [CHAIN_OF_CUSTODY.md](./CHAIN_OF_CUSTODY.md) for details on how provenance, SBOMs, along with static analysis tools, enhances confidence in software supply chain security.

### Dependabot PRs

[Dependabot](https://docs.github.com/en/code-security/supply-chain-security/keeping-your-dependencies-updated-automatically) is configured to automatically merge dependency update PRs after all required checks pass. This ensures dependencies remain up to date with minimal manual intervention while maintaining code security and stability.

## OpenAPI Specification and Data Schema Validation

This repository includes a GitHub Action to validate OpenAPI specifications and data payload JSON Schemas.

### Run Validation Locally

> Node must be installed on your machine.

Install dependencies:
```bash
npm install -g @stoplight/spectral-cli ajv-cli jsonlint
```

Validate the OpenAPI specification:
```bash
spectral lint "openapi/**/*.{yml,yaml}"
```
Documentation: https://stoplight.io/open-source/spectral

Validate the data payload JSON Schemas:
```bash
jsonlint -q ./openapi/path/to/example_payload.json
```

Validate the data payload JSON Schemas:
```bash
ajv --spec=draft2020 --strict=false -s "./openapi/path/to/schema.json" -d "./openapi/path/to/example_payload.json"
```
Documentation: https://ajv.js.org/
