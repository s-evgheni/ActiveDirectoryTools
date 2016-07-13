import ad.connection.ActiveDirectoryFactoryConnection;
import controlInterface.AppControlInterface;

import javax.naming.NamingException;



public class RunApp {

    public static void main(String[] args) {

        AppControlInterface.checkIfActiveDirectoryServerIsUp();
        AppControlInterface.setupConnectionDetails();
        AppControlInterface.changeUserPassword();

        if(ActiveDirectoryFactoryConnection.getInstance().getLdapContext() != null){
            try {
                ActiveDirectoryFactoryConnection.getInstance().getLdapContext().close();
            } catch (NamingException e) {
                System.err.println("Failed to close LdapContext" + e.getExplanation());
                e.printStackTrace();
            }
        }
    }
}
