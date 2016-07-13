package controlInterface;

import ad.connection.LdapHost;
import ad.dao.UserDAO;
import ad.dao.impl.UserDAOImpl;
import org.apache.commons.lang.StringUtils;
import util.tools.inputOutput.CharacterDevice;
import util.tools.inputOutput.InputOutputDevice;
import util.tools.properties.ConfigProperty;


import java.io.*;

import static java.lang.System.exit;

/**
 * Created by evgheni.s on July 13, 2016.
 */
public class AppControlInterface {
    private static Boolean verbose_mode_enabled = true;

    private static String adBindUser = "";        //user used to bind to AD
    private static String adBindUserPwd = "";     //his password

    private static String adUser = "";            //user to be manipulated in AD


    private static boolean adminMode;

    private static InputOutputDevice IO = streamDevice(System.in, System.out);

    public static void checkIfActiveDirectoryServerIsUp() {
        verbose("Checking if directory server is up");
        if(LdapHost.isServiceUp()) {
            verbose("... server is up");
        }
        else {
            System.err.println(".. directory service is not running, shutting down.");
            exit(1);
        }
    }

    public static void setupConnectionDetails() {
        try {
            adBindUser = askQuestion("Enter bind user name");
            adBindUserPwd = askPassword("Enter bind user password");
            adminMode = StringUtils.equalsIgnoreCase("y", askQuestion("Is bind user a domain administrator (Y/N)?"));
            adUser = adminMode ? askQuestion("Enter sAMAccountName of the user which will be manipulated in AD") : adBindUser;

            verbose("______________________________________________________________________");
            verbose("Bind sAMAccountName of the user: " + adBindUser);
            verbose("Bind user password: " + adBindUserPwd);
            verbose("Bind user is domain admin: " + adminMode);
            verbose("User which will be manipulated in AD: " + adUser);
            verbose("______________________________________________________________________");

            verbose("Saving bind user credentials as system properties");
            System.setProperty(ConfigProperty.AD_BIND_USERNAME, adBindUser);
            System.setProperty(ConfigProperty.AD_BIND_USER_PWD, adBindUserPwd);
        }
        catch (Exception e) {
            System.err.println("Error when parsing CLI arguments..: " + e.getMessage());
            exit(1);
        }
    }

    //ACTIVE DIRECTORY OPERATIONS:
    public static void changeUserPassword() {
        String newUserPassword = "";
        boolean userRequiredToChangePasswordOnNextLogon = false;

        try {
            newUserPassword = askPasswordTwice("Enter new password for " + adUser);

            if(adminMode)
                userRequiredToChangePasswordOnNextLogon = StringUtils.equalsIgnoreCase("y", askQuestion("Allow user to change password on next login (Y/N)?"));

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
        String dn = dao.getUserDN(adUser);
        System.out.println("... retrieved DN as: " + dn);

        boolean result = adminMode ? dao.resetUserPasswordAsAdmin(adUser, newUserPassword, userRequiredToChangePasswordOnNextLogon)
                :
                dao.changePassword(adBindUser, adBindUserPwd, newUserPassword);

        if(result) {
            System.out.println("password change has been successful");
        }
        else {
            System.out.println("Failed to change password");
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

    //HELPER METHODS:
    private static void verbose(String message) {
        if(verbose_mode_enabled){
            System.out.println(message);
        }
    }

    private static String askQuestion(String question) throws Exception {
        String username;
        if (IO != null) {
            IO.printf(question + ": ");
            username = IO.readLine();
        } else {
            throw new Exception();
        }
        return username;
    }

    private static String askPassword(String passwordPrompt) throws Exception {
        String password;
        if (IO != null) {
            IO.printf(passwordPrompt + ": ");
            char[] pwd = IO.readPassword();
            password = String.valueOf(pwd);
        } else {
            throw new Exception();
        }
        return password;
    }

    private static String askPasswordTwice(String passwordPrompt) throws Exception {
        String newPassword = askPassword(passwordPrompt);
        String newPasswordConfirm = askPassword(passwordPrompt + " again");

        if (newPassword.equals(newPasswordConfirm)) {
            verbose("passwords match, processing change ...");
            return newPassword;
        } else {
            System.err.println("\nPasswords didn't matched, please repeat");
            return askPasswordTwice(passwordPrompt);
        }
    }

}
