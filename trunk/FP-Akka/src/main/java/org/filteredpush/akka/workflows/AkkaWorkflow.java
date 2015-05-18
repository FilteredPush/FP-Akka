/** 
 * AkkaWorkflow.java 
 * 
 * Copyright 2015 President and Fellows of Harvard College
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
package org.filteredpush.akka.workflows;

/**
 * Top level interface for FP-Akka workflows.
 * 
 * Created by thsong on 5/15/15.
 * 
 * @author Tianhong Song
 */
public interface AkkaWorkflow {

	/**
	 * Setup the preconditions for executing the workflow.
	 * 
	 * @param args command line arguments passed from a Main() method.
	 * @return true if preconditions for workflow execution were successfully met, 
	 * otherwise false.
	 */
    public boolean setup(String[] args);

    /**
     *  Setup and execute the workflow.  
     */
    public void calculate();
}
