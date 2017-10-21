package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class Customers
{
  private String dob;
  private java.util.Date updated;
  private String name;
  private String namak;
  private Boolean first_login_ok;
  private String txn_tables;
  private String regMchntId;
  private String ownerId;
  private java.util.Date created;
  private String msgDevId;
  private String objectId;
  private String txn_pin;
  private java.util.Date delLocalFilesReq;
  private String mobile_num;
  private java.util.Date status_update_time;
  private Boolean debugLogs;
  private String private_id;
  private String defaultAddressId;
  private java.util.Date regDate;
  private Integer sex;
  private String city;
  private String cashback_table;
  private Integer admin_status;
  private String status_reason;
  private java.util.List<CustAddress> addressesNIDB;
  public String getDob()
  {
    return dob;
  }

  public void setDob( String dob )
  {
    this.dob = dob;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public String getNamak()
  {
    return namak;
  }

  public void setNamak( String namak )
  {
    this.namak = namak;
  }

  public Boolean getFirst_login_ok()
  {
    return first_login_ok;
  }

  public void setFirst_login_ok( Boolean first_login_ok )
  {
    this.first_login_ok = first_login_ok;
  }

  public String getTxn_tables()
  {
    return txn_tables;
  }

  public void setTxn_tables( String txn_tables )
  {
    this.txn_tables = txn_tables;
  }

  public String getRegMchntId()
  {
    return regMchntId;
  }

  public void setRegMchntId( String regMchntId )
  {
    this.regMchntId = regMchntId;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getMsgDevId()
  {
    return msgDevId;
  }

  public void setMsgDevId( String msgDevId )
  {
    this.msgDevId = msgDevId;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getTxn_pin()
  {
    return txn_pin;
  }

  public void setTxn_pin( String txn_pin )
  {
    this.txn_pin = txn_pin;
  }

  public java.util.Date getDelLocalFilesReq()
  {
    return delLocalFilesReq;
  }

  public void setDelLocalFilesReq( java.util.Date delLocalFilesReq )
  {
    this.delLocalFilesReq = delLocalFilesReq;
  }

  public String getMobile_num()
  {
    return mobile_num;
  }

  public void setMobile_num( String mobile_num )
  {
    this.mobile_num = mobile_num;
  }

  public java.util.Date getStatus_update_time()
  {
    return status_update_time;
  }

  public void setStatus_update_time( java.util.Date status_update_time )
  {
    this.status_update_time = status_update_time;
  }

  public Boolean getDebugLogs()
  {
    return debugLogs;
  }

  public void setDebugLogs( Boolean debugLogs )
  {
    this.debugLogs = debugLogs;
  }

  public String getPrivate_id()
  {
    return private_id;
  }

  public void setPrivate_id( String private_id )
  {
    this.private_id = private_id;
  }

  public String getDefaultAddressId()
  {
    return defaultAddressId;
  }

  public void setDefaultAddressId( String defaultAddressId )
  {
    this.defaultAddressId = defaultAddressId;
  }

  public java.util.Date getRegDate()
  {
    return regDate;
  }

  public void setRegDate( java.util.Date regDate )
  {
    this.regDate = regDate;
  }

  public Integer getSex()
  {
    return sex;
  }

  public void setSex( Integer sex )
  {
    this.sex = sex;
  }

  public String getCity()
  {
    return city;
  }

  public void setCity( String city )
  {
    this.city = city;
  }

  public String getCashback_table()
  {
    return cashback_table;
  }

  public void setCashback_table( String cashback_table )
  {
    this.cashback_table = cashback_table;
  }

  public Integer getAdmin_status()
  {
    return admin_status;
  }

  public void setAdmin_status( Integer admin_status )
  {
    this.admin_status = admin_status;
  }

  public String getStatus_reason()
  {
    return status_reason;
  }

  public void setStatus_reason( String status_reason )
  {
    this.status_reason = status_reason;
  }

  public java.util.List<CustAddress> getAddressesNIDB()
  {
    return addressesNIDB;
  }

  public void setAddressesNIDB(java.util.List<CustAddress> addressesNIDB)
  {
    this.addressesNIDB = addressesNIDB;
  }

                                                    
  public Customers save()
  {
    return Backendless.Data.of( Customers.class ).save( this );
  }

  public Future<Customers> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Customers> future = new Future<Customers>();
      Backendless.Data.of( Customers.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<Customers> callback )
  {
    Backendless.Data.of( Customers.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Customers.class ).remove( this );
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
      Backendless.Data.of( Customers.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Customers.class ).remove( this, callback );
  }

  public static Customers findById( String id )
  {
    return Backendless.Data.of( Customers.class ).findById( id );
  }

  public static Future<Customers> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Customers> future = new Future<Customers>();
      Backendless.Data.of( Customers.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<Customers> callback )
  {
    Backendless.Data.of( Customers.class ).findById( id, callback );
  }

  public static Customers findFirst()
  {
    return Backendless.Data.of( Customers.class ).findFirst();
  }

  public static Future<Customers> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Customers> future = new Future<Customers>();
      Backendless.Data.of( Customers.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<Customers> callback )
  {
    Backendless.Data.of( Customers.class ).findFirst( callback );
  }

  public static Customers findLast()
  {
    return Backendless.Data.of( Customers.class ).findLast();
  }

  public static Future<Customers> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Customers> future = new Future<Customers>();
      Backendless.Data.of( Customers.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<Customers> callback )
  {
    Backendless.Data.of( Customers.class ).findLast( callback );
  }

  public static BackendlessCollection<Customers> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( Customers.class ).find( query );
  }

  public static Future<BackendlessCollection<Customers>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<Customers>> future = new Future<BackendlessCollection<Customers>>();
      Backendless.Data.of( Customers.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<Customers>> callback )
  {
    Backendless.Data.of( Customers.class ).find( query, callback );
  }
}