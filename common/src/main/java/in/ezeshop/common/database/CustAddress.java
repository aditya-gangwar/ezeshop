package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

public class CustAddress
{
  private String ownerId;
  private String custPrivateId;
  private String text1;
  private java.util.Date created;
  private String id;
  private String objectId;
  private String toName;
  private String contactNum;
  private java.util.Date updated;
  private String areaId;
  private Areas areaNIDB;
  public String getOwnerId()
  {
    return ownerId;
  }

  public String getCustPrivateId()
  {
    return custPrivateId;
  }

  public void setCustPrivateId( String custPrivateId )
  {
    this.custPrivateId = custPrivateId;
  }

  public String getText1()
  {
    return text1;
  }

  public void setText1( String text1 )
  {
    this.text1 = text1;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getId()
  {
    return id;
  }

  public void setId( String id )
  {
    this.id = id;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getToName()
  {
    return toName;
  }

  public void setToName( String toName )
  {
    this.toName = toName;
  }

  public String getContactNum()
  {
    return contactNum;
  }

  public void setContactNum( String contactNum )
  {
    this.contactNum = contactNum;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getAreaId()
  {
    return areaId;
  }

  public void setAreaId( String areaId )
  {
    this.areaId = areaId;
  }

  public Areas getAreaNIDB()
  {
    return areaNIDB;
  }

  public void setAreaNIDB( Areas areaNIDB )
  {
    this.areaNIDB = areaNIDB;
  }

                                                    
  public CustAddress save()
  {
    return Backendless.Data.of( CustAddress.class ).save( this );
  }

  public Future<CustAddress> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustAddress> future = new Future<CustAddress>();
      Backendless.Data.of( CustAddress.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<CustAddress> callback )
  {
    Backendless.Data.of( CustAddress.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( CustAddress.class ).remove( this );
  }

  public Future<Long> removeAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Long> future = new Future<Long>();
      Backendless.Data.of( CustAddress.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( CustAddress.class ).remove( this, callback );
  }

  public static CustAddress findById( String id )
  {
    return Backendless.Data.of( CustAddress.class ).findById( id );
  }

  public static Future<CustAddress> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustAddress> future = new Future<CustAddress>();
      Backendless.Data.of( CustAddress.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<CustAddress> callback )
  {
    Backendless.Data.of( CustAddress.class ).findById( id, callback );
  }

  public static CustAddress findFirst()
  {
    return Backendless.Data.of( CustAddress.class ).findFirst();
  }

  public static Future<CustAddress> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustAddress> future = new Future<CustAddress>();
      Backendless.Data.of( CustAddress.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<CustAddress> callback )
  {
    Backendless.Data.of( CustAddress.class ).findFirst( callback );
  }

  public static CustAddress findLast()
  {
    return Backendless.Data.of( CustAddress.class ).findLast();
  }

  public static Future<CustAddress> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustAddress> future = new Future<CustAddress>();
      Backendless.Data.of( CustAddress.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<CustAddress> callback )
  {
    Backendless.Data.of( CustAddress.class ).findLast( callback );
  }

  public static BackendlessCollection<CustAddress> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( CustAddress.class ).find( query );
  }

  public static Future<BackendlessCollection<CustAddress>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<CustAddress>> future = new Future<BackendlessCollection<CustAddress>>();
      Backendless.Data.of( CustAddress.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<CustAddress>> callback )
  {
    Backendless.Data.of( CustAddress.class ).find( query, callback );
  }
}