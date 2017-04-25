Ext.define('Ung.apps.openvpn.Main', {
    extend: 'Ung.cmp.AppPanel',
    alias: 'widget.app-openvpn',
    controller: 'app-openvpn',

    viewModel: {
        stores: {
            remoteClients: {
                data: '{settings.remoteClients.list}'
            },
            remoteServers: {
                data: '{settings.remoteServers.list}'
            },
            groups: {
                data:'{settings.groups.list}'
            },
            exportedNetworks: {
                data:'{settings.exports.list}'
            },
            serverConfiguration: {
                data: '{settings.serverConfiguration.list}'
            },
            clientConfiguration: {
                data: '{settings.clientConfiguration.list}'
            },
            clientStatusList: {
                data: '{clientStatusData}'
            },
            serverStatusList: {
                data: '{serverStatusData}'
            }

        },

        data: {
            clientStatusData: [],
            serverStatusData: [],
        },

        formulas: {
            getSiteUrl: {
                get: function(get) {
                    var publicUrl = rpc.networkManager.getPublicUrl();
                    return(publicUrl.split(":")[0] + ":" + get('settings.port'));
                }
            }
        }
    },

    items: [
        { xtype: 'app-openvpn-status' },
        { xtype: 'app-openvpn-server' },
        { xtype: 'app-openvpn-client' },
        { xtype: 'app-openvpn-advanced' }
    ]

});
