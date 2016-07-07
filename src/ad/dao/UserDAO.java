package ad.dao;

import ad.model.ActiveDirectoryUser;

import javax.naming.NameNotFoundException;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by evgheni.s on July 07, 2016.
 */
public interface UserDAO {

    /**
     * Changes password for specified user
     * @param userName - account username
     * @param password - new password
     * @return
     */
    public boolean changePassword(String userName, String password);

    /**
     * Searches for specified user and returns user's distinguishedName name
     * @param userName
     * @return
     */
    public String getUserDN(String userName);

    /**
     * Searches for specified user group and returns group's distinguishedName name
     * @param groupName
     * @return
     */
    public String getGroupDN(String groupName);

    /**
     * Searches for specified user and returns user's cn name <br>
     * Ex: "cn=user1,cn=users"
     * @param userName
     * @return
     * @throws NameNotFoundException
     */
    public String getUserCN(String userName) throws NameNotFoundException;

    /**
     * Searches for specified user group and returns group's cn name<br>
     * Ex: "cn=user1,cn=users"
     * @param groupName
     * @return
     */
    public String getGroupCN(String groupName);

    /**
     * Creates new user account with an option and force him to set a new password on next login
     * @param activeDirectoryUser - ad user object to create
     * @param changePasswordNextLogon - if set to true user will be forced to set new password on next login
     * @return
     */
    public boolean addUser(ActiveDirectoryUser activeDirectoryUser, boolean changePasswordNextLogon);

    /**
     * Set user account expiration date
     * @param userName - user
     * @param dateToExpire - date on which account should expire
     * @return
     */
    public boolean setDateExpireAccount(String userName, GregorianCalendar dateToExpire);

    /**
     * Searches for specified user in AD and returns it as ActiveDirectoryUser object
     * @param userName
     * @return
     */
    public ActiveDirectoryUser getUser(String userName);

    /**
     * Deletes user from AD
     * @param userName
     * @return
     */
    public boolean removeUser(String userName);

    /**
     * Enables user in AD
     * @param userName
     * @return
     */
    public boolean enableUser(String userName);

    /**
     * Disables user in AD
     * @param userName
     * @return
     */
    public boolean disableUser(String userName);

    /**
     * Add user to specific group in AD
     * @param userName
     * @param groupName
     * @return
     */
    public boolean addUserToGroup(String userName, String groupName);

    /**
     * Removes user from specific group in AD
     * @param userName
     * @param groupName
     * @return
     */
    public boolean removeUserFromGroup(String userName, String groupName);

    /**
     * Returns a lists of all users in specific group
     * @param groupName
     * @return
     */
    public List<String> getMembersFromGroup(String groupName);
}
