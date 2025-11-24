# Court Case Result Subscription API – Functional Requirements (Draft)

Consumer: Initial consumer Remand and Sentence Service (RaSS)
Producer: HMCTS Common Platform
Version: Draft 0.1
Status: For discussion

## Purpose of the API

The Prisons Case Result Event API replaces the current email-based process for sending custodial warrants and sentencing documents from HMCTS to Prisons.
This manual process contributes to approximately 100 releases in error per year.

The API will:
* Notify Prisons when a custody-relevant event occurs (e.g. result created or updated).
* Provide structured metadata and a reference to retrieve associated documents (PDF warrants).
* Enable Prisons (RaSS) to fetch documents programmatically to drive automated workflow actions.

Email delivery will run in parallel during the early adoption phase to eliminate operational risk.

## Functional Requirements

### Event-Based Notification Model

HMCTS must publish case result events whenever:
* A new custodial outcome occurs.
* An amended custodial result is recorded (treated identically to “create”).

## Subscription

### Subscription Registration

`POST /cases/results/subscriptions`

```json
{
    "ClientSubscriptionId": "string",
    "Events": ["ResultEventType", "..."],
    "NotificationEndpoint": {
      "WebhookUrl": "https://consumer.gov.uk/hooks/case-events",
      "Auth": "string"
    }
}
```

Response:
```json
{
  "subscriptionId": "{UUID}"
}
```

(TBC) Event Payload Must Include:
* Case ID
* Defendant ID
* PNC ID (if present)
* Event timestamp
* Custody relevance flag
* Metadata describing the result/warrant

These events will ultimately replace the existing email “action point”.

### Webhook Delivery Requirements

* Consumer must provide HTTPS POST endpoint.
* API Marketplace will sign webhook deliveries (HMAC header).
* Consumer must return 2xx to acknowledge.
* Retries managed by Worker with exponential backoff - 3 tries over 15 minutes.
* Failures routed to DLQ - DLQs will be kept per subscription for 28 days, then they will be purged.

### Subscription Retrieval

Retrieve all subscriptions for the consumer

`GET /cases/results/subscriptions`

Response:
```json
{
    "subscriptions": [
        {
          "THE_SUBSCRIPTION_ID": ["ResultEventType", "..."]
        },
        {
          "ANOTHER_SUBSCRIPTION_ID": ["ResultEventType", "..."]
        }
    ]
}
```

Retrieve a specific subscription

`GET /cases/results/subscriptions/{subscriptionId}`

Response:
```json
    {
      "subscriptions": [
        {
          "THE_SUBSCRIPTION_ID": ["ResultEventType", "..."]
        }
      ]
    }
```

#### Sequence Diagrams: Subscription Registration and Retrieval

```mermaid
sequenceDiagram
    
    participant Consumer as Remand and Sentencing Service (HMPPS)
    participant APIM as API Management (Gateway)
    participant CCRS as Crime Court Hearing Cases<br/>Results Subscription
    

    Note over Consumer,CCRS: Subscription Registration

    Consumer->>APIM: POST /cases/results/subscriptions<br/>{ClientSubscriptionId, Events[], NotificationEndpoint}
    APIM->>CCRS: Validate subscription request
    CCRS->>CCRS: Create subscription record (subscriptionId)
    CCRS-->>Consumer: 201 Created<br/>{subscriptionId}

    Note over Consumer,CCRS: Retrieve Subscription

    Consumer->>CCRS: GET /cases/results/subscriptions/{subscriptionId}
    APIM->>CCRS: 
    CCRS-->>Consumer: 200 OK<br/>{subscription details}

```

## Subscription Event Delivery & Document Retrieval

```mermaid
sequenceDiagram
    autonumber

    participant Webhook as RaSS (HMPPS)<br/>Webhook Endpoint
    participant APIM as API Management (Gateway)
    
    participant CCRS as Crime Courthearing Cases<br/>Results Subscription<br/>(Worker)
    participant AB as Azure Service Bus

    participant FILTER as Courthearing Cases Result<br/>Event Subscription Filter
    participant ART as Artemis

    FILTER-->>ART: Listening: Result Events
    FILTER->>FILTER: Filter messages to only include those<br/>relevant to the consumer’s interests or subscriptions.
    FILTER->>AB: Publish messages to queue
    CCRS-->>AB: Listening: Pop Results messages
    
    Note over CCRS,Webhook: Producer publishes result events
    CCRS->>APIM: Case Result Event
    
    APIM-->>Webhook: Consumer: Deliver result event<br/>(asynchronously)
```


### Document Retrieval Process

After receiving an event, the consumer (RaSS) will:
1.	Receive webhook POST event.
2.	MVP behaviour:
   * Request the event details and a “document link pointer” via: `GET /cases/{case_id}/results/{result_event_type}`
   * **NOTE:** the underlying service must only allow retrieval of subscription-relevant events.
3.	Follow the URL to obtain a signed URL for the PDF warrant document.

