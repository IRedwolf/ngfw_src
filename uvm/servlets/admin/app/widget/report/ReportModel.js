Ext.define('Ung.widget.report.ReportModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.reportwidget',

    formulas: {
        title: {
            get: function(get) {
                return '<h1>' +
                    //(get('entry.readOnly') ? ' <i class="material-icons" style="color: #ec3610; font-size: 16px;">lock</i>' : '') +
                    get('entry.title') +
                    (get('entry.timeDataInterval') ? ' <span style="text-transform: lowercase; color: #777; font-weight: 100;">per ' + get('entry.timeDataInterval') + '</span>' : '') +
                    '</h1><p>since ' + get('timeframe') + ' ago</p></h1>';
            }
        },
        timeframe: {
            get: function(get) {
                return get('widget.timeframe');
                //return Ung.util.Services.secondsToString(get('widget.timeframe'));
            }
        }
    }

});