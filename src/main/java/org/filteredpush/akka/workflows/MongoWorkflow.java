/** 
 * MongoWorkflow.java 
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
 *
 * @author Tianhong Song
 * @author bluecobalt27
 * @author chicoreus
 */

package org.filteredpush.akka.workflows;

import akka.actor.*;

import org.filteredpush.akka.actors.BasisOfRecordValidator;
import org.filteredpush.akka.actors.GEORefValidator;
import org.filteredpush.akka.actors.InternalDateValidator;
import org.filteredpush.akka.actors.NewScientificNameValidator;
import org.filteredpush.akka.actors.io.MongoDBReader;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
import org.filteredpush.akka.data.Prov;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

/*
* @begin MongoWorkflow
* @param inputFilename
* @param outputFilename
* @param nameAuthority
* @in inputFile @uri {mongoHost}{db}{inputCollection}
* @out outputFile @uri {mongoHost}{db}{outputCollection} 
*/
public class MongoWorkflow implements AkkaWorkflow {

    public static void main(String[] args) {
        MongoWorkflow fp = new MongoWorkflow();
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

    @Option(name="-h",usage="MongoDB Host")
    private String mongoHost = "fp2.acis.ufl.edu";

    @Option(name="-d",usage="db")
    private String db = "db";

    @Option(name="-ci",usage="Input Collection in mongo to query for records to process.")
    private String inputCollection = "scan_prod_occurrences";

    @Option(name="-co",usage="Output Collection in mongo into which to write results.")
    private String outputCollection = "test";

    @Option(name="-q",usage="Query on Mongo collection to select records to process, e.g. {\"institutionCode\" : \"NMSU\"} ")
    private String query = "{month:\"12\"}";
    //private String query = "{\"institutionCode\" : \"NAU\", \"year\" : \"1966\"}";
    //private String query = "{\"institutionCode\" : \"NMSU\"}";
    //private String query = "{oaiid:\"SCAN.occurrence.1098032\"}";   //834964 829560 833567   SCAN.occurrence.907687
    //private String query = "{year:\"1898\"}";
    //private String query = "{collectionCode: \"ASUHIC\" }";
    //private String query = "{catalogNumber: \"NAUF4A0038275\" }";
    //private String query = "";
    
    @Option(name="-a",usage="Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.")
    private String service = "GBIF";
    
    @Option(name="-t",usage="Run scientific name validator in taxonomic mode (look up name in current use).")
    private boolean taxonomicMode = false;
    
    /**
     * Setup conditions for the workflow to execute
     * 
     * @param args command line arguments
     * @return true if setup was successfull, false otherwise
     */
    public boolean setup(String[] args) {
    	boolean result = false;
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            //System.err.println("java FP [options...] arguments...");
            //parser.printUsage(System.err);
            //if (parser.getArguments().size()<1 ) throw new CmdLineException(parser,"No argument is given");
            result = true;
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            //System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            //System.err.println();
        }
        Prov.init("testProv.log");
        return result;
    }

    public void calculate() {
        this.calculate(mongoHost, db, inputCollection, outputCollection, query, 200.0);
    }


    public void calculate(
            final String host,
            final String db,
            final String collectionIn,
            final String collectionOut,
            final String query,
            final Double certainty) {

        long starttime = System.currentTimeMillis();

        // Create an Akka system
        ActorSystem system = ActorSystem.create("FpSystem");
        /*
        // create the result listener, which will print the result and shutdown the system
        //final ActorRef display = system.actorOf(new Props(TextDisplay.class), "display");
        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBWriter(host,db,collectionOut,"",out,enc);
            }
        }), "MongoDBWriter");


        final ActorRef flwtValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new FloweringTimeValidator("org.filteredpush.kuration.services.test.FNAFloweringTimeService",true,true,writer);
            }
        }), "flwtValidator");


        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ScientificNameValidator("org.filteredpush.kuration.services.test.IPNIService",true,true,flwtValidator);
            }
        }), "scinValidator");


        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
          public UntypedActor create() {
           return new GEORefValidator("org.filteredpush.kuration.services.test.GeoLocate2",true,certainty,flwtValidator);
          }
        }), "geoValidator");

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBReader(host,db,collectionIn,query,geoValidator);
            }
        }), "reader");

        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVWriter(outputFilename);
            }
        }), "MongoDBWriter");

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader(inputFilename, scinValidator);
            }
        }), "reader");

        final ActorRef starter = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new Starter(reader);
            }
        }), "starter");

          final ActorRef dateValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new InternalDateValidator("org.filteredpush.kuration.services.test.InternalDateValidationService", writer);
            }
        }), "geoValidator");

        final ActorRef annotationInserter = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new AnnotationInserter(writer);
            }
        }), "annotationInserter");


        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoSummaryWriter(host,db,collectionOut,null);
            }
        }), "MongoDBWriter");
        */

        /* @begin MongoSummaryWriter
         * @param outputFilename
         * @in geoRefValidatedRecords
         * @out outputFile @uri {host}{db}{collectionOut}
         */
        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoSummaryWriter(host,db,collectionOut,null);
            }
        }), "MongoDBWriter");
        /* @end MongoSummaryWriter */

        /* @begin GEORefValidator
         * @in dateValidatedRecords
         * @out geoRefValidatedRecords
         */
        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new GEORefValidator("org.filteredpush.kuration.services.GeoLocate3",false,certainty, writer);
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
            	// TODO: Need to see if this sort of picking inside create() will work 
            	// to allow choice between CSV or MongoDB input from command line parameters 
            	// letting DwCaWorkflow and MongoWorkflow be collapsed into a single workflow.
            	if (service.toUpperCase().equals("GLOBALNAMES")) { 
                    return new SciNameWorkflow("-t",false,basisOfRecordValidator);
            	} else { 
            		boolean useCache = true;
            		boolean insertGuid = true;
                    return new NewScientificNameValidator(useCache,insertGuid,service, taxonomicMode, basisOfRecordValidator);
            	}
            }
        }), "scinValidator");
        /* @end ScientificNameValidator */

        /* @begin MongoDbReader 
         * @param inputFilename
         * @in inputFile @uri {host}{db}{collectionIn}{query}
         * @out inputSpecimenRecords
         */
        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBReader(host,db,collectionIn,query,scinValidator);
            }
        }), "reader");
        /* @end CSVReader */

        /*
        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader("/home/tianhong/Downloads/data/tt2.txt",scinValidator);
            }
        }), "reader");
        */


        // start the calculation
        System.err.println("systemstart#"+" " + "#" + System.currentTimeMillis());
        reader.tell(new Curate());
        //system.shutdown();
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        //System.out.printf("\nTime: %f s\n",(stoptime-starttime)/1000.0);
        System.err.printf("runtime: %d\n",stoptime-starttime);
    }

    static class Curate {
    }
}
/* @end MongoWorkflow */
