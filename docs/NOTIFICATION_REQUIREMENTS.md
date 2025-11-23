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

Subscription Request (Illustrative)
```json
POST /cases/results/subscriptions
{
    "ClientSubscriptionId": "string",
    "Events": ["ResultEventType", "..."],
    "NotificationChannel": {
        "Role": "string",
        "Topic": "string"
    }
}
```

Response:
```json
{
  "subscriptionId": "{UUID}"
}
```

Event Payload Must Include:
* Case ID
* Defendant ID
* PNC ID (if present)
* Event timestamp
* Custody relevance flag
* Metadata describing the result/warrant

These events will ultimately replace the existing email “action point”.

### Subscription Retrieval

Retrieve all subscriptions

`GET /cases/results/subscriptions`

Response:
```json
{
    "subscriptions": [
        {
          "subscriptionId": ["ResultEventType", "..."]
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
          "subscriptionId": ["ResultEventType", "..."]
        }
      ]
    }
```

```mermaid
sequenceDiagram
    autonumber

    participant Consumer as RaSS (Prison Service)
    participant AMP as API Marketplace (AMP)
    participant CP as Common Platform
    participant Broker as Notification Channel (Topic/Queue)

    Note over Consumer,AMP: Subscription Registration

    Consumer->>AMP: POST /cases/results/subscriptions<br/>{ClientSubscriptionId, Events[], NotificationChannel}
    AMP->>AMP: Validate subscription request
    AMP->>AMP: Create subscription record (subscriptionId)
    AMP-->>Consumer: 201 Created<br/>{subscriptionId}

    Note over Consumer,AMP: Retrieve Subscription

    Consumer->>AMP: GET /cases/results/subscriptions/{subscriptionId}
    AMP-->>Consumer: 200 OK<br/>{subscription details}

    Note over CP,AMP: Producer publishes result events

    CP->>AMP: Case Result Event<br/>(custody-relevant only)
    AMP->>AMP: Apply subscription filters
    AMP->>Broker: Publish event to Topic/Queue<br/>{event metadata}

    Note over Broker,Consumer: Event Delivery

    Broker-->>Consumer: Deliver result event<br/>(asynchronously)

    Note over Consumer,Broker: Consumer Pull Model

    Consumer->>AMP: GET /cases/{caseId}/results/{eventType}
    AMP-->>Consumer: 200 OK<br/>{metadata + link to signed document URL}

    Note over Consumer: Optional Document Retrieval

    Consumer->>AMP: GET /cases/{caseId}/results/{eventType}/document
    AMP-->>Consumer: 200 OK<br/>{signed URL}

    Note over Consumer,AMP: End of Subscription Flow
```


### Consumer Pull Model

After receiving an event, the consumer (RaSS) will:
1.	Receive notification through the subscribed channel (topic).
2.	MVP behaviour:
* Request the event details and a “document link pointer” via:
`GET /cases/{case_id}/results/{result_event_type}`
3.	Follow the URL to obtain a signed URL for the PDF warrant document.

```mermaid
sequenceDiagram
    autonumber

    participant Consumer as RaSS (Prison Service)
    participant Broker as Notification Channel (Topic/Queue)
    participant AMP as API Marketplace
    participant CP as Common Platform (Producer)
    participant DocStore as HMCTS Document Store

    Note over CP,AMP: Custody-Relevant Event Produced
    CP->>AMP: Publish Result Event<br/>(custody-relevant)

    AMP->>Broker: Push event to subscribed topic/queue

    Note over Broker,Consumer: Event Delivery
    Broker-->>Consumer: Deliver result event notification<br/>(asynchronous)

    Note over Consumer: Consumer Pull Model Begins

    Consumer->>AMP: GET /cases/{caseId}/results/{eventType}<br/>(request event metadata)
    AMP-->>Consumer: 200 OK<br/>{result metadata + document pointer}

    Consumer->>AMP: GET /cases/{caseId}/results/{eventType}/document<br/>(request signed URL)
    AMP-->>Consumer: 200 OK<br/>{signedDocumentUrl}

    Note over Consumer,DocStore: Document Retrieval
    Consumer->>DocStore: GET {signedDocumentUrl}
    DocStore-->>Consumer: 200 OK<br/>PDF Warrant

    Note over Consumer: Consumer stores PDF locally<br/>(S3 or equivalent)
```

