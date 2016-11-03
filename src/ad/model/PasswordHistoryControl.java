package ad.model;

import javax.naming.ldap.Control;

/**
 * Created by evgheni.s on November 03, 2016.
 */
public class PasswordHistoryControl implements Control {

    private static final long serialVersionUID = -4233907508771791687L;
    //control's object identifier string
    public final static String LDAP_SERVER_POLICY_HINTS_OID = "1.2.840.113556.1.4.2066";
    //control's ASN.1 BER encoded value
    public final static byte[] LDAP_SERVER_POLICY_HINTS_VALUE = { 48, (byte) 132, 0, 0, 0, 3, 2, 1, 1 };
    //The control's criticality
    protected boolean criticality = false; // default

    /**
     * Retrieves the control's object identifier string.
     *
     * @return The non-null object identifier string.
     */
    public String getID() {
        return LDAP_SERVER_POLICY_HINTS_OID;
    }

    /**
     * Determines the control's criticality.
     *
     * @return true if the control is critical; false otherwise.
     */
    public boolean isCritical() {
        return criticality;
    }

    /**
     * Retrieves the control's ASN.1 BER encoded value.
     *
     * @return A possibly null byte array representing the control's
     *          ASN.1 BER encoded value.
     */
    public byte[] getEncodedValue() {
        return LDAP_SERVER_POLICY_HINTS_VALUE;
    }

}
