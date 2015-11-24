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

import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;

import java.io.Serializable;

import static org.filteredpush.model.annotations.Namespace.BASE_URI;
import static org.filteredpush.model.annotations.Namespace.BOM;

public class Resolution implements Serializable {
    public static final Resolution WONT_FIX = new WontFix();
    public static final Resolution FIXED = new Fixed();

    private String name;

    @RDFSubject(prefix = "resolution:")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @RDFNamespaces({
            "bom = " + BOM,
            "resolution = " + BASE_URI + "resolution/"
    })
    @RDFBean("bom:WontFix")
    protected static class WontFix extends Resolution {
        public WontFix() {
            setName("wontFix");
        }
    }

    @RDFNamespaces({
            "bom = " + BOM,
            "resolution = " + BASE_URI + "resolution/"
    })
    @RDFBean("bom:Fixed")
    protected static class Fixed extends Resolution {
        public Fixed() {
            setName("fixed");
        }
    }

    public static Resolution valueOf(String str) {
        if (Resolution.FIXED.getName().equals(str)) {
            return Resolution.FIXED;
        } else if (Resolution.WONT_FIX.getName().equals(str)) {
            return Resolution.WONT_FIX;
        } else {
            return null;
        }
    }
}
