package in.ezeshop.common.database;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.Date;

public class MerchantOps
{
  private String ownerId;
  private String mobile_num;
  private String op_code;
  private String initiatedBy;
  private String initiatedVia;
  private String extra_op_params;
  private String objectId;
  private java.util.Date created;
  private String ticketNum;
  private String merchant_id;
  private java.util.Date updated;
  private String otp;
  private String op_status;
  private String reason;
  private String agentId;
  private String remarks;
  private java.util.Date createTime;

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getAgentId() {
    return agentId;
  }

  public void setAgentId(String agentId) {
    this.agentId = agentId;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public String getMobile_num()
  {
    return mobile_num;
  }

  public void setMobile_num( String mobile_num )
  {
    this.mobile_num = mobile_num;
  }

  public String getOp_code()
  {
    return op_code;
  }

  public void setOp_code( String op_code )
  {
    this.op_code = op_code;
  }

  public String getInitiatedBy()
  {
    return initiatedBy;
  }

  public void setInitiatedBy( String initiatedBy )
  {
    this.initiatedBy = initiatedBy;
  }

  public String getInitiatedVia()
  {
    return initiatedVia;
  }

  public void setInitiatedVia( String initiatedVia )
  {
    this.initiatedVia = initiatedVia;
  }

  public String getExtra_op_params()
  {
    return extra_op_params;
  }

  public void setExtra_op_params( String extra_op_params )
  {
    this.extra_op_params = extra_op_params;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getTicketNum()
  {
    return ticketNum;
  }

  public void setTicketNum( String ticketNum )
  {
    this.ticketNum = ticketNum;
  }

  public String getMerchant_id()
  {
    return merchant_id;
  }

  public void setMerchant_id( String merchant_id )
  {
    this.merchant_id = merchant_id;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public String getOtp()
  {
    return otp;
  }

  public void setOtp( String otp )
  {
    this.otp = otp;
  }

  public String getOp_status()
  {
    return op_status;
  }

  public void setOp_status( String op_status )
  {
    this.op_status = op_status;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason(String reason)
  {
    this.reason = reason;
  }


  public MerchantOps save()
  {
    return Backendless.Data.of( MerchantOps.class ).save( this );
  }

  public Future<MerchantOps> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MerchantOps> future = new Future<MerchantOps>();
      Backendless.Data.of( MerchantOps.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<MerchantOps> callback )
  {
    Backendless.Data.of( MerchantOps.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( MerchantOps.class ).remove( this );
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
      Backendless.Data.of( MerchantOps.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( MerchantOps.class ).remove( this, callback );
  }

  public static MerchantOps findById( String id )
  {
    return Backendless.Data.of( MerchantOps.class ).findById( id );
  }

  public static Future<MerchantOps> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MerchantOps> future = new Future<MerchantOps>();
      Backendless.Data.of( MerchantOps.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<MerchantOps> callback )
  {
    Backendless.Data.of( MerchantOps.class ).findById( id, callback );
  }

  public static MerchantOps findFirst()
  {
    return Backendless.Data.of( MerchantOps.class ).findFirst();
  }

  public static Future<MerchantOps> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MerchantOps> future = new Future<MerchantOps>();
      Backendless.Data.of( MerchantOps.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<MerchantOps> callback )
  {
    Backendless.Data.of( MerchantOps.class ).findFirst( callback );
  }

  public static MerchantOps findLast()
  {
    return Backendless.Data.of( MerchantOps.class ).findLast();
  }

  public static Future<MerchantOps> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<MerchantOps> future = new Future<MerchantOps>();
      Backendless.Data.of( MerchantOps.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<MerchantOps> callback )
  {
    Backendless.Data.of( MerchantOps.class ).findLast( callback );
  }

  public static BackendlessCollection<MerchantOps> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( MerchantOps.class ).find( query );
  }

  public static Future<BackendlessCollection<MerchantOps>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<MerchantOps>> future = new Future<BackendlessCollection<MerchantOps>>();
      Backendless.Data.of( MerchantOps.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<MerchantOps>> callback )
  {
    Backendless.Data.of( MerchantOps.class ).find( query, callback );
  }
}