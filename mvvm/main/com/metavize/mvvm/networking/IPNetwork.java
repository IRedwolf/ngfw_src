/*
 * Copyright (c) 2003, 2004, 2005 Metavize Inc.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Metavize Inc. ("Confidential Information").  You shall
 * not disclose such Confidential Information.
 *
 *  $Id$
 */

package com.metavize.mvvm.networking;

import java.io.Serializable;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.UnknownHostException;

import com.metavize.mvvm.tran.IPaddr;

import com.metavize.mvvm.tran.ParseException;

public class IPNetwork implements Serializable
{
    private static final String MARKER_NETMASK = "/";
    private static final IPNetwork EMPTY_IPNETWORK;
    
    private final IPaddr network;
    private final IPaddr netmask;
    private final String user;

    /* XXX Perhaps this should be stored in CIDR notation */
    private IPNetwork( IPaddr network, IPaddr netmask, String user )
    {
        this.network = network;
        this.netmask = netmask;
        this.user = user;
    }

    public IPaddr getNetwork()
    {
        return this.network;
    }

    public IPaddr getNetmask()
    {
        return this.netmask;
    }

    public boolean isUnicast()
    {
        return  NetworkUtil.getInstance().isUnicast( this );
    }

    public String toString()
    {
        return this.user;
    }

    public static IPNetwork parse( String value ) throws ParseException
    {
        value = value.trim();

        String ipArray[] = value.split( MARKER_NETMASK );
        if ( ipArray.length != 2 ) {
            throw new ParseException( "IP Network contains two components: " + value );
        }
        
        try {
            String cidr = ipArray[1].trim();
            if ( cidr.length() < 3 ) {
                return new IPNetwork( IPaddr.parse( ipArray[0] ), IPaddr.cidrToIPaddr( cidr ), value );
            } else {
                return new IPNetwork( IPaddr.parse( ipArray[0] ), IPaddr.parse( ipArray[1] ), value );
            }
        } catch ( UnknownHostException e ) {
            throw new ParseException( e );
        }
    }

    public static IPNetwork makeInstance( InetAddress network, InetAddress netmask )
    {
        return makeInstance( new IPaddr((Inet4Address)network), new IPaddr((Inet4Address)netmask));
    }

    public static IPNetwork makeInstance( IPaddr network, IPaddr netmask )
    {
        String user = network + "/" + netmask;
        return new IPNetwork( network, netmask, user );
    }

    public static IPNetwork getEmptyNetwork()
    {
        return EMPTY_IPNETWORK;
    }

    static
    {
        Inet4Address EMPTY;
        try {
            EMPTY = (Inet4Address)InetAddress.getByName( "0.0.0.0" );
        } catch ( Exception e ) {
            System.err.println( "Unable to parse empty IP address, this is bad" );
            EMPTY = null;
        }

        EMPTY_IPNETWORK = new IPNetwork( new IPaddr( EMPTY ), new IPaddr( EMPTY ), "0.0.0.0/0" );
    }
}
