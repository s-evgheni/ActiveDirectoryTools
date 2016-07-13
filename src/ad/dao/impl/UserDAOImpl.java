package ad.dao.impl;

import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import javax.naming.ldap.LdapContext;

import java.util.GregorianCalendar;
import java.util.List;

import java.io.UnsupportedEncodingException;

import ad.connection.ActiveDirectoryFactoryConnection;
import ad.dao.UserDAO;
import ad.model.ActiveDirectoryUser;
import ad.model.LdapAttributes;
import org.apache.commons.lang.StringUtils;


/**
 * Created by evgheni.s on July 07, 2016.
 */
public class UserDAOImpl implements UserDAO {

    private static final int FLAG_TO_DISABLE_USER = 0x2;

    private static final int UF_PASSWORD_EXPIRED = 0x800000;
    private static final int UF_NORMAL_ACCOUNT = 0x0200;
    private static final int CHANGE_PASSWORD_NEXT_LOGON = -1;


    /*
       There are two ways to modify the unicodePwd attribute.
       resetUserPasswordAsAdmin method below is analogous to an administrator resetting a password for a user.
       To do this, the client must have bound as an administrator a user who has sufficient rights to modify other users' passwords.
       The modify request should contain a single replace operation with the new password enclosed in quotation marks and be Base64 encoded.
       If the client has sufficient rights, this password becomes the new password regardless of what the old password was.
       The change might take a few minutes to be applied @ AD server side. During this time user who's password has been changed will be able
       to use his new and old password during bind/login operation.

       IMPORTANT NOTE:
       Successful password reset will trigger reset of the values defined in Default Domain Password Policy for that user.
       If an administrator sets a password for a user and wants that user to change the administrator-defined password,
       the administrator must add option 'User must change password at next logon'.
       Otherwise, the user will not be able to change the password until the number of days specified in
       'Minimum password age' attribute in the Policy.

       Default value for minimum password Age on AD domain is usually set to 1 day.
       Setting the number of days to 0 on minimum password change policy will allow
       immediate password changes, which is not recommended.

       Passwrod policies can be configured via Group Policy Management tool on MS AD Domain Server
       For HOW TO on Group Policy Management see: https://www.youtube.com/watch?v=buZewCeg_cY
    */
    @Override
    public boolean resetUserPasswordAsAdmin(String userName, String newPassword) {
        LdapContext ldapContext = ActiveDirectoryFactoryConnection.getInstance().getLdapContext();

        try {
            ModificationItem[] mods = new ModificationItem[1];

            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                                           new BasicAttribute(LdapAttributes.UNICODE_PWD, encodePassword(newPassword))
                                          );
            ldapContext.modifyAttributes(getUserCN(userName), mods);

            return true;
        }
        catch(NamingException e) {
            System.err.println("[ERROR] :" + e.getExplanation());
            return false;
        }
        catch (Exception e) {
            System.err.println("[ERROR] Something went wrong. See stacktrace details below");
            return false;
        }
    }

    /*
        IMPORTANT NOTES:
        There are two ways to modify the unicodePwd attribute.
        changePassword method below is analogous to a typical user change-password operation.
        In this case, the modify request must contain both a delete operation and an add operation.
        The delete operation must contain the current password enclosed in quotation marks and be Base64 encoded as described in RFC 1521.'
        The add operation must contain the new password enclosed in quotation marks and be Base64 encoded.
        As with  resetUserPasswordAsAdmin method successful password change
        will trigger reset of the values defined in Default Domain Password Policy for that user.
        Subsequent attempts to change user password will cause exception similar to:
        [LDAP: error code 19 - 0000052D: AtrErr: DSID-03191083, #1:
	    0: 0000052D: DSID-03191083, problem 1005 (CONSTRAINT_ATT_TYPE), data 0, Att 9005a (unicodePwd)[]];
    */

    @Override
    public boolean changePassword(String userName, String oldPassword, String newPassword) {
        LdapContext ldapContext = ActiveDirectoryFactoryConnection.getInstance().getLdapContext();

        System.out.println("Trying to change "+ userName +" password from <"+ oldPassword + "> to <"+ newPassword+">");

        try {
            ModificationItem[] mods = new ModificationItem[2];

            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                                           new BasicAttribute(LdapAttributes.UNICODE_PWD, encodePassword(oldPassword)));

            mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE,
                                           new BasicAttribute(LdapAttributes.UNICODE_PWD, encodePassword(newPassword)));

            ldapContext.modifyAttributes(getUserCN(userName), mods);

            return true;
        }
        catch(NamingException e) {
            System.err.println("[ERROR] :" + e.getExplanation());
            return false;
        }
        catch (Exception e) {
            System.err.println("[ERROR] Something went wrong. See stacktrace details below");
            System.err.println(e);
            return false;
        }
    }

    private byte[] encodePassword(String password) throws UnsupportedEncodingException {
        String quotedPassword = "\"" + password + "\"";

        byte [] encodedPassResult = quotedPassword.getBytes("UTF-16LE");

        StringBuilder sb = new StringBuilder();

        System.out.print("encodedPassResult as byte[]: ");
        for (int i = 0; i < encodedPassResult.length; i++) {
            System.out.print(encodedPassResult[i] + " ");
        }
        System.out.println();
        System.out.println("encodedPassResult as String: " + new String(encodedPassResult));

        return encodedPassResult;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String getUserDN(String userName){
        LdapContext ldapContext = ActiveDirectoryFactoryConnection.getInstance().getLdapContext();

        String distinguishedName = "";
        String returnedAtts[] = { LdapAttributes.OBJECT_CLASS, LdapAttributes.DISTINGUISHED_NAME };
        try {
            String searchFilter = "(&(objectClass=user)(sAMAccountName="+userName+"))";
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            searchCtls.setReturningAttributes(returnedAtts);
            NamingEnumeration answer = ldapContext.search("", searchFilter, searchCtls);
            if (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    Attribute attr = attrs.get(LdapAttributes.DISTINGUISHED_NAME);
                    if(attr != null) {
                        distinguishedName = (String)attr.get();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return distinguishedName;
    }

    @Override
    public String getGroupDN(String groupName) {
        return null;
    }

    @Override
    public String getUserCN(String userName) throws NameNotFoundException {
        String dn = getUserDN(userName);

        if (StringUtils.isBlank(dn)) {
            throw new NameNotFoundException();
        }

        return dn.substring(0, dn.indexOf("DC=")-1);
    }

    @Override
    public String getGroupCN(String groupName) {
        return null;
    }

    @Override
    public boolean addUser(ActiveDirectoryUser activeDirectoryUser, boolean changePasswordNextLogon) {
        return false;
    }

    @Override
    public boolean setDateExpireAccount(String userName, GregorianCalendar dateToExpire) {
        return false;
    }

    @Override
    public ActiveDirectoryUser getUser(String userName) {
        return null;
    }

    @Override
    public boolean removeUser(String userName) {
        return false;
    }

    @Override
    public boolean enableUser(String userName) {
        return false;
    }

    @Override
    public boolean disableUser(String userName) {
        return false;
    }

    @Override
    public boolean addUserToGroup(String userName, String groupName) {
        return false;
    }

    @Override
    public boolean removeUserFromGroup(String userName, String groupName) {
        return false;
    }

    @Override
    public List<String> getMembersFromGroup(String groupName) {
        return null;
    }
}
