# Data Product Definition Template

## 1. Overview
- **Name**:  
  _e.g. "Active Case Listings", "Probation Orders Issued"_
- **Owning Domain / Department**:  
  _e.g. Listing Service Team, Probation Service_
- **Product Owner**:  
  _Name + contact – responsible for the data product’s upkeep_
- **Purpose / Business Context**:  
  _Short paragraph explaining why this data exists and how it can be used_

---

## 2. Data Interface
- **Interface Type**:  
  _e.g. REST API, SQL Table, S3/Blob Path, GraphQL, Pub/Sub topic_
- **Access Mechanism**:  
  _e.g. URL, connection string, auth method, IAM role, API key_
- **Schema / Contract Location**:  
  _Link to JSON Schema, OpenAPI spec, Parquet schema definition, etc._
- **Versioning Strategy**:  
  _How are changes handled – breaking vs non-breaking, deprecation policy_

---

## 3. Technical Metadata
- **Data Format**:  
  _e.g. JSON, CSV, Parquet, Avro_
- **Update Frequency**:  
  _e.g. real-time, daily batch, monthly snapshot_
- **Latency / Freshness Expectations**:  
  _e.g. available within 2 hours of event_
- **Retention Period**:  
  _e.g. 90 days, 1 year, indefinite_
- **Quality Expectations / SLAs**:  
  _Optional – % completeness, null rates, timeliness_

---

## 4. Documentation & Discovery
- **Data Dictionary / Field Descriptions**:  
  _Link or inline – describe each key field_
- **Known Caveats or Data Gaps**:  
  _Are there known issues or things consumers should be aware of?_
- **Consumer Onboarding Guide**:  
  _Steps for a new team to access and use the data_

---

## 5. Governance
- **Data Classification / Sensitivity**:  
  _e.g. Public, Internal, Official-Sensitive, etc._
- **Access Policy**:  
  _Who can request access, approval process if any_
- **Audit Logging (if any)**:  
  _Are accesses logged? How long are logs retained?_

---

## 6. Feedback & Issue Management
- **Support Channel**:  
  _e.g. Slack channel, email address, JIRA board_
- **Change Request Process**:  
  _How do consumers request new fields or changes?_