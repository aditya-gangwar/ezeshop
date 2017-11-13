package in.ezeshop.common.database;

/*
 * All fields below in logical custOrder for easier understanding
 *
 * - create_time
 * - trans_id
 * - status
 *
 * Merchant and Customer data
 * - merchant_id
 * - merchant_name
 * - cust_private_id
 * - cust_mobile
 *
 * Billing details
 * - total_billed
 * - delCharge
 * - cl_credit
 * - cl_debit
 * - cl_overdraft
 * - paymentAmt
 * Cashback related
 * - cb_eligible_amt
 * - cb_percent
 * - cb_credit
 * Extra cashback related
 * - extra_cb_percent
 * - extracb_eligible_amt
 * - extra_cb_credit
 *
 * Other data
 * - invoiceNum
 * - cpin
 * - archived
 *
 * Child objects
 * - cashback -- will be null for non-committed txns i.e. isFinal = false
 * - online customer custOrder
 */


import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.persistence.BackendlessDataQuery;

public class Transaction
{
  private Integer cb_credit;
  private Integer extracb_eligible_amt;
  private Integer extra_cb_credit;
  private String cust_private_id;
  private String invoiceNum;
  private Integer cl_credit;
  private String extra_cb_percent;
  private java.util.Date created;
  private String cpin;
  private String trans_id;
  private String objectId;
  private String ownerId;
  private String merchant_id;
  private Boolean archived;
  private java.util.Date updated;
  private Integer paymentAmt;
  private java.util.Date create_time;
  private Integer cl_overdraft;
  private Integer total_billed;
  private Integer cb_eligible_amt;
  private String merchant_name;
  private String cb_percent;
  private String cust_mobile;
  private Integer cl_debit;
  private Integer delCharge;
  private String status;
  private Cashback cashback;
  private CustomerOrder custOrder;

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getDelCharge() {
    return delCharge;
  }

  public void setDelCharge(Integer delCharge) {
    this.delCharge = delCharge;
  }

  public Integer getCb_credit()
  {
    return cb_credit;
  }

  public void setCb_credit( Integer cb_credit )
  {
    this.cb_credit = cb_credit;
  }

  public Integer getExtracb_eligible_amt()
  {
    return extracb_eligible_amt;
  }

  public void setExtracb_eligible_amt( Integer extracb_eligible_amt )
  {
    this.extracb_eligible_amt = extracb_eligible_amt;
  }

  public Integer getExtra_cb_credit()
  {
    return extra_cb_credit;
  }

  public void setExtra_cb_credit( Integer extra_cb_credit )
  {
    this.extra_cb_credit = extra_cb_credit;
  }

  public String getCust_private_id()
  {
    return cust_private_id;
  }

  public void setCust_private_id( String cust_private_id )
  {
    this.cust_private_id = cust_private_id;
  }

  public String getInvoiceNum()
  {
    return invoiceNum;
  }

  public void setInvoiceNum( String invoiceNum )
  {
    this.invoiceNum = invoiceNum;
  }

  public Integer getCl_credit()
  {
    return cl_credit;
  }

  public void setCl_credit( Integer cl_credit )
  {
    this.cl_credit = cl_credit;
  }

  public String getExtra_cb_percent()
  {
    return extra_cb_percent;
  }

  public void setExtra_cb_percent( String extra_cb_percent )
  {
    this.extra_cb_percent = extra_cb_percent;
  }

  public java.util.Date getCreated()
  {
    return created;
  }

  public String getCpin()
  {
    return cpin;
  }

  public void setCpin( String cpin )
  {
    this.cpin = cpin;
  }

  public String getTrans_id()
  {
    return trans_id;
  }

  public void setTrans_id( String trans_id )
  {
    this.trans_id = trans_id;
  }

  public String getObjectId()
  {
    return objectId;
  }

  public String getOwnerId()
  {
    return ownerId;
  }

  public String getMerchant_id()
  {
    return merchant_id;
  }

  public void setMerchant_id( String merchant_id )
  {
    this.merchant_id = merchant_id;
  }

  public Boolean getArchived()
  {
    return archived;
  }

  public void setArchived( Boolean archived )
  {
    this.archived = archived;
  }

  public java.util.Date getUpdated()
  {
    return updated;
  }

  public Integer getPaymentAmt()
  {
    return paymentAmt;
  }

  public void setPaymentAmt( Integer paymentAmt )
  {
    this.paymentAmt = paymentAmt;
  }

  public java.util.Date getCreate_time()
  {
    return create_time;
  }

  public void setCreate_time( java.util.Date create_time )
  {
    this.create_time = create_time;
  }

