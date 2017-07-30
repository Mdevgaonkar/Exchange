package flo.org.campusmein.app.Home.search;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.MainHomeActivity;
import flo.org.campusmein.app.utils.Person;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.SuggestionObject;
import flo.org.campusmein.app.utils.SuggestionRealmObject;
import flo.org.campusmein.app.utils.campusExchangeApp;
import io.realm.Realm;
import io.realm.RealmResults;

public class Search {

    private static final String RECORD_ID = "id";
    private static final String RECORD_TITLE = "title";
    private static final String RECORD_SUBTITLE = "subTitle";
    private static final String RECORD_TAGS= "tags";

    Context c;
    AppCompatActivity activity;
    String d;
    public boolean process_complete_indicator = false;
    Gson g = campusExchangeApp.getInstance().getGson();

    Realm realm = RealmController.getInstance().getRealm();
    Person p = campusExchangeApp.getInstance().getUniversalPerson();
    private SimpleCursorAdapter busStopCursorAdapter;
    private MatrixCursor cursor;

    private ProgressDialog mProgressDialog;


    public Search(Context context, AppCompatActivity activity) {
        this.c = context;
        this.activity = activity;

    }

    public Search() {
    }

    public void getNewDataset(String dataset_upd_dt) {
        showProgressDialog(c.getString(R.string.loading));
        this.d = dataset_upd_dt;

        JsonObjectRequest fetchDataset = new JsonObjectRequest(
                Request.Method.GET,
                c.getString(R.string.suggestionsDataset), // new link to get data set from
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Dataset",response.toString());

                        JsonArray data = responseToJsonDataArray(response.toString());
                        saveDataset(data);
                        Toast.makeText(c, "updating dataset", Toast.LENGTH_SHORT).show();
                        process_complete_indicator = true;

                        hideProgressDialog();



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley error",error.toString());
                        Toast.makeText(c,"Error Occured while loading search dataset",Toast.LENGTH_SHORT).show();
                    }
                }
        );

        campusExchangeApp.getInstance().addToRequestQueue(fetchDataset,"FetchingSearchDataSet");

    }

    private void saveDataset(JsonArray data) {
        String branches_json_str= data.toString();

        ArrayList<SuggestionObject> SuggestionObjectArray = g.fromJson(branches_json_str, new TypeToken<List<SuggestionObject>>() {}.getType());
//        Log.d("SuggestionObjectArray", SuggestionObjectArray.size()+"");
//        Log.d("SuggestionObjectArray",SuggestionObjectArray.get(62).getTitle());
        clearDataset();


        for (int i=0; i<SuggestionObjectArray.size()-1;i++) {
            //Log.d("SuggestionObjectArray",SuggestionObjectArray.get(0).getTitle());
//            SuggestionObjectArray.get(2).getTitle();
//            Log.d("suggestionObject", suggestionObject.getId());

            String id = SuggestionObjectArray.get(i).getId();
            String title = SuggestionObjectArray.get(i).getTitle();
            String subTitle = SuggestionObjectArray.get(i).getSubTitle();
            String whereClause = SuggestionObjectArray.get(i).getWhereClause();
            String tags = SuggestionObjectArray.get(i).getTags();
//            Log.d("sgs",sgstnObj.toString());

            SuggestionRealmObject sggObj = new SuggestionRealmObject(id,title,subTitle,whereClause,tags);
            addToDataset(sggObj);
            if(i==SuggestionObjectArray.size()-2){
//                Log.d("Dataset_upd_dt",d);
                p.setDataset_upd_dt(d);
            }
        }



    }

    private JsonArray responseToJsonDataArray(String respStr) {
        JsonParser respParser = new JsonParser();
        JsonObject respObject = respParser.parse(respStr).getAsJsonObject();
        JsonArray data = (JsonArray) respObject.get("data");
        return data;
    }


    //search Suggestion realm

    //clear all dataset
    public void clearDataset() {
        realm.beginTransaction();
        realm.clear(SuggestionRealmObject.class);
        realm.commitTransaction();
    }

    //get all items in dataset
    public RealmResults<SuggestionRealmObject> getAllDataset() {
        return realm.where(SuggestionRealmObject.class).findAll();
    }

    //find from dataset
    public RealmResults<SuggestionRealmObject> FindInDataset(String keyword) {
        keyword = applyFilters(keyword);
        Log.i("keyword",keyword);
        return realm.where(SuggestionRealmObject.class).contains(RECORD_TAGS,keyword,false).or().contains(RECORD_TITLE,keyword,false).or().contains(RECORD_SUBTITLE,keyword,false).findAll();
    }

    private String applyFilters(String keyword) {
        if(keyword.equals("fe") || keyword.equals("Fe") || keyword.equals("FE") || keyword.equals("fE")){
            return "#FE#";
        }else if(keyword.equals("se") || keyword.equals("Se") || keyword.equals("SE") || keyword.equals("sE")){
            return "#SE#";
        }else if(keyword.equals("te") || keyword.equals("Te") || keyword.equals("TE") || keyword.equals("tE")){
            return "#TE#";
        }else if(keyword.equals("be") || keyword.equals("Be") || keyword.equals("BE") || keyword.equals("bE")){
            return "#BE#";
        }else {
            return keyword;
        }
    }

    public void addToDataset(SuggestionRealmObject suggestionObject){


        realm.beginTransaction();
        realm.copyToRealm(suggestionObject);
        realm.commitTransaction();
    }

    //remove any record from data set
    public void removeDatasetRecord(String id){
        realm.beginTransaction();
        RealmResults<SuggestionRealmObject> b = realm.where(SuggestionRealmObject.class).equalTo(RECORD_ID,id).findAll();
        if(b.size()>0){
            b.remove(0);
        }
        realm.commitTransaction();
    }


    public CursorAdapter getSuggestionAdapter() {

        String[] from = {"title", "subTitle" ,"whereClause","tags"};
        int[] to = {R.id.search_title,R.id.search_subtitle,R.id.search_where,R.id.search_tags};
        busStopCursorAdapter = new SimpleCursorAdapter(c, R.layout.activity_search, cursor, from, to);
        return  busStopCursorAdapter;
    }

    public void setNewSuggestions(ArrayList<SuggestionRealmObject> s){
        String[] columnNames = {"_id","id","title", "subTitle" ,"whereClause","tags"};

        cursor = new MatrixCursor(columnNames);
        //if strings are in resources


//        sggstn_str_arr = new String[];

        String[] temp = new String[6];

        int id = 0;
//        for(String item : sggstn_str_arr){
//        }
//        id=0;
        for(SuggestionRealmObject realmObject : s){
            temp[0] = Integer.toString(id++);
            temp[1] = realmObject.getId();
            temp[2] = realmObject.getTitle();
            temp[3] = realmObject.getSubTitle();
            temp[4] = realmObject.getWhereClause();
            temp[5] = realmObject.getTags();
            cursor.addRow(temp);
        }
    }

    public void showProgressDialog(String progressString) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage(progressString);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(progressString);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
            mProgressDialog.dismiss();
        }
    }



}
