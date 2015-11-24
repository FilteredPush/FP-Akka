package org.filteredpush.model.annotations;

import com.viceversatech.rdfbeans.annotations.RDF;
import com.viceversatech.rdfbeans.annotations.RDFBean;
import com.viceversatech.rdfbeans.annotations.RDFNamespaces;
import com.viceversatech.rdfbeans.annotations.RDFSubject;
import org.filteredpush.model.dwc.Occurrence;

import java.util.Date;
import java.util.UUID;

import static org.filteredpush.model.annotations.Namespace.BASE_URI;
import static org.filteredpush.model.annotations.Namespace.OA;

@RDFNamespaces({
        "oa = " + OA,
        "state = " + BASE_URI + "state/"
})
@RDFBean("oa:TimeState")
public class State {
    private String id;
    private Date when;
    private Occurrence cachedSource;

    public State() {
        id = UUID.randomUUID().toString();
    }

    @RDFSubject(prefix="state:")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @RDF("oa:when")
    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
    }

    @RDF("oa:cachedSource")
    public Occurrence getCachedSource() {
        return cachedSource;
    }

    public void setCachedSource(Occurrence cachedSource) {
        this.cachedSource = cachedSource;
    }
}
