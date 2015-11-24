package org.filteredpush.model.dwc;

/**
 * Created by lowery on 10/20/15.
 */
public class DwcTriplet {
    private String collectionCode;
    private String institutionCode;
    private String catalogNumber;

    public DwcTriplet(String collectionCode, String institutionCode, String catalogNumber) {
        this.collectionCode = collectionCode;
        this.institutionCode = institutionCode;
        this.catalogNumber = catalogNumber;
    }

    public DwcTriplet() {

    }

    public String getCollectionCode() {
        return collectionCode;
    }

    public void setCollectionCode(String collectionCode) {
        this.collectionCode = collectionCode;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(String catalogNumber) {
        this.catalogNumber = catalogNumber;
    }
}
