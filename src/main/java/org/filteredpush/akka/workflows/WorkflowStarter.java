/** 
 * WorkflowStarter.java 
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


import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Pick a FP-Akka workflow to execute, pass on command line parameters to it,
 * set it up, and if successfull, execute it. 
 * 
 * @author Tianhong Song
 *
 */
public class WorkflowStarter{

    public static void main(String[] args) {
        WorkflowStarter ws = new WorkflowStarter();
        ws.execute(args);
    }


    @Option(name="-w",usage="Workflow Name")
    private String workflowName = "CSV";

    public void execute(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);

        //split the args array in order to accommodate -w switch to select workflow
        List<String> remaining = new ArrayList<String>();
        int count = 0;
        while(count < args.length){
            if(args[count].equals("-w")){
                if((count + 1 < args.length && !args[count+1].contains("-"))){
                    workflowName = args[count+1];
                    count++;
                }
                else System.out.println("-w option is not valid");
            }else{
                remaining.add(args[count]);
            }
            count++;
        }
        args = new String[remaining.size()];
        count = 0;
        for(String item : remaining){
            args[count] = item;
            count++;
        }

        /*

        try {
           parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            //System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            //System.err.println();
        }
        */

        System.out.println("Selected Workflow: " + workflowName);

        AkkaWorkflow fp = null;
        if(workflowName.toUpperCase().equals("CSV")) fp = new CSVWorkflow();
        else if(workflowName.toUpperCase().equals("MONGO")) fp = new MongoWorkflow();
        else if(workflowName.toUpperCase().equals("DWCA")) fp = new DwCaWorkflow();
        else System.out.println("Unknown workflow name: " + workflowName);



        if (fp.setup(args)) fp.calculate();
    }





}

