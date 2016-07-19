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


import kamon.Kamon;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Pick a FP-Akka workflow to execute, pass on command line parameters to it,
 * set it up, and if successful, execute it. 
 * 
 * @author Tianhong Song
 *
 */
public class WorkflowStarter{
    private final String WORKFLOW_PROPERTY = "analysis.workflow";
    private final String LIMIT_PROPERTY = "analysis.recordlimit";
    private final String INPUT_PROPERTY = "analysis.input";
    private final String OUTPUT_PROPERTY = "analysis.output";
    private final String AUTHORITY_PROPERTY = "analysis.authority";
    private final String TAXONOMIC_MODE_PROPERTY = "analysis.taxonomicMode";
    private final String SCI_NAME_VALIDATOR_ONLY_PROPERTY = "analysis.sciNameValidatorOnly";

    public static void main(String[] args) {
        Kamon.start();
        WorkflowStarter ws = new WorkflowStarter();
        ws.execute(args);
        Kamon.shutdown();
    }


    @Option(name="-w",usage="Workflow Name")
    private String workflowName = "CSV";

    public void execute(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);

        if (args.length > 0) {
            //split the args array in order to accommodate -w switch to select workflow
            List<String> remaining = new ArrayList<String>();
            int count = 0;
            while (count < args.length) {
                if (args[count].equals("-w")) {
                    if ((count + 1 < args.length && !args[count + 1].contains("-"))) {
                        workflowName = args[count + 1];
                        count++;
                    } else System.out.println("-w option is not valid");
                } else {
                    remaining.add(args[count]);
                }
                count++;
            }
            args = new String[remaining.size()];
            count = 0;
            for (String item : remaining) {
                args[count] = item;
                count++;
            }
        } else {
            args = loadOptionsFromPropertiesFile();
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

        System.out.println("Available Workflows: DwCa, CSV, MONGO (specify workflow to run with -w).");
        System.out.println("Selected Workflow: " + workflowName);

        AkkaWorkflow fp = null;
        if(workflowName.toUpperCase().equals("CSV")) fp = new CSVWorkflow();
        else if(workflowName.toUpperCase().equals("MONGO")) fp = new MongoWorkflow();
        else if(workflowName.toUpperCase().equals("DWCA")) fp = new DwCaWorkflow();
        else System.out.println("Unknown workflow name: " + workflowName);



        if (fp.setup(args)) fp.calculate();
    }

    private String[] loadOptionsFromPropertiesFile() {
        Properties properties = new Properties();
        InputStream in = WorkflowStarter.class.getResourceAsStream("/analysis.properties");
        if (in == null) {
            properties = createDefaultPropertiesFile();
        } else {
            try {
                properties.load(in);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        workflowName = properties.getProperty(WORKFLOW_PROPERTY);

        List<String> argsList = new ArrayList<String>();
        if (properties.containsKey(INPUT_PROPERTY)) {
            argsList.add("-i");
            argsList.add(properties.getProperty(INPUT_PROPERTY));
        }
        if (properties.containsKey(LIMIT_PROPERTY)) {
            argsList.add("-l");
            argsList.add(properties.getProperty(INPUT_PROPERTY));
        }
        if (properties.containsKey(OUTPUT_PROPERTY)) {
            argsList.add("-o");
            argsList.add(properties.getProperty(OUTPUT_PROPERTY));
        }
        if (properties.containsKey(AUTHORITY_PROPERTY)) {
            argsList.add("-a");
            argsList.add(properties.getProperty(AUTHORITY_PROPERTY));
        }
        if (properties.containsKey(TAXONOMIC_MODE_PROPERTY) &&
                Boolean.parseBoolean(properties.getProperty(AUTHORITY_PROPERTY))) {
            argsList.add("-t");
        }
        if (properties.containsKey(SCI_NAME_VALIDATOR_ONLY_PROPERTY) &&
                Boolean.parseBoolean(properties.getProperty(SCI_NAME_VALIDATOR_ONLY_PROPERTY))) {
            argsList.add("-s");
        }

        String[] args = new String[argsList.size()];
        argsList.toArray(args);
        return args;
    }

    private Properties createDefaultPropertiesFile() {

        Properties properties = new Properties();
        properties.setProperty(WORKFLOW_PROPERTY, "DwCa");
        properties.setProperty(INPUT_PROPERTY, "occurrence.txt");
        properties.setProperty(OUTPUT_PROPERTY, "occurrence_qc.json");
        properties.setProperty(AUTHORITY_PROPERTY, "COL");
        properties.setProperty(TAXONOMIC_MODE_PROPERTY, "false");
        properties.setProperty(SCI_NAME_VALIDATOR_ONLY_PROPERTY, "false");

        try {
            FileOutputStream output = new FileOutputStream("analysis.properties");
            properties.store(output, null);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return properties;
    }


}

