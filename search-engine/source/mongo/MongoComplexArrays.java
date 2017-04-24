package mongo;

import java.util.ArrayList;
import java.util.Iterator;

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


/**
 * The point of this code is to explore creating and maintaining arrays in Mongo documents.
 */
public class MongoComplexArrays
{
    private static MongoSetup   mongo      = new MongoSetup( "funstuff" );
    private static DBCollection collection = null;

    private static void cleanup()
    {
        // refresh the collection each time...
        collection = mongo.getCollection( "accounts" );
        collection.drop();
        collection = mongo.getCollection( "accounts" );
    }

    /** Illustrates how to create a document with an array inside. In a proper service, this
     * would just create (add) a new address and the method would take a parameter indicating
     * which user and another to communicate the new address.
     *
     * @return the oid of Jack's new user account for playing around with.
     */
    private static ObjectId setup()
    {
        /* Let's create a new object with an array of addresses inside. Something like:
         * {
         *   "name" : "Jack",
         *   "addresses" :
         *   [
         *     { "type" : 1, "street" : "123 My Street", "city" : "Bedford Falls", "state" : "NJ" },
         *     { "type" : 2, "street" : "456 My Street", "city" : "Bedford Falls", "state" : "NJ" }
         *   ]
         * }
         */
        BasicDBObject account = new BasicDBObject();
        account.put( "name", "Jack" );

        Address address1 = new Address( Address.HOME_ADDRESS, "123 My Street", "Bedford Falls", "NJ" );
        Address address2 = new Address( Address.BILLING_ADDRESS, "456 My Street", "Bedford Falls", "NJ" );

        address1.generateId();
        address2.generateId();

        DBObject              dbo;
        ArrayList< DBObject > array = new ArrayList< DBObject >();

        dbo = address1.bsonFromPojo(); array.add( dbo );
        dbo = address2.bsonFromPojo(); array.add( dbo );

        account.put( "addresses", array );

        collection.insert( account );

        System.out.println( "At this point, open the Mongo console and type: " );
        System.out.println( " > use funstuff" );
        System.out.println( " > db.accounts.find().pretty();" );

        // let's find this document and record Jack's user account oid...
        DBCursor cursor = collection.find();

        while( cursor.hasNext() )
            account = ( BasicDBObject ) cursor.next();

        return ( ObjectId ) account.get( "_id" );
    }

    /** Illustrates how to create a new address inside the document now set up.
     *
     * @param accountoid identifies the user whose address vector is to be added to.
     * @param address the new address to add.
     */
    private static void create( ObjectId accountoid, Address address )
    {
        BasicDBObject match = new BasicDBObject();
        match.put( "_id", accountoid );

        BasicDBObject addressSpec = new BasicDBObject();
        addressSpec.put( "_id",    address.getId() );
        addressSpec.put( "type",   address.getType() );
        addressSpec.put( "street", address.getStreet() );
        addressSpec.put( "city",   address.getCity() );
        addressSpec.put( "state", address.getState() );

        BasicDBObject update = new BasicDBObject();
        update.put( "$push", new BasicDBObject( "addresses", addressSpec ) );

        collection.update( match, update );
    }

    /** Illustrates how to read elements of the embedded array out. We want to save the
     * type-2 address' oid for later update and delete in the example, also the account
     * oid too for we'd have that in a usual application setting.
     *
     * @param accountoid identifies the user whose address vector is to be read.
     * @param type the sort of address to find in the vector; in a real service there
     *              could be more than one of these, but for this exercise, we're just
     *              going to assume only one.
     * @return the address read or null.
     */
    private static Address readByType( ObjectId accountoid, int type )
    {
        BasicDBObject match = new BasicDBObject();
        match.put( "_id", accountoid );

        DBCursor cursor  = collection.find( match );

        BasicDBObject account = null;

        while( cursor.hasNext() )
            account = ( BasicDBObject ) cursor.next();

        System.out.println( ( String ) account.get( "name" ) + ":" );

        BasicDBList addresses = ( BasicDBList ) account.get( "addresses" );

        for( Iterator< Object > it = addresses.iterator(); it.hasNext(); )
        {
            BasicDBObject dbo     = ( BasicDBObject ) it.next();
            Address       address = new Address();

            address.makePojoFromBson( dbo );

            System.out.println( "  [" + address.getId()     + "] "
                                      + address.getStreet() + ", "
                                      + address.getCity()   + ", "
                                      + address.getState()  + " ("
                                      + address.getType()   + ")" );

            // we're going to return when we find the first one with this type...
            if( address.getType() == type )
                return address;
        }

        return null;
    }

