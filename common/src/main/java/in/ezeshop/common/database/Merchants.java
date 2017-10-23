package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.geo.GeoPoint;
import com.backendless.persistence.BackendlessDataQuery;

public class Merchants
{
  private Boolean invoiceNumOnlyNumbers;
  private String mobile_num;
  private java.util.Date lastRenewDate;
  private Boolean debugLogs;
  private String status_reason;
  private String dob;
  private String weekOffDay;
  private String contactName;
  private String cashback_table;
  private String cb_rate;
  private Integer prepaidCbMinAmt;
  private Boolean cl_overdraft_enable;
  private String agentId;
  private String email;
  private String name;
  private Boolean autoPasswd;
  private java.util.Date last_txn_archive;
  private String objectId;
  private java.util.Date created;
  private java.util.Date updated;
  private String ownerId;
  private java.util.Date delLocalFilesReq;
  private String txn_table;
  private String auto_id;
  private String contactPhone;
  private Integer freeDlvrMinAmt;
  private Boolean invoiceNumOptional;
  private java.util.Date removeReqDate;
  private String buss_category;
  private String displayImage;
  private java.util.Date status_update_time;
  private Boolean first_login_ok;
  private Integer cl_debit_limit_for_pin;
  private String prepaidCbRate;
  private Boolean cl_add_enable;
  private Integer cl_credit_limit_for_pin;
  private Boolean invoiceNumAsk;
  private String regFormNum;
  private Integer admin_status;
  private Address address;
  private String msgDevId;

  public String getMsgDevId() {
    return msgDevId;
  }

  public void setMsgDevId(String msgDevId) {
    this.msgDevId = msgDevId;
  }

  public Boolean getInvoiceNumOnlyNumbers()
  {
    return invoiceNumOnlyNumbers;
  }

  public void setInvoiceNumOnlyNumbers( Boolean invoiceNumOnlyNumbers )
  {
    this.invoiceNumOnlyNumbers = invoiceNumOnlyNumbers;
  }

  public String getMobile_num()
  {
    return mobile_num;
  }

  public void setMobile_num( String mobile_num )
  {
    this.mobile_num = mobile_num;
  }

  public java.util.Date getLastRenewDate()
  {
    return lastRenewDate;
  }

  public void setLastRenewDate( java.util.Date lastRenewDate )
  {
    this.lastRenewDate = lastRenewDate;
  }

  public Boolean getDebugLogs()
  {
    return debugLogs;
  }

  public void setDebugLogs( Boolean debugLogs )
  {
    this.debugLogs = debugLogs;
  }

  public String getStatus_reason()
  {
    return status_reason;
  }

  public void setStatus_reason( String status_reason )
  {
    this.status_reason = status_reason;
  }

  public String getDob()
  {
    return dob;
  }

  public void setDob( String dob )
  {
    this.dob = dob;
  }

  public String getWeekOffDay()
  {
    return weekOffDay;
  }

  public void setWeekOffDay( String weekOffDay )
  {
    this.weekOffDay = weekOffDay;
  }

  public String getContactName()
  {
    return contactName;
  }

  public void setContactName( String contactName )
  {
    this.contactName = contactName;
  }

  public String getCashback_table()
  {
    return cashback_table;
  }

  public void setCashback_table( String cashback_table )
  {
    this.cashback_table = cashback_table;
  }

  public String getCb_rate()
  {
    return cb_rate;
  }

  public void setCb_rate( String cb_rate )
  {
    this.cb_rate = cb_rate;
  }

  public Integer getPrepaidCbMinAmt()
  {
    return prepaidCbMinAmt;
  }

  public void setPrepaidCbMinAmt( Integer prepaidCbMinAmt )
  {
    this.prepaidCbMinAmt = prepaidCbMinAmt;
  }

  public Boolean getCl_overdraft_enable()
  {
    return cl_overdraft_enable;
  }

  public void setCl_overdraft_enable( Boolean cl_overdraft_enable )
  {
    this.cl_overdraft_enable = cl_overdraft_enable;
  }

  public String getAgentId()
  {
    return agentId;
  }

  public void setAgentId( String agentId )
  {
    this.agentId = agentId;
  }

  public String getEmail()
  {
    return email;
  }

