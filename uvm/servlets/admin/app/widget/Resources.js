Ext.define('Ung.widget.Resources', {
    extend: 'Ext.container.Container',
    alias: 'widget.resourceswidget',

    controller: 'widget',

    hidden: true,
    border: false,
    baseCls: 'widget small',

    bind: {
        hidden: '{!widget.enabled}'
    },

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [{
        xtype: 'container',
        layout: {
            type: 'hbox',
            align: 'top'
        },
        cls: 'header',
        style: {
            height: '50px'
        },
        items: [{
            xtype: 'component',
            flex: 1,
            html: '<h1>' + 'Resources'.t() + '</h1>'
        }]
    }, {
        xtype: 'container',
        margin: '10 0 0 0',
        layout: {
            type: 'vbox',
            align: 'stretch'
        },
        items: [{
            xtype: 'component',
            flex: 1,
            padding: '5 10',
            bind: {
                html: '<div>' +
                        '<p style="margin: 2px; text-align: left; font-weight: bold; font-size: 14px;">' + 'Memory'.t() + '</p>' +
                        '<div class="load-bar"><div class="load-bar-inner" style="left: -{stats.freeMemoryPercent}%;"></div><p>{stats.totalMemory}</p></div>' +
                        '<div class="load-bar-values">' +
                            '<div class="load-used"><strong>{stats.usedMemoryPercent}%</strong> used<br/><span>{stats.usedMemory}</span></div>' +
                            '<div class="load-free">free <strong>{stats.freeMemoryPercent}%</strong><br/><span>{stats.freeMemory}</span></div>' +
                        '</div>' +
                      '</div>'
            }
        }, {
            xtype: 'component',
            flex: 1,
            padding: '5 10',
            bind: {
                html: '<div>' +
                        '<p style="margin: 2px; text-align: left; font-weight: bold; font-size: 14px;">' + 'Swap'.t() + '</p>' +
                        '<div class="load-bar"><div class="load-bar-inner" style="left: -{stats.freeSwapPercent}%;"></div><p>{stats.totalSwap}</p></div>' +
                        '<div class="load-bar-values">' +
                            '<div class="load-used"><strong>{stats.usedSwapPercent}%</strong> used<br/><span>{stats.usedSwap}</span></div>' +
                            '<div class="load-free">free <strong>{stats.freeSwapPercent}%</strong><br/><span>{stats.freeSwap}</span></div>' +
                        '</div>' +
                      '</div>'
            }
        }, {
            xtype: 'component',
            flex: 1,
            padding: '5 10',
            bind: {
                html: '<div>' +
                        '<p style="margin: 2px; text-align: left; font-weight: bold; font-size: 14px;">' + 'Disk'.t() + '</p>' +
                        '<div class="load-bar"><div class="load-bar-inner" style="left: -{stats.freeDiskPercent}%;"></div><p>{stats.totalDisk}</p></div>' +
                        '<div class="load-bar-values">' +
                            '<div class="load-used"><strong>{stats.usedDiskPercent}%</strong> used<br/><span>{stats.usedDisk}</span></div>' +
                            '<div class="load-free">free <strong>{stats.freeDiskPercent}%</strong><br/><span>{stats.freeDisk}</span></div>' +
                        '</div>' +
                      '</div>'
            }
        }]
    }]
});