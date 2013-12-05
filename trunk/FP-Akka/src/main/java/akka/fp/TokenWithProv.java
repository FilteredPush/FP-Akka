package akka.fp;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 30.05.2013
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
public class TokenWithProv<T> extends Token<T> {

    private final long timeCreated;
    private final String actorCreated;
    private final int invocCreated;

    public TokenWithProv(T data, String actorCreated, int invocCreated) {
        super(data);
        this.timeCreated = System.currentTimeMillis();
        this.actorCreated = actorCreated;
        this.invocCreated = invocCreated;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public String getActorCreated() {
        return actorCreated;
    }

    public int getInvocCreated() {
        return invocCreated;
    }
}
