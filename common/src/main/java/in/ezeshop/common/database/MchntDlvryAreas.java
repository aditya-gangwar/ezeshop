package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

public class MchntDlvryAreas
{
  private String areaId;
  private String ownerId;
  private java.util.Date created;
  private java.util.Date updated;
  private String merchantId;
  private String objectId;
  public String getAreaId()
  {
    return areaId;
  }

  public void setAreaId( String areaId )
  {
    this.areaId = areaId;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getMerchantId()
  {
    return merchantId;
  }

  public void setMerchantId( String merchantId )
  {
    this.merchantId = merchantId;
  }

  public String getObjectId()
  {
    return objectId;
  }

                                                    
  public MchntDlvryAreas save()
  {
    return Backendless.Data.of( MchntDlvryAreas.class ).save( this );
  }

  public Future<MchntDlvryAreas> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MchntDlvryAreas> future = new Future<MchntDlvryAreas>();
      Backendless.Data.of( MchntDlvryAreas.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<MchntDlvryAreas> callback )
  {
    Backendless.Data.of( MchntDlvryAreas.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( MchntDlvryAreas.class ).remove( this );
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
      Backendless.Data.of( MchntDlvryAreas.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( MchntDlvryAreas.class ).remove( this, callback );
  }

  public static MchntDlvryAreas findById( String id )
  {
    return Backendless.Data.of( MchntDlvryAreas.class ).findById( id );
  }

  public static Future<MchntDlvryAreas> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MchntDlvryAreas> future = new Future<MchntDlvryAreas>();
      Backendless.Data.of( MchntDlvryAreas.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<MchntDlvryAreas> callback )
  {
    Backendless.Data.of( MchntDlvryAreas.class ).findById( id, callback );
  }

  public static MchntDlvryAreas findFirst()
  {
    return Backendless.Data.of( MchntDlvryAreas.class ).findFirst();
  }

  public static Future<MchntDlvryAreas> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MchntDlvryAreas> future = new Future<MchntDlvryAreas>();
      Backendless.Data.of( MchntDlvryAreas.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<MchntDlvryAreas> callback )
  {
    Backendless.Data.of( MchntDlvryAreas.class ).findFirst( callback );
  }

  public static MchntDlvryAreas findLast()
  {
    return Backendless.Data.of( MchntDlvryAreas.class ).findLast();
  }

  public static Future<MchntDlvryAreas> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MchntDlvryAreas> future = new Future<MchntDlvryAreas>();
      Backendless.Data.of( MchntDlvryAreas.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<MchntDlvryAreas> callback )
  {
    Backendless.Data.of( MchntDlvryAreas.class ).findLast( callback );
  }

  public static BackendlessCollection<MchntDlvryAreas> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( MchntDlvryAreas.class ).find( query );
  }

  public static Future<BackendlessCollection<MchntDlvryAreas>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<MchntDlvryAreas>> future = new Future<BackendlessCollection<MchntDlvryAreas>>();
      Backendless.Data.of( MchntDlvryAreas.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<MchntDlvryAreas>> callback )
  {
    Backendless.Data.of( MchntDlvryAreas.class ).find( query, callback );
  }
}