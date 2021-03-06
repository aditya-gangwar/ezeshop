package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

public class CustomerOrder
{
  private java.util.Date updated;
  private String custMobile;
  //private String addressId;
  private String id;
  private java.util.Date dispatchTime;
  private java.util.Date cancelTime;
  private String statusChgReason;
  private Integer statusChgByUserType;
  private java.util.Date deliverTime;
  private String custName;
  private java.util.Date createTime;
  private String custComments;
  private java.util.Date acceptTime;
  private String merchantId;
  private String prevStatus;
  private String ownerId;
  private java.util.Date created;
  private String objectId;
  private String custPrivId;
  private String currStatus;
  //private Transaction txn;
  private java.util.List<Prescriptions> prescrips;
  private Customers customerNIDB;
  private Merchants merchantNIDB;
  //private CustAddress addressNIDB;
  private String delvryToName;
  private String delvryContactNum;
  private String delvryAddrText;
  private String delvryAddrArea;
  private String delvryAddrCity;
  private String delvryAddrState;

  public String getDelvryToName() {
    return delvryToName;
  }

  public void setDelvryToName(String delvryToName) {
    this.delvryToName = delvryToName;
  }

  public String getDelvryContactNum() {
    return delvryContactNum;
  }

  public void setDelvryContactNum(String delvryContactNum) {
    this.delvryContactNum = delvryContactNum;
  }

  public String getDelvryAddrText() {
    return delvryAddrText;
  }

  public void setDelvryAddrText(String delvryAddrText) {
    this.delvryAddrText = delvryAddrText;
  }

  public String getDelvryAddrArea() {
    return delvryAddrArea;
  }

  public void setDelvryAddrArea(String delvryAddrArea) {
    this.delvryAddrArea = delvryAddrArea;
  }

  public String getDelvryAddrCity() {
    return delvryAddrCity;
  }

  public void setDelvryAddrCity(String delvryAddrCity) {
    this.delvryAddrCity = delvryAddrCity;
  }

  public String getDelvryAddrState() {
    return delvryAddrState;
  }

