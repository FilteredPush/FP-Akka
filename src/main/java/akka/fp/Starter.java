package akka.fp;

import akka.actor.ActorRef;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 16.10.2013
 * Time: 13:08
 * To change this template use File | Settings | File Templates.
 */
public class Starter extends UntypedConsumerActor {
    private final ActorRef listener;

    public Starter(ActorRef reader) {
        this.listener = reader;
    }

    public String getEndpointUri() {
        return "jetty:http://localhost:8877/camel/default";
    }

    public void onReceive(Object message) {
        if (message instanceof CamelMessage) {
            CamelMessage camelMessage = (CamelMessage) message;
            String body = "Bla";
            getSender().tell(String.format("Received message: %s",body), getSelf());
            //listener.tell(new FP.Curate(), getSelf());
            listener.tell("XXXXXXXXXXXXXXXXXXXX", getSelf());
        } else
            unhandled(message);
    }
}
