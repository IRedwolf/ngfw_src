{
    "uniqueId": "web-filter-L0Jv2FWalnw",
    "category": "Web Filter",
    "description": "The amount of total, flagged, and blocked web requests over time.",
    "displayOrder": 100,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderDesc": false,
    "units": "hits",
    "readOnly": true,
    "table": "http_events",
    "timeDataColumns": [
        "count(*) as scanned",
        "sum(web_filter_flagged::int) as flagged",
        "sum(web_filter_blocked::int) as blocked"
    ],
    "colors": [
        "#396c2b",
        "#e5e500",
        "#8c0000"
    ],
    "timeDataInterval": "AUTO",
    "timeStyle": "BAR_3D_OVERLAPPED",
    "title": "Web Usage (all)",
    "type": "TIME_GRAPH"
}
