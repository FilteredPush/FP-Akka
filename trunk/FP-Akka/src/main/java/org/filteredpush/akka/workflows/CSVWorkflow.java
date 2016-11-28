/** 
 * AkkaWorkflow.java 
 * 
 * Copyright 2013 President and Fellows of Harvard College
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


import akka.actor.*;

import org.filteredpush.akka.actors.GEORefValidator;
import org.filteredpush.akka.actors.CollectorCollectedDateValidator;
import org.filteredpush.akka.actors.NewScientificNameValidator;
import org.filteredpush.akka.actors.PullRequestor;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.actors.io.CSVWriter;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
import org.filteredpush.akka.data.Curate;
import org.filteredpush.akka.data.SetUpstreamListener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

/**
 * Read DarwinCore data from a csv file, and execute one of two workflows on it
 * one workflow checks scientific name, georeference, and date collected, and then 
 * writes out as json.  Other workflow checks scientific name and writes out as csv.
 * 
 * @author bluecobalt
 * @author swanskysong
 * @author chicoreus
 *
 */
public class CSVWorkflow implements AkkaWorkflow{

    public static void main(String[] args) {
        CSVWorkflow fp = new CSVWorkflow();
        if (fp.setup(args)) { 
           fp.calculate();
        }
    }

    /*@Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";          */

    @Option(name="-o",usage="output file (.json unless -s is specified, in which case .csv)")
    //private String out = "/Users/cobalt/X31out.txt";
    private String outputFilename = "/home/thsong/data/scan_data/test.json";
    //private String outputFilename = "output.json";

    @Option(name="-i",usage="Input CSV file")
    private String inputFilename = "/home/thsong/data/scan_data/occurrenceproblem2.txt";
    //private String inputFilename = "input.txt";

    @Option(name="-s",usage="Only check scientific names with SciNameValidator (outputs will be .csv, not .json)")
    private boolean sciNameOnly = false;

    @Option(name="-h",usage="If checking only scientific names (-s), include higher taxa for each name in the output, if available from the selected source.")
    private boolean includeHigher = false;

    @Option(name="-a",usage="Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.")
    private String service = "GBIF";

    @Option(name="-t",usage="SciNameValidator taxonomicMode Mode (look up name in current use).")
    private boolean taxonomicMode = false;
    
    @Option(name="-l",usage="Limit on the number of records to read before stopping.")
    private int recordLimit = 0;

    final Double certainty = 200.0;

    /**
     * Setup conditions to run the workflow.
     * @param args command line arguments
     * @return true if setup was successful, false otherwise.
     */
    public boolean setup(String[] args) {
        boolean setupOK = false;
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {

            //if(args==null || args.length==0)
                //throw new CmdLineException(parser,"No arguments were provided, you must specify -i and -o.");


            parser.parseArgument(args);
            File inputFile = new File(inputFilename);
            if (!inputFile.canRead()) { 
                throw new CmdLineException(parser,"Can't read Input File " + inputFilename );
            }
            if (sciNameOnly && !outputFilename.endsWith(".csv")) {
            	outputFilename = outputFilename + ".csv";
            }
            if (!sciNameOnly && !outputFilename.endsWith(".json")) {
            	outputFilename = outputFilename + ".json";
            }
            File outputFile = new File(outputFilename);
            if (outputFile.exists()) { 
                throw new CmdLineException(parser,"Output File Exists " + outputFilename );
            }
            setupOK = true;
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            //System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            //System.err.println();
        }
        //Prov.init("testProv.log");
        return setupOK;
    }

    public void calculate() {

        long starttime = System.currentTimeMillis();

        // Create an Akka system
        ActorSystem system = ActorSystem.create("FpSystem");

        final ActorRef scinValidator;
        
        final ActorRef pullTap;

        if(!sciNameOnly){
            final ActorRef writer = system.actorOf(Props.create(MongoSummaryWriter.class, outputFilename), "JsonWriter");

            pullTap = system.actorOf(Props.create(PullRequestor.class, writer), "pullRequestor");            
            
            final ActorRef geoValidator = system.actorOf(Props.create(GEORefValidator.class, "org.filteredpush.kuration.services.GeoLocate3",false,certainty,pullTap), "geoValidator");

            final ActorRef dateValidator = system.actorOf(Props.create(CollectorCollectedDateValidator.class, "org.filteredpush.kuration.services.InternalDateValidationService", geoValidator), "dateValidator");

            scinValidator = system.actorOf(Props.create(NewScientificNameValidator.class, true,true, service, taxonomicMode, dateValidator), "scinValidator");
            
        }else{
        	System.out.println("Validating scientific names only, writing to csv.");
            final ActorRef writer = system.actorOf(Props.create(CSVWriter.class, outputFilename, true, includeHigher), "CSVWriter");

            pullTap = system.actorOf(Props.create(PullRequestor.class, writer), "pullRequestor");
            
            scinValidator = system.actorOf(Props.create(NewScientificNameValidator.class, true,true, service, false, pullTap), "scinValidator");
            
        }

        final ActorRef reader = system.actorOf(Props.create(CSVReader.class, inputFilename, scinValidator, recordLimit), "reader");

        pullTap.tell(new SetUpstreamListener(), reader);
        
        // start the calculation
        reader.tell(new Curate(),null);
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        //System.out.printf("\nTime: %f s\n",(stoptime-starttime)/1000.0);
        System.err.printf("%d",stoptime-starttime);
        System.err.println();
    }

}