  public void setEmail( String email )
  {
    this.email = email;
  }

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;
  }

  public Boolean getAutoPasswd()
  {
    return autoPasswd;
  }

  public void setAutoPasswd( Boolean autoPasswd )
  {
    this.autoPasswd = autoPasswd;
  }

  public java.util.Date getLast_txn_archive()
  {
    return last_txn_archive;
  }

  public void setLast_txn_archive( java.util.Date last_txn_archive )
  {
    this.last_txn_archive = last_txn_archive;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public java.util.Date getDelLocalFilesReq()
  {
    return delLocalFilesReq;
  }

  public void setDelLocalFilesReq( java.util.Date delLocalFilesReq )
  {
    this.delLocalFilesReq = delLocalFilesReq;
  }

  public String getTxn_table()
  {
    return txn_table;
  }

  public void setTxn_table( String txn_table )
  {
    this.txn_table = txn_table;
  }

  public String getAuto_id()
  {
    return auto_id;
  }

  public void setAuto_id( String auto_id )
  {
    this.auto_id = auto_id;
  }

  public String getContactPhone()
  {
    return contactPhone;
  }

  public void setContactPhone( String contactPhone )
  {
    this.contactPhone = contactPhone;
  }

  public Integer getFreeDlvrMinAmt()
  {
    return freeDlvrMinAmt;
  }

  public void setFreeDlvrMinAmt( Integer freeDlvrMinAmt )
  {
    this.freeDlvrMinAmt = freeDlvrMinAmt;
  }

  public Boolean getInvoiceNumOptional()
  {
    return invoiceNumOptional;
  }

  public void setInvoiceNumOptional( Boolean invoiceNumOptional )
  {
    this.invoiceNumOptional = invoiceNumOptional;
  }

  public java.util.Date getRemoveReqDate()
  {
    return removeReqDate;
  }

  public void setRemoveReqDate( java.util.Date removeReqDate )
  {
    this.removeReqDate = removeReqDate;
  }

  public String getBuss_category()
  {
    return buss_category;
  }

  public void setBuss_category( String buss_category )
  {
    this.buss_category = buss_category;
  }

  public String getDisplayImage()
  {
    return displayImage;
  }

  public void setDisplayImage( String displayImage )
  {
    this.displayImage = displayImage;
  }

  public java.util.Date getStatus_update_time()
  {
    return status_update_time;
  }

  public void setStatus_update_time( java.util.Date status_update_time )
  {
    this.status_update_time = status_update_time;
  }

  public Boolean getFirst_login_ok()
  {
    return first_login_ok;
  }

  public void setFirst_login_ok( Boolean first_login_ok )
  {
    this.first_login_ok = first_login_ok;
  }

  public Integer getCl_debit_limit_for_pin()
  {
    return cl_debit_limit_for_pin;
  }

  public void setCl_debit_limit_for_pin( Integer cl_debit_limit_for_pin )
  {
    this.cl_debit_limit_for_pin = cl_debit_limit_for_pin;
  }

  public String getPrepaidCbRate()
  {
    return prepaidCbRate;
  }

  public void setPrepaidCbRate( String prepaidCbRate )
  {
    this.prepaidCbRate = prepaidCbRate;
  }

  public Boolean getCl_add_enable()
  {
    return cl_add_enable;
  }

  public void setCl_add_enable( Boolean cl_add_enable )
  {
    this.cl_add_enable = cl_add_enable;
  }

  public Integer getCl_credit_limit_for_pin()
  {
    return cl_credit_limit_for_pin;
  }

  public void setCl_credit_limit_for_pin( Integer cl_credit_limit_for_pin )
  {
    this.cl_credit_limit_for_pin = cl_credit_limit_for_pin;
  }

  public Boolean getInvoiceNumAsk()
  {
    return invoiceNumAsk;
  }

  public void setInvoiceNumAsk( Boolean invoiceNumAsk )
  {
    this.invoiceNumAsk = invoiceNumAsk;
  }

  public String getRegFormNum()
  {
    return regFormNum;
  }

  public void setRegFormNum( String regFormNum )
  {
    this.regFormNum = regFormNum;
  }

  public Integer getAdmin_status()
  {
    return admin_status;
  }

  public void setAdmin_status( Integer admin_status )
  {
    this.admin_status = admin_status;
  }

  public Address getAddress()
  {
    return address;
  }

  public void setAddress( Address address )
  {
    this.address = address;
  }

                                                    
  public Merchants save()
  {
    return Backendless.Data.of( Merchants.class ).save( this );
  }

  public Future<Merchants> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Merchants> future = new Future<Merchants>();
      Backendless.Data.of( Merchants.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<Merchants> callback )
  {
    Backendless.Data.of( Merchants.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Merchants.class ).remove( this );
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
      Backendless.Data.of( Merchants.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Merchants.class ).remove( this, callback );
  }

  public static Merchants findById( String id )
  {
    return Backendless.Data.of( Merchants.class ).findById( id );
  }

  public static Future<Merchants> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Merchants> future = new Future<Merchants>();
      Backendless.Data.of( Merchants.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<Merchants> callback )
  {
    Backendless.Data.of( Merchants.class ).findById( id, callback );
  }

  public static Merchants findFirst()
  {
    return Backendless.Data.of( Merchants.class ).findFirst();
  }

  public static Future<Merchants> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Merchants> future = new Future<Merchants>();
      Backendless.Data.of( Merchants.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<Merchants> callback )
  {
    Backendless.Data.of( Merchants.class ).findFirst( callback );
  }

  public static Merchants findLast()
  {
    return Backendless.Data.of( Merchants.class ).findLast();
  }

  public static Future<Merchants> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Merchants> future = new Future<Merchants>();
      Backendless.Data.of( Merchants.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<Merchants> callback )
  {
    Backendless.Data.of( Merchants.class ).findLast( callback );
  }

  public static BackendlessCollection<Merchants> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( Merchants.class ).find( query );
  }

  public static Future<BackendlessCollection<Merchants>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<Merchants>> future = new Future<BackendlessCollection<Merchants>>();
      Backendless.Data.of( Merchants.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<Merchants>> callback )
  {
    Backendless.Data.of( Merchants.class ).find( query, callback );
  }
}