package flo.org.exchange.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by Mayur on 04/12/16.
 */

public class buyFragmentVariables {
    public static final String BUY_FRAGMENT_VARIABLES_FILE = "buyFragmentVariables";
    public static final String BUY_FRAGMENT_VARIABLES = "buyFragmentVariablesJson";
    private String buyFragmentVariablesJson;

    public carousal carousal;
    public categories categories;
    public stores stores;
    public String lastUpdatedOn;


    private Context context;

    public buyFragmentVariables (Context context){ this.context = context;}


    public class carousal{
        public int count;
        public ArrayList<items> items  = new ArrayList<items>();

        public class items{
            public String  whereClause;
            public String  photoUrl;
            public int screenType;
            public String  title;
            public boolean poll;
            public String  pollUrl;
            public int Status;
            public String ___class;
        }

    }
    public class categories {
        public int count;
        public ArrayList<items> items = new ArrayList<items>();

        public class items {
            public String title;
            public int Status;
            public String whereClause;
            public boolean poll;
            public String  pollUrl;
            public String ___class;
        }
    }
    public class stores{
        public int    count;
        public ArrayList<items>    items = new ArrayList<items>();
        public class items{
            public String title;
            public int    Status;
            public String whereClause;
            public String photoUrl;
            public boolean poll;
            public String  pollUrl;
            public String ___class;
        }

    }

    public String getOfflineJson() {

        return readPreferences(context,BUY_FRAGMENT_VARIABLES,null);
    }

    public void setOfflineJson(String buyFragmentVariablesJson) {
        this.buyFragmentVariablesJson = buyFragmentVariablesJson;
        setPreferences(context, BUY_FRAGMENT_VARIABLES, buyFragmentVariablesJson);
    }


    private static void setPreferences(Context actContext, String preferenceName, String preferenceValue){

        SharedPreferences savedPreferences = actContext.getSharedPreferences(BUY_FRAGMENT_VARIABLES_FILE,actContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putString(preferenceName,preferenceValue);
        editor.apply();

    }

    private static String readPreferences(Context actContext, String preferenceName, String preferenceDefaultValue){

        SharedPreferences savedPreferences = actContext.getSharedPreferences(BUY_FRAGMENT_VARIABLES_FILE,actContext.MODE_PRIVATE);
        return savedPreferences.getString(preferenceName,preferenceDefaultValue);

    }

}
