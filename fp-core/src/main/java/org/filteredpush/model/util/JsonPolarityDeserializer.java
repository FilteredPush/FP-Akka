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

package org.filteredpush.model.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.filteredpush.model.annotations.Polarity;

import java.io.IOException;

/**
 * Used by Jackson to deserialize org.filteredpush.model.annotations.Polarity
 */
public class JsonPolarityDeserializer extends JsonDeserializer<Polarity> {

    @Override
    public Polarity deserialize(JsonParser jsonParser, 
            DeserializationContext deserializationContext) throws IOException {

        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return Polarity.valueOf(node.get("name").textValue());
    }
}