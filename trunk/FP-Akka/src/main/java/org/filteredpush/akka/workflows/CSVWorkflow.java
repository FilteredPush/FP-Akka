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
import org.filteredpush.akka.actors.InternalDateValidator;
import org.filteredpush.akka.actors.NewScientificNameValidator;
import org.filteredpush.akka.actors.io.CSVReader;
import org.filteredpush.akka.actors.io.CSVWriter;
import org.filteredpush.akka.actors.io.MongoSummaryWriter;
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

    @Option(name="-o",usage="output JSON file")
    //private String out = "/Users/cobalt/X31out.txt";
    //private String outputFilename = "/home/tianhong/data/akka/2011Demo_out.csv";
    //private String outputFilename = "/home/tianhong/data/akka/test.json";
    private String outputFilename = "/home/thsong/data/scan_data/test.json";

    @Option(name="-i",usage="Input CSV file")
    private String inputFilename = "/home/thsong/data/scan_data/tt.txt";

    @Option(name="-s",usage="Only with SciNameValidator")
    private boolean sciNameOnly = false;

    @Option(name="-a",usage="Authority to check scientific names against (IPNI, IF, WoRMS, COL, GBIF, GlobalNames), default GBIF.")
    private String service = "GBIF";

    @Option(name="-t",usage="SciNameValidator Taxonomic Mode")
    private boolean Taxonomic = false;

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

         final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader(inputFilename, scinValidator);
            }
        }), "reader");


        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVWriter(outputFilename);
            }
        }), "writer");


        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBWriter(host,db,collectionOut,"", enc);
            }
        }), "MongoDBWriter");

        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVWriter(outputFilename);
            }
        }), "writer");

            scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new SciNameWorkflow("-t",false,writer);
                }
            }), "scinValidator");*/

        final ActorRef scinValidator;

        if(!sciNameOnly){
            final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new MongoSummaryWriter(outputFilename);
                }
            }), "JsonWriter");

            final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new GEORefValidator("org.filteredpush.kuration.services.GeoLocate3",false,certainty,writer);
                }
            }), "geoValidator");


            final ActorRef dateValidator = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new InternalDateValidator("org.filteredpush.kuration.services.InternalDateValidationService", geoValidator);
                }
            }), "dateValidator");

            scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new NewScientificNameValidator("org.filteredpush.kuration.services.sciname.COLService",true,true, service, Taxonomic, dateValidator);
                }
            }), "scinValidator");

        }else{
            final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new CSVWriter(outputFilename, true);
                }
            }), "CSVWriter");

            scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
                public UntypedActor create() {
                    return new NewScientificNameValidator("org.filteredpush.kuration.services.sciname.COLService",true,true, service, false, writer);
                }
            }), "scinValidator");
        }



        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new CSVReader(inputFilename, scinValidator);
            }
        }), "reader");


        // start the calculation
        reader.tell(new Curate());
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        //System.out.printf("\nTime: %f s\n",(stoptime-starttime)/1000.0);
        System.err.printf("%d",stoptime-starttime);
    }

    static class Curate {
    }
}

