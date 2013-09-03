package akka.fp;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 30.05.2013
 * Time: 15:55
 * To change this template use File | Settings | File Templates.
 */
public class Token<T> {
    private final T data;

    public Token(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
