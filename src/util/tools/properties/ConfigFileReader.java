package util.tools.properties;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.System.exit;

/**
 * Created by evgheni.s on July 07, 2016.
 */
public class ConfigFileReader implements PropertiesReader {

    private static Properties toolProperties;

    public ConfigFileReader() {
        if(toolProperties == null){
            toolProperties = new Properties();
            try {
                InputStream inputData = ConfigFileReader.class.getClassLoader().getResourceAsStream(ConfigProperty.PROPERTIES_FILE_NAME);
                toolProperties.load(inputData);
            }
            catch (IOException e) {
                e.printStackTrace(System.err);
                exit(1);
            }
        }
    }

    public Properties getProperties() {
        return toolProperties;
    }

    public String getProperty(String propertyKey){
        if(StringUtils.isBlank(propertyKey)) {
            return "";
        }

        toolProperties = getProperties();

        if(toolProperties.isEmpty()) {
            return "";
        }

        return toolProperties.getProperty(propertyKey);
    }

}
