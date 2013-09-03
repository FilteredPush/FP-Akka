package akka.fp;

import akka.actor.*;
import fp.services.GeoLocate2;
import fp.util.CurationComment;
import akka.routing.Broadcast;
import akka.routing.SmallestMailboxRouter;
import fp.util.SpecimenRecord;

import java.util.Random;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:05
* To change this template use File | Settings | File Templates.
*/
public class GeoRefValidateActor extends UntypedActor {

    private final ActorRef listener;
    private final ActorRef workerRouter;
    private boolean done;
    private int num;

    public GeoRefValidateActor(Double certainty, ActorRef listener) {

        this.listener = listener;

        workerRouter = this.getContext().actorOf(new Props(GeoRefValidateInvocation.class).withRouter(
                new SmallestMailboxRouter(4)), "workerRouter");
        num = 0;
        done = false;
        getContext().watch(workerRouter);
    }

    public void onReceive(Object message) {
        //System.out.println("GeoRef message: "+ message+toString());
        if (message instanceof Token) {
            if (!getSender().equals(getSelf())) {
                workerRouter.tell(message, getSelf());
            } else {
                listener.tell(message, getSelf());
            }
        } else if (message instanceof Broadcast) {
            workerRouter.tell(new Broadcast(((Broadcast) message).message()), getSender());
        } else if (message instanceof Terminated) {
            if (((Terminated) message).getActor().equals(workerRouter))
                this.getContext().stop(getSelf());
        } else {
            unhandled(message);
        }
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped GeoRefValidator");
        listener.tell(new Broadcast(PoisonPill.getInstance()), getSelf());
    }

    public static class GeoRefValidateInvocation extends UntypedActor {

        private final GeoLocate2 gl;
        private final Random rand;
        int invoc;
        private final ActorRef listener;

        public GeoRefValidateInvocation() {
            super();
            gl = new GeoLocate2();
            rand = new Random();
            listener = null;
        }

        public GeoRefValidateInvocation(ActorRef listener) {
            super();
            gl = new GeoLocate2();
            rand = new Random();
            this.listener = listener;
        }


        public void onReceive(Object message) {
            //System.out.println("Worker message: "+ message+toString());
            long start = System.currentTimeMillis();
            invoc = rand.nextInt();

            if (message instanceof TokenWithProv) {
                Prov.log().printf("datadep\t%s\t%d\t%s\t%d\t%d\t%d\n",
                                        ((TokenWithProv) message).getActorCreated(),
                                        ((TokenWithProv) message).getInvocCreated(),
                                        this.getClass().getSimpleName(),
                                        invoc,
                                        ((TokenWithProv) message).getTimeCreated(),
                                        System.currentTimeMillis());
            }

            if (message instanceof Token) {
                if (((Token) message).getData() instanceof SpecimenRecord) {


                    gl.validateGeoRef(((SpecimenRecord) ((Token) message).getData()).get("country"),
                            ((SpecimenRecord) ((Token) message).getData()).get("stateProvince"),
                            ((SpecimenRecord) ((Token) message).getData()).get("county"),
                            ((SpecimenRecord) ((Token) message).getData()).get("locality"),
                            ((SpecimenRecord) ((Token) message).getData()).get("latitude"),
                            ((SpecimenRecord) ((Token) message).getData()).get("longitude"),
                            200.0);

                    SpecimenRecord res = new SpecimenRecord((SpecimenRecord)((Token) message).getData());
                    if (gl.getCurationStatus() == CurationComment.CURATED) {
                        res.put("latitude",Double.toString(gl.getCorrectedLatitude()));
                        res.put("longitude",Double.toString(gl.getCorrectedLongitude()));
                    }
                    res.put("geoRefStatus",gl.getCurationStatus().toString());
                    //res.put("geoRefComment",gl.getComment());
                    if (listener == null) {
                        getSender().tell(new TokenWithProv<SpecimenRecord>(res,this.getClass().getSimpleName(),invoc), getContext().parent());
                    } else {
                        listener.tell(new TokenWithProv<SpecimenRecord>(res,this.getClass().getSimpleName(),invoc), getContext().parent());
                    }
                } else {
                    unhandled(message);
                }
                Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
            }
        }

        @Override
        public void postStop() {
            super.postStop();
            //System.out.println("Worker done!");
        }
    }
}
