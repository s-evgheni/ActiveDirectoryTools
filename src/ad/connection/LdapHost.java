package ad.connection;

import util.tools.ConfigFileReader;
import util.tools.ConfigProperty;
import util.tools.PropertiesReader;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by evgheni.s on July 07, 2016.
 */
public class LdapHost {

    private String hostName;
    private int servicePort;

    public LdapHost(String hostName, int servicePort) {
        this.hostName = hostName;
        this.servicePort = servicePort;
    }

    public static boolean isServiceUp() {
        Socket socket = new Socket();
        int timeoutInMs = 5000;
        InetSocketAddress connectionDetails;
        boolean result;
        PropertiesReader reader = new ConfigFileReader();

        try {
            connectionDetails = new InetSocketAddress(reader.getProperty(ConfigProperty.AD_IP),
                                                      Integer.parseInt(reader.getProperty(ConfigProperty.AD_PORT))
                                                     );
            socket.connect(connectionDetails, timeoutInMs);
            result = socket.isConnected();
            socket.close();
        }
        catch (IOException ex) {
            System.err.println("Failed to establish connection with host @ " + reader.getProperty(ConfigProperty.AD_IP));
            return false;
        }

        return result;
    }

    public String getUrl() {
        StringBuilder sb = servicePort == 636 ? new StringBuilder("ldaps://") : new StringBuilder("ldap://");
        sb.append(hostName);
        sb.append(":");
        sb.append(servicePort);
        return sb.toString();
    }

    public String getIP() {
        try {
            return InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "0.0.0.0";
        }
    }

    public String getServicePort() {
        return Integer.toString(servicePort);
    }

}
