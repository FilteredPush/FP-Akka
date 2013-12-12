package akka;

import akka.actor.UntypedActor;
import akka.fp.Prov;
import akka.routing.Broadcast;

/**
* Created with IntelliJ IDEA.
* User: cobalt
* Date: 06.05.2013
* Time: 13:06
* To change this template use File | Settings | File Templates.
*/
public class TextDisplay extends UntypedActor {
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
            System.out.println(((Token) message).getData());
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
        getContext().system().shutdown();
        super.postStop();
    }
}
