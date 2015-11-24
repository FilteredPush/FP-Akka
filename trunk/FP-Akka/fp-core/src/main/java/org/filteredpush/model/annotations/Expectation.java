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

public class Expectation implements Serializable {
    public static final Expectation INSERT = new Insert();
    public static final Expectation UPDATE = new Update();
    public static final Expectation SOLVE_WITH_MORE_DATA = new Update();

    private String name;

    @RDFSubject(prefix = "expectation:")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @RDFNamespaces({
            "oad = " + Namespace.OAD,
            "expectation = " + Namespace.BASE_URI + "expectation/"
    })
    @RDFBean("oad:Expectation_Insert")
    protected static class Insert extends Expectation {
        public Insert() {
            setName("insert");
        }
    }

    @RDFNamespaces({
            "oad = " + Namespace.OAD,
            "expectation = " + Namespace.BASE_URI + "expectation/"
    })
    @RDFBean("oad:Expectation_Update")
    protected static class Update extends Expectation {
        public Update() {
            setName("update");
        }
    }
    
    @RDFNamespaces({
        "oad = " + Namespace.OAD,
        "expectation = " + Namespace.BASE_URI + "expectation/"
})
@RDFBean("oad:Expectation_Solve_With_More_Data")
protected static class SolveWithMoreData extends Expectation {
    public SolveWithMoreData() {
        setName("solve_with_more_data");
    }
}
    
    public static Expectation valueOf(String value) {
        if ("INSERT".equalsIgnoreCase(value)) {
            return INSERT;
        } else if ("UPDATE".equalsIgnoreCase(value)) {
            return UPDATE;
        } else if ("SOLVE_WITH_MORE_DATA".equalsIgnoreCase(value)) {
            return SOLVE_WITH_MORE_DATA;
        } else throw new IllegalArgumentException("Unsupported value " + value + "for Expectation.");
        
    }
}
