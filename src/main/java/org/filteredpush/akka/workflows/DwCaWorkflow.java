/**
 * DwCaWorkflow.java
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

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import org.filteredpush.akka.actors.BasisOfRecordValidator;
import org.filteredpush.akka.actors.GEORefValidator;
import org.filteredpush.akka.actors.InternalDateValidator;
import org.filteredpush.akka.actors.NewScientificNameValidator;
import org.filteredpush.akka.actors.PullRequestor;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.actors.io.DwCaReader;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
import org.filteredpush.akka.data.Curate;
import org.filteredpush.akka.data.SetUpstreamListener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

/* 
 * @begin DwCaWorkflow
 * @param inputFilename
 * @param outputFilename
 * @param nameAuthority
 * @in inputFile @uri {inputFilename}
 * @out outputFile @uri {outputFilename} 
 */
/**
 * Workflow to take a tab delimited occurrence.txt file and run it through FP 
 * data QC actors for scientific name, collecting event date, and georeference validation, 
 * and to output the result as a JSON file for post processing.
 * 
 * @author mole
 */
public class DwCaWorkflow implements AkkaWorkflow{
	
	public static final String REV = "$Id$";

	private boolean inputIsDwCarchive = false;
	
    public static void main(String[] args) {
        DwCaWorkflow fp = new DwCaWorkflow();
        
        /* @begin ParseOptions
         * @call setup
         * @param inputFilename
         * @param outputFilename
         * @param nameAuthority
         * @out serviceClass @as nameService
         */
        if (fp.setup(args)) { 
        /* @end ParseOptions */
            fp.calculate();
        }
    }


    /*@Option(name="-e",usage="encoding for output file")
    private String enc = "UTF-8";          */

    @Option(name="-o",usage="output JSON file")
    private String outputFilename = "/home/thsong/data/scan_data/test.json";
    //private String outputFilename = "output.json";

    @Option(name="-i",usage="Input occurrence.txt (tab delimited occurrence core from a DwC archive) file.")
    private String inputFilename = "/home/thsong/data/scan_data/hy.txt";
    //private String inputFilename = "input.txt";
    
    @Option(name="-a",usage="Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.")
    private String service = "GBIF";
    
    @Option(name="-t",usage="Run scientific name validator in taxonomic mode (look up name in current use).")
    private boolean taxonomicMode = false;
    
    @Option(name="-l",usage="Limit on the number of records to read before stopping.")
    private int recordLimit = 0;
    /**
     * Setup conditions to run the workflow.
     * @begin setup
     * @param args command line arguments
     * @return true if setup was successful, false otherwise.
     */
    public boolean setup(String[] args) {
        boolean setupOK = false;
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            File inputFile = new File(inputFilename);
            if (!inputFile.canRead()) { 
                throw new CmdLineException(parser,"Can't read Input File " + inputFilename );
            } else { 
            	if (inputFile.isDirectory()) { 
            		inputIsDwCarchive = true;
            	} else { 
            		if (inputFile.getName().endsWith(".zip")) { 
            		    inputIsDwCarchive = true;
            		}
            	}
            }
            File outputFile = new File(outputFilename);
            if (outputFile.exists()) { 
                throw new CmdLineException(parser,"Output File Exists " + outputFilename );
            }
            
            setupOK = true;
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
        return setupOK;
    }
    
    /** @end setup */

    public void calculate() {
        this.calculate( "db", "Occurrence", GEORefValidator.DEFAULT_CERTAINTY);
    }

    public void calculate(
            final String fileIn,
            final String fileOut,
            //final String query,
            final Double certainty) {

        long starttime = System.currentTimeMillis();

        // Create an Akka system
        ActorSystem system = ActorSystem.create("FpSystem");
        
        /* @begin MongoSummaryWriter
         * @param outputFilename
         * @in passThroughRecords
         * @out outputFile @uri {outputFilename}
         */
        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoSummaryWriter(outputFilename);
            }
        }), "JsonWriter");
        /* @end MongoSummaryWriter */
        
        /* @begin PullRequestor
         * @in geoRefValidatedRecords
         * @out passThroughRecords
         * @out loadMore
         */
        final ActorRef pullTap = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new PullRequestor(writer);
            }
        }), "pullRequestor");
        /* @end PullRequestor */
        
        
        /* @begin GEORefValidator
         * @in dateValidatedRecords
         * @out geoRefValidatedRecords
         */
        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new GEORefValidator("org.filteredpush.kuration.services.GeoLocate3",false,certainty,pullTap);
            }
        }), "geoValidator");
        /* @end GEORefValidator */
        
        /* @begin InternalDateValidator
         * @in borValidatedRecords
         * @out dateValidatedRecords
         */
        final ActorRef dateValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new InternalDateValidator("org.filteredpush.kuration.services.InternalDateValidationService", geoValidator);
            }
        }), "dateValidator");
        /* @end InternalDateValidator */
        
        /* @begin BasisOfRecordValidator
         * @in nameValidatedRecords
         * @out borValidatedRecords
         */
        final ActorRef basisOfRecordValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new BasisOfRecordValidator("org.filteredpush.kuration.services.BasisOfRecordValidationService", dateValidator);
            }
        }), "basisOfRecordValidator");
        /* @end BasisOfRecordValidator */        
        
        /* @begin ScientificNameValidator
         * @param service @as nameService
         * @in inputSpecimenRecords
         * @out nameValidatedRecords
         */
        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	if (service.toUpperCase().equals("GLOBALNAMES")) { 
                    return new SciNameSubWorkflow("-t",false,basisOfRecordValidator);
            	} else { 
            		boolean useCache = true;
            		boolean insertGuid = true;
                    return new NewScientificNameValidator(useCache,insertGuid,service, taxonomicMode, basisOfRecordValidator);
            	}
            }
        }), "scinValidator");
        /* @end ScientificNameValidator */

        /* @begin CSVReader 
         * @param inputFilename
         * @in inputFile @uri {inputFilename}
         * @in loadMore
         * @out inputSpecimenRecords
         */
        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
            	if (inputIsDwCarchive) { 
                    return new DwCaReader(inputFilename, scinValidator, recordLimit);
            	} else { 
                    return new CSVReader(inputFilename, scinValidator, recordLimit);
            	}
            }
        }), "reader");
        /* @end CSVReader */

        // Notify the pull requestor that it should tell the reader to load more
        // records when records reach it.
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

/* @end DwCaWorkflow */

