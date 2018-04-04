Ext.define('Ung.view.reports.EntrySettingsModel', {
    extend: 'Ext.app.ViewModel',
    alias: 'viewmodel.entrysettings',

    stores: {
        textColumnsStore: {
            data: '{textColumns}',
            listeners: {
                datachanged: 'onTextColumnsChanged',
                update: 'onTextColumnsChanged'
            }
        },
        timeDataColumnsStore: {
            data: '{timeDataColumns}',
            listeners: {
                datachanged: 'onTimeDataColumnsChanged',
                update: 'onTimeDataColumnsChanged'
            }
        }
    },
    formulas: {
        f_activeReportCard: function (get) {
            var activeCard;
            switch(get('entry.type')) {
            case 'TEXT': activeCard = 'textreport'; break;
            case 'EVENT_LIST': activeCard = 'eventreport'; break;
            default: activeCard = 'graphreport';
            }
            return activeCard;
        },
        f_tableColumns: function (get) {
            var table = get('eEntry.table'), tableConfig, defaultColumns;

            if (!table) { return []; }

            tableConfig = TableConfig.generate(table);

            if (get('eEntry.type') !== 'EVENT_LIST') {
                return tableConfig.comboItems;
            }

            // for EVENT_LIST setup the columns
            defaultColumns = Ext.clone(get('eEntry.defaultColumns'));

            // initially set none as default
            Ext.Array.each(tableConfig.comboItems, function (item) {
                item.isDefault = false;
            });

            Ext.Array.each(get('eEntry.defaultColumns'), function (defaultColumn) {
                var col = Ext.Array.findBy(tableConfig.comboItems, function (item) {
                    return item.value === defaultColumn;
                });
                // remove default columns if not in TableConfig
                if (!col) {
                    // vm.set('eEntry.defaultColumns', Ext.Array.remove(defaultColumns, defaultColumn));
                } else {
                    // otherwise set it as default
                    col.isDefault = true;
                }
            });
            return tableConfig.comboItems;
        },
        f_approximation: {
            get: function (get) {
                return get('eEntry.approximation') || 'sum';
            },
            set: function (value) {
                this.set('eEntry.approximation', value !== 'sum' ? value : null);
            }
        },
    }
});