```mermaid

sequenceDiagram
    autonumber

    participant Consumer as RaSS (HMPPS)
    participant Webhook as RaSS (HMPPS)<br/>Webhook Endpoint
    participant APIM as API Management (Gateway)    
    participant CCRS as Crime Courthearing Cases<br/>Results Subscription<br/>(Worker)
    participant DocStore as Azure Storage<br/>Document Store

    Note over APIM,Webhook: Producer publishes result events
    CCRS->>APIM: Case Result Event
    
    APIM-->>Webhook: Consumer: Deliver result event<br/>(asynchronously)

    Note over Consumer,APIM: Result Event Data Retrieval

    Consumer->>APIM: GET /cases/{caseId}/results/{eventType}
    APIM-->>Consumer: 200 OK<br/>{metadata + link to signed document URL}

    Note over Consumer: Document Retrieval via signed URL

    Consumer->>DocStore: GET {signedDocumentUrl}
    DocStore-->>Consumer: 200 OK<br/>PDF Document Stream
    
    Note over Consumer: Consumer stores PDF locally<br/>(S3 or equivalent)
```

Future enhancements will expand JSON payload richness so prisons rely less on PDFs.

**Important Note:**
The PDF remains the operational currency today.
Until the operational process changes, this must remain part of the producer–consumer relationship.

#### Document Retrieval Requirements

* Documents must not be embedded in any JSON event payload.
* API returns metadata and a signed URL for the PDF.
* HMCTS document storage remains the source of truth.
* Prisons will store a local copy (AWS S3) to support their workflow automation.

**Benefits**

* Digital transfer supports prisoner movement between establishments.
* Reduces the amount of repeated document requests to HMCTS.
* Provides traceability and reduces “missing document” incidents.

### Reliability & Failure Handling

The existing email process offers no delivery guarantee.

The API must support:

Delivery Guarantees
* Retry with exponential backoff
* Dead-letter queue (DLQ) for undeliverable events
* Ability for consumers to inspect DLQs
* Ability for consumers to replay DLQs once systems recover

#### DLQ Inspection

`GET /cases/results/subscriptions/{subscriptionId}/events`

#### DLQ Replay

`POST /cases/results/subscriptions/{subscriptionId}/events/replay`

This strictly aligns replay with the subscription that owns the events.

```mermaid

sequenceDiagram
    autonumber

    participant Consumer as RaSS (HMPPS)
    participant APIM as API Management (Gateway)
    participant Worker as Crime Courthearing Cases<br/>Results Subscription<br/>(Worker)

    participant DLQ as Dead-Letter Queue
    participant AB as Azure Service Bus

    Note over APIM,Worker: Event Published

    Worker->>APIM: POST Case Result Event
    APIM->>Consumer: Deliver webhook event

    alt Consumer Unavailable or Delivery Failure
        Consumer--x APIM: Delivery fails
        Worker->>Worker: Retry with exponential backoff
        Worker--x APIM: Retry attempt<br/>(still failing)

        Note over Worker,DLQ: Event moved to DLQ after final retry

        Worker->>DLQ: Move undeliverable event
    else Delivery Successful
        Consumer-->>APIM: 200 OK<br/>(event accepted)
    end

    Note over Consumer,DLQ: DLQ Inspection

    Consumer->>APIM: GET /cases/results/subscriptions/{subscriptionId}/events
    APIM->>DLQ: Retrieve DLQ events
    DLQ-->>APIM: Return stored failed events
    APIM-->>Consumer: 200 OK<br/>{events[]}

    Note over Consumer,DLQ: DLQ Replay

    Consumer->>APIM: POST /cases/results/subscriptions/{subscriptionId}/events/replay

    APIM->>+Worker: Request replay of events
    Worker-->>AB: Add DLQ events to Queue for replay
    Worker-->>-APIM: HTTP Accepted 202
    APIM-->>Consumer: HTTP Accepted 202

    Note over APIM,Worker: (Replayed) Event Published
    Worker->>APIM: POST Case Result Event
    APIM->>Consumer: Deliver webhook event
```

## Event Filtering Requirements

RaSS must only receive events relevant to custodial processing.

Event Types the API Should Support:
* Custodial outcomes
* Bail from custody
* Events that change a prisoner’s legal status

Events RaSS does NOT want:
* Full case data
* All court events
* Civil or non-custodial results

The API must surface only custody-impacting result events.

## Requirements for Updates / Amendments

API Requirements
* Every amendment must generate a new event
* Updates treated the same as creates (idempotent notification model)
* The API must always allow retrieval of the latest document version

## Security Requirements

The new system must:
* Use secure authentication (OAuth2 preferred long-term)
* Provide time-limited signed URLs for documents
* Enforce strong audit trails and access controls
* Never embed PDFs directly in event payloads
* Ensure privacy and integrity of custody-related data

## Additional Future Considerations

* Discoverable list of event types: GET /cases/results/event-types
* Potential expansion to other justice partners (DWP, Probation)
* Multi-consumer patterns enabled via API Marketplace

## Next Steps

* Align subscription model with API Marketplace standards
* Define authentication model for all consumer but initially for RaSS (temporary → long-term OAuth2)
* Produce OpenAPI v1.0 draft
  * Including event schema for MVP
