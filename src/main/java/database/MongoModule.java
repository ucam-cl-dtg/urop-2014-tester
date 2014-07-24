package database;

import com.google.inject.AbstractModule;
import com.mongodb.DB;

public class MongoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DB.class).toInstance(Mongo.getDb());

    }
}
