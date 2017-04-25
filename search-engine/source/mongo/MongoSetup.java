package mongo;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

/**
 * A sort of quick and dirty Mongo manager.
 */
public class MongoSetup
{
    Mongo  mongo = null;
    DB     database = null;
    String databaseName;

    public MongoSetup()
    {
        setup();
    }

    public MongoSetup(String database)
    {
        setup();
        this.databaseName = database;
    }

    private void setup()
    {
        try
        {
            mongo = new Mongo();
        }
        catch( UnknownHostException e )
        {
            e.printStackTrace();
        }
    }

    public String getDatabaseName() { 
    	return this.databaseName; 
    }
    
    public void setDatabaseName(String databaseName) { 
    	this.databaseName = databaseName; 
    }

    public DB getDatabase() { 
    	return this.database; 
    }

    public DBCollection getCollection( String collection )
    {
        return this.getCollection(this.databaseName, collection) ;
    }
    
    public DBCollection getCollection(String database, String collection)
    {
        this.database = mongo.getDB(database);
        return this.database.getCollection(collection);
    }
    
}