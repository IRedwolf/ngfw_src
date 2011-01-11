/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.untangle.node.openvpn;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;

import com.untangle.uvm.node.IPAddress;
import com.untangle.uvm.node.ValidateException;

/**
 * A site network for a client.  Done this way so the client site
 * networks and the server site networks are in their own tables.
 *
 * @author <a href="mailto:rbscott@untangle.com">Robert Scott</a>
 * @version 1.0
 */
@Entity
@Table(name="n_openvpn_site_2", schema="settings")
@SuppressWarnings("serial")
public class VpnSite extends VpnClientBase
{
    private static final IPAddress EMPTY_ADDR = new IPAddress( null );

    // List of addresses at this site,
    // initially, may not be supported, just use one address.
    private List<ClientSiteNetwork>     exportedAddressList;

    public VpnSite()
    {
        /* XXXXXXXXXXXXXXXXXXXXXXXXXXXXX This should have all of the stuff
         * about exports in it, but for now just keep it in both places */
    }

    /**
     * The list of exported networks for this site.
     * Should rename the column from client_id to site_id.
     *
     * @return the list of exported networks for this site.
     */
    @OneToMany(fetch=FetchType.EAGER)
    @Cascade({ org.hibernate.annotations.CascadeType.ALL,
                   org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    @JoinColumn(name="client_id")
    @IndexColumn(name="position")
    public List<ClientSiteNetwork> getExportedAddressList()
    {
        if ( this.exportedAddressList == null ) this.exportedAddressList = new LinkedList<ClientSiteNetwork>();

        if (this.exportedAddressList != null) this.exportedAddressList.removeAll(java.util.Collections.singleton(null));
        return this.exportedAddressList;
    }

    public void setExportedAddressList( List<ClientSiteNetwork> exportedAddressList )
    {
        this.exportedAddressList = exportedAddressList;
    }

    /**
     * XXXX This is a convenience method that doesn't scale if there are
     * multiple networks and netmasks
     */
    @Transient
    public ClientSiteNetwork getSiteNetwork()
    {
        List<ClientSiteNetwork> list = getExportedAddressList();

        ClientSiteNetwork site;
        if ( list.size() < 1 ) {
            site = new ClientSiteNetwork();
            site.setNetwork( EMPTY_ADDR );
            site.setNetmask( EMPTY_ADDR );
            site.setLive( true );
            list.add( site );
        } else {
            site = list.get( 0 );
        }

        return site;
    }

    public void setSiteNetwork( IPAddress network, IPAddress netmask )
    {
    	List<ClientSiteNetwork>  list = getExportedAddressList();

        ClientSiteNetwork site;
        if ( list.size() < 1 ) {
            site = new ClientSiteNetwork();
            list.add( site );
        } else {
            site = list.get( 0 );
        }

        site.setLive( true );
        site.setNetwork( network );
        site.setNetmask( netmask );
    }

    public void validate() throws ValidateException
    {
        super.validate();

        if (( this.exportedAddressList == null ) || ( this.exportedAddressList.size()  == 0 )) {
            throw new ValidateException( "A site must have at least one exported address" );
        }
    }
}


