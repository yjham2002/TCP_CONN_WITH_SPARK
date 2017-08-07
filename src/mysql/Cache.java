package mysql;

import models.DataMap;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2017-08-07.
 */
public class Cache {

    private static Cache instance;

    public ConcurrentHashMap<String, String> farmNames = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> harvNames = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> phones = new ConcurrentHashMap<>();

    private static final String QUERY_FARM_NAMES = "SELECT farm_code, farm_name FROM farm_list;";
    private static final String QUERY_HARV_NAMES = "SELECT farm_code, dong_code, dong_name FROM dong_list;";

    private Cache(){
        init();
    }

    public void init(){
        farmNames = new ConcurrentHashMap<>();
        harvNames = new ConcurrentHashMap<>();
        phones = new ConcurrentHashMap<>();

        System.out.println("[INFO] Cache DB Started");
        List<DataMap> farmNamesDB = DBManager.getInstance().getList(QUERY_FARM_NAMES);
        for(DataMap map : farmNamesDB) farmNames.put(map.getString("farm_code"), map.getString("farm_name"));

        List<DataMap> harvNamesDB = DBManager.getInstance().getList(QUERY_HARV_NAMES);
        for(DataMap map : harvNamesDB) harvNames.put(map.getString("farm_code") + "_" + map.getString("dong_code"), map.getString("dong_name"));

        System.out.println("[INFO] Cache DB Done");
    }

    public static Cache getInstance(){
        if(instance == null) instance = new Cache();
        return instance;
    }

    public void recache(){
        init();
    }

    public static String getHarvKey(String farm, String harv){
        return farm + "_" + harv;
    }

}
