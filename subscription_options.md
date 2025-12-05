# SubscriptionRequest Design Options – Comparison

Below is a clear comparison of the **three approaches** for modelling a SubscriptionRequest in your API.

---

## ✅ OPTION 1 — Single Webhook + Array of Event Types

### 📦 Payload
```json
{
  "webhookUrl": "https://example.com/callback",
  "eventTypes": ["PCR", "CUSTODIAL_RESULT"]
}
```

### ⭐ Pros
- Simple and easy to understand.
- One subscription request defines all event types.
- Clients only maintain **one webhook endpoint**.
- Reduces number of subscriptions stored.

### ⚠️ Cons
- Cannot map different event types to different webhook URLs.
- Harder to customise routing per event.
- Updating event types may disrupt all events at once.

### 🎯 Best When
- The client wants **one handler** for all events.
- Simplicity is preferred over flexibility.

---

## ✅ OPTION 2 — Array of Webhooks, Each With Event Type (Batch Create)

### 📦 Payload
```json
{
  "subscriptions": [
    {
      "eventType": "PCR",
      "webhookUrl": "https://example.com/pcr"
    },
    {
      "eventType": "CUSTODIAL_RESULT",
      "webhookUrl": "https://example.com/custody"
    }
  ]
}
```

### ⭐ Pros
- Maximum flexibility — each event type can map to a **unique webhook URL**.
- Allows **batch creation** of multiple subscriptions.
- Ideal for enterprise clients with different event processors.

### ⚠️ Cons
- More complex validation and server logic.
- Potential partial failures in batch operations.
- Harder for clients to configure.

### 🎯 Best When
- Clients require **different receivers for different event types**.
- The system supports or requires **individual routing logic** per event.

---

## ✅ OPTION 3 — Single Webhook + Single Event Type (One Subscription Per Request)

### 📦 Payload
```json
{
  "webhookUrl": "https://example.com/callback",
  "eventType": "PCR"
}
```

### ⭐ Pros
- Cleanest and most RESTful design.
- Subscription = 1 event type + 1 webhook.
- Very easy to update, delete, or manage each subscription independently.
- Very low validation complexity.

### ⚠️ Cons
- Requires multiple API calls if a client wants multiple event types.
- More stored subscription rows (one per event).

### 🎯 Best When
- You want **clean CRUD semantics**.
- Each subscription is treated as its own resource.
- Event types may evolve independently.

---

# 📘 Quick Decision Guide

| Option | Flexibility | Complexity | Best For |
|--------|-------------|------------|----------|
| **Option 1** | Low | Low | Simple setups where one webhook receives all events |
| **Option 2** | High | Medium | Complex systems needing event-specific routing |
| **Option 3** | Medium | Low | Clean REST APIs where subscriptions are independent |

---

# 🎯 Recommendation

**Option 3** is generally the most scalable, flexible, and aligned with REST principles.  
Options 1 and 2 can be implemented later as convenience layers.
