/*
 * Copyright (c) 2013 President and Fellows of Harvard College
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of Version 2 of the GNU General Public License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.filteredpush.model.annotations;

import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

@RDFNamespaces({
        "foaf = " + Namespace.FOAF,
        "agent = " + Namespace.BASE_URI + "agent/"
})
@RDFBean("foaf:Agent")
public class Agent implements Serializable {
    private String id;

    private String phone;
    private String name;
    private String mbox_sha1sum;
    private String email;
    private URI workplaceHomepage;

    public Agent() {
        id = Namespace.BASE_URI + "agent/" + UUID.randomUUID().toString();
    }

    @RDFSubject
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("foaf:phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @RDF("foaf:name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @RDF("foaf:mbox_sha1sum")
    public String getMboxSha1sum() {
        return mbox_sha1sum;
    }

    public void setMboxSha1sum(String mbox_sha1sum) {
        this.mbox_sha1sum = mbox_sha1sum;
    }

    @RDF("foaf:mbox")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;

        DigestUtils.sha(email);
    }

    @RDF("foaf:workplaceHomepage")
    public URI getWorkplaceHomepage() {
        return workplaceHomepage;
    }

    public void setWorkplaceHomepage(URI workplaceHomepage) {
        this.workplaceHomepage = workplaceHomepage;
    }
}
