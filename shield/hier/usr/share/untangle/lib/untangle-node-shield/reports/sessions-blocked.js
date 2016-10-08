{
    "uniqueId": "shield-tVO1Cx3HjO",
    "category": "Shield",
    "description": "The amount of blocked sessions over time.",
    "displayOrder": 101,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderDesc": false,
    "units": "hits",
    "readOnly": true,
    "table": "sessions",
    "timeDataColumns": [
        "count(*) as blocked"
    ],
    "conditions": [
        {
            "column": "filter_prefix",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "=",
            "value": "shield_blocked"
        }
    ],
    "colors": [
        "#8c0000"
    ],
    "timeDataInterval": "AUTO",
    "timeStyle": "BAR_3D_OVERLAPPED",
    "title": "Blocked Sessions",
    "type": "TIME_GRAPH"
}
