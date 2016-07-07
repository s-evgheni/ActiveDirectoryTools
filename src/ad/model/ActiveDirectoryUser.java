package ad.model;

import java.util.List;

/**
 * Created by evgheni.s on July 07, 2016.
 */

public class ActiveDirectoryUser {
    private List<String> objectclass;

    private String userPrincipalName;

    private String sAMAccountName;

    @SuppressWarnings("unused")
    private String name;

    private String sn;

    @SuppressWarnings("unused")
    private String displayName;

    private String userAccountControl;

    private List<String> memberOf;

    private String unicodepwd;

    private String givenName;

    public String getUserAccountControl() {
        return userAccountControl;
    }

    public ActiveDirectoryUser setUserAccountControl(String userAccountControl) {
        this.userAccountControl = userAccountControl;
        return this;
    }

    public List<String> getObjectclass() {
        return objectclass;
    }

    public ActiveDirectoryUser setObjectclass(List<String> objectclass) {
        this.objectclass = objectclass;
        return this;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public ActiveDirectoryUser setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
        return this;
    }

    public String getsAMAccountName() {
        return sAMAccountName;
    }

    public ActiveDirectoryUser setsAMAccountName(String sAMAccountName) {
        this.sAMAccountName = sAMAccountName;
        return this;
    }

    public String getName() {
        return givenName + " "  + sn;
    }

    public ActiveDirectoryUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getSn() {
        return sn;
    }

    public ActiveDirectoryUser setSn(String sn) {
        this.sn = sn;
        return this;
    }

    public String getDisplayName() {
        return givenName + " "  + sn;
    }

    public ActiveDirectoryUser setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public List<String> getMemberOf() {
        return memberOf;
    }

    public ActiveDirectoryUser setMemberOf(List<String> memberOf) {
        this.memberOf = memberOf;
        return this;
    }

    public String getUnicodepwd() {
        return unicodepwd;
    }

    public ActiveDirectoryUser setUnicodepwd(String unicodepwd) {
        this.unicodepwd = unicodepwd;
        return this;
    }

    public String getGivenName() {
        return givenName;
    }

    public ActiveDirectoryUser setGivenName(String givenName) {
        this.givenName = givenName;
        return this;
    }
}
