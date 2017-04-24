package mongo;

import java.io.Serializable;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Address implements Serializable
{
    private ObjectId _id;
    private int    type;
    private String street;
    private String city;
    private String state;

    public static final int HOME_ADDRESS      = 1;
    public static final int BILLING_ADDRESS   = 2;
    public static final int SHIPPING_ADDRESS  = 3;
    public static final int ALTERNATE_ADDRESS = 5;

    public Address() {}

    public Address( int type, String street, String city, String state )
    {
        this.type   = type;
        this.street = street;
        this.city   = city;
        this.state  = state;
    }

    public ObjectId getId() { return this._id; }
    public void setId( ObjectId _id ) { this._id = _id; }
    public void generateId() { if( this._id == null ) this._id = new ObjectId(); }

    public int getType() { return this.type; }
    public void setType( int type ) { this.type = type; }
    public String getStreet() { return this.street; }
    public void setStreet( String street ) { this.street = street; }

    public String getCity() { return this.city; }
    public void setCity( String city ) { this.city = city; }

    public String getState() { return this.state; }
    public void setState( String state ) { this.state = state; }

    public DBObject bsonFromPojo()
    {
        BasicDBObject document = new BasicDBObject();

        document.put( "_id",    this._id );
        document.put( "type",   this.type );
        document.put( "street", this.street );
        document.put( "city",   this.city );
        document.put( "state",  this.state );

        return document;
    }

    public void makePojoFromBson( DBObject bson )
    {
        BasicDBObject b = ( BasicDBObject ) bson;

        this._id    = ( ObjectId ) b.get( "_id" );
        this.type   = ( Integer )  b.get( "type" );
        this.street = ( String )   b.get( "street" );
        this.city   = ( String )   b.get( "city" );
        this.state  = ( String )   b.get( "state" );
    }
}