Ext.define('Ung.apps.bandwidthcontrol.view.Rules', {
    extend: 'Ung.cmp.Grid',
    alias: 'widget.app-bandwidth-control-rules',
    itemId: 'rules',
    title: 'Rules'.t(),

    dockedItems: [{
        xtype: 'toolbar',
        dock: 'top',
        items: [{
            xtype: 'tbtext',
            padding: '8 5',
            style: { fontSize: '12px', fontWeight: 600 },
            html: 'Rules are evaluated in-order on network traffic.'.t()
        }]
    }, {
        xtype: 'toolbar',
        dock: 'top',
        items: ['@add', '->', '@import', '@export']
    }],

    recordActions: ['edit', 'delete', 'reorder'],

    listProperty: 'settings.rules.list',
    ruleJavaClass: 'com.untangle.app.bandwidth_control.BandwidthControlRuleCondition',

    conditions: [
        Condition.dstLocal,
        Condition.dstAddr,
        Condition.dstPort,
        Condition.srcAddr,
        Condition.srcPort,
        Condition.srcIntf,
        Condition.tagged,
        Condition.protocol([['TCP','TCP'],['UDP','UDP'],['ICMP','ICMP'],['GRE','GRE'],['ESP','ESP'],['AH','AH'],['SCTP','SCTP']]),

        Condition.hostHostname,
        Condition.hostMac,
        Condition.hostMacVendor,
        Condition.hostPenalty,
        Condition.hostNoQuota,
        Condition.hostQuotaExceeded,
        Condition.hostQuotaAtt,

        Condition.clientHostname,
        Condition.clientMacVendor,
        Condition.clientPenalty,
        Condition.clientNoQuota,
        Condition.clientQuotaExceeded,
        Condition.clientQuotaAtt,

        Condition.serverHostname,
        Condition.serverMacVendor,
        Condition.serverPenalty,
        Condition.serverQuotaExceeded,
        Condition.serverNoQuota,
        Condition.serverQuotaAtt,


        Condition.httpHost,
        Condition.httpReferer,
        Condition.httpUri,
        Condition.httpUrl,
        Condition.httpContentType,
        Condition.httpContentLength,
        Condition.httpUserAgent,
        Condition.httpUserAgentOs,


        Condition.appCtrlApp,
        Condition.appCtrlCategory,
        Condition.appCtrlProto,
        Condition.appCtrlDetail,
        Condition.appCtrlConfidence,
        Condition.appCtrlProductivity,
        Condition.appCtrlRisk,

        Condition.protocolSignature,
        Condition.protocolCategory,
        Condition.protocolDescription,

        Condition.webFilterCategory,
        Condition.webFilterCategDesc,
        Condition.webFilterFlagged
    ],

    emptyRow: {
        ruleId: 0,
        enabled: true,
        description: '',
        action: {
            actionType: 'SET_PRIORITY',
            javaClass: 'com.untangle.app.bandwidth_control.BandwidthControlRuleAction',
            priority: 7,
            quotaBytes: null,
            quotaTime: null,
            tagName: null,
            tagTime: null
        },
        conditions: {
            javaClass: 'java.util.LinkedList',
            list: []
        },
        javaClass: 'com.untangle.app.bandwidth_control.BandwidthControlRule'
    },

    bind: '{rules}',

    columns: [
        Column.ruleId,
        Column.live,
        Column.description,
        Column.conditions, {
            header: 'Action'.t(),
            dataIndex: 'action',
            width: 250,
            renderer: function (value, metaData, record) {
                if (typeof value === 'undefined') {
                    return 'Unknown action'.t();
                }
                switch(value.actionType) {
                  case 'SET_PRIORITY': return 'Set Priority' + ' [' + this.priorityRenderer(value.priority) + ']';
                  case 'TAG_HOST': return 'Tag Host'.t();
                  case 'APPLY_PENALTY_PRIORITY': return 'Apply Penalty Priority'.t(); // DEPRECATED
                  case 'GIVE_CLIENT_HOST_QUOTA': return 'Give Client a Quota'.t();
                  case 'GIVE_HOST_QUOTA': return 'Give Host a Quota'.t();
                  case 'GIVE_USER_QUOTA': return 'Give User a Quota'.t();
                default: return 'Unknown Action: ' + value;
                }
            }
        }
    ],

    // todo: continue this stuff
    editorFields: [
        Field.live,
        Field.description,
        Field.conditions, {
            xtype: 'combo',
            reference: 'actionType',
            publishes: 'value',
            fieldLabel: 'Action Type'.t(),
            bind: '{_action.actionType}',
            allowBlank: false,
            editable: false,
            store: [
                ['SET_PRIORITY', 'Set Priority'.t()],
                ['PENALTY_BOX_CLIENT_HOST', 'Send Client to Penalty Box'.t()],
                ['GIVE_HOST_QUOTA', 'Give Host a Quota'.t()],
                ['GIVE_USER_QUOTA', 'Give User a Quota'.t()]
            ],
            queryMode: 'local',
        }, {
            xtype: 'combo',
            fieldLabel: 'Priority'.t(),
            disabled: true,
            bind: {
                value: '{_action.priority}',
                disabled: '{actionType.value !== "SET_PRIORITY"}'
            },
            allowBlank: false,
            editable: false,
            store:[
                [1, 'Very High'.t()],
                [2, 'High'.t()],
                [3, 'Medium'.t()],
                [4, 'Low'.t()],
                [5, 'Limited'.t()],
                [6, 'Limited More'.t()],
                [7, 'Limited Severely'.t()]
            ],
            queryMode: 'local',
        },
        // {
        //     xtype: 'container',
        //     layout: {
        //         type: 'hbox'
        //     },
        //     items: [{
        //         xtype: 'numberfield',
        //         bind: {
        //             value: '{_action.tagTime}'
        //         },
        //         fieldLabel: 'Penalty Time'.t(),
        //         allowBlank: false,
        //         width: 350,
        //         labelWidth: 150
        //     }, {
        //         xtype: 'displayfield',
        //         html: 'seconds'.t()
        //     }]
        // }
    ]
});