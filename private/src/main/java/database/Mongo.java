package database;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import configuration.ConfigurationFile;
import configuration.ConfigurationLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

public class Mongo {
    private static Logger log = LoggerFactory.getLogger(Mongo.class);
    private static DB db;

    static {
        log.debug("Initialising MongoDB");
        ConfigurationFile config = ConfigurationLoader.getConfig();
        try {
            MongoClient client = new MongoClient(config.getMongoHost(), config.getMongoPort());
            db = client.getDB(config.getMongoReportDBName());
            log.debug("MongoDB initialised");
        }
        catch (UnknownHostException e) {
            log.error("Couldn't connect to MongoDB");
        }
    }

    public static DB getDb() {
        return db;
    }
}
