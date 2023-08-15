package jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertyFileLoader {
    private final Logger LOGGER = Logger.getLogger("PropertyFileLoader");
    private final Properties PROPERTIES = new Properties();


    public PropertyFileLoader(String filename){
        loadPropertyFile(filename);
    }
    private void loadPropertyFile(String filename){

        // Load properties from file
        try (InputStream inputStream = PropertyFileLoader.class
                .getClassLoader()
                .getResourceAsStream(filename)
        ) {
            PROPERTIES.load(inputStream);

        } catch (IOException ioe) {
            LOGGER.info(ioe.getMessage());
        }

    }

    public Properties getPROPERTIES(){
        return PROPERTIES;
    }
}