  public void setDelvryAddrState(String delvryAddrState) {
    this.delvryAddrState = delvryAddrState;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getCustMobile()
  {
    return custMobile;
  }

  public void setCustMobile( String custMobile )
  {
    this.custMobile = custMobile;
  }

  /*public String getAddressId()
  {
    return addressId;
  }

  public void setAddressId( String addressId )
  {
    this.addressId = addressId;
  }*/

  public String getId()
  {
    return id;
  }

  public void setId( String id )
  {
    this.id = id;
  }

  public java.util.Date getDispatchTime()
  {
    return dispatchTime;
  }

  public void setDispatchTime( java.util.Date dispatchTime )
  {
    this.dispatchTime = dispatchTime;
  }

  public java.util.Date getCancelTime()
  {
    return cancelTime;
  }

  public void setCancelTime( java.util.Date cancelTime )
  {
    this.cancelTime = cancelTime;
  }

  public String getStatusChgReason()
  {
    return statusChgReason;
  }

  public void setStatusChgReason( String statusChgReason )
  {
    this.statusChgReason = statusChgReason;
  }

  public Integer getStatusChgByUserType()
  {
    return statusChgByUserType;
  }

  public void setStatusChgByUserType( Integer statusChgByUserType )
  {
    this.statusChgByUserType = statusChgByUserType;
  }

  public java.util.Date getDeliverTime()
  {
    return deliverTime;
  }

  public void setDeliverTime( java.util.Date deliverTime )
  {
    this.deliverTime = deliverTime;
  }

  public String getCustName()
  {
    return custName;
  }

  public void setCustName( String custName )
  {
    this.custName = custName;
  }

  public java.util.Date getCreateTime()
  {
    return createTime;
  }

  public void setCreateTime( java.util.Date createTime )
  {
    this.createTime = createTime;
  }

  public String getCustComments()
  {
    return custComments;
  }

  public void setCustComments( String custComments )
  {
    this.custComments = custComments;
  }

  public java.util.Date getAcceptTime()
  {
    return acceptTime;
  }

  public void setAcceptTime( java.util.Date acceptTime )
  {
    this.acceptTime = acceptTime;
  }

  public String getMerchantId()
  {
    return merchantId;
  }

  public void setMerchantId( String merchantId )
  {
    this.merchantId = merchantId;
  }

  public String getPrevStatus()
  {
    return prevStatus;
  }

  public void setPrevStatus( String prevStatus )
  {
    this.prevStatus = prevStatus;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getCustPrivId()
  {
    return custPrivId;
  }

  public void setCustPrivId( String custPrivId )
  {
    this.custPrivId = custPrivId;
  }

  public String getCurrStatus()
  {
    return currStatus;
  }

  public void setCurrStatus( String currStatus )
  {
    this.currStatus = currStatus;
  }

  /*public Transaction getTxn()
  {
    return txn;
  }

  public void setTxn( Transaction txn )
  {
    this.txn = txn;
  }*/

  public java.util.List<Prescriptions> getPrescrips()
  {
    return prescrips;
  }

  public void setPrescrips( java.util.List<Prescriptions> prescrips )
  {
    this.prescrips = prescrips;
  }

  public Customers getCustomerNIDB()
  {
    return customerNIDB;
  }

  public void setCustomerNIDB( Customers customerNIDB )
  {
    this.customerNIDB = customerNIDB;
  }

  public Merchants getMerchantNIDB()
  {
    return merchantNIDB;
  }

  public void setMerchantNIDB( Merchants merchantNIDB )
  {
    this.merchantNIDB = merchantNIDB;
  }

  /*public CustAddress getAddressNIDB()
  {
    return addressNIDB;
  }

  public void setAddressNIDB( CustAddress addressNIDB )
  {
    this.addressNIDB = addressNIDB;
  }*/


  public CustomerOrder save()
  {
    return Backendless.Data.of( CustomerOrder.class ).save( this );
  }

  public Future<CustomerOrder> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustomerOrder> future = new Future<CustomerOrder>();
      Backendless.Data.of( CustomerOrder.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<CustomerOrder> callback )
  {
    Backendless.Data.of( CustomerOrder.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( CustomerOrder.class ).remove( this );
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
      Backendless.Data.of( CustomerOrder.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( CustomerOrder.class ).remove( this, callback );
  }

  public static CustomerOrder findById( String id )
  {
    return Backendless.Data.of( CustomerOrder.class ).findById( id );
  }

  public static Future<CustomerOrder> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustomerOrder> future = new Future<CustomerOrder>();
      Backendless.Data.of( CustomerOrder.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<CustomerOrder> callback )
  {
    Backendless.Data.of( CustomerOrder.class ).findById( id, callback );
  }

  public static CustomerOrder findFirst()
  {
    return Backendless.Data.of( CustomerOrder.class ).findFirst();
  }

  public static Future<CustomerOrder> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustomerOrder> future = new Future<CustomerOrder>();
      Backendless.Data.of( CustomerOrder.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<CustomerOrder> callback )
  {
    Backendless.Data.of( CustomerOrder.class ).findFirst( callback );
  }

  public static CustomerOrder findLast()
  {
    return Backendless.Data.of( CustomerOrder.class ).findLast();
  }

  public static Future<CustomerOrder> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<CustomerOrder> future = new Future<CustomerOrder>();
      Backendless.Data.of( CustomerOrder.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<CustomerOrder> callback )
  {
    Backendless.Data.of( CustomerOrder.class ).findLast( callback );
  }

  public static BackendlessCollection<CustomerOrder> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( CustomerOrder.class ).find( query );
  }

  public static Future<BackendlessCollection<CustomerOrder>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<CustomerOrder>> future = new Future<BackendlessCollection<CustomerOrder>>();
      Backendless.Data.of( CustomerOrder.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<CustomerOrder>> callback )
  {
    Backendless.Data.of( CustomerOrder.class ).find( query, callback );
  }
}