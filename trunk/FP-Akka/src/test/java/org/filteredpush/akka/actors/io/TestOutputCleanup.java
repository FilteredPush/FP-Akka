/** 
 * TestOutputCleanup.java
 * 
 * Copyright 2016 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.filteredpush.akka.actors.io;

import static org.junit.Assert.*;

import org.filteredpush.kuration.services.sciname.SciNameServiceParent;
import org.junit.Test;

/**
 * @author mole
 *
 */
public class TestOutputCleanup {

	@Test
	public void test() {
		// checking the regex for stripping out "Filled In" higher taxon comments when the output doesn't include those fields.
		// Not actually testing code here...
		String output = "| can't construct sciName from atomic fields | Found exact match in WoRMS. |  Authorship: Exact Match Similarity: 1.0 | Filled In Kingdom  | Filled In Phylum  | Filled In Class  | Filled In Order  | Filled In Family";
		output = output.replaceAll(SciNameServiceParent.FILL_IN_HIGHER_REGEX, "");
		System.out.println(output);
		assertFalse(output.contains("Filled In Kingdom")); 
	}

}
