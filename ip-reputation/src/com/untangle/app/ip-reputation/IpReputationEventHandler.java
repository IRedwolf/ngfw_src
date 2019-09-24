/**
 * $Id: EventHandler.java,v 1.00 2018/05/10 20:44:51 dmorris Exp $
 */
package com.untangle.app.ip_reputation;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import com.untangle.uvm.app.IPMaskedAddress;
import com.untangle.uvm.vnet.AbstractEventHandler;
import com.untangle.uvm.vnet.IPNewSessionRequest;
import com.untangle.uvm.vnet.Protocol;
import com.untangle.uvm.vnet.AppSession;
import com.untangle.uvm.vnet.TCPNewSessionRequest;
import com.untangle.uvm.vnet.UDPNewSessionRequest;
import com.untangle.app.webroot.WebrootQuery;

/**
 * The ip reputation event handler
 */
public class IpReputationEventHandler extends AbstractEventHandler
{
    private final Logger logger = Logger.getLogger(IpReputationEventHandler.class);

    private List<IpReputationPassRule> ipReputationPassRuleList = new LinkedList<>();

    private boolean blockSilently = true;

    /* IP Reputation App */
    private final IpReputationApp app;

    /**
     * Create a new EventHandler.
     * @param app - the containing ip reputation app
     */
    public IpReputationEventHandler( IpReputationApp app )
    {
        super(app);

        this.app = app;
    }

    /**
     * Handle a new TCP session
     * @param sessionRequest
     */
    public void handleTCPNewSessionRequest( TCPNewSessionRequest sessionRequest )
    {
        handleNewSessionRequest( sessionRequest, Protocol.TCP );
    }

    /**
     * Handle a new UDP session
     * @param sessionRequest
     */
    public void handleUDPNewSessionRequest( UDPNewSessionRequest sessionRequest )
    {
        handleNewSessionRequest( sessionRequest, Protocol.UDP );
    }

