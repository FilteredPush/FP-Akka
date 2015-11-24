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

public class Motivation implements Serializable {
    public static final Motivation EDITING = new Editing();
    public static final Motivation TRANSCRIBING = new Transcribing();
    public static final Motivation CONSENSUS = new ConsensusBuilding();
    public static final Motivation COMMENTING = new Commenting();

    /*
    oa:bookmarking  oa:classifying  oa:commenting  oa:describing  oa:editing  oa:highlighting  oa:identifying  oa:linking  oa:moderating  oa:questioning  oa:replying  oa:tagging

     */
    private String name;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @RDFNamespaces({
            "oa = " + Namespace.OA
    })
    @RDFBean("oa:Motivation")
    protected static class Editing extends Motivation {
        private String name;

        public Editing() {
            setName("editing");
        }

        @RDFSubject(prefix = "oa:")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @RDFNamespaces({
            "oad = " + Namespace.OAD
    })
    @RDFBean("oa:Motivation")
    protected static class Transcribing extends Motivation {

        private String name;

        public Transcribing() {
            setName("transcribing");
        }

        @RDFSubject(prefix = "oad:")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
    @RDFNamespaces({
            "oa = " + Namespace.OA
    })
    @RDFBean("oa:Motivation")
    protected static class Commenting extends Motivation {

        private String name;

        public Commenting() {
            setName("commenting");
        }

        @RDFSubject(prefix = "oa:")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

    @RDFNamespaces({
            "oad = " + Namespace.OAD
    })
    @RDFBean("oad:ConsensusBuilding")
    protected static class ConsensusBuilding extends Motivation {

        private String name;

        public ConsensusBuilding() {
            setName("consensusBuilding");
        }

        @RDFSubject(prefix = "oad:")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
    
    public static Motivation valueOf(String value) {
        if ("EDITING".equalsIgnoreCase(value)) {
            return EDITING;
        } else if ("TRANSCRIBING".equalsIgnoreCase(value)) {
            return TRANSCRIBING;
        } else if ("CONSENSUS".equalsIgnoreCase(value)) {
            return CONSENSUS;
        } else if ("COMMENTING".equalsIgnoreCase(value)) {
            return COMMENTING;
        } else throw new IllegalArgumentException("Unsupported value " + value + "for Motivation.");
        
    }
}

