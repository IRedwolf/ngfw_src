{
    "uniqueId": "virus-blocker-X1koPTuI",
    "category": "Virus Blocker",
    "description": "The amount of blocked FTP requests over time.",
    "displayOrder": 203,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderDesc": false,
    "units": "hits",
    "readOnly": true,
    "table": "ftp_events",
    "timeDataColumns": [
        "sum(case when virus_blocker_clean is false then 1 else null end::int) as blocked"
    ],
    "colors": [
        "#8c0000"
    ],
    "timeDataInterval": "AUTO",
    "timeStyle": "BAR_3D_OVERLAPPED",
    "title": "FTP Usage (blocked)",
    "type": "TIME_GRAPH"
}
