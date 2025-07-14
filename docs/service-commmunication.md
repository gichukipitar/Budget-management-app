
### 3.2 Service Communication Patterns

#### 3.2.1 Synchronous Communication

┌─────────────────┐ HTTP/REST ┌─────────────────┐ │ Client App │◄────────────────►│ API Gateway │ └─────────────────┘ └─────────────────┘ │ ▼ ┌─────────────────┐ OpenFeign ┌─────────────────┐ │ Service A │◄────────────────►│ Service B │ └─────────────────┘ └─────────────────┘