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

import java.io.Serializable;
import java.util.UUID;

@RDFNamespaces({
        "cnt = " + Namespace.CNT,
        "content = " + Namespace.BASE_URI + "contentAsText/"
})
@RDFBean("cnt:ContentAsText")
public class ContentAsText implements Serializable {
    private String id;

    private String chars;

    public ContentAsText() {
        id = UUID.randomUUID().toString();
    }

    @RDFSubject(prefix = "content:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("cnt:chars")
    public String getChars() {
        return chars;
    }

    public void setChars(String chars) {
        this.chars = chars;
    }
}