    /**
     * Handle a new session
     * @param request
     * @param protocol
     */
    private void handleNewSessionRequest( IPNewSessionRequest request, Protocol protocol )
    {
        boolean block = app.getSettings().getAction().equals("block");
        boolean flag = app.getSettings().getFlag();
        int ruleIndex     = 0;

        if ( Boolean.TRUE == request.globalAttachment( AppSession.KEY_FTP_DATA_SESSION) ) {
            logger.info("Passing FTP related session: " + request);
            return;
        }

        boolean localSrc = false;
        boolean localDst = false;
        for(IPMaskedAddress address : app.localNetworks){
            localSrc = address.contains(request.getOrigClientAddr());
            localDst = address.contains(request.getNewServerAddr());
        }

        long lookupTimeBegin = System.currentTimeMillis();
        JSONArray answer = null;
        if(!localSrc && !localDst){
            answer = app.webrootQuery.ipGetInfo(request.getOrigClientAddr().getHostAddress(), request.getNewServerAddr().getHostAddress());
        }else if(!localSrc){
            answer = app.webrootQuery.ipGetInfo(request.getOrigClientAddr().getHostAddress());
        }else if(!localDst){
            answer = app.webrootQuery.urlGetInfo(request.getNewServerAddr().getHostAddress());
        }
        app.adjustLookupAverage(System.currentTimeMillis() - lookupTimeBegin);

        JSONObject ipInfo = null;
        if(answer != null){
            String ip = null;
            try{
                for(int i = 0; i < answer.length(); i++){
                    ipInfo = answer.getJSONObject(i);
                    if(ipInfo.has(WebrootQuery.BCTI_API_DAEMON_RESPONSE_IPINFO_IP_KEY)){
                        ip = ipInfo.getString(WebrootQuery.BCTI_API_DAEMON_RESPONSE_IPINFO_IP_KEY);
                        if(!localSrc && ip.equals(request.getOrigClientAddr().getHostAddress())){
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_CLIENT_REPUTATION, ipInfo.getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_IPINFO_REPUTATION_KEY));
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_CLIENT_THREATMASK, ipInfo.getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_IPINFO_THREATMASK_KEY));
                        }else if(!localDst && ip.equals(request.getNewServerAddr().getHostAddress())){
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_SERVER_REPUTATION, ipInfo.getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_IPINFO_REPUTATION_KEY));
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_SERVER_THREATMASK, ipInfo.getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_IPINFO_THREATMASK_KEY));
                        }
                    }else if(ipInfo.has(WebrootQuery.BCTI_API_DAEMON_RESPONSE_URLINFO_URL_KEY)){
                        ip = ipInfo.getString(WebrootQuery.BCTI_API_DAEMON_RESPONSE_URLINFO_URL_KEY);
                        int threatmask = 0;

                        JSONArray catids = ipInfo.getJSONArray(WebrootQuery.BCTI_API_DAEMON_RESPONSE_URLINFO_CATEGORY_LIST_KEY);
                        for(int j = 0; j < catids.length(); j++){
                            Integer cat = catids.getJSONObject(i).getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_URLINFO_CATEGORY_ID_KEY);
                            if(IpReputationApp.UrlCatThreatMap.get(cat) != null){
                                threatmask += IpReputationApp.UrlCatThreatMap.get(cat);
                            }
                        }

                        if(!localSrc && ip.equals(request.getOrigClientAddr().getHostAddress())){
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_CLIENT_REPUTATION, ipInfo.getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_URLINFO_REPUTATION_KEY));
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_CLIENT_THREATMASK, threatmask);
                        }else if(!localDst && ip.equals(request.getNewServerAddr().getHostAddress())){
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_SERVER_REPUTATION, ipInfo.getInt(WebrootQuery.BCTI_API_DAEMON_RESPONSE_URLINFO_REPUTATION_KEY));
                            request.globalAttach(AppSession.KEY_IP_REPUTATION_SERVER_THREATMASK, threatmask);
                        }
                    }
                }
            }catch(Exception e){
                logger.warn("Cannot pull IP information ", e);
            }
        }

        Object clientReputation = request.globalAttachment(AppSession.KEY_IP_REPUTATION_CLIENT_REPUTATION);
        Object clientThreatmask = request.globalAttachment(AppSession.KEY_IP_REPUTATION_CLIENT_THREATMASK);
        Object serverReputation = request.globalAttachment(AppSession.KEY_IP_REPUTATION_SERVER_REPUTATION);
        Object serverThreatmask = request.globalAttachment(AppSession.KEY_IP_REPUTATION_SERVER_THREATMASK);

        Boolean match = false;

        match = ( ( ( serverReputation != null ) && (Integer) serverReputation > 0 && (Integer) serverReputation <= app.getSettings().getThreatLevel() ) )
                ||
                ( ( ( clientReputation != null ) && (Integer) clientReputation > 0 && (Integer) clientReputation <= app.getSettings().getThreatLevel() ) );

        for (IpReputationPassRule rule : ipReputationPassRuleList) {
            if( rule.isMatch(request.getProtocol(),
                            request.getClientIntf(), request.getServerIntf(),
                            request.getOrigClientAddr(), request.getNewServerAddr(),
                            request.getOrigClientPort(), request.getNewServerPort(),
                            request) ){
                block = !rule.getPass();
                flag = rule.getFlag();
                ruleIndex = rule.getRuleId();
                break;
            }
        }

        // !!!! VERIFY NO MATCH IF BCTID NOT RUNNING

        /**
         * Take the appropriate actions
         */
        if (match && block) {
            if (logger.isDebugEnabled()) {
                logger.debug("Blocking session: " + request);
            }

            if (blockSilently) {
                request.rejectSilently();
            } else {
                if (protocol == Protocol.UDP) {
                    request.rejectReturnUnreachable( IPNewSessionRequest.PORT_UNREACHABLE );
                } else {
                    ((TCPNewSessionRequest)request).rejectReturnRst();
                }
            }

            /* Increment the block counter and flag counter*/
            app.incrementBlockCount(); 
            if (flag) app.incrementFlagCount();

            /* We just blocked, so we have to log too, regardless of what the rule actually says */
            IpReputationEvent fwe = new IpReputationEvent(request.sessionEvent(), block && match, match && flag, ruleIndex, clientReputation != null ? (Integer) clientReputation : 0, clientThreatmask != null ? (Integer) clientThreatmask : 0, serverReputation != null ? (Integer) serverReputation : 0, serverThreatmask != null ? (Integer) serverThreatmask : 0);
            app.logEvent(fwe);

        } else { /* not blocked */
            if (logger.isDebugEnabled()) {
                logger.debug("Releasing session: " + request);
            }

            /* only finalize if logging */
            request.release();

            /* Increment the pass counter and flag counter */
            app.incrementPassCount();
            if (match && flag){
                app.incrementFlagCount();
            }

            /* If necessary log the event */
            IpReputationEvent fwe = new IpReputationEvent(request.sessionEvent(), false, flag && match, ruleIndex, clientReputation != null ? (Integer) clientReputation : 0, clientThreatmask != null ? (Integer) clientThreatmask : 0, serverReputation != null ? (Integer) serverReputation : 0, serverThreatmask != null ? (Integer) serverThreatmask : 0);
            app.logEvent(fwe);
        }
    }

    /**
     * Configure this event handler with the provided settings
     * @param settings
     */
    public void configure(IpReputationSettings settings)
    {
        this.ipReputationPassRuleList = settings.getPassRules();
    }

}
