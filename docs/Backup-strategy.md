┌─────────────────────────────────────────────────────────────────┐
│                     BACKUP STRATEGY                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                 TIER 1 - CONTINUOUS                         │ │
│  │  - Database WAL shipping    - Real-time replication        │ │
│  │  - Redis persistence       - File system snapshots        │ │
│  │  - Recovery Point: < 1 min  - Recovery Time: < 5 min       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                 TIER 2 - DAILY                             │ │
│  │  - Full database backup     - Application data export      │ │
│  │  - Configuration backup     - Certificate backup           │ │
│  │  - Recovery Point: 24 hours - Recovery Time: < 1 hour      │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                 TIER 3 - WEEKLY                            │ │
│  │  - Archive to cold storage  - Compliance backup           │ │
│  │  - Cross-region replication - Long-term retention         │ │
│  │  - Recovery Point: 7 days   - Recovery Time: < 4 hours    │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