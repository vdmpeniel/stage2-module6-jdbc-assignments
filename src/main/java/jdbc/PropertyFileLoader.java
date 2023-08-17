package jdbc;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class PropertyFileLoader {
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

        } catch (IOException ioe) {
            log.info(ioe.getMessage());
        }
    }

    public Properties getProperties(){
        return properties;
    }
}
