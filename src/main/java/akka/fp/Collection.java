package akka.fp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: cobalt
 * Date: 15.05.2013
 * Time: 10:22
 * To change this template use File | Settings | File Templates.
 */
public class Collection<Type> {

    final String label;
    final List<Type> elements;
    final Map<String,?> annotations;

    public Collection(String label) {
        this.label = label;
        this.elements = new LinkedList<Type>();
        this.annotations = new HashMap<String, Object>();
    }

    public Collection(String label, List<Type> elements) {
        this.label = label;
        this.elements = elements;
        this.annotations = new HashMap<String, Object>();
    }

    public Collection(String label, List<Type> elements, Map<String, ?> annotations) {
        this.elements = elements;
        this.label = label;
        this.annotations = annotations;
    }

    public List<Type> getElements() {
        return elements;
    }

    public String getLabel() {
        return label;
    }

    public Map<String, ?> getAnnotations() {
        return annotations;
    }
}
