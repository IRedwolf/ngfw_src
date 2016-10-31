/**
 * $Id: ReportEntry.java,v 1.00 2015/02/24 15:19:32 dmorris Exp $
 */
package com.untangle.node.reports;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONString;

/**
 * The settings for an individual report entry (graph)
 */
@SuppressWarnings("serial")
public class EmailProfile implements JSONString, Serializable
{
    private static final Logger logger = Logger.getLogger( EmailProfile.class );

    private Integer profileId;
    private String title;
    private String description;
    private Boolean readOnly = null; /* If the rule is read-only (built-in) */
    private List<String> enabledConfigIds;
    private List<String> enabledAppIds;

    public EmailProfile()
    {
    }

    public EmailProfile( String title, String description, List<String> enabledConfigIds, List<String> enabledAppIds)
    {
        this.setTitle( title );
        this.setDescription( description );
        this.setEnabledConfigIds( enabledConfigIds );
        this.setEnabledAppIds( enabledAppIds );
    }
    
    public Integer getProfileId() { return this.profileId; }
    public void setProfileId( Integer newValue ) { this.profileId = newValue; }

    public String getTitle() { return this.title; }
    public void setTitle( String newValue ) { this.title = newValue; }

    public String getDescription() { return this.description; }
    public void setDescription( String newValue ) { this.description = newValue; }

    public Boolean getReadOnly() { return this.readOnly; }
    public void setReadOnly( Boolean newValue ) { this.readOnly = newValue; }
    
     public List<String> getEnabledConfigIds() { return this.enabledConfigIds; }
    public void setEnabledConfigIds( List<String> newValue ) { this.enabledConfigIds = newValue; }

    public List<String> getEnabledAppIds() { return this.enabledAppIds; }
    public void setEnabledAppIds( List<String> newValue ) { this.enabledAppIds = newValue; }

    public String toJSONString()
    {
        JSONObject jO = new JSONObject(this);
        return jO.toString();
    }

    
}
