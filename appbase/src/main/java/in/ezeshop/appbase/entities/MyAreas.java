package in.ezeshop.appbase.entities;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import in.ezeshop.appbase.utilities.LogMy;
import in.ezeshop.common.constants.CommonConstants;
import in.ezeshop.common.constants.ErrorCodes;
import in.ezeshop.common.database.Areas;

/**
 * Created by adgangwa on 11-10-2017.
 */

public class MyAreas {
    private static String TAG="BaseApp-MyAreas";

    // City -> Area List
    private static HashMap<String,ArrayList<Areas>> mCityToAreas;
    // City -> Area Name List
    private static HashMap<String,ArrayList<String>> mCityToAreaNames;
    // AreaID -> Area object
    private static HashMap<Integer,Areas> mIdToAreas;

    public static int initSync() {

        try {
            if (mCityToAreas == null) {
                mCityToAreas = new HashMap<>();
            } else {
                mCityToAreas.clear();
            }
            if (mIdToAreas == null) {
                mIdToAreas = new HashMap<>();
            } else {
                mIdToAreas.clear();
            }
            if (mCityToAreaNames == null) {
                mCityToAreaNames = new HashMap<>();
            } else {
                mCityToAreaNames.clear();
            }

            // Fetch all Areas from DB
            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.addRelated("city");
            dataQuery.setQueryOptions(queryOptions);
            dataQuery.setPageSize(CommonConstants.DB_QUERY_PAGE_SIZE);

            BackendlessCollection<Areas> collection = Backendless.Data.of(Areas.class).find(dataQuery);
            int cnt = collection.getTotalObjects();
            if (cnt == 0) {
                LogMy.e(TAG, "No Areas fetched from DB");
                return ErrorCodes.GENERAL_ERROR;
            } else {
                LogMy.d(TAG, "Fetched areas: " + cnt);
            }
            while (collection.getCurrentPage().size() > 0) {
                Iterator<Areas> iterator = collection.getCurrentPage().iterator();
                while (iterator.hasNext()) {
                    Areas item = iterator.next();
                    if (!item.getAreaName().equals(CommonConstants.DUMMY_AREA_NAME)) {
                        // add to id->area hash
                        mIdToAreas.put(item.getId(), item);

                        String cityName = item.getCity().getCity();

                        // add to city->areaList hash
                        ArrayList<Areas> areaList = mCityToAreas.get(cityName);
                        if (areaList == null) {
                            // no value added yet for this city
                            LogMy.d(TAG, "First area for city: " + cityName);
                            areaList = new ArrayList<>();
                            mCityToAreas.put(cityName, areaList);
                        }
                        areaList.add(item);

                        // add to city->areaNames hash
                        ArrayList<String> areaNamesList = mCityToAreaNames.get(cityName);
                        if (areaNamesList == null) {
                            // no value added yet for this city
                            LogMy.d(TAG, "First area name for city: " + cityName);
                            areaNamesList = new ArrayList<>();
                            mCityToAreaNames.put(cityName, areaNamesList);
                        }
                        areaNamesList.add(item.getAreaName());
                    }
                }
            }
        } catch (Exception e) {
            LogMy.e(TAG,"Exception  in MyAreas:initSync",e);
            mCityToAreas = null;
            mIdToAreas = null;
            return ErrorCodes.GENERAL_ERROR;
        }
        return ErrorCodes.NO_ERROR;
    }

    public static CharSequence[] getAreaNameList(String city) {
        if(mCityToAreaNames==null) {
            LogMy.e(TAG,"getAreaValueSet: mCityToAreaNames store is null");
            return null;
        }
        ArrayList<String> areaNameList = mCityToAreaNames.get(city);
        if(areaNameList==null) {
            LogMy.e(TAG,"getAreaValueSet: No areas available for "+city);
            return null;
        }
        return areaNameList.toArray(new CharSequence[areaNameList.size()]);
    }

    public static Areas getAreaObject(String city, String areaName) {
        if(mCityToAreas==null) {
            LogMy.e(TAG,"getAreaObject: mCityToAreas store is null");
            return null;
        }
        ArrayList<Areas> areaList = mCityToAreas.get(city);
        if(areaList==null) {
            LogMy.e(TAG,"getAreaObject: No areas available for "+city);
            return null;
        }

        // loop on list to find matching area
        for (Areas area : areaList) {
            if (area.getAreaName().equals(areaName)) {
                return area;
            }
        }
        LogMy.e(TAG,"Area object not found: "+city+":"+areaName);
        return null;
    }
}
