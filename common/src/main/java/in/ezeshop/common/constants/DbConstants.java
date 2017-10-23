package in.ezeshop.common.constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adgangwa on 07-05-2016.
 */
public class DbConstants {

    // Users table - 'user_type' column values
    public static final int USER_TYPE_MERCHANT = 1;
    public static final int USER_TYPE_CUSTOMER = 2;
    public static final int USER_TYPE_AGENT = 3;
    public static final int USER_TYPE_CC = 4;
    //public static final int USER_TYPE_CCNT = 5;
    public static final int USER_TYPE_ADMIN = 0;
    // user type code to text description
    public static String userTypeDesc[] = {
            "Admin",
            "Merchant",
            "Customer",
            "Agent",
            "CustomerCare",
            "Controller"
    };

    // 'admin_status' column values - user tables
    public static final int USER_STATUS_ACTIVE = 1;
    // Disabled means permanent - until further action by the admin to explicitly enable the account
    public static final int USER_STATUS_DISABLED = 2;
    // Locked means temporary - and will be automatically unlocked after defined duration
    public static final int USER_STATUS_LOCKED = 3;
    // Error during registration - to be manually deleted
    public static final int USER_STATUS_REG_ERROR = 4;
    //public static final int USER_STATUS_READY_TO_ACTIVE = 5;
    public static final int USER_STATUS_UNDER_CLOSURE = 6;
    // mainly for customer users: indicate mobile number is changed recently
    // and access to account will be restricted. Only 'Credit' txns will be allowed
    public static final int USER_STATUS_LIMITED_CREDIT_ONLY = 7;
    // status code to text description
    public static String userStatusDesc[] = {
            "",
            "Active",
            "Disabled",
            "Locked",
            "Not Registered",
            "",
            "Under Closure Notice",
            "Limited: Credit Only"
    };

    // CustomerCards table - 'status' column values
    /*public static final int CUSTOMER_CARD_STATUS_FOR_PRINT = 0;
    public static final int CUSTOMER_CARD_STATUS_NEW = 1;
    //public static final int CUSTOMER_CARD_STATUS_WITH_AGENT = 2;
    public static final int CUSTOMER_CARD_STATUS_WITH_MERCHANT = 3;
    public static final int CUSTOMER_CARD_STATUS_ACTIVE = 4;
    public static final int CUSTOMER_CARD_STATUS_DISABLED = 5;
    public static final int CUSTOMER_CARD_NOT_AVAILABLE = 6;
    // Map int status values to corresponding descriptions
    public static String cardStatusDesc[] = {
            "Invalid",
            "Invalid",
            "Invalid",
            "Allotted to Merchant",
            "Active",
            "Disabled",
            "Not Allotted"
    };
    public static String cardStatusDescInternal[] = {
            "Out for Print",
            "New Card",
            "Allotted to Agent",
            "Allotted to Merchant",
            "Active",
            "Disabled",
            "Not Allotted"
    };*/

    // 'opcode' values used in different tables
    public static final String OP_REG_CUSTOMER = "Register Customer";

    public static final String OP_LOGIN = "Login";
    public static final String OP_TXN_COMMIT = "Transaction Submit";
    public static final String OP_CHANGE_MOBILE = "Mobile Number Change";

    public static final String OP_FORGOT_USERID = "Forgot User ID";
    public static final String OP_RESET_PASSWD = "Password Reset";
    public static final String OP_CHANGE_PASSWD = "Password Change";

    public static final String OP_SEND_PASSWD_RESET_HINT = "Send Password Reset Hint";
    //public static final String OP_RESET_TRUSTED_DEVICES = "Reset Trusted Devices";
    public static final String OP_ENABLE_ACC = "Enable Account";
    public static final String OP_DISABLE_ACC = "Disable Account";
    public static final String OP_LIMITED_MODE_ACC = "Limit Account";
    public static final String OP_ACC_CLOSURE = "Account Closure";
    public static final String OP_CANCEL_ACC_CLOSURE = "Cancel Account Closure";

    //public static final String OP_NEW_CARD = "Change Member Card";
    //public static final String OP_DISABLE_CARD = "Disable Member Card";
    public static final String OP_RESET_PIN = "Reset PIN";
    public static final String OP_CHANGE_PIN = "Change PIN";

    // Transactions table values
    public static final String TXN_CUSTOMER_PIN_USED = "Yes";
    public static final String TXN_CUSTOMER_PIN_NOT_USED = "No";
    public static final String TXN_CUSTOMER_OTP_USED = "OTP";

    // Customer Orders Table values
    public enum CUSTOMER_ORDER_STATUS {
        New,
        Accepted,
        Dispatched,
        Delivered,
        Cancelled,
        Rejected;

        public static String toString(CUSTOMER_ORDER_STATUS status) {
            switch (status) {
                case New:
                    return "New";
                case Accepted:
                    return "Accepted";
                case Dispatched:
                    return "Dispatched";
                case Delivered:
                    return "Delivered";
                case Cancelled:
                    return "Cancelled";
                case Rejected:
                    return "Rejected";
                default:
                    return "";
            }
        }

        // reverse of toString
        public static CUSTOMER_ORDER_STATUS fromString(String status) {
            switch (status) {
                case "New":
                    return New;
                case "Accepted":
                    return Accepted;
                case "Dispatched":
                    return Dispatched;
                case "Delivered":
                    return Delivered;
                case "Cancelled":
                    return Cancelled;
                case "Rejected":
                    return Rejected;
                default:
                    return null;
            }
        }
    }

    public static String custOrderStatusChgReasons[] = {
            "Not Attended",
            "Invalid"
    };

    // SKU to Item Name mapping
    /*public static final String SKU_CUSTOMER_CARDS = "1001";
    public static final Map<String, String> skuToItemName;
    static {
        Map<String, String> aMap = new HashMap<>(5);
        aMap.put(SKU_CUSTOMER_CARDS, "Customer Cards");
        skuToItemName = Collections.unmodifiableMap(aMap);
    }
    public enum MO_PAY_MODE {
        Cash,
        Cheque,
        Credit_Card,
        Debit_Card,
        PayTm,
        NEFT,
        IMPS,
        UPI,
        Other;

        public static CharSequence[] getValueSet() {
            ArrayList<String> list = new ArrayList<>();
            for (MO_PAY_MODE s : MO_PAY_MODE.values()) {
                list.add(s.name());
            }
            return list.toArray(new CharSequence[list.size()]);
        }
    }*/



}
