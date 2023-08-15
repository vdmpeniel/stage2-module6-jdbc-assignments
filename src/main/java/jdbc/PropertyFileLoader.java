package jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class PropertyFileLoader {
    private final Logger logger = Logger.getLogger("CustomConnector");
    private final Properties properties = new Properties();


    public PropertyFileLoader(String filename){
        loadPropertyFile(filename);
    }
    private void loadPropertyFile(String filename){

        // Load properties from file
        try (InputStream inputStream = PropertyFileLoader.class
                .getClassLoader()
                .getResourceAsStream(filename)
        ) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

    }

    public Properties getProperties(){
        return properties;
    }

    public static void main(String[] args) {
        PropertyFileLoader pl = new PropertyFileLoader("app.properties");
        Properties props = pl.getProperties();
        System.out.println("Driver: " + props.getProperty("postgres.driver"));
    }
}