  public Integer getCl_overdraft()
  {
    return cl_overdraft;
  }

  public void setCl_overdraft( Integer cl_overdraft )
  {
    this.cl_overdraft = cl_overdraft;
  }

  public Integer getTotal_billed()
  {
    return total_billed;
  }

  public void setTotal_billed( Integer total_billed )
  {
    this.total_billed = total_billed;
  }

  public Integer getCb_eligible_amt()
  {
    return cb_eligible_amt;
  }

  public void setCb_eligible_amt( Integer cb_eligible_amt )
  {
    this.cb_eligible_amt = cb_eligible_amt;
  }

  public String getMerchant_name()
  {
    return merchant_name;
  }

  public void setMerchant_name( String merchant_name )
  {
    this.merchant_name = merchant_name;
  }

  public String getCb_percent()
  {
    return cb_percent;
  }

  public void setCb_percent( String cb_percent )
  {
    this.cb_percent = cb_percent;
  }

  public String getCust_mobile()
  {
    return cust_mobile;
  }

  public void setCust_mobile( String cust_mobile )
  {
    this.cust_mobile = cust_mobile;
  }

  public Integer getCl_debit()
  {
    return cl_debit;
  }

  public void setCl_debit( Integer cl_debit )
  {
    this.cl_debit = cl_debit;
  }

  public Cashback getCashback()
  {
    return cashback;
  }

  public void setCashback( Cashback cashback )
  {
    this.cashback = cashback;
  }

  public CustomerOrder getCustOrder() {
    return custOrder;
  }

  public void setCustOrder(CustomerOrder custOrder) {
    this.custOrder = custOrder;
  }

  public Transaction save()
  {
    return Backendless.Data.of( Transaction.class ).save( this );
  }

  public Future<Transaction> saveAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Transaction> future = new Future<Transaction>();
      Backendless.Data.of( Transaction.class ).save( this, future );

      return future;
    }
  }

  public void saveAsync( AsyncCallback<Transaction> callback )
  {
    Backendless.Data.of( Transaction.class ).save( this, callback );
  }

  public Long remove()
  {
    return Backendless.Data.of( Transaction.class ).remove( this );
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
      Backendless.Data.of( Transaction.class ).remove( this, future );

      return future;
    }
  }

  public void removeAsync( AsyncCallback<Long> callback )
  {
    Backendless.Data.of( Transaction.class ).remove( this, callback );
  }

  public static Transaction findById( String id )
  {
    return Backendless.Data.of( Transaction.class ).findById( id );
  }

  public static Future<Transaction> findByIdAsync( String id )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Transaction> future = new Future<Transaction>();
      Backendless.Data.of( Transaction.class ).findById( id, future );

      return future;
    }
  }

  public static void findByIdAsync( String id, AsyncCallback<Transaction> callback )
  {
    Backendless.Data.of( Transaction.class ).findById( id, callback );
  }

  public static Transaction findFirst()
  {
    return Backendless.Data.of( Transaction.class ).findFirst();
  }

  public static Future<Transaction> findFirstAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Transaction> future = new Future<Transaction>();
      Backendless.Data.of( Transaction.class ).findFirst( future );

      return future;
    }
  }

  public static void findFirstAsync( AsyncCallback<Transaction> callback )
  {
    Backendless.Data.of( Transaction.class ).findFirst( callback );
  }

  public static Transaction findLast()
  {
    return Backendless.Data.of( Transaction.class ).findLast();
  }

  public static Future<Transaction> findLastAsync()
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<Transaction> future = new Future<Transaction>();
      Backendless.Data.of( Transaction.class ).findLast( future );

      return future;
    }
  }

  public static void findLastAsync( AsyncCallback<Transaction> callback )
  {
    Backendless.Data.of( Transaction.class ).findLast( callback );
  }

  public static BackendlessCollection<Transaction> find( BackendlessDataQuery query )
  {
    return Backendless.Data.of( Transaction.class ).find( query );
  }

  public static Future<BackendlessCollection<Transaction>> findAsync( BackendlessDataQuery query )
  {
    if( Backendless.isAndroid() )
    {
      throw new UnsupportedOperationException( "Using this method is restricted in Android" );
    }
    else
    {
      Future<BackendlessCollection<Transaction>> future = new Future<BackendlessCollection<Transaction>>();
      Backendless.Data.of( Transaction.class ).find( query, future );

      return future;
    }
  }

  public static void findAsync( BackendlessDataQuery query, AsyncCallback<BackendlessCollection<Transaction>> callback )
  {
    Backendless.Data.of( Transaction.class ).find( query, callback );
  }
}