This project uses an automated GitHub Actions workflow to manage versioning and publishing of the OpenAPI specification to SwaggerHub.

### 📌 Version Types
This project follows [Semantic Versioning](https://semver.org) (SemVer) conventions.

We support two types of versions:

### 🔧 Draft Versions (on every merge to main or master)
* Triggered by: push to main or master
* Version format: vX.Y.Z-&lt;short-sha&gt;, e.g. v0.2.1-a1b2c3d
    * If no release tag exists, defaults to v0.0.0-&lt;short-sha&gt;
* Purpose: Used for previewing or collaborating on in-progress API definitions.
* Behaviour:
    * The OpenAPI info.version is set automatically.
    * The spec is uploaded to SwaggerHub as:
        * visibility: public
        * published: false

### 🚀 Release Versions (on GitHub release publish)

* Triggered by: Publishing a GitHub release via the UI
* Git tag format: vX.Y.Z (e.g. v1.0.0)
* Version used: X.Y.Z (without the v)
* Purpose: Final, published version of the API for public consumption.
* Behaviour:
    * The OpenAPI info.version is set to the release tag version.
    * The spec is uploaded to SwaggerHub as:
        * visibility: public
        * published: true

### 📁 OpenAPI File Location

The OpenAPI spec file must be located in: `src/main/resources/openapi/*.openapi.yml`

Only the first file matching that pattern will be used.

> ℹ️ Note: The `info.version` field in the OpenAPI file is static and serves as a placeholder.
> The correct version is automatically applied during the GitHub Actions workflow based on the build context (e.g. short SHA for drafts, tag for releases).
