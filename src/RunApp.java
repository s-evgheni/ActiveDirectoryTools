import ad.connection.ActiveDirectoryFactoryConnection;
import ad.connection.LdapHost;
import ad.dao.UserDAO;
import ad.dao.impl.UserDAOImpl;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import static java.lang.System.exit;


public class RunApp {

    private static Boolean verbose_mode_enabled = true;

    public static void main(String[] args) {

        verbose("Checking if directory server is up");
        if(LdapHost.isServiceUp()) {
            verbose("... server is up");
        }
        else {
            System.err.println(".. directory service is not running, shutting down.");
            exit(1);
        }

        verbose("Trying bind to ActiveDirectory");

        System.out.println("Checking DN for test.user");
        UserDAO dao = new UserDAOImpl();
        String sAMAccountName="test.user";
        String dn = dao.getUserDN(sAMAccountName);
        System.out.println("... retrieved DN as: " + dn);
        String newUserPassword = "P@ssw0rd!!!";
        System.out.println("Changing "+ sAMAccountName +" password to: "+ newUserPassword);
        if(dao.changePassword(sAMAccountName, newUserPassword)){
            System.out.println("password change has been successful");
        }
    }

    private static void verbose(String message) {
        if(verbose_mode_enabled){
            System.out.println(message);
        }
    }
}
