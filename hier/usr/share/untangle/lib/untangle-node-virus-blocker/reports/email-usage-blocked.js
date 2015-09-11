{
    "uniqueId": "virus-blocker-DODqrSqE",
    "category": "Virus Blocker",
    "description": "The amount of blocked email over time.",
    "displayOrder": 303,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderDesc": false,
    "units": "hits",
    "readOnly": true,
    "table": "mail_addrs",
    "timeDataColumns": [
        "sum(case when virus_blocker_clean is false then 1 else null end::int) as blocked"
    ],
    "conditions": [
        {
            "column": "addr_kind",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "=",
            "value": "B"
        }
    ],
    "colors": [
        "#8c0000"
    ],
    "timeDataInterval": "AUTO",
    "timeStyle": "BAR_3D_OVERLAPPED",
    "title": "Email Usage (blocked)",
    "type": "TIME_GRAPH"
}
