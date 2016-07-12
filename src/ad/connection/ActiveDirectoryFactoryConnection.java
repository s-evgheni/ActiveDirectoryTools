package ad.connection;

import org.apache.commons.lang.StringUtils;
import util.tools.properties.ConfigFileReader;
import util.tools.properties.ConfigProperty;

import javax.naming.Context;
import javax.naming.ldap.LdapContext;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;


import static java.lang.System.exit;
/**
 * Created by evgheni on 16-07-06.
 */
public class ActiveDirectoryFactoryConnection {

    private static ActiveDirectoryFactoryConnection instance;

    private LdapContext ldapContext;
    private Properties properties;

    private ActiveDirectoryFactoryConnection() {
        init();
    }

    private void init() {
        loadPropertiesFromFile();

        //build username/password for LDAP bind
        String userName = System.getProperty(ConfigProperty.AD_BIND_USERNAME)
                + "@"
                + properties.getProperty(ConfigProperty.AD_BASE_DN);

        String userPassword = System.getProperty(ConfigProperty.AD_BIND_USER_PWD);

        initTrustStore();

        bindUserToActiveDirectory(userName, userPassword);
    }

    public static ActiveDirectoryFactoryConnection getInstance() {
        if (instance == null) {
            instance = new ActiveDirectoryFactoryConnection();
        }
        return instance;
    }

    public LdapContext getLdapContext() {
        return ldapContext;
    }

    private void loadPropertiesFromFile() {
        if(properties == null)
            properties = new ConfigFileReader().getProperties();
    }

    //NOTE: must be executed after loadPropertiesFromFile()
    private void initTrustStore() {
        if(properties != null) {
            System.setProperty("javax.net.ssl.trustStore", properties.getProperty(ConfigProperty.TRUSTSTORE_PATH));
            System.setProperty("javax.net.ssl.trustStorePassword", properties.getProperty(ConfigProperty.TRUSTSTORE_PWD));
            return;
        }
        exit(1);
    }

    private void bindUserToActiveDirectory(String userName, String userPassword) {
        if(properties != null) {
            LdapHost ldapHost = buildLdapHostFromApplicationProperties();
            if(ldapHost == null)
                exit(1);


            //populate Ldap connection context
            Properties connectionContext = buildConnectionContextForHost(ldapHost);
            if(connectionContext == null)
                exit(1);

            addUserToConnectionContext(connectionContext, userName, userPassword);

            try {
                ldapContext = new InitialLdapContext(connectionContext, null);
                System.out.println("... bind to active directory succeeded on behalf of: " + connectionContext.getProperty(Context.SECURITY_PRINCIPAL));
                return;
            } catch (NamingException e) {
                System.err.println("[ERROR] Connection to active directory server failed!");
                e.printStackTrace();
                exit(1);
            }
        }

        System.err.println("[ERROR] Failed to read data from application properties");
        exit(1);
    }

    private LdapHost buildLdapHostFromApplicationProperties() {
        if(properties != null) {
            return new LdapHost(properties.getProperty(ConfigProperty.AD_IP),
                                Integer.parseInt(properties.getProperty(ConfigProperty.AD_PORT))
                               );
        }

        System.err.println("[ERROR] Failed to build LDAP host from property file ");
        return null;
    }

    private Properties buildConnectionContextForHost(LdapHost host) {
        if(properties != null && host != null) {
            Properties connectionContext = new Properties();
            connectionContext.put(Context.REFERRAL, "follow");
            connectionContext.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            connectionContext.put(Context.SECURITY_AUTHENTICATION, "simple");
            connectionContext.put(Context.SECURITY_PROTOCOL, "ssl");

            //build ldap URL
            StringBuilder sb = new StringBuilder(host.getUrl());
            sb.append("/");

            StringTokenizer st = new StringTokenizer(properties.getProperty(ConfigProperty.AD_BASE_DN), ".");
            while (st.hasMoreTokens()) {
                sb.append("DC=");
                sb.append(st.nextToken());
                sb = st.hasMoreTokens() ? sb.append(",") : sb.append("");
            }

            String ldapURL = sb.toString();

            connectionContext.put(Context.PROVIDER_URL, ldapURL);
            return connectionContext;
        }
        System.err.println("[ERROR] Failed to build connection context for specified host");
        return null;
    }

    private void addUserToConnectionContext(Properties connectionContext, String username, String password) {
        if(StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && connectionContext != null) {
            connectionContext.put(Context.SECURITY_PRINCIPAL, username);
            connectionContext.put(Context.SECURITY_CREDENTIALS, password);
            return;
        }
        System.err.println("[ERROR] Failed to add user to connection context");
        exit(1);
    }
}
