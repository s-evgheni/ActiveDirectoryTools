package ad.dao.impl;

import javax.naming.NameNotFoundException;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;


import java.util.ArrayList;
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
        catch(NameNotFoundException e) {
            System.err.println("[ERROR] User not found!");
            return false;
        }
        catch (Exception e) {
            System.err.println("[ERROR] Something went wrong. See stacktrace details below");
            System.err.println(e);
            return false;
        }
    }

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
        catch(NameNotFoundException e) {
            System.err.println("[ERROR] User not found!");
            return false;
        }
        catch (Exception e) {
            System.err.println("[ERROR] Something went wrong. See stacktrace details below");
            System.err.println(e);
            return false;
        }
    }

    private byte[] encodePassword(String password) throws UnsupportedEncodingException {
        String newQuotedPassword = "\"" + password + "\"";
        return newQuotedPassword.getBytes("UTF-16LE");
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
