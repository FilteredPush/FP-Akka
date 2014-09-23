/**
 * Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */

package akka.fp;


import akka.actor.*;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Loader {

    public static void main(String[] args) {
        Loader fp = new Loader();
        fp.setup(args);
        fp.calculate();
    }

    @Option(name="-h",usage="MongoDB Host")
    private String mongoHost = "fp2.acis.ufl.edu";

    @Option(name="-d",usage="db")
    private String db = "db";

    @Option(name="-ci",usage="Input Collection")
    private String inputCollection = "scan_prod_occurrences";

    @Option(name="-co",usage="Output Collection")
    private String outputCollection = "NAUAll5";

    @Option(name="-q",usage="Query")
    //private String query = "{\"institutionCode\" : \"NAU\", \"year\" : \"1934\"}";
    private String query = "{\"institutionCode\" : \"NAU\"}";
    //private String query = "{oaiid:\"SCAN.occurrence.9378021\"}";   //834964 829560 833567
    //private String query = "{year:\"1898\"}";

    
    public void setup(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(4096);
        try {
            parser.parseArgument(args);
            //System.err.println("java FP [options...] arguments...");
            //parser.printUsage(System.err);
            //if (parser.getArguments().size()<1 ) throw new CmdLineException(parser,"No argument is given");


        } catch( CmdLineException e ) {
            //System.err.println(e.getMessage());
            //System.err.println("java FP [options...] arguments...");
            parser.printUsage(System.err);
            //System.err.println();
            return;
        }
        Prov.init("testProv.log");
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
                return new FloweringTimeValidator("fp.services.FNAFloweringTimeService",true,true,writer);
            }
        }), "flwtValidator");


        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new ScientificNameValidator("fp.services.IPNIService",true,true,flwtValidator);
            }
        }), "scinValidator");


        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
          public UntypedActor create() {
           return new GEORefValidator("fp.services.GeoLocate2",true,certainty,flwtValidator);
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
                return new InternalDateValidator("fp.services.InternalDateValidationService", writer);
            }
        }), "geoValidator");

        final ActorRef annotationInserter = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new AnnotationInserter(writer);
            }
        }), "annotationInserter");

         final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoSummaryWriter("/home/tianhong/data/test.json");
            }
        }), "MongoDBWriter");
                                  */

        final ActorRef writer = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoSummaryWriter(host,db,collectionOut,null);
            }
        }), "MongoDBWriter");



        final ActorRef geoValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new GEORefValidator("fp.services.GeoLocate2",false,certainty,writer);
            }
        }), "geoValidator");


        final ActorRef dateValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new InternalDateValidator("fp.services.InternalDateValidationService", geoValidator);
            }
        }), "dateValidator");

        final ActorRef scinValidator = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new NewScientificNameValidator("fp.services.COLService",true,true,dateValidator);
            }
        }), "scinValidator");

        final ActorRef reader = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new MongoDBReader(host,db,collectionIn,query,scinValidator);
            }
        }), "reader");



        // start the calculation
        System.err.println("systemstart#"+" " + "#" + System.currentTimeMillis());
        reader.tell(new Curate());
        //system.shutdown();
        system.awaitTermination();
        long stoptime = System.currentTimeMillis();
        //System.out.printf("\nTime: %f s\n",(stoptime-starttime)/1000.0);
        System.err.printf("runtime: %d",stoptime-starttime);
    }

    static class Curate {
    }
}

