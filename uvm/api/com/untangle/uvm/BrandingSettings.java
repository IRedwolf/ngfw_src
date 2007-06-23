/*
 * $HeadURL$
 * Copyright (c) 2003-2007 Untangle, Inc. 
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2,
 * as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules,
 * and to copy and distribute the resulting executable under terms of your
 * choice, provided that you also meet, for each linked independent module,
 * the terms and conditions of the license of that module.  An independent
 * module is a module which is not derived from or based on this library.
 * If you modify this library, you may extend this exception to your version
 * of the library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.untangle.uvm;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Contains properties that a vendor may use to rebrand the product.
 *
 * @author <a href="mailto:amread@untangle.com">Aaron Read</a>
 * @version 1.0
 */
@Entity
@Table(name="uvm_branding_settings", schema="settings")
public class BrandingSettings implements Serializable
{
    private Long id;
    private String companyName = "Untangle";
    private String companyUrl = "http://www.untangle.com";
    private byte[] logo = null;
    private String contactName = "Your System Administrator";
    private String contactEmail = null;

    public BrandingSettings() { }

    @Id
    @Column(name="settings_id")
    @GeneratedValue
    Long getId()
    {
        return id;
    }

    void setId(Long id)
    {
        this.id = id;
    }

    /**
     * Get the vendor name.
     *
     * @return vendor name.
     */
    @Column(name="company_name")
    public String getCompanyName()
    {
        return null == companyName ? "Untangle" : companyName;
    }

    public void setCompanyName(String companyName)
    {
        this.companyName = companyName;
    }

    /**
     * Get the vendor URL.
     *
     * @return vendor url
     */
    @Column(name="company_url")
    public String getCompanyUrl()
    {
        return null == companyUrl ? "http://www.untangle.com" : companyUrl;
    }

    public void setCompanyUrl(String companyUrl)
    {
        this.companyUrl = companyUrl;
    }

    /**
     * The vendor logo to use, null means use the default Untangle
     * logo.
     *
     * @return GIF image bytes, null if Untangle logo.
     */
    public byte[] getLogo()
    {
        return logo;
    }

    public void setLogo(byte[] logo)
    {
        this.logo = logo;
    }

    /**
     * Get the vendor contact name.
     *
     * @return vendor contact name.
     */
    @Column(name="contact_name")
    public String getContactName()
    {
        return null == contactName ? "Your System Administrator" : contactName;
    }

    public void setContactName(String name)
    {
        this.contactName = name;
    }

    /**
     * Get the vendor contact email.
     *
     * @return vendor contact email.
     */
    @Column(name="contact_email")
    public String getContactEmail()
    {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail)
    {
        this.contactEmail = contactEmail;
    }

    public void copy(BrandingSettings settings)
    {
        settings.setCompanyName(this.companyName);
        settings.setCompanyUrl(this.companyUrl);
        settings.setContactName(this.contactName);
        settings.setLogo(this.logo);
        settings.setContactEmail(this.contactEmail);
    }
}
