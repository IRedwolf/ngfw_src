{
    "category": "Virus Blocker",
    "conditions": [
        {
            "column": "virus_blocker_clean",
            "javaClass": "com.untangle.node.reports.SqlCondition",
            "operator": "is",
            "value": "FALSE"
        }
    ],
    "defaultColumns": ["time_stamp","hostname","username","uri","virus_blocker_clean","virus_blocker_name","s_server_addr"],
    "description": "Infected FTP sessions blocked by Virus Blocker.",
    "displayOrder": 31,
    "javaClass": "com.untangle.node.reports.EventEntry",
    "table": "ftp_events",
    "title": "Infected Ftp Events",
    "uniqueId": "virus-blocker-75DO3RB1AE"
}
