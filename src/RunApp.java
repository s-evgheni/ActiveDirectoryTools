import ad.connection.LdapHost;
import ad.dao.UserDAO;
import ad.dao.impl.UserDAOImpl;
import org.apache.commons.lang.StringUtils;
import util.tools.inputOutput.CharacterDevice;
import util.tools.inputOutput.InputOutputDevice;
import util.tools.properties.ConfigProperty;

import java.io.*;

import static java.lang.System.exit;


public class RunApp {

    private static Boolean verbose_mode_enabled = true;

    static String adBindUser = "";        //user used to bind to AD
    static String adBindUserPwd = "";     //his password

    static String adUser = "";            //user to be manipulated in AD


    static Boolean adminMode;

    private static InputOutputDevice IO = streamDevice(System.in, System.out);

    public static void main(String[] args) {

        verbose("Checking if directory server is up");
        if(LdapHost.isServiceUp()) {
            verbose("... server is up");
        }
        else {
            System.err.println(".. directory service is not running, shutting down.");
            exit(1);
        }

        adminMode = true;

        try {
            if(adminMode) {
                adBindUser = askUserName("Enter Domain Admin user name");
                adBindUserPwd = askPassword("Enter Domain Admin password");
                adUser = askUserName("Enter sAMAccountName of the user which will be manipulated in AD");
            }
            else {
                adBindUser = askUserName("Enter username");
                adBindUserPwd = askPassword("Enter user password");
                adUser = adBindUser;
            }
            System.setProperty(ConfigProperty.AD_BIND_USERNAME, adBindUser);
            System.setProperty(ConfigProperty.AD_BIND_USER_PWD, adBindUserPwd);
            verbose("______________________________________________________________________");
            verbose("sAMAccountName of the bind user: " + adBindUser);
            verbose("Bind user password: " + adBindUserPwd);
            verbose("sAMAccountName of the user which will be manipulated in AD: " + adUser);
            verbose("______________________________________________________________________");
        }
        catch (Exception e) {
            System.err.println("Error when parsing CLI arguments..: " + e.getMessage());
            exit(1);
        }


        String newUserPassword = "";
        try {
            newUserPassword = askPasswordTwice("Enter new user password");
            if(StringUtils.isBlank(newUserPassword) && newUserPassword.length()<=5) {
                System.err.println("New password must be at least 6 characters long");
                exit(1);
            }
        }
        catch (Exception e){
            System.err.println("Error when parsing CLI arguments..: " + e.getMessage());
            exit(1);
        }

        System.out.println("Checking DN for " + adUser);
        UserDAO dao = new UserDAOImpl();
        String sAMAccountName = adUser;
        String dn = dao.getUserDN(adUser);
        System.out.println("... retrieved DN as: " + dn);
        System.out.println("Changing "+ sAMAccountName +" password to: "+ newUserPassword);

        boolean result = adminMode ? dao.changePassword(adBindUser, adBindUserPwd, newUserPassword) : dao.resetUserPasswordAsAdmin(sAMAccountName, newUserPassword);

        if(result) {
            System.out.println("password change has been successful");
        }
        else {
            System.out.println("Failed to change password");
        }
    }

    private static void verbose(String message) {
        if(verbose_mode_enabled){
            System.out.println(message);
        }
    }

    public static String askUserName(String usernamePrompt) throws Exception {
        String username;
        if (IO != null) {
            IO.printf(usernamePrompt + ": ");
            username = IO.readLine();
        } else {
            throw new Exception();
        }
        return username;
    }

    public static String askPassword(String passwordPrompt) throws Exception {
        String password;
        if (IO != null) {
            IO.printf(passwordPrompt);
            char[] pwd = IO.readPassword();
            password = String.valueOf(pwd);
        } else {
            throw new Exception();
        }
        return password;
    }

    public static String askPasswordTwice(String passwordPrompt) throws Exception {
        String newPassword = askPassword(passwordPrompt + ": ");
        String newPasswordConfirm = askPassword(passwordPrompt + " again: ");

        if (newPassword.equals(newPasswordConfirm)) {
            verbose("passwords match, processing change ...");
            return newPassword;
        } else {
            System.err.println("\nPasswords didn't matched, please repeat");
            return askPasswordTwice(passwordPrompt);
        }
    }

    /**
     * Returns a character I/O wrapper.
     * Default system encoding is used to decode/encode data.
     */
    private static InputOutputDevice streamDevice(InputStream in, PrintStream out) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out, true);
        return new CharacterDevice(reader, writer);
    }
}
