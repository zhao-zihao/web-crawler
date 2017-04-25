package mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class SearchEngin {
	private static MongoSetup   mongo      = new MongoSetup("stackoverflow");
    private static DBCollection questions = null;
    private static DBCollection occurence = null;
    
    private SearchEngin() {
    }
    
    public static void setUp(){
        // refresh the collection each time...
    	questions = mongo.getCollection("questions" );
        occurence = mongo.getCollection("occurence_list");
        occurence.drop();
        occurence = mongo.getCollection("occurence_list");
    }
    
    public static void createForwardIndex() {
    	DBCursor cursor = questions.find();
    	try {
    	   while(cursor.hasNext()) {
    	       DBObject docMap = cursor.next();
    	       
    	       if (docMap.get("title") instanceof String) {
    	    	   List<String> words = new ArrayList<>();
    	    	   String title = (String) docMap.get("title");
        	       String[] titleWordList = title.replaceAll("[^A-Za-z0-9]+", " ").split(" ");
        	       for (String str : titleWordList) {
        	    	   words.add(str.toLowerCase());
        	       }
        	       
        	       //update docuemnt
        	       BasicDBObject newDocument = new BasicDBObject();
        	       newDocument.append("$set", new BasicDBObject().append("words", words));
        	       
        	       BasicDBObject searchQuery = new BasicDBObject().append("_id", docMap.get("_id"));
        	       
        	       questions.update(searchQuery, newDocument);
    	       }
    	   }
    	} finally {
    	   cursor.close();
    	}
    }
    
    public static void createOccurenceList() {
    	DBCursor cursor = questions.find();
    	try {
    	   cursor = questions.find();
    	   while(cursor.hasNext()) {
    	       DBObject docMap = cursor.next();
    	       String url = (String) docMap.get("url");
    	       
    	       if (docMap.get("words") instanceof List) {
    	    	   List<String> words = (List<String>) docMap.get("words");
    	    	   
    	    	   //traverse words
    	    	   for (String word : words) {
    	    		   if (Stopwords.isStopword(word)) {
    	    			   continue;
    	    		   }
    	    		   BasicDBObject document = new BasicDBObject();
    	    		   DBObject oneQuery = occurence.findOne(new BasicDBObject("word", word));
    	    		   //no word in collections now
    	    		   if (oneQuery == null) {
    	    			   List<String> urls = new ArrayList<>();
    	    			   urls.add(url);
    	    			   document.put("word", word);
        	    		   document.put("urls", urls);
        	    		   occurence.insert(document);
    	    		   } else {

        	    		   BasicDBObject match = new BasicDBObject();
        	    	       match.put( "_id", oneQuery.get("_id") );
        	    	       
        	    	       BasicDBObject newUrl = new BasicDBObject();
        	    	       newUrl.put("urls", url);
        	    	       
        	    	       BasicDBObject update = new BasicDBObject();
        	    	       update.put("$push", newUrl);  //use $push to push new url to url array
        	    	       
        	    	       occurence.update(match, update);
    	    		   }
    	    	   }
    	       }
    	   }
    	} finally {
    	   cursor.close();
    	}
    }
    
    public static void main(String[] args) {
    	setUp();
    	createForwardIndex();
    	createOccurenceList();

    	System.out.println("***Done***");
    }
}
