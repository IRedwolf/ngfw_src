{
    "uniqueId": "virus-blocker-4dDOWWnF",
    "category": "Virus Blocker",
    "description": "The number of clients with blocked viruses by Email activity.",
    "displayOrder": 306,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "orderByColumn": "value",
    "orderDesc": true,
    "units": "hits",
    "pieGroupColumn": "s_server_addr",
    "pieSumColumn": "count(*)",
    "conditions": [
        {
            "column": "virus_blocker_clean",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "=",
            "value": "false"
        },
        {
            "column": "addr_kind",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "=",
            "value": "B"
        }
    ],
    "readOnly": true,
    "table": "mail_addrs",
    "title": "Email Top Blocked Sites",
    "type": "PIE_GRAPH"
}
