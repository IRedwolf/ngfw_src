/**
 * $Id$
 */
package com.untangle.app.ad_blocker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONString;

import com.untangle.uvm.app.GenericRule;

/**
 * The settings for Ad Blocker
 */
@SuppressWarnings("serial")
public class AdBlockerSettings implements Serializable, JSONString
{
    private List<GenericRule> blockingRules = new LinkedList<GenericRule>();
    private List<GenericRule> userBlockingRules = new LinkedList<GenericRule>();
    private List<GenericRule> passingRules = new LinkedList<GenericRule>();
    private List<GenericRule> userPassingRules = new LinkedList<GenericRule>();
    private List<GenericRule> passedClients = new LinkedList<GenericRule>();
    private List<GenericRule> passedUrls = new LinkedList<GenericRule>();
    private List<GenericRule> cookies = new LinkedList<GenericRule>();
    private List<GenericRule> userCookies = new LinkedList<GenericRule>();
    
    private String lastUpdate;

    private Boolean scanCookies = Boolean.TRUE;
    private Boolean scanAds = Boolean.TRUE;

    public AdBlockerSettings() {
    }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
    
    public Boolean getScanCookies() { return scanCookies; }
    public void setScanCookies(Boolean scanCookies) { this.scanCookies = scanCookies; }
    
    public Boolean getScanAds() { return scanAds; }
    public void setScanAds(Boolean scanAds) { this.scanAds = scanAds; }

    public List<GenericRule> getPassedClients() { return passedClients; }
    public void setPassedClients(List<GenericRule> passedClients) { this.passedClients = passedClients; }

    public List<GenericRule> getPassedUrls() { return passedUrls; }
    public void setPassedUrls(List<GenericRule> passedUrls) { this.passedUrls = passedUrls; }
    
    public List<GenericRule> getCookies() { return cookies; }
    public void setCookies(List<GenericRule> cookies) { this.cookies = cookies; }

    public List<GenericRule> getUserCookies() { return userCookies; }
    public void setUserCookies(List<GenericRule> userCookies) { this.userCookies = userCookies; }

    public List<GenericRule> _getBlockingRules() { return blockingRules; }
    public void _setBlockingRules(List<GenericRule> blockingRules) { this.blockingRules = blockingRules; }

    public List<GenericRule> _getUserBlockingRules() { return userBlockingRules; }
    public void _setUserBlockingRules(List<GenericRule> newValue) { this.userBlockingRules = newValue; }

    public List<GenericRule> _getPassingRules() { return passingRules; }
    public void _setPassingRules(List<GenericRule> passingRules) { this.passingRules = passingRules; }

    public List<GenericRule> _getUserPassingRules() { return userPassingRules; }
    public void _setUserPassingRules(List<GenericRule> newValue) { this.userPassingRules = newValue; }

    public List<GenericRule> getUserRules()
    {
        List<GenericRule> result = new ArrayList<GenericRule>();
        result.addAll(userBlockingRules);
        result.addAll(userPassingRules);
        return result;
    }

    public void setUserRules(List<GenericRule> userRules)
    {
        userBlockingRules = new LinkedList<GenericRule>();
        userPassingRules = new LinkedList<GenericRule>();
        for (GenericRule r : userRules) {
            if (r.getBlocked() != null && !r.getBlocked()) {
                userPassingRules.add(r);
            } else {
                userBlockingRules.add(r);
            }
        }
    }

    public List<GenericRule> getRules()
    {
        List<GenericRule> result = new LinkedList<GenericRule>();
        result.addAll(blockingRules);
        result.addAll(passingRules);
        return result;
    }

    public void setRules(List<GenericRule> rules)
    {
        blockingRules = new LinkedList<GenericRule>();
        passingRules = new LinkedList<GenericRule>();
        for (GenericRule r : rules) {
            if (r.getBlocked() != null && !r.getBlocked()) {
                passingRules.add(r);
            } else {
                blockingRules.add(r);
            }
        }
    }

    public String toJSONString()
    {
        JSONObject jO = new JSONObject(this);
        return jO.toString();
    }
}
