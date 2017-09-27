package in.ezeshop.customerbase.helper;

import com.backendless.push.BackendlessBroadcastReceiver;
import com.backendless.push.BackendlessPushService;

/**
 * Created by adgangwa on 27-09-2017.
 */

public class CustPushReceiver extends BackendlessBroadcastReceiver
{
    @Override
    public Class<? extends BackendlessPushService> getServiceClass()
    {
        return CustPushService.class;
    }
}