Future enhancements will expand JSON payload richness so prisons rely less on PDFs.

**Important Note:**
The PDF remains the operational currency today.
Until the operational process changes, this must remain part of the producer–consumer relationship.

### Document Retrieval Requirements

* Documents must not be embedded in any JSON event payload.
* API returns metadata and a signed URL for the PDF.
* HMCTS document storage remains the source of truth.
* Prisons will store a local copy (AWS S3) to support their workflow automation.

```mermaid
sequenceDiagram
    autonumber

    participant Consumer as RaSS (Prison Service)
    participant AMP as API Marketplace
    participant DocStore as HMCTS Document Store
    participant LocalStore as RaSS S3 Storage

    Note over Consumer,AMP: Consumer requests document metadata

    Consumer->>AMP: GET /cases/{caseId}/results/{eventType}
    AMP-->>Consumer: 200 OK<br/>{result metadata + document pointer}

    Note over Consumer,AMP: Request signed URL

    Consumer->>AMP: GET /documents/{documentId}/signed-url
    AMP-->>Consumer: 200 OK<br/>{signedDocumentUrl}

    Note over Consumer,DocStore: Retrieve actual PDF

    Consumer->>DocStore: GET {signedDocumentUrl}
    DocStore-->>Consumer: 200 OK<br/>PDF Document Stream

    Note over Consumer,LocalStore: Store locally for workflow automation

    Consumer->>LocalStore: PUT /warrants/{documentId}.pdf<br/>(store PDF)
    LocalStore-->>Consumer: 200 OK

    Note over Consumer: Consumer local system now has<br/>a durable copy for automations and movement workflows
```

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

### DLQ Inspection

`GET /cases/results/subscriptions/{subscriptionId}/events`

### DLQ Replay

`POST /cases/results/subscriptions/{subscriptionId}/events/replay`

This strictly aligns replay with the subscription that owns the events.

```mermaid
sequenceDiagram
    autonumber

    participant AMP as API Marketplace
    participant Broker as Notification Channel
    participant Consumer as RaSS (Prison Service)
    participant DLQ as Dead-Letter Queue

    Note over AMP,Broker: Event Published

    AMP->>Broker: Publish custody-relevant event
    Broker->>Consumer: Deliver event notification

    alt Consumer Unavailable or Delivery Failure
        Consumer--x Broker: Delivery fails
        Broker->>Broker: Retry with exponential backoff
        Broker--x Consumer: Retry attempt<br/>(still failing)

        Note over Broker,DLQ: Event moved to DLQ after final retry

        Broker->>DLQ: Move undeliverable event
    else Delivery Successful
        Consumer-->>Broker: 200 OK<br/>(event accepted)
    end

    Note over Consumer,DLQ: DLQ Inspection

    Consumer->>AMP: GET /cases/results/subscriptions/{subscriptionId}/events
    AMP->>DLQ: Retrieve DLQ events
    DLQ-->>AMP: Return stored failed events
    AMP-->>Consumer: 200 OK<br/>{events[]}

    Note over Consumer,DLQ: DLQ Replay

    Consumer->>AMP: POST /cases/results/subscriptions/{subscriptionId}/events/replay
    AMP->>DLQ: Retrieve events for replay
    DLQ-->>AMP: Return DLQ events
    AMP->>Broker: Resubmit events to correct topic/queue
    Broker->>Consumer: Redeliver events
    Consumer-->>Broker: 200 OK<br/>(event processed successfully)
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
