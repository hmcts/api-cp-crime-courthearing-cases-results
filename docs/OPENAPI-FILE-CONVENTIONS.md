# OpenAPI Specification: File Conventions

## 💡 **One Spec per Repo:**
Each repository is intended to contain a *single* OpenAPI specification. This aligns with the build workflows and repository template design.

## 🔤 Specification File Naming

- The OpenAPI file must end with `.openapi.yml`
- Place it in the following directory:
  ```
  /src/main/resources/openapi/
  ```

## 🏷️ Specification Version

- Set the version in the OpenAPI file to:
  ```
  0.0.0
  ```
- The actual version is injected automatically during the build process.
- For details, see: [OPENAPI-SPEC-VERSIONING](../../../../docs/OPENAPI-SPEC-VERSIONING.md)

## 🧩 Components vs. Schemas

- Define your schemas under `components` in the OpenAPI spec.
- Do **not** reference external JSON Schema files using `$ref` — even if technically supported, they will not render correctly in Swagger UI.
- This means payload definitions must be included directly in the OpenAPI spec, which can affect maintainability.