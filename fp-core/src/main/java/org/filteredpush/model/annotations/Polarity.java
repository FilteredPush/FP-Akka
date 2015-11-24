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
import static org.filteredpush.model.annotations.Namespace.MARL;

public class Polarity implements Serializable {
    public static final Polarity POSITIVE = new Positive();
    public static final Polarity NEUTRAL = new Neutral();
    public static final Polarity NEGATIVE = new Negative();

    private String name;

    @RDFSubject(prefix = "polarity:")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @RDFNamespaces({
            "marl = " + MARL,
            "polarity = " + BASE_URI + "polarity/"
    })
    @RDFBean("marl:Positive")
    public static class Positive extends Polarity {
        public Positive() {
            setName("positive");
        }
    }

    @RDFNamespaces({
            "marl = " + MARL,
            "polarity = " + BASE_URI + "polarity/"
    })
    @RDFBean("marl:Negative")
    public static class Negative extends Polarity {
        public Negative() {
            setName("negative");
        }
    }

    @RDFNamespaces({
            "marl = " + MARL,
            "polarity = " + BASE_URI + "polarity/"
    })
    @RDFBean("marl:Negative")
    public static class Neutral extends Polarity {
        public Neutral() {
            setName("neutral");
        }
    }

    public static Polarity valueOf(String value) {
        if ("POSITIVE".equalsIgnoreCase(value)) {
            return POSITIVE;
        } else if ("NEGATIVE".equalsIgnoreCase(value)) {
            return NEGATIVE;
        } else if ("NEUTRAL".equalsIgnoreCase(value)) {
            return NEUTRAL;
        } else throw new IllegalArgumentException("Unsupported value " + value + "for Polarity.");
    }
}
