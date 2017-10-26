package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

public class Prescriptions
{
  private String ownerId;
  private String id;
  private String objectId;
  private java.util.Date updated;
  private String url;
  private java.util.Date created;
  private String customerId;
  public String getOwnerId()
  {
    return ownerId;
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

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl( String url )
  {
    this.url = url;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getCustomerId()
  {
    return customerId;
  }

  public void setCustomerId( String customerId )
  {
    this.customerId = customerId;
  }

                                                    
  public Prescriptions save()
  {
    return Backendless.Data.of( Prescriptions.class ).save( this );
  }

  public Future<Prescriptions> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Prescriptions> future = new Future<Prescriptions>();
      Backendless.Data.of( Prescriptions.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<Prescriptions> callback )
  {
    Backendless.Data.of( Prescriptions.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Prescriptions.class ).remove( this );
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
      Backendless.Data.of( Prescriptions.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Prescriptions.class ).remove( this, callback );
  }

  public static Prescriptions findById( String id )
  {
    return Backendless.Data.of( Prescriptions.class ).findById( id );
  }

  public static Future<Prescriptions> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Prescriptions> future = new Future<Prescriptions>();
      Backendless.Data.of( Prescriptions.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<Prescriptions> callback )
  {
    Backendless.Data.of( Prescriptions.class ).findById( id, callback );
  }

  public static Prescriptions findFirst()
  {
    return Backendless.Data.of( Prescriptions.class ).findFirst();
  }

  public static Future<Prescriptions> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Prescriptions> future = new Future<Prescriptions>();
      Backendless.Data.of( Prescriptions.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<Prescriptions> callback )
  {
    Backendless.Data.of( Prescriptions.class ).findFirst( callback );
  }

  public static Prescriptions findLast()
  {
    return Backendless.Data.of( Prescriptions.class ).findLast();
  }

  public static Future<Prescriptions> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Prescriptions> future = new Future<Prescriptions>();
      Backendless.Data.of( Prescriptions.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<Prescriptions> callback )
  {
    Backendless.Data.of( Prescriptions.class ).findLast( callback );
  }

  public static BackendlessCollection<Prescriptions> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( Prescriptions.class ).find( query );
  }

  public static Future<BackendlessCollection<Prescriptions>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<Prescriptions>> future = new Future<BackendlessCollection<Prescriptions>>();
      Backendless.Data.of( Prescriptions.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<Prescriptions>> callback )
  {
    Backendless.Data.of( Prescriptions.class ).find( query, callback );
  }
}