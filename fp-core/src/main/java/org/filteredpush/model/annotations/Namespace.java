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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Namespace {
    public static final String BASE_URI = "http://filteredpush.org/ontologies/";

    public static final String OA = "http://www.w3.org/ns/oa#";
    public static final String OAD = "http://filteredpush.org/ontologies/oa/oad#";
    public static final String FOAF = "http://xmlns.com/foaf/0.1/";
    public static final String DWC = "http://rs.tdwg.org/dwc/terms/";
    public static final String DWC_FP = "http://filteredpush.org/ontologies/oa/dwcFP#";
    public static final String CNT = "http://www.w3.org/2011/content#";
    public static final String MARL = "http://purl.org/marl/ns/";
    public static final String BOM = "http://www.ifi.uzh.ch/ddis/evoont/2001/11/bom#";
    public static final String DCTERMS = "http://purl.org/dc/terms/";

    public static final URI ANY_SOURCE;

    static {
        URI sourceAnySuchResource = null;

        try {
            sourceAnySuchResource = new URI(OAD + "AnySuchResource");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ANY_SOURCE = sourceAnySuchResource;
    }

    public static Map<String, String> getNsPrefixes() {
        Map<String, String> nsPrefixes = new HashMap<String, String>();

        nsPrefixes.put("dwcFP", DWC_FP);
        nsPrefixes.put("oad", OAD);
        nsPrefixes.put("dwc", DWC);
        nsPrefixes.put("oa", OA);
        nsPrefixes.put("cnt", CNT);
        nsPrefixes.put("foaf", FOAF);
        nsPrefixes.put("marl", MARL);
        nsPrefixes.put("bom", BOM);

        return nsPrefixes;
    }
}
