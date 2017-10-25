Ext.define('Ung.view.reports.EntryController', {
    extend: 'Ext.app.ViewController',
    alias: 'controller.entry',

    control: {
        '#': {
            afterrender: 'onAfterRender',
        }
    },

    refreshTimeout: null,


    onAfterRender: function () {
        var me = this, vm = this.getViewModel(),
            dataGrid = this.getView().down('#currentData');

        /**
         * each time report selection changes
         */
        vm.bind('{entry}', function (entry) {
            vm.set('eEntry', null);
            vm.set('_currentData', []);
            me.setReportCard(entry.get('type'));


            // dataGrid.setColumns([]);
            // dataGrid.setLoading(true);

            // if (entry.get('type') === 'EVENT_LIST') {
            //     me.lookup('filterfield').setValue('');
            // }

            // check if widget in admin context
            if (Ung.app.context === 'ADMIN') {
                // widget = Ext.getStore('widgets').findRecord('entryId', entry.get('uniqueId')) || null;
                vm.set('widget', Ext.getStore('widgets').findRecord('entryId', entry.get('uniqueId')));
            }
        });

        // each time the eEntry changes by selecting 'Settings'
        vm.bind('{eEntry}', function (eEntry) {
            me.getView().up('#reports').getViewModel().set('editing', eEntry ? true : false);
            if (!vm.get('entry')) { return; }
            else {
                // on cancel when eEntry turns null reloads the selected entry if exists
                if (!eEntry) {
                    me.reload();
                    return;
                }
            }

            // if eEntry is readOnly, alter the title to avoid initial validation error
            if (eEntry.get('readOnly')) {
                eEntry.set('title', eEntry.get('title') + ' ' + '[new]'.t());
            }

            // if not defaultColumns initialize with [] to avoid editing errors
            if (!eEntry.get('defaultColumns')) {
                eEntry.set('defaultColumns', []);
            }

            // transform eEntry text and time data columns to usable data for the stores
            vm.set('textColumns', Ext.Array.map(eEntry.get('textColumns') || [], function (col) { return { str: col }; }));
            vm.set('timeDataColumns', Ext.Array.map(eEntry.get('timeDataColumns') || [], function (col) { return { str: col }; }));
        });

        // each time the eEntry type is changed check valid edit form
        vm.bind('{eEntry.type}', function (type) {
            // console.log('TYPE', type);
            if (!type) { return; }
            me.setReportCard(type);
            Ext.defer(function () {
                if (me.getView().down('form').isValid()) {
                    vm.set('validForm', true);
                    me.reload();
                } else {
                    vm.set('validForm', false);
                    me.reset();
                }
            }, 100);
        });

        // watch since date switching and reload the report
        vm.bind('{sinceDate.value}', function () {
            // vm.set({
            //     f_startdate: Util.serverToClientDate(new Date((Math.floor(Util.getMilliseconds()/60000) * 60000) - vm.get('sinceDate.value') * 3600 * 1000)),
            //     f_enddate: null
            // });
            me.reload();
        });

        // watch custom range switch on/off
        vm.bind('{r_customRangeCk.value}', function (checked) {
            // vm.set({
            //     f_startdate: Util.serverToClientDate(new Date((Math.floor(Util.getMilliseconds()/60000) * 60000) - vm.get('sinceDate.value') * 3600 * 1000)),
            //     f_enddate: null
            // });
            if (checked) {
                // when checked, disable autorefresh because of the fixed range
                me.lookup('r_autoRefreshBtn').setPressed(false);
            } else {
                // when unckecked, reload the current data from Since
                me.reload();
            }
        });

        // watch auto refresh button switch on/off
        vm.bind('{r_autoRefreshBtn.pressed}', function (pressed) {
            if (pressed) {
                me.reload();
            } else {
                if (me.refreshTimeout) {
                    clearTimeout(me.refreshTimeout);
                    me.refreshTimeout = null;
                }
            }
        });

        // vm.bind('{f_startdate}', function () { me.reload(); });
        // vm.bind('{f_enddate}', function () { me.reload(); });
    },

    /**
     * sets active card based on report type
     */
    setReportCard: function (type) {
        var me = this, reportCard = '';
        switch(type) {
        case 'TEXT': reportCard = 'textreport'; break;
        case 'PIE_GRAPH':
        case 'TIME_GRAPH':
        case 'TIME_GRAPH_DYNAMIC': reportCard = 'graphreport'; break;
        case 'EVENT_LIST': reportCard = 'eventreport'; break;
        }
        me.getView().down('#reportCard').setActiveItem(reportCard);
    },

    formatTimeData: function (data) {
        var vm = this.getViewModel(),
            entry = vm.get('eEntry') || vm.get('entry'),
            dataGrid = this.getView().down('#currentData'), i, column;

        dataGrid.setLoading(false);

        // var storeFields = [{
        //     name: 'time_trunc'
        // }];

        var reportDataColumns = [{
            dataIndex: 'time_trunc',
            header: 'Timestamp'.t(),
            width: 130,
            flex: 1,
            renderer: function (val) {
                return (!val) ? 0 : Util.timestampFormat(val);
            }
        }];
        var title;

        for (i = 0; i < entry.get('timeDataColumns').length; i += 1) {
            column = entry.get('timeDataColumns')[i].split(' ').splice(-1)[0];
            title = column;
            reportDataColumns.push({
                dataIndex: column,
                header: title,
                width: entry.get('timeDataColumns').length > 2 ? 60 : 90,
                renderer: function (val) {
                    return val !== undefined ? val : '-';
                }
            });
        }

        dataGrid.setColumns(reportDataColumns);
        dataGrid.getStore().loadData(data);
        // vm.set('_currentData', data);
    },

    formatTimeDynamicData: function (data) {
        var vm = this.getViewModel(),
            entry = vm.get('entry'),
            timeDataColumns = [],
            dataGrid = this.getView().down('#currentData'), i, column;

        dataGrid.setLoading(false);

        for (i = 0; i < data.length; i += 1) {
            for (var _column in data[i]) {
                if (data[i].hasOwnProperty(_column) && _column !== 'time_trunc' && _column !== 'time' && timeDataColumns.indexOf(_column) < 0) {
                    timeDataColumns.push(_column);
                }
            }
        }

        var reportDataColumns = [{
            dataIndex: 'time_trunc',
            header: 'Timestamp'.t(),
            width: 130,
            flex: 1,
            renderer: function (val) {
                return (!val) ? 0 : Util.timestampFormat(val);
            }
        }];
        var seriesRenderer = null, title;
        if (!Ext.isEmpty(entry.get('seriesRenderer'))) {
            seriesRenderer = Renderer[entry.get('seriesRenderer')];
        }

        for (i = 0; i < timeDataColumns.length; i += 1) {
            column = timeDataColumns[i];
            title = seriesRenderer ? seriesRenderer(column) + ' [' + column + ']' : column;
            // storeFields.push({name: timeDataColumns[i], type: 'integer'});
            reportDataColumns.push({
                dataIndex: column,
                header: title,
                width: timeDataColumns.length > 2 ? 60 : 90
            });
        }

        dataGrid.setColumns(reportDataColumns);
        dataGrid.getStore().loadData(data);
        // vm.set('_currentData', data);
    },

    formatPieData: function (data) {
        var me = this, vm = me.getViewModel(),
            entry = vm.get('eEntry') || vm.get('entry'),
            dataGrid = me.getView().down('#currentData');

        dataGrid.setLoading(false);

        dataGrid.setColumns([{
            dataIndex: entry.get('pieGroupColumn'),
            header: me.sqlColumnRenderer(entry.get('pieGroupColumn')),
            flex: 1,
            renderer: Renderer[entry.get('pieGroupColumn')] || null
        }, {
            dataIndex: 'value',
            header: 'value'.t(),
            width: 200,
            renderer: function (value) {
                if (entry.get('units') === 'bytes' || entry.get('units') === 'bytes/s') {
                    return Util.bytesToHumanReadable(value, true);
                } else {
                    return value;
                }
            }
        }, {
            xtype: 'actioncolumn',
            menuDisabled: true,
            width: 30,
            align: 'center',
            items: [{
                iconCls: 'fa fa-filter',
                tooltip: 'Add Condition'.t(),
                handler: 'addPieFilter'
            }]
        }]);
        dataGrid.getStore().loadData(data);
        // vm.set('_currentData', data);

    },

    addPieFilter: function (view, rowIndex, colIndex, item, e, record) {
        var me = this, vm = me.getViewModel(),
            gridFilters =  me.getView().down('#sqlFilters'),
            col = vm.get('entry.pieGroupColumn');

        if (col) {
            vm.get('sqlFilterData').push({
                column: col,
                operator: '=',
                value: record.get(col),
                javaClass: 'com.untangle.app.reports.SqlCondition'
            });
        } else {
            console.log('Issue with pie column!');
            return;
        }

        gridFilters.setCollapsed(false);
        gridFilters.setTitle(Ext.String.format('Conditions: {0}'.t(), vm.get('sqlFilterData').length));
        gridFilters.getStore().reload();
        me.reload();
    },

    formatTextData: function (data) {
        var vm = this.getViewModel(),
            entry = vm.get('eEntry') || vm.get('entry'),
            dataGrid = this.getView().down('#currentData'), column, i;

        dataGrid.setLoading(false);
        dataGrid.setColumns([{
            dataIndex: 'data',
            header: 'data'.t(),
            flex: 1
        }, {
            dataIndex: 'value',
            header: 'value'.t(),
            width: 200
        }]);

        var reportData = [], value;
        if (data.length > 0 && entry.get('textColumns') !== null) {
            for (i = 0; i < entry.get('textColumns').length; i += 1) {
                column = entry.get('textColumns')[i].split(' ').splice(-1)[0];
                value = Ext.isEmpty(data[0][column]) ? 0 : data[0][column];
                reportData.push({data: column, value: value});
            }
        }
        // vm.set('_currentData', reportData);
        dataGrid.getStore().loadData(reportData);
    },

    filterData: function (min, max) {
        // aply filtering only on timeseries
        if (this.getViewModel().get('entry.type').indexOf('TIME_GRAPH') >= 0) {
            this.getView().down('#currentData').getStore().clearFilter();
            this.getView().down('#currentData').getStore().filterBy(function (point) {
                var t = point.get('time_trunc').time;
                return t >= min && t <= max ;
            });
        }
    },



    updateColor: function (menu, color) {
        var vm = this.getViewModel(),
            newColors = vm.get('entry.colors') ? Ext.clone(vm.get('entry.colors')) : Ext.clone(Util.defaultColors);

        menu.up('button').setText('<i class="fa fa-square" style="color: #' + color + ';"></i>');
        newColors[menu.up('button').idx] = '#' + color;
        vm.set('entry.colors', newColors);
        return false;
    },

    // addColor: function (btn) {
    //     btn.up('grid').getStore().add({color: 'FF0000'});
    //     // var vm = this.getViewModel();
    //     // var colors = vm.get('report.colors');
    //     // colors.push('#FF0000');
    //     // vm.set('report.colors', colors);
    // },

    reload: function () {
        var me = this, vm = me.getViewModel(),
            entry = vm.get('eEntry') || vm.get('entry'), ctrl;

        vm.set('validForm', true); // te remove the valid warning

        if (!vm.get('r_customRangeCk.value')) {
            vm.set({
                f_startdate: Util.serverToClientDate(new Date((Math.floor(Util.getMilliseconds()/600000) * 600000) - vm.get('sinceDate.value') * 3600 * 1000)),
                f_enddate: null
            });
        }

        if (!entry) { return; }

        switch(entry.get('type')) {
        case 'TEXT': ctrl = me.getView().down('textreport').getController(); break;
        case 'EVENT_LIST': ctrl = me.getView().down('eventreport').getController(); break;
        default: ctrl = me.getView().down('graphreport').getController();
        }

        if (!ctrl) {
            console.error('Entry controller not found!');
            return;
        }

        // if (reps) { reps.getViewModel().set('fetching', true); }
        ctrl.fetchData(false, function () {
            // if (reps) { reps.getViewModel().set('fetching', false); }
            // if autorefresh enabled refetch data in 5 seconds
            if (vm.get('r_autoRefreshBtn.pressed')) {
                me.refreshTimeout = setTimeout(function () {
                    me.reload();
                }, 5000);
            }
        });
    },

    reset: function () {
        var me = this, vm = me.getViewModel(),
            entry = vm.get('eEntry') || vm.get('entry'), ctrl;

        if (!entry) { return; }

        switch(entry.get('type')) {
        case 'TEXT': ctrl = me.getView().down('textreport').getController(); break;
        case 'EVENT_LIST': ctrl = me.getView().down('eventreport').getController(); break;
        default: ctrl = me.getView().down('graphreport').getController();
        }

        if (!ctrl) {
            console.error('Entry controller not found!');
            return;
        }
        if (Ext.isFunction(ctrl.reset)) {
            ctrl.reset();
        }
    },

    // resetView: function(){
    //     var grid = this.getView().down('grid');
    //     Ext.state.Manager.clear(grid.stateId);
    //     grid.filters.clearFilters();
    //     grid.reconfigure(null, grid.tableConfig.columns);

    //     grid.getColumns().forEach( function(column){
    //         if( column.xtype == 'actioncolumn'){
    //             return;
    //         }
    //         column.setHidden( Ext.Array.indexOf(grid.visibleColumns, column.dataIndex) < 0 );
    //         if( column.columns ){
    //             column.columns.forEach( Ext.bind( function( subColumn ){
    //                 subColumn.setHidden( Ext.Array.indexOf(grid.visibleColumns, column.dataIndex) < 0 );
    //             }, this ) );
    //         }
    //     });
    // },


    // TABLE COLUMNS / CONDITIONS
    updateDefaultColumns: function (el, value) {
        this.getViewModel().set('entry.defaultColumns', value.split(','));
    },

    addSqlCondition: function () {
        var me = this, vm = me.getViewModel(),
            conds = vm.get('_sqlConditions') || [];

        conds.push({
            autoFormatValue: false,
            column: me.getView().down('#sqlConditionsCombo').getValue(),
            javaClass: 'com.untangle.app.reports.SqlCondition',
            operator: '=',
            value: ''
        });

        me.getView().down('#sqlConditionsCombo').setValue(null);

        vm.set('_sqlConditions', conds);
        me.getView().down('#sqlConditions').getStore().reload();
    },

    removeSqlCondition: function (table, rowIndex) {
        var me = this, vm = me.getViewModel(),
            conds = vm.get('_sqlConditions');
        Ext.Array.removeAt(conds, rowIndex);
        vm.set('_sqlConditions', conds);
        me.getView().down('#sqlConditions').getStore().reload();
    },

    sqlColumnRenderer: function (val) {
        return '<strong>' + TableConfig.getColumnHumanReadableName(val) + '</strong> <span style="float: right;">[' + val + ']</span>';
    },
    // TABLE COLUMNS / CONDITIONS END


    // FILTERS
    addSqlFilter: function () {
        var me = this, vm = me.getViewModel(),
            _filterComboCmp = me.getView().down('#sqlFilterCombo'),
            _operatorCmp = me.getView().down('#sqlFilterOperator'),
            _filterValueCmp = me.getView().down('#sqlFilterValue');

        vm.get('sqlFilterData').push({
            column: _filterComboCmp.getValue(),
            operator: _operatorCmp.getValue(),
            value: _filterValueCmp.getValue(),
            javaClass: 'com.untangle.app.reports.SqlCondition'
        });

        _filterComboCmp.setValue(null);
        _operatorCmp.setValue('=');

        me.getView().down('#filtersToolbar').remove('sqlFilterValue');

        me.getView().down('#sqlFilters').setTitle(Ext.String.format('Conditions: {0}'.t(), vm.get('sqlFilterData').length));
        me.getView().down('#sqlFilters').getStore().reload();
        me.reload();
    },

    removeSqlFilter: function (table, rowIndex) {
        var me = this, vm = me.getViewModel();
        Ext.Array.removeAt(vm.get('sqlFilterData'), rowIndex);

        me.getView().down('#filtersToolbar').remove('sqlFilterValue');

        me.getView().down('#sqlFilters').setTitle(Ext.String.format('Conditions: {0}'.t(), vm.get('sqlFilterData').length));
        me.getView().down('#sqlFilters').getStore().reload();
        me.reload();
    },

    onColumnChange: function (cmp, newValue) {
        var me = this;

        cmp.up('toolbar').remove('sqlFilterValue');

        if (!newValue) { return; }
        var column = Ext.Array.findBy(me.tableConfig.columns, function (column) {
            return column.dataIndex === newValue;
        });

        if (column.widgetField) {
            column.widgetField.itemId = 'sqlFilterValue';
            cmp.up('toolbar').insert(4, column.widgetField);
        } else {
            cmp.up('toolbar').insert(4, {
                xtype: 'textfield',
                itemId: 'sqlFilterValue',
                value: ''
            });
        }
    },

    onFilterKeyup: function (cmp, e) {
        if (e.keyCode === 13) {
            this.addSqlFilter();
        }
    },

    sqlFilterQuickItems: function (btn) {
        var menuItem, menuItems = [];
        Rpc.asyncData('rpc.reportsManager.getConditionQuickAddHints').then(function (result) {
            Ext.Object.each(result, function (key, vals) {
                menuItem = {
                    text: TableConfig.getColumnHumanReadableName(key),
                    disabled: vals.length === 0
                };
                if (vals.length > 0) {
                    menuItem.menu = {
                        plain: true,
                        items: Ext.Array.map(vals, function (val) {
                            return {
                                text: val,
                                column: key
                            };
                        }),
                        listeners: {
                            click: 'selectQuickFilter'
                        }
                    };
                }
                menuItems.push(menuItem);


            });
            btn.getMenu().removeAll();
            btn.getMenu().add(menuItems);
        });
    },

    selectQuickFilter: function (menu, item) {
        var me = this, vm = this.getViewModel(),
            _filterComboCmp = me.getView().down('#sqlFilterCombo'),
            _operatorCmp = me.getView().down('#sqlFilterOperator');

        vm.get('sqlFilterData').push({
            column: item.column,
            operator: '=',
            value: item.text,
            javaClass: 'com.untangle.app.reports.SqlCondition'
        });

        _filterComboCmp.setValue(null);
        _operatorCmp.setValue('=');

        me.getView().down('#filtersToolbar').remove('sqlFilterValue');

        me.getView().down('#sqlFilters').setTitle(Ext.String.format('Conditions: {0}'.t(), vm.get('sqlFilterData').length));
        me.getView().down('#sqlFilters').getStore().reload();
        me.reload();

    },

    // END FILTERS


    // // DASHBOARD ACTION
    dashboardAddRemove: function () {
        var vm = this.getViewModel(), widget = vm.get('widget'), entry = vm.get('entry'), action;

        if (!widget) {
            action = 'add';
            widget = Ext.create('Ung.model.Widget', {
                displayColumns: entry.get('displayColumns'),
                enabled: true,
                entryId: entry.get('uniqueId'),
                javaClass: 'com.untangle.uvm.DashboardWidgetSettings',
                refreshIntervalSec: 60,
                timeframe: '',
                type: 'ReportEntry'
            });
        } else {
            action = 'remove';
        }

        Ext.fireEvent('widgetaction', action, widget, entry, function (wg) {
            vm.set('widget', wg);
            Util.successToast('<span style="color: yellow; font-weight: 600;">' + vm.get('entry.title') + '</span> ' + (action === 'add' ? 'added to' : 'removed from') + ' Dashboard!');
        });
    },

    // titleChange: function( control, newValue) {
    //     var me = this, vm = me.getViewModel();

    //     var currentRecord = vm.get('entry');

    //     var titleConflictSave = false;
    //     var titleConflictSaveNew = false;
    //     var sameCustomizableReport = false;
    //     var sameReport = false;
    //     Rpc.asyncData('rpc.reportsManager.getReportEntries')
    //         .then(function(result) {
    //             result.list.forEach( function(reportEntry) {
    //                 if( ( reportEntry.category + '/' + reportEntry.title.trim() )  == ( currentRecord.get('category') + '/' + newValue.trim() ) ){
    //                     titleConflictSave = true;
    //                     titleConflictSaveNew = true;

    //                     if( reportEntry.uniqueId == currentRecord.get('uniqueId') ){
    //                         sameReport = true;
    //                     }
    //                     if( sameReport &&
    //                         currentRecord.get('readOnly') == false){
    //                         sameCustomizableReport = true;
    //                         titleConflictSave = false;
    //                     }
    //                 }
    //             });

    //             if (control){
    //                 if( titleConflictSave && !sameReport ){
    //                     control.setValidation('Another report within this category has this title'.t());
    //                 }else{
    //                     control.setValidation(true);
    //                 }
    //             }

    //             var messages = [];
    //             if(currentRecord.get('readOnly')){
    //                 messages.push( '<i class="fa fa-info-circle fa-lg"></i>&nbsp;' + 'This default report is read-only. Delete and Save are disabled.'.t());
    //             }
    //             if( ( titleConflictSaveNew && !sameCustomizableReport ) || titleConflictSaveNew){
    //                 messages.push( '<i class="fa fa-info-circle fa-lg"></i>&nbsp;'+ 'Change Title to Save as New Report.'.t());
    //             }
    //             vm.set('reportMessages',  messages.join('<br>'));

    //             if(!titleConflictSave){
    //                 vm.set('entry.title', newValue);
    //             }
    //         });
    // },

    updateReport: function () {
        var me = this,
            v = this.getView(),
            vm = this.getViewModel(),
            entry = vm.get('eEntry'), tdcg, tdc = [];

        // update timeDataColumns or textColumns
        if (entry.get('type') === 'TIME_GRAPH') {
            tdcg = v.down('#timeDataColumnsGrid');
            tdcg.getStore().each(function (col) { tdc.push(col.get('str')); });
            entry.set('timeDataColumns', tdc);
        }
        if (entry.get('type') === 'TEXT') {
            tdcg = v.down('#textColumnsGrid');
            tdcg.getStore().each(function (col) { tdc.push(col.get('str')); });
            entry.set('textColumns', tdc);
        }

        v.setLoading(true);
        Rpc.asyncData('rpc.reportsManager.saveReportEntry', entry.getData())
            .then(function() {
                v.setLoading(false);

                var updatedRec = Ext.getStore('reports').findRecord('uniqueId', entry.get('uniqueId'));
                if (updatedRec) {
                    updatedRec.copyFrom(entry);
                    updatedRec.commit();
                    Ext.getStore('reportstree').build();
                }
                Util.successToast('<span style="color: yellow; font-weight: 600;">' + vm.get('entry.title') + '</span> report updated!');

                Ung.app.redirectTo('#reports/' + entry.get('category').replace(/ /g, '-').toLowerCase() + '/' + entry.get('title').replace(/\s+/g, '-').toLowerCase());
                vm.set('eEntry', null);
                me.reload();
            });
    },

    saveNewReport: function () {
        var me = this,
            v = this.getView(),
            vm = this.getViewModel(),
            entry = vm.get('eEntry'), tdcg, tdc = [];

        entry.set('uniqueId', 'report-' + Math.random().toString(36).substr(2));
        entry.set('readOnly', false);

        // update timeDataColumns or textColumns
        if (entry.get('type') === 'TIME_GRAPH') {
            tdcg = v.down('#timeDataColumnsGrid');
            tdcg.getStore().each(function (col) { tdc.push(col.get('str')); });
            entry.set('timeDataColumns', tdc);
        }
        if (entry.get('type') === 'TEXT') {
            tdcg = v.down('#textColumnsGrid');
            tdcg.getStore().each(function (col) { tdc.push(col.get('str')); });
            entry.set('textColumns', tdc);
        }

        v.setLoading(true);
        Rpc.asyncData('rpc.reportsManager.saveReportEntry', entry.getData())
            .then(function() {
                v.setLoading(false);
                Ext.getStore('reports').add(entry);
                Util.successToast('<span style="color: yellow; font-weight: 600;">' + entry.get('title') + ' report added!');
                Ung.app.redirectTo('#reports/' + entry.get('category').replace(/ /g, '-').toLowerCase() + '/' + entry.get('title').replace(/\s+/g, '-').toLowerCase());

                Ext.getStore('reportstree').build(); // rebuild tree after save new
                me.reload();
            });
    },

    removeReport: function () {
        var me = this, vm = this.getViewModel(),
            entry = vm.get('entry');

        Ext.MessageBox.confirm('Warning'.t(),
            'Deleting this report will also remove Dashboard widgets containing this report!'.t() + '<br/><br/>' +
            'Do you want to continue?'.t(),
        function (btn) {
            if (btn === 'yes') {
                if (vm.get('widget')) {
                    // remove it from dashboard first
                    Ext.fireEvent('widgetaction', 'remove', vm.get('widget'), entry, function (wg) {
                        vm.set('widget', wg);
                        me.removeReportAction(entry.getData());
                    });
                } else {
                    me.removeReportAction(entry.getData());
                }
            }
        });

    },

    removeReportAction: function (entry) {
        var vm = this.getViewModel();
        Rpc.asyncData('rpc.reportsManager.removeReportEntry', entry)
            .then(function () {
                Ung.app.redirectTo('#reports/' + entry.category.replace(/ /g, '-').toLowerCase());
                Util.successToast(entry.title + ' ' + 'deleted successfully'.t());
                vm.set('eEntry', null);
                var removableRec = Ext.getStore('reports').findRecord('uniqueId', entry.uniqueId);
                if (removableRec) {
                    Ext.getStore('reports').remove(removableRec); // remove record
                    Ext.getStore('reportstree').build(); // rebuild tree after save new
                }
            }, function (ex) {
                Util.handleException(ex);
            });
    },

    downloadGraph: function () {
        var me = this, vm = me.getViewModel(), now = new Date();
        try {
            me.getView().down('#graphreport').getController().chart.exportChart({
                filename: (vm.get('entry.category') + '-' + vm.get('entry.title') + '-' + Ext.Date.format(now, 'd.m.Y-Hi')).replace(/ /g, '_'),
                type: 'image/png'
            });
        } catch (ex) {
            console.log(ex);
            Util.handleException('Unable to download!');
        }
    },

    exportEventsHandler: function () {
        var me = this, vm = me.getViewModel(), entry = vm.get('entry').getData(), columns = [], startDate, endDate;
        if (!entry) { return; }

        var grid = me.getView().down('eventreport > ungrid');

        if (!grid) {
            console.log('Grid not found');
            return;
        }

        Ext.Array.each(grid.getColumns(), function (col) {
            if (col.dataIndex && !col.hidden) {
                columns.push(col.dataIndex);
            }
        });

        var conditions = [];
        Ext.Array.each(Ext.clone(vm.get('sqlFilterData')), function (cnd) {
            delete cnd._id;
            conditions.push(cnd);
        });

        // startDate converted from UI to server date
        startDate = Util.clientToServerDate(vm.get('startDate'));
        // endDate converted from UI to server date
        endDate = Util.clientToServerDate(vm.get('endDate'));

        Ext.MessageBox.wait('Exporting Events...'.t(), 'Please wait'.t());
        var downloadForm = document.getElementById('downloadForm');
        downloadForm['type'].value = 'eventLogExport';
        downloadForm['arg1'].value = (entry.category + '-' + entry.title + '-' + Ext.Date.format(vm.get('startDate'), 'd.m.Y-H:i') + '-' + Ext.Date.format(vm.get('endDate'), 'd.m.Y-H:i')).replace(/ /g, '_');
        downloadForm['arg2'].value = Ext.encode(entry);
        downloadForm['arg3'].value = conditions.length > 0 ? Ext.encode(conditions) : '';
        downloadForm['arg4'].value = columns.join(',');
        downloadForm['arg5'].value = startDate ? startDate.getTime() : -1;
        downloadForm['arg6'].value = endDate ? endDate.getTime() : -1;
        downloadForm.submit();
        Ext.MessageBox.hide();
    },

    exportGraphData: function (btn) {
        var me = this, vm = me.getViewModel(), entry = vm.get('entry').getData(), columns = [], headers = [], j;
        if (!entry) { return; }

        var grid = btn.up('grid'), csv = [];

        if (!grid) {
            console.log('Grid not found');
            return;
        }

        var processRow = function (row) {
            var data = [], j, innerValue;
            for (j = 0; j < row.length; j += 1) {
                innerValue = !row[j] ? '' : row[j].toString();
                data.push('"' + innerValue.replace(/"/g, '""') + '"');
            }
            return data.join(',') + '\r\n';
        };

        Ext.Array.each(grid.getColumns(), function (col) {
            if (col.dataIndex && !col.hidden) {
                columns.push(col.dataIndex);
                headers.push(col.text);
            }
        });
        csv.push(processRow(headers));

        grid.getStore().each(function (row) {
            var r = [];
            for (j = 0; j < columns.length; j += 1) {
                if (columns[j] === 'time_trunc') {
                    r.push(Util.timestampFormat(row.get('time_trunc')));
                } else {
                    r.push(row.get(columns[j]));
                }
            }
            csv.push(processRow(r));
        });

        me.download(csv.join(''), (entry.category + '-' + entry.title + '-' + Ext.Date.format(new Date(), 'd.m.Y-Hi')).replace(/ /g, '_') + '.csv', 'text/csv');

    },

    download: function(content, fileName, mimeType) {
        var a = document.createElement('a');
        mimeType = mimeType || 'application/octet-stream';

        if (navigator.msSaveBlob) { // IE10
            return navigator.msSaveBlob(new Blob([ content ], {
                type : mimeType
            }), fileName);
        } else if ('download' in a) { // html5 A[download]
            a.href = 'data:' + mimeType + ',' + encodeURIComponent(content);
            a.setAttribute('download', fileName);
            document.body.appendChild(a);
            setTimeout(function() {
                a.click();
                document.body.removeChild(a);
            }, 100);
            return true;
        } else { //do iframe dataURL download (old ch+FF):
            var f = document.createElement('iframe');
            document.body.appendChild(f);
            f.src = 'data:' + mimeType + ',' + encodeURIComponent(content);
            setTimeout(function() {
                document.body.removeChild(f);
            }, 400);
            return true;
        }
    },

    editEntry: function () {
        var me = this, vm = me.getViewModel();
        vm.set('eEntry', vm.get('entry').copy(null));
        vm.notify();
    },

    cancelEdit: function () {
        var me = this, vm = me.getViewModel();
        vm.set('eEntry', null);
        vm.set('validForm', true);
        vm.notify();

        // if New Entry was initiated from a category view
        if (!vm.get('entry')) {
            me.getView().up('#reports').lookup('cards').setActiveItem('category');
            return;
        }

        me.setReportCard(vm.get('entry.type'));
    },

    onTextColumnsChanged: function (store) {
        var vm = this.getViewModel(), tdc = [];
        // update validation counter
        vm.set('textColumnsCount', store.getCount());
        // update the actual entry
        store.each(function (col) { tdc.push(col.get('str')); });
        vm.set('eEntry.textColumns', tdc);
    },

    onTimeDataColumnsChanged: function (store) {
        var vm = this.getViewModel(), tdc = [];
        // update validation counter
        vm.set('timeDataColumnsCount', store.getCount());
        // update the actual entry
        store.each(function (col) { tdc.push(col.get('str')); });
        vm.set('eEntry.timeDataColumns', tdc);
    },

    removeTextColumn: function (view, rowIndex, colIndex, item, e, record) {
        var me = this, vm = me.getViewModel(), store = view.getStore(), tdc = [];
        store.remove(record);
        view.refresh();
    },

    removeTimeDataColumn: function (view, rowIndex, colIndex, item, e, record) {
        var me = this, vm = me.getViewModel(), store = view.getStore(), tdc = [];
        store.remove(record);
        // vm.set('timeDataColumns', Ext.Array.removeAt(vm.get('timeDataColumns'), rowIndex));
        // store.commitChanges();
        // store.reload();
        // record.drop();
        // store.each(function (col) { tdc.push(col.get('str')); });
        // vm.set('eEntry.timeDataColumns', tdc);
    },

    exportSettings: function () {
        var me = this, vm = me.getViewModel(),
            rep = vm.get('entry').getData();

        delete rep._id;
        delete rep.localizedTitle;
        delete rep.localizedDescription;
        delete rep.slug;
        delete rep.categorySlug;
        delete rep.url;
        delete rep.icon;

        var exportForm = document.getElementById('exportGridSettings');
        exportForm.gridName.value = 'Report-' + rep.title.replace(/ /g, '_');
        exportForm.gridData.value = Ext.encode([rep]);
        exportForm.submit();
    }
});
