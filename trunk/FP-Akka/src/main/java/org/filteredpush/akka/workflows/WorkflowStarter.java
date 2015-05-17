/**
 * Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */

package org.filteredpush.akka.workflows;


import akka.actor.*;
import org.filteredpush.akka.actors.GEORefValidator;
import org.filteredpush.akka.actors.InternalDateValidator;
import org.filteredpush.akka.actors.NewScientificNameValidator;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

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

        //split the args array in order to accomerdate with -w to select workflow
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
        if(workflowName.equals("CSV")) fp = new CSVWorkflow();
        else if(workflowName.equals("Mongo")) fp = new MongoWorkflow();
        else if(workflowName.equals("DwCa")) fp = new DwCaWorkflow();
        else System.out.println("Unknown workflow name: " + workflowName);



        if (fp.setup(args)) fp.calculate();
    }





}

