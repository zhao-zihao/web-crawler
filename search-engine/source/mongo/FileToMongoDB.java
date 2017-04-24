package mongo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;


/**
 * Java MongoDB : Convert JSON data to DBObject
 *
 */

public class FileToMongoDB {
	private static Mongo mongo = null;
	private static DB db = null;
    private static DBCollection collection = null;
    
	public static void cleanup()
    {
        // refresh the collection each time...
		try {
			mongo =  new Mongo("localhost", 27017);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
        db = mongo.getDB("stackoverflow");
        collection = db.getCollection("questions");
        collection.drop();
        collection = db.getCollection("questions");
    }
	
	public static void StoreFileToDB(File file) {
		JSONParser parser = new JSONParser();
		
		try {
			cleanup();
						
			//decode json
            JSONArray list = (JSONArray)parser.parse(new FileReader(file));
            System.out.println(list.size());
			// convert JSON to DBObject directly
			for (Object o : list) {
				DBObject dbObject = (DBObject) JSON
						.parse(o.toString());
				collection.insert(dbObject);
			}
			
			DBCursor cursorDoc = collection.find();
			System.out.println(cursorDoc.count());
			int i = 2;
			while (cursorDoc.hasNext()) {
				i++;
				System.out.println(cursorDoc.next());
			}
			System.out.println(i);
			System.out.println("Done");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (MongoException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		File directory = new File("/Users/howezhao/Documents/workspace/javaBasic/jsonFile/items.json");
		cleanup();
		StoreFileToDB(directory);
	}
}
