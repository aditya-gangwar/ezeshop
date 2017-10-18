package in.ezeshop.appbase.entities;

/**
 * Created by adgangwa on 19-02-2016.
 */

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Areas;
import in.ezeshop.common.database.Cities;
import in.ezeshop.appbase.utilities.LogMy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MyCities
{
    private static String TAG="BaseApp-MyCities";

    private static ArrayList<String> mCityValueSet;
    private static HashMap<String,Cities> mObjectMap;

    public static int initSync() {
        try {
            if(mCityValueSet == null) {
                mCityValueSet = new ArrayList<>();
            } else {
                mCityValueSet.clear();
            }
            if(mObjectMap == null) {
                mObjectMap = new HashMap<>();
            } else {
                mObjectMap.clear();
            }

            // Fetch all categories from DB and build value set
            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions("city");
            dataQuery.setQueryOptions(queryOptions);
            dataQuery.setPageSize(CommonConstants.DB_QUERY_PAGE_SIZE);

            BackendlessCollection<Cities> collection = Backendless.Data.of(Cities.class).find(dataQuery);
            int cnt = collection.getTotalObjects();
            if (cnt == 0) {
                LogMy.e(TAG, "No Cities fetched from DB");
                return ErrorCodes.GENERAL_ERROR;
            } else {
                LogMy.d(TAG, "Fetched Cities: " + cnt);
            }
            while (collection.getCurrentPage().size() > 0) {
                Iterator<Cities> iterator = collection.getCurrentPage().iterator();
                while (iterator.hasNext()) {
                    Cities item = iterator.next();
                    if (!item.getCity().equals(CommonConstants.DUMMY_CITY_NAME)) {
                        mObjectMap.put(item.getCity(), item);
                        mCityValueSet.add(item.getCity());
                    }
                }
                collection = collection.nextPage();
            }
        } catch (Exception e) {
            LogMy.e(TAG,"Exception  in MyCities:fetchAreas",e);
            mObjectMap = null;
            mCityValueSet = null;
            return ErrorCodes.GENERAL_ERROR;
        }

        return ErrorCodes.NO_ERROR;
    }

    /*public static void init() {
        if(mCityValueSet == null) {
            mCityValueSet = new ArrayList<>();
        } else {
            mCityValueSet.clear();
        }
        if(mObjectMap == null) {
            mObjectMap = new HashMap<>();
        } else {
            mObjectMap.clear();
        }

        // Fetch all categories from DB and build value set
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        QueryOptions queryOptions = new QueryOptions("city");
        dataQuery.setQueryOptions(queryOptions);
        dataQuery.setPageSize(CommonConstants.DB_QUERY_PAGE_SIZE);

        Backendless.Data.of( Cities.class ).find(dataQuery, new AsyncCallback<BackendlessCollection<Cities>>() {
            @Override
            public void handleResponse(BackendlessCollection<Cities> items) {
                Iterator<Cities> iterator = items.getCurrentPage().iterator();
                while (iterator.hasNext()) {
                    Cities item = iterator.next();
                    if(!item.getCity().equals(CommonConstants.DUMMY_CITY_NAME)) {
                        mObjectMap.put(item.getCity(), item);
                        mCityValueSet.add(item.getCity());
                    }
                }

                if( items.getCurrentPage().size() > 0 )
                    items.nextPage( this );
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                LogMy.e(TAG, "Failed to get Cities rows: " + backendlessFault.getMessage());
                mCityValueSet = null;
                mObjectMap = null;
            }
        });
    }*/

    public static Cities getCityWithName(String name) {
        return mObjectMap==null?null:mObjectMap.get(name);
    }

    public static CharSequence[] getCityValueSet() {
        return mCityValueSet==null?null:mCityValueSet.toArray(new CharSequence[mCityValueSet.size()]);
    }
}
