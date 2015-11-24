package org.filteredpush.model.util;

import org.filteredpush.model.annotations.*;
import org.filteredpush.model.dwc.DwcTriplet;
import org.filteredpush.model.dwc.Georeference;
import org.filteredpush.model.dwc.Identification;

/**
 * Created by lowery on 10/20/15.
 */
public class AnnotationUtil {
    public static DwcTriplet dwcTripletFromAnnotation(Annotation<SpecificResource<DwcTripletSelector>, ?> annotation) {
        DwcTripletSelector selector = annotation.getTarget().getSelector();
        return new DwcTriplet(selector.getCollectionCode(), selector.getInstitutionCode(), selector.getCatalogNumber());
    }

    public static AnnotationDigest createAnnotationDigest(Annotation annotation) {
        return new AnnotationDigest(annotation.getUri().toString(),
                    annotation.getAnnotationType(), annotation.getAnnotator().getName(),
                    annotation.getAnnotated(), AnnotationUtil.dwcTripletFromAnnotation(annotation));
        }

    }