    /** Illustrates how to update an element in the embedded array. This method morphs us
     * toward a more proper service implementation (of update).
     *
     * db.accounts.update( { "_id" : "$saveAccountOid" }, { $set : { "addresses" : { "_id" : "$saveAddressOid" } } } )
     *
     * @param accountoid identifies the user whose address vector is to be changed.
     * @param address the address to change assuming its _id corresponds with an existing one for the user.
     */
    private static void update( ObjectId accountoid, Address address )
    {
        // start by pulling the address to be updated from the array...
        delete( accountoid, address.getId() );

        // now re-add the address as updated...
        create( accountoid, address );
    }

    /**
     * This is a more intelligent version of update(). The first half of the update
     * operation is to locate the user account (by oid) and the address (also by oid).
     * The second half is to update all the data fields requiring it. This way, the
     * address entity passed need only specify those fields that are to change.
     *
     * @param accountoid identifies the user whose address vector is to be changed.
     * @param address the address to change assuming its _id corresponds with an existing one for the user.
     */
    private static void update2( ObjectId accountoid, Address address )
    {
        BasicDBObject match = new BasicDBObject();
        match.put( "_id", accountoid );
        match.put( "addresses._id", address.getId() );

        BasicDBObject addressSpec = new BasicDBObject();
        Integer type = address.getType();
        String temp;

        if( type > 0 && type < 6 )
            addressSpec.put( "addresses.$.type", type );
        if( ( temp = address.getStreet() ) != null )
            addressSpec.put( "addresses.$.street", temp );
        if( ( temp = address.getCity() ) != null )
            addressSpec.put( "addresses.$.city", temp );
        if( ( temp = address.getState() ) != null )
            addressSpec.put( "addresses.$.state", temp );

        BasicDBObject update = new BasicDBObject();
        update.put( "$set", addressSpec );

        collection.update( match, update );
    }

    /** Illustrates how to remove an element from the embedded array. The element is
     * an address as indicated by the oid passed. At this console, this would be:
     *
     * db.accounts.update( { "_id" : "$saveAccountOid" }, { $pull : { "addresses" : { "_id" : "$saveAddressOid" } } } )
     *
     * There is an account specification, the first argument, that uniquely describes
     * Jack's account. This is followed by the specification of the address we wish to
     * delete from the array of addresses in that account.
     *
     * If we were going to delete Jack, the user, this would all be simpler and we'd
     * call collection.remove(). This example is only on how to remove an element from
     * an embedded array (remember?).
     *
     * This method, like update() above, is closer to a real service implementation.
     *
     * @param accountoid identifies the user whose address vector is to be changed.
     * @param addressoid the address to be deleted from the vector.
     */
    private static void delete( ObjectId accountoid, ObjectId addressoid )
    {
        BasicDBObject match = new BasicDBObject();
        match.put( "_id", accountoid );

        BasicDBObject addressSpec = new BasicDBObject();
        addressSpec.put( "_id", addressoid );

        BasicDBObject update = new BasicDBObject();
        update.put( "$pull", new BasicDBObject( "addresses", addressSpec ) );

        collection.update( match, update );
    }

    private static void seeChange( String where )
    {
        DBCursor cursor  = collection.find();
        DBObject account = null;

        while( cursor.hasNext() )
            account = cursor.next();

        System.out.println( where );
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson( account );
        System.out.println( json );
    }

    public static void main( String[] args )
    {
        ObjectId saveAccountOid = null;
        Address  newAddress     = new Address( Address.SHIPPING_ADDRESS, "789 My Street", "Bedford Falls", "NJ" );
        Address  billingAddress = null;
        Address  changedAddress = new Address( Address.BILLING_ADDRESS, "1852 Exeter Avenue", "Bedford Falls", "NJ" );
        
        newAddress.generateId();

        System.out.println( "CLEANUP -----------------------------------------" );
        cleanup();

        System.out.println( "SETUP -------------------------------------------" );
        saveAccountOid = setup();

        System.out.println( "CREATE ------------------------------------------" );
        create( saveAccountOid, newAddress );
        seeChange( "After creating new address..." );

        System.out.println( "READBYTYPE --------------------------------------" );
        billingAddress = readByType( saveAccountOid, Address.BILLING_ADDRESS );

        changedAddress.setId( billingAddress.getId() );

        System.out.println( "UPDATE ------------------------------------------" );
        update( saveAccountOid, changedAddress );
        seeChange( "After updating billing address..." );

        Address changedAddress2 = new Address( Address.ALTERNATE_ADDRESS, "2222 Bulldog Boulevard", null, null );

        changedAddress2.setId( billingAddress.getId() );

        System.out.println( "UPDATE2 -----------------------------------------" );
        update2( saveAccountOid, changedAddress2 );
        seeChange( "After updating billing to alternate address (second method)..." );

        System.out.println( "DELETE ------------------------------------------" );
        delete( saveAccountOid, billingAddress.getId() );
        seeChange( "After deleting billing address..." );
    }
}