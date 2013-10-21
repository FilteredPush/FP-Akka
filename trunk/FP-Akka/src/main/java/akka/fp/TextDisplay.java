package akka.fp;

import akka.Token;
import akka.TokenWithProv;
import akka.actor.UntypedActor;
import akka.routing.Broadcast;
import fp.util.SpecimenRecord;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
* To change this template use File | Settings | File Templates.
*/
public class TextDisplay extends UntypedActor {
    int cRecords = 0;
    int invoc = 0;
    public void onReceive(Object message) {
        long start = System.currentTimeMillis();
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
                System.out.print("[ ");
                for (String s : ((SpecimenRecord)message).keySet()) {
                    System.out.printf("%s: %s, ", s, ((SpecimenRecord)message).get(s));
                }
                System.out.println("]");
                if (++cRecords % 100 == 0) {
                    System.out.println("Wrote " + cRecords + " records.");
                }
            }
        } else if (message instanceof Broadcast) {
            getSelf().tell(((Broadcast) message).message(), getSender());
        } else {
            unhandled(message);
        }
        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n",this.getClass().getSimpleName(),invoc,start,System.currentTimeMillis());
        invoc++;
    }

    @Override
    public void postStop() {
        //System.out.println("Stopped Display");
        System.out.println("Wrote " + cRecords + " records.");
        getContext().system().shutdown();
        super.postStop();
    }
}
