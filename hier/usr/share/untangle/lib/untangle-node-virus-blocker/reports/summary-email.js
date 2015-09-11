{
    "uniqueId": "virus-blocker-dR0pxxoH",
    "category": "Virus Blocker",
    "description": "A summary of virus blocking actions for Email activity.",
    "displayOrder": 3,
    "enabled": true,
    "javaClass": "com.untangle.node.reports.ReportEntry",
    "textColumns": [
        "sum(case when virus_blocker_clean is not null then 1 else null end::int) as scanned",
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
    "textString": "Virus Blocker scanned {0} email messages of which {1} were blocked.", 
    "readOnly": true,
    "table": "mail_addrs",
    "title": "Virus Blocker Email Summary",
    "type": "TEXT"
}
