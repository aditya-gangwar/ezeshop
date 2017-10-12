package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class Areas
{
  private java.util.Date created;
  private String areaName;
  private String pincode;
  private String ownerId;
  private String id;
  private String objectId;
  private java.util.Date updated;
  private Cities city;
  private Boolean validated;

  public Boolean getValidated() {
    return validated;
  }

  public void setValidated(Boolean validated) {
    this.validated = validated;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getAreaName()
  {
    return areaName;
  }

  public void setAreaName( String areaName )
  {
    this.areaName = areaName;
  }

  public String getPincode()
  {
    return pincode;
  }

  public void setPincode( String pincode )
  {
    this.pincode = pincode;
  }

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

  public Cities getCity()
  {
    return city;
  }

  public void setCity( Cities city )
  {
    this.city = city;
  }

                                                    
  public Areas save()
  {
    return Backendless.Data.of( Areas.class ).save( this );
  }

  public Future<Areas> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Areas> future = new Future<Areas>();
      Backendless.Data.of( Areas.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<Areas> callback )
  {
    Backendless.Data.of( Areas.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Areas.class ).remove( this );
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
      Backendless.Data.of( Areas.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Areas.class ).remove( this, callback );
  }

  public static Areas findById(String id )
  {
    return Backendless.Data.of( Areas.class ).findById( id );
  }

  public static Future<Areas> findByIdAsync(String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Areas> future = new Future<Areas>();
      Backendless.Data.of( Areas.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<Areas> callback )
  {
    Backendless.Data.of( Areas.class ).findById( id, callback );
  }

  public static Areas findFirst()
  {
    return Backendless.Data.of( Areas.class ).findFirst();
  }

  public static Future<Areas> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Areas> future = new Future<Areas>();
      Backendless.Data.of( Areas.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<Areas> callback )
  {
    Backendless.Data.of( Areas.class ).findFirst( callback );
  }

  public static Areas findLast()
  {
    return Backendless.Data.of( Areas.class ).findLast();
  }

  public static Future<Areas> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Areas> future = new Future<Areas>();
      Backendless.Data.of( Areas.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<Areas> callback )
  {
    Backendless.Data.of( Areas.class ).findLast( callback );
  }

  public static BackendlessCollection<Areas> find(BackendlessDataQuery query )
  {
    return Backendless.Data.of( Areas.class ).find( query );
  }

  public static Future<BackendlessCollection<Areas>> findAsync(BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<Areas>> future = new Future<BackendlessCollection<Areas>>();
      Backendless.Data.of( Areas.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<Areas>> callback )
  {
    Backendless.Data.of( Areas.class ).find( query, callback );
  }
}