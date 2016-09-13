/*
 * $Id$
 */
package com.untangle.node.web_filter_lite;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.untangle.uvm.UvmContextFactory;
import com.untangle.uvm.node.NodeManager;
import com.untangle.uvm.node.NodeSettings;
import com.untangle.node.web_filter.WebFilter;

@SuppressWarnings("serial")
public class UnblockerServlet extends HttpServlet
{
    // HttpServlet methods ----------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException
    {
        resp.setContentType("text/xml");
        resp.addHeader("Cache-Control", "no-cache");

        String nonce = req.getParameter("nonce");
        String appidStr = req.getParameter("appid");
        boolean global = Boolean.parseBoolean(req.getParameter("global"));

        try {
            NodeManager tman = UvmContextFactory.context().nodeManager();
            WebFilter tran = (WebFilter) tman.node( Long.parseLong(appidStr) );

            if (tran.unblockSite(nonce, global)) {
                resp.getOutputStream().println("<success/>");
            } else {
                resp.getOutputStream().println("<failure/>");
            }
        } catch (IOException exn) {
            throw new ServletException(exn);
        }
    }
}

