package util.tools;

import java.util.Properties;

/**
 * Created by evgheni.s on July 07, 2016.
 */
public interface PropertiesReader {
    public Properties getProperties();
    public String getProperty(String propertyName);
}
