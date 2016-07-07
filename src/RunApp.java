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
        LdapContext ldapContext = ActiveDirectoryFactoryConnection.getInstance().getLdapContext();

        System.out.println("Checking DN for test.user");
        UserDAO dao = new UserDAOImpl();
        String dn = dao.getUserDN("test.user");
        System.out.println("... retrieved DN as: " + dn);

        try {
            verbose("Closing connection to AD");
            ldapContext.close();
        } catch (NamingException e) {
            e.printStackTrace();
            exit(1);
        }
    }

    private static void verbose(String message) {
        if(verbose_mode_enabled){
            System.out.println(message);
        }
    }
}
