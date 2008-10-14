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

package com.untangle.uvm.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.ServiceUnavailableException;

import org.hibernate.Query;
import org.hibernate.Session;

import com.untangle.uvm.LocalUvmContextFactory;
import com.untangle.uvm.addrbook.AddressBookConfiguration;
import com.untangle.uvm.addrbook.AddressBookSettings;
import com.untangle.uvm.addrbook.NoSuchEmailException;
import com.untangle.uvm.addrbook.RemoteAddressBook;
import com.untangle.uvm.addrbook.RepositorySettings;
import com.untangle.uvm.addrbook.RepositoryType;
import com.untangle.uvm.addrbook.UserEntry;

import com.untangle.uvm.license.ProductIdentifier;

import com.untangle.uvm.util.TransactionWork;
import org.apache.log4j.Logger;


/**
 * Concrete implementation of the AddressBook.  Note that this class
 * should only be used by classes in "engine" (the parent package).
 *
 */
class DefaultAddressBookImpl implements RemoteAddressBook {

    private static final ABStatus STATUS_NOT_WORKING = new ABStatus( false, "unconfigured" );
    
    private AddressBookSettings m_settings;
    private LocalLdapAdapter m_localAdapter;

    private final Logger m_logger =
        Logger.getLogger(getClass());

    DefaultAddressBookImpl() {
        TransactionWork<AddressBookSettings> work = new TransactionWork<AddressBookSettings>() {
            private AddressBookSettings settings;
            
            public boolean doWork(org.hibernate.Session s) {
                Query q = s.createQuery("from AddressBookSettings");
                settings = (AddressBookSettings)q.uniqueResult();

                if(settings == null) {
                    m_logger.info("creating new AddressBookSettings");
                    settings = new AddressBookSettings();
                    settings.setAddressBookConfiguration(AddressBookConfiguration.LOCAL_ONLY);
                    settings.setADRepositorySettings(new RepositorySettings("Administrator",
                                                                            "mypassword",
                                                                            "mydomain",
                                                                            "ad_server",
                                                                            389));
                    s.save(settings);
                }
                return true;
            }

            public AddressBookSettings getResult() { return settings; }
        };
        
        LocalUvmContextFactory.context().runTransaction(work);

        m_settings =  work.getResult();

        //We create the local adapter regardless
        m_localAdapter = new LocalLdapAdapter();
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public AddressBookSettings getAddressBookSettings() {
        return m_settings;
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public void setAddressBookSettings(final AddressBookSettings newSettings) {
        m_settings = newSettings;

        //Hibernate stuff
        TransactionWork tw = new TransactionWork()
            {
                public boolean doWork(Session s)
                {
                    s.saveOrUpdate(m_settings);
                    return true;
                }

                public Object getResult() { return null; }
            };
        LocalUvmContextFactory.context().runTransaction(tw);
    }


    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public boolean authenticate(String uid, String pwd)
        throws ServiceUnavailableException {
        m_logger.info("authenticating against local directory.");

        return isNotConfigured() ? false : m_localAdapter.authenticate(uid, pwd);
    }

    public static class ABStatus implements RemoteAddressBook.Status, Serializable {
        private final boolean isLocalWorking;
        private final String localDetail;

        private ABStatus(boolean isLocalWorking, String localDetail)
        {
            this.isLocalWorking = isLocalWorking;
            this.localDetail = localDetail;
        }

        public boolean isLocalWorking() { return this.isLocalWorking; }
        public boolean isADWorking() { return false; }
        public String localDetail() { return localDetail; }
        public String adDetail() { return "unconfigured"; }
    }

    public Status getStatus() {
        if (m_settings.getAddressBookConfiguration() == AddressBookConfiguration.NOT_CONFIGURED) {
            return STATUS_NOT_WORKING;
        }
        
        try {
            List<UserEntry> localRet = m_localAdapter.listAll();
            return new ABStatus(true, "working, " + localRet.size() + " users found");
        } catch (Exception x) {
            return new ABStatus(false, x.getMessage());
        }
    }


    public Status getStatusForSettings(AddressBookSettings newSettings) {
        return this.getStatus();
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public boolean authenticateByEmail(String email, String pwd)
        throws ServiceUnavailableException, NoSuchEmailException {

        m_logger.info("authenticate against the local repository.");
        return isNotConfigured() ? false : m_localAdapter.authenticateByEmail(email, pwd);
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public RepositoryType containsEmail(String address, RepositoryType searchIn)
        throws ServiceUnavailableException {
        m_logger.info("containsEmail in default address book.");

        switch(searchIn) {
            //---------------------------------------
        case LOCAL_DIRECTORY:
            if (!isNotConfigured())
                return m_localAdapter.getEntryByEmail(address) == null ?
                    RepositoryType.NONE : RepositoryType.LOCAL_DIRECTORY;

        case MS_ACTIVE_DIRECTORY:
            // fallthrough
            //---------------------------------------
        case NONE:
        default:
            break;
        }

        return RepositoryType.NONE;
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public RepositoryType containsEmail(String address)
        throws ServiceUnavailableException {
        m_logger.info("containsEmail <" + address + ">.");

        return isNotConfigured()?
                RepositoryType.NONE:
                m_localAdapter.getEntryByEmail(address) == null?
                RepositoryType.NONE:RepositoryType.LOCAL_DIRECTORY;
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public RepositoryType containsUid(String uid, RepositoryType searchIn)
        throws ServiceUnavailableException {
        m_logger.info("containsUid <" + uid + ">.");

        switch(searchIn) {
            //---------------------------------------
        case LOCAL_DIRECTORY:
            return isNotConfigured()?
                RepositoryType.NONE:
                m_localAdapter.containsUser(uid)?
                RepositoryType.LOCAL_DIRECTORY:RepositoryType.NONE;

        case MS_ACTIVE_DIRECTORY:
            // fallthrough
            //---------------------------------------
        case NONE:
        default:
            return RepositoryType.NONE;
        }
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public RepositoryType containsUid(String uid)
        throws ServiceUnavailableException {
        m_logger.info("containsUid <" + uid + ">.");

        return isNotConfigured()?
            RepositoryType.NONE:
            m_localAdapter.containsUser(uid)?
            RepositoryType.LOCAL_DIRECTORY:RepositoryType.NONE;
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public List<UserEntry> getLocalUserEntries()
        throws ServiceUnavailableException {
        m_logger.info("getLocalUserEntries local.");

        if(isNotConfigured()) {
            return new ArrayList<UserEntry>();
        } else {
            return m_localAdapter.listAll();
        }
    }


    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public void setLocalUserEntries(List<UserEntry> userEntries)
        throws ServiceUnavailableException, NameNotFoundException, NameAlreadyBoundException {
        // compute the add/delete/keep lists
        HashMap<UserEntry,UserEntry> currentEntries = new HashMap<UserEntry,UserEntry>();
        for( UserEntry userEntry : getLocalUserEntries() )
            currentEntries.put(userEntry,userEntry);
        List<UserEntry> keepList = new ArrayList<UserEntry>();
        List<UserEntry> addList = new ArrayList<UserEntry>();
        for( UserEntry userEntry : userEntries ){
            UserEntry foundEntry = currentEntries.remove(userEntry);
            if( foundEntry != null )
                keepList.add(userEntry);
            else
                addList.add(userEntry);
        }
        // perform the add/removes
        for( UserEntry userEntry : keepList ){
            updateLocalEntry(userEntry);
            if (!UserEntry.UNCHANGED_PASSWORD.equals(userEntry.getPassword()))
                updateLocalPassword(userEntry.getUID(), userEntry.getPassword());
        }
        for( UserEntry userEntry : currentEntries.keySet())
            deleteLocalEntry(userEntry.getUID());
        for( UserEntry userEntry : addList ){
            String password = userEntry.getPassword();
            userEntry.setPassword(null);
            createLocalEntry(userEntry,password);
        }
    }


    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public List<UserEntry> getUserEntries()
        throws ServiceUnavailableException {
        m_logger.info("getUserEntries");
        
        return isNotConfigured()?
            new ArrayList<UserEntry>():
            m_localAdapter.listAll();
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public List<UserEntry> getUserEntries(RepositoryType searchIn)
        throws ServiceUnavailableException {
        m_logger.info("getUserEntries <" + searchIn + ">");

        switch(searchIn) {
            //---------------------------------------
        case LOCAL_DIRECTORY:
            return isNotConfigured()?
                new ArrayList<UserEntry>():
                m_localAdapter.listAll();

            //---------------------------------------
        case MS_ACTIVE_DIRECTORY:
            // fallthrough
            //---------------------------------------
        case NONE:
        default:
            break;
        }

        return new ArrayList<UserEntry>();
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public UserEntry getEntry(String uid)
        throws ServiceUnavailableException {
        m_logger.info("getEntry <" + uid + ">");

        return isNotConfigured()?
            null : m_localAdapter.getEntry(uid);
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public UserEntry getEntry(String uid, RepositoryType searchIn)
        throws ServiceUnavailableException {
        m_logger.info("getEntry <" + uid + ">");

        switch(searchIn) {
            //---------------------------------------
        case LOCAL_DIRECTORY:
            if(!isNotConfigured()) {
                return m_localAdapter.getEntry(uid);
            }
            //---------------------------------------
        case MS_ACTIVE_DIRECTORY:
            // fallthrough
            //---------------------------------------
        case NONE:
        default:
            break;
        }
        return null;
    }

    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public UserEntry getEntryByEmail(String email)
        throws ServiceUnavailableException {
        m_logger.info("getEntryByEmail <" + email + ">" );

        if(!isNotConfigured()) {
            return m_localAdapter.getEntryByEmail(email);
        }
        return null;
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public UserEntry getEntryByEmail(String email, RepositoryType searchIn)
        throws ServiceUnavailableException {
        m_logger.info("getEntryByEmail <" + email + "," + searchIn + ">" );

        switch(searchIn) {
            //---------------------------------------
        case LOCAL_DIRECTORY:
            if(!isNotConfigured()) {
                return m_localAdapter.getEntryByEmail(email);
            }
            break;

            //---------------------------------------
        case MS_ACTIVE_DIRECTORY:
            //---------------------------------------
        case NONE:
        default:
            break;
        }
        return null;
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public void createLocalEntry(UserEntry newEntry, String password)
        throws NameAlreadyBoundException, ServiceUnavailableException {
        m_localAdapter.createUserEntry(newEntry, password);
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public boolean deleteLocalEntry(String entryUid)
        throws ServiceUnavailableException {
        return m_localAdapter.deleteUserEntry(entryUid);
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public void updateLocalEntry(UserEntry changedEntry)
        throws ServiceUnavailableException, NameNotFoundException {
        m_localAdapter.modifyUserEntry(changedEntry, null);
    }



    //====================================================
    // See doc on com.untangle.uvm.addrbook.AddressBook
    //====================================================
    public void updateLocalPassword(String uid, String newPassword)
        throws ServiceUnavailableException, NameNotFoundException {
        m_localAdapter.changePassword(uid, newPassword);
    }

    public String productIdentifier()
    {
        return ProductIdentifier.ADDRESS_BOOK;
    }

    private boolean isNotConfigured() {
        return m_settings.getAddressBookConfiguration() ==
            AddressBookConfiguration.NOT_CONFIGURED;
    }
}


