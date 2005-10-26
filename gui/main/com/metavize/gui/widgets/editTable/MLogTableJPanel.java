/*
 * Copyright (c) 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 * $Id: MLogTableJPanel.java 194 2005-04-06 19:13:55Z inieves $
 */

package com.metavize.gui.widgets.editTable;

import com.metavize.gui.widgets.coloredTable.*;
import com.metavize.gui.widgets.dialogs.RefreshLogFailureDialog;
import com.metavize.gui.transform.*;
import com.metavize.gui.util.*;

import com.metavize.mvvm.tran.Transform;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public abstract class MLogTableJPanel extends javax.swing.JPanel implements Shutdownable, ComponentListener {

    protected static final String ALL_EVENTS_STRING = "All events";    

    protected Object settings;

    protected static MLogTableJPanel lastMLogTableJPanel;
    private static final long STREAM_SLEEP_MILLIS = 15000l;    
    private static final Color TABLE_BACKGROUND_COLOR = new Color(213, 213, 226);
    
    protected Transform logTransform;
    protected MTransformControlsJPanel mTransformControlsJPanel;

    
    public MLogTableJPanel(Transform logTransform, MTransformControlsJPanel mTransformControlsJPanel) {
        this.logTransform = logTransform;
	this.mTransformControlsJPanel = mTransformControlsJPanel;
	try{
	    SwingUtilities.invokeAndWait( new Runnable(){ public void run(){
		// INIT GUI & CUSTOM INIT
		initComponents();
		entryJScrollPane.getViewport().setOpaque(true);
		entryJScrollPane.getViewport().setBackground(TABLE_BACKGROUND_COLOR);
		entryJScrollPane.setViewportBorder(new MatteBorder(2, 2, 2, 1, TABLE_BACKGROUND_COLOR));
		addComponentListener(MLogTableJPanel.this);
		Dictionary dictionary = depthJSlider.getLabelTable();
		Enumeration enumeration = dictionary.elements();
		while(enumeration.hasMoreElements()){
		    Object object = enumeration.nextElement();
		    if(object instanceof JComponent) ((JComponent)object).setFont(new Font("Dialog", 0, 9));
		}
		depthJSlider.putClientProperty("JSlider.isFilled", Boolean.TRUE);
                queryJComboBox.addItem(ALL_EVENTS_STRING);
	    }});
	}
	catch(Exception e){ Util.handleExceptionNoRestart("Error building Log Table", e); }
    }

    public void doShutdown(){
	if( refreshThread != null )
	    refreshThread.interrupt();
    }

    protected abstract void refreshSettings();
    
    public void setTableModel(MSortedTableModel mSortedTableModel){
        entryJTable.setModel( mSortedTableModel );
        entryJTable.setColumnModel( mSortedTableModel.getTableColumnModel() );
        mSortedTableModel.setTableHeader( entryJTable.getTableHeader() );
        mSortedTableModel.hideColumns( entryJTable );
    }
    
    public MSortedTableModel getTableModel(){
        return (MSortedTableModel) entryJTable.getModel();
    }
    

    public MColoredJTable getJTable(){
        return (MColoredJTable) entryJTable;
    }
        
    protected Vector<Vector> generateRows(Object settings) {
        return null;
    }

    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentShown(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
	((MColoredJTable)entryJTable).doGreedyColumn(entryJScrollPane.getViewport().getExtentSize().width);
    }
    
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        contentJPanel = new javax.swing.JPanel();
        eventJPanel = new javax.swing.JPanel();
        queryJComboBox = new javax.swing.JComboBox();
        refreshLogJButton = new javax.swing.JButton();
        streamingJToggleButton = new javax.swing.JToggleButton();
        entryJScrollPane = new javax.swing.JScrollPane();
        entryJTable = new MColoredJTable();
        depthJSlider = new javax.swing.JSlider();

        setLayout(new java.awt.GridBagLayout());

        setOpaque(false);
        contentJPanel.setLayout(new java.awt.GridBagLayout());

        contentJPanel.setOpaque(false);
        eventJPanel.setLayout(new java.awt.GridBagLayout());

        eventJPanel.setFocusCycleRoot(true);
        eventJPanel.setFocusable(false);
        eventJPanel.setOpaque(false);
        queryJComboBox.setFont(new java.awt.Font("Dialog", 0, 12));
        queryJComboBox.setFocusable(false);
        queryJComboBox.setMinimumSize(new java.awt.Dimension(230, 25));
        queryJComboBox.setPreferredSize(new java.awt.Dimension(230, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        eventJPanel.add(queryJComboBox, gridBagConstraints);

        refreshLogJButton.setFont(new java.awt.Font("Dialog", 0, 12));
        refreshLogJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/images/Button_Refresh_Log_106x17.png")));
        refreshLogJButton.setDoubleBuffered(true);
        refreshLogJButton.setFocusPainted(false);
        refreshLogJButton.setFocusable(false);
        refreshLogJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshLogJButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        refreshLogJButton.setMaximumSize(new java.awt.Dimension(100, 25));
        refreshLogJButton.setMinimumSize(new java.awt.Dimension(100, 25));
        refreshLogJButton.setPreferredSize(new java.awt.Dimension(100, 25));
        refreshLogJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshLogJButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        eventJPanel.add(refreshLogJButton, gridBagConstraints);

        streamingJToggleButton.setFont(new java.awt.Font("Dialog", 0, 12));
        streamingJToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/metavize/gui/images/Button_Start_Auto_Refresh_106x17.png")));
        streamingJToggleButton.setFocusPainted(false);
        streamingJToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        streamingJToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        streamingJToggleButton.setMaximumSize(new java.awt.Dimension(125, 25));
        streamingJToggleButton.setMinimumSize(new java.awt.Dimension(125, 25));
        streamingJToggleButton.setPreferredSize(new java.awt.Dimension(125, 25));
        streamingJToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                streamingJToggleButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        eventJPanel.add(streamingJToggleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        contentJPanel.add(eventJPanel, gridBagConstraints);

        entryJScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        entryJScrollPane.setDoubleBuffered(true);
        entryJTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        entryJTable.setDoubleBuffered(true);
        entryJScrollPane.setViewportView(entryJTable);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        contentJPanel.add(entryJScrollPane, gridBagConstraints);

        depthJSlider.setFont(new java.awt.Font("Dialog", 0, 12));
        depthJSlider.setMajorTickSpacing(50);
        depthJSlider.setMaximum(500);
        depthJSlider.setMinimum(50);
        depthJSlider.setMinorTickSpacing(25);
        depthJSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        depthJSlider.setPaintLabels(true);
        depthJSlider.setPaintTicks(true);
        depthJSlider.setSnapToTicks(true);
        depthJSlider.setToolTipText("<html>\n<b>Event Log Depth Slider</b><br>\nThis slider allows you to specify the maximum number of visible events<br>\nwhen the \"Refresh Log\" or \"Start Auto-refresh\" buttons are pressed.</html>");
        depthJSlider.setValue(100);
        depthJSlider.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        depthJSlider.setDoubleBuffered(true);
        depthJSlider.setFocusable(false);
        depthJSlider.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 10);
        contentJPanel.add(depthJSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 2, 2);
        add(contentJPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private RefreshThread refreshThread;
    
    private void streamingJToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streamingJToggleButtonActionPerformed
       if(streamingJToggleButton.isSelected()){
	   if( (lastMLogTableJPanel != null) && (lastMLogTableJPanel != this) && (lastMLogTableJPanel.streamingJToggleButton.isSelected()) )
	       lastMLogTableJPanel.streamingJToggleButton.doClick();
           refreshLogJButton.setEnabled(false); 
           refreshThread = new RefreshThread(true);
           streamingJToggleButton.setIcon(Util.getButtonStopAutoRefresh());
	   lastMLogTableJPanel = this;
       }
       else{
           refreshLogJButton.setEnabled(true);
           refreshThread.interrupt();
	   refreshThread = null;
           streamingJToggleButton.setIcon(Util.getButtonStartAutoRefresh());
       }
    }//GEN-LAST:event_streamingJToggleButtonActionPerformed

    private void refreshLogJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshLogJButtonActionPerformed
	if( (lastMLogTableJPanel != null) && (lastMLogTableJPanel != this) && (lastMLogTableJPanel.streamingJToggleButton.isSelected()) )
	    lastMLogTableJPanel.streamingJToggleButton.doClick();
	new RefreshThread(false);
    }//GEN-LAST:event_refreshLogJButtonActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentJPanel;
    protected javax.swing.JSlider depthJSlider;
    protected javax.swing.JScrollPane entryJScrollPane;
    protected javax.swing.JTable entryJTable;
    private javax.swing.JPanel eventJPanel;
    protected javax.swing.JComboBox queryJComboBox;
    private javax.swing.JButton refreshLogJButton;
    private javax.swing.JToggleButton streamingJToggleButton;
    // End of variables declaration//GEN-END:variables
    
    private Exception refreshException;
    class RefreshThread extends Thread implements ActionListener {
	private boolean isAutoRefresh;
	public RefreshThread(boolean isAutoRefresh){
	    super("MVCLIENT-MLogTableJPanel.RefreshThread: " + logTransform.getTransformDesc().getDisplayName());
	    setContextClassLoader(Util.getClassLoader());
	    this.isAutoRefresh = isAutoRefresh;
	    if( !isAutoRefresh // a button was pressed, stop other streaming
		&& (MLogTableJPanel.this.lastMLogTableJPanel != null)
		&& (MLogTableJPanel.this.lastMLogTableJPanel != MLogTableJPanel.this)
		&& (MLogTableJPanel.this.lastMLogTableJPanel.streamingJToggleButton.isSelected()) )
		MLogTableJPanel.this.lastMLogTableJPanel.streamingJToggleButton.doClick();
	    if(!isAutoRefresh){
		MLogTableJPanel.this.streamingJToggleButton.setEnabled(false);
		MLogTableJPanel.this.refreshLogJButton.setEnabled(false);
		MLogTableJPanel.this.refreshLogJButton.setIcon(Util.getButtonRefreshing());
	    }
	    this.start();
	}
	
	public void actionPerformed(ActionEvent actionEvent){
	    JToggleButton controlsJToggleButton = (JToggleButton) actionEvent.getSource();
	    if( !controlsJToggleButton.isSelected() ){
		MLogTableJPanel.this.lastMLogTableJPanel.streamingJToggleButton.doClick();
	    }
	}
	public void run(){
	    try{
		if(isAutoRefresh){
		    MLogTableJPanel.this.mTransformControlsJPanel.getControlsJToggleButton().addActionListener(this);
		}
		do{
		    refreshSettings();
		    refreshException = null;
		    SwingUtilities.invokeAndWait( new Runnable(){ public void run(){
			try{
			    getTableModel().doRefresh(settings);
			}
			catch(Exception f){
			    refreshException = f;
			}
		    }});
		    if( refreshException != null)
			throw refreshException;
		    if(isAutoRefresh){
			this.sleep(STREAM_SLEEP_MILLIS);
		    }
		}
		while(isAutoRefresh);
	    }
	    catch(InterruptedException e){
		// this is normal, means the thread was stopped by pressing the toggle button, or doShutdown
	    }
	    catch(Exception g){
		try{
		    Util.handleExceptionWithRestart("Error refreshing event log", g);
		}
		catch(Exception h){
		    Util.handleExceptionNoRestart("Error refreshing event log", h);
		    new RefreshLogFailureDialog( logTransform.getTransformDesc().getDisplayName() );
		}
	    }
	    finally{
		if(!isAutoRefresh){ // the case where the toggle was stopped by a button press
		    SwingUtilities.invokeLater( new Runnable(){ public void run(){
			MLogTableJPanel.this.streamingJToggleButton.setEnabled(true);
			MLogTableJPanel.this.refreshLogJButton.setEnabled(true);
			MLogTableJPanel.this.refreshLogJButton.setIcon(Util.getButtonRefreshLog());
		    }});
		}
		else{
		    MLogTableJPanel.this.mTransformControlsJPanel.getControlsJToggleButton().removeActionListener(this);
		}
	    }
	}
    }
    
}
