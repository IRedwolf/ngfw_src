{
    "uniqueId": "web-filter-bHCInGuJp6c",
    "category": "Web Filter",
    "description": "The number of web requests grouped by hostname.",
    "displayOrder": 400,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderByColumn": "value",
    "orderDesc": true,
    "units": "hits",
    "pieGroupColumn": "hostname",
    "pieSumColumn": "count(*)",
    "readOnly": true,
    "table": "http_events",
    "title": "Top Hostnames (by requests)",
    "type": "PIE_GRAPH"
}
