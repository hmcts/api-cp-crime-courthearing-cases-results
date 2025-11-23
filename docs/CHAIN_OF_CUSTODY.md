# 🔐 Chain of Custody: Build Provenance & Static Analysis

This project has been enhanced with several build-time verifications and provenance artefacts to ensure supply chain integrity and traceability. These measures aim to provide a reliable **chain of custody** from source to published artefact.

### 1. **Static Code Analysis with PMD (via CLI)**
- PMD runs on every pull request to `main` or `master`.
- Violations are captured in both **HTML** and **XML** reports.
- Builds fail automatically if PMD detects violations (`exit code 4`) or execution errors (`exit code 1` or `2`).
- HTML report is conditionally uploaded only when violations exist.

### 2. **Software Bill of Materials (SBOM)**
- Generated using the [`cyclonedx-gradle-plugin`](https://github.com/CycloneDX/cyclonedx-gradle-plugin).
- Format: JSON (`bom.json`)
- Location: `META-INF/sbom` inside the built JAR and/or BootJar.
- Provides full dependency inventory to support audit, compliance, and vulnerability scans.

### 3. **CodeQL Static Analysis**
- CodeQL runs on pull requests and scheduled builds.
- Targets Java code using the `security-extended` query pack.
- Analyses for vulnerabilities and known patterns of misuse.
- SBOM hash is logged, and the SBOM artefact is uploaded with the scan results to enhance traceability of scanned components.

### 4. **Dependabot for Dependency Tracking**
- Dependabot monitors GitHub Actions and Gradle dependencies.
- Automatically creates pull requests for available security updates.
- Grouped update strategy is applied to avoid pull request spam.
