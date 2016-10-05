Ext.define('Ung.chart.TimeChart', {
    extend: 'Ext.container.Container',
    alias: 'widget.timechart',
    requires: [
        'Ung.chart.TimeChartController'
    ],

    controller: 'timechart',
    viewModel: true,

    config: {
        widget: null,
        entry: null
    },

    listeners: {
        afterrender: 'onAfterRender',
        resize: 'onResize',
        setseries: 'onSetSeries',
        //setstyle: 'onSetStyle',
        //setcolors: 'onSetColors',
        beginfetchdata: 'onBeginFetchData'
    },

    items: [{
        xtype: 'component',
        reference: 'timechart',
        cls: 'chart'
    }, {
        xtype: 'component',
        reference: 'loader',
        cls: 'loader',
        hideMode: 'visibility',
        html: '<div class="spinner"><div class="bounce1"></div><div class="bounce2"></div><div class="bounce3"></div></div>'
    }]
});