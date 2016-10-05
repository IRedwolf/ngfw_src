Ext.define('Ung.view.grid.Grid', {
    extend: 'Ext.grid.Panel',
    xtype: 'ung.grid',

    requires: [
        'Ung.view.grid.GridController',
        'Ung.view.grid.Editor'
    ],

    controller: 'ung.grid',

    config: {
        toolbarFeatures: null, // ['add', 'delete', 'revert', 'importexport'] add specific buttons to top toolbar
        columnFeatures: null, // ['delete', 'edit', 'reorder', 'select'] add specific actioncolumns to grid
        inlineEdit: null, // 'cell' or 'row',
        dataProperty: null // the settings data property, e.g. settings.dataProperty.list
    },

    bind: {
        store: '{store}'
    },

    resizable: false,
    //border: false,
    bodyBorder: false,

    viewConfig: {
        plugins: [],
        stripeRows: false,
        getRowClass: function(record) {
            //console.log(record);
            if (record.markDelete) {
                return 'delete';
            }
            //if (record.phantom) {
            //    return 'added';
            //}
            if (record.dirty) {
                return 'dirty';
            }
        }
    },

    listeners: {
        beforedestroy: 'onBeforeDestory',
        //beforerender: 'onBeforeRender',
        //selectionchange: 'onSelectionChange',
        //beforeedit: 'onBeforeEdit',
        save: 'onSave'
        //reloaded: 'onReloaded'
        //edit: 'onEdit'
    },

    initComponent: function () {
        // add any action columns
        var columnFeatures = this.getColumnFeatures(),
            actionColumns = [];

        // Edit column
        if (Ext.Array.contains(columnFeatures, 'edit')) {
            actionColumns.push({
                xtype: 'ung.actioncolumn',
                text: 'Edit'.t(),
                align: 'center',
                width: 50,
                sortable: false,
                hideable: false,
                resizable: false,
                menuDisabled: true,
                materialIcon: 'edit',
                handler: 'editRecord',
                editor: false,
                type: 'edit'
            });
        }

        // Delete column
        if (Ext.Array.contains(columnFeatures, 'delete')) {
            actionColumns.push({
                xtype: 'ung.actioncolumn',
                text: 'Delete'.t(),
                align: 'center',
                width: 50,
                //tdCls: 'stripe-col',
                sortable: false,
                hideable: false,
                resizable: false,
                menuDisabled: true,
                materialIcon: 'delete',
                handler: 'deleteRecord',
                type: 'delete'
            });
        }

        // Select column which add checkboxes for each row
        if (Ext.Array.contains(columnFeatures, 'select')) {
            this.selModel = {
                type: 'checkboxmodel'
            };
        }

        // Reorder column, allows sorting columns, overriding any other sorters
        if (Ext.Array.contains(columnFeatures, 'reorder')) {
            this.sortableColumns = false; // disable column sorting as it would affect drag sorting

            Ext.apply(this, {
                viewConfig: {
                    plugins: {
                        ptype: 'gridviewdragdrop',
                        dragText: 'Drag and drop to reorganize'.t(),
                        // allow drag only from drag column icons
                        dragZone: {
                            onBeforeDrag: function(data, e) {
                                return Ext.get(e.target).hasCls('draggable');
                            }
                        }
                    }
                }
            });

            // add the droag/drop sorting column as the first column
            actionColumns.unshift({
                xtype: 'ung.actioncolumn',
                align: 'center',
                width: 30,
                sortable: false,
                hideable: false,
                resizable: false,
                menuDisabled: true,

                dragEnabled: true,
                materialIcon: 'more_vert'
            });
        }

        // set action columns
        this.columns = this.columns.concat(actionColumns);

        this.callParent(arguments);
    }
});