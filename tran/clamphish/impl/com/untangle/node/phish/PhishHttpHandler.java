/*
 * Copyright (c) 2003-2007 Untangle, Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Untangle, Inc. ("Confidential Information"). You shall
 * not disclose such Confidential Information.
 *
 * $Id: SpywareHttpHandler.java 8668 2007-01-29 19:17:09Z amread $
 */

package com.untangle.node.phish;

import java.net.InetAddress;
import java.net.URI;

import com.untangle.uvm.tapi.TCPSession;
import com.untangle.node.http.HttpStateMachine;
import com.untangle.node.http.RequestLineToken;
import com.untangle.node.http.StatusLine;
import com.untangle.node.token.Chunk;
import com.untangle.node.token.Header;
import com.untangle.node.token.Token;
import com.untangle.node.util.UrlDatabaseResult;
import org.apache.log4j.Logger;

public class PhishHttpHandler extends HttpStateMachine
{
    private final Logger logger = Logger.getLogger(getClass());

    private final PhishNode node;

    // constructors -----------------------------------------------------------

    PhishHttpHandler(TCPSession session, PhishNode node)
    {
        super(session);

        this.node = node;
    }

    // HttpStateMachine methods -----------------------------------------------

    @Override
    protected RequestLineToken doRequestLine(RequestLineToken requestLine)
    {
        String path = requestLine.getRequestUri().getPath();

        return requestLine;
    }

    @Override
    protected Header doRequestHeader(Header requestHeader)
    {
        RequestLineToken rlToken = getRequestLine();
        URI uri = rlToken.getRequestUri();

        // XXX this code should be factored out
        String host = uri.getHost();
        if (null == host) {
            host = requestHeader.getValue("host");
            if (null == host) {
                InetAddress clientIp = getSession().clientAddr();
                host = clientIp.getHostAddress();
            }
        }
        host = host.toLowerCase();

        // XXX yuck
        UrlDatabaseResult result;
        if (node.isWhitelistedDomain(host, getSession().clientAddr())) {
            result = null;
        } else {
            result = node.getUrlDatabase()
                .search(getSession(), uri, requestHeader);
        }

        if (null != result) {
            // XXX fire off event
            if (result.blacklisted()) {
                // XXX change this category value
                node.logHttp(new PhishHttpEvent(rlToken.getRequestLine(), Action.BLOCK, "Google Safe Browsing"));

                InetAddress clientIp = getSession().clientAddr();

                PhishBlockDetails bd = new ClamPhishBlockDetails
                    (host, uri.toString(), clientIp);

                Token[] r = node.generateResponse(bd, getSession(),
                                                       isRequestPersistent());

                blockRequest(r);
                return requestHeader;
            } // else log Action.PASS now
        } // else log Action.PASS now

        releaseRequest();
        return requestHeader;
    }

    @Override
    protected Chunk doRequestBody(Chunk chunk)
    {
        return chunk;
    }

    @Override
    protected void doRequestBodyEnd() { }

    @Override
    protected StatusLine doStatusLine(StatusLine statusLine)
    {
        releaseResponse();
        return statusLine;
    }

    @Override
    protected Header doResponseHeader(Header header)
    {
        return header;
    }

    @Override
    protected Chunk doResponseBody(Chunk chunk)
    {
        return chunk;
    }

    @Override
    protected void doResponseBodyEnd()
    {
    }
}
