package flo.org.campusmein.app.Home.sell;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.MainHomeActivity;
import flo.org.campusmein.app.utils.buyFragmentVariables;
import flo.org.campusmein.app.utils.campusExchangeApp;

/**
 * A simple {@link Fragment} subclass.
 */
public class SellFragment extends Fragment implements View.OnClickListener{

    private LinearLayout sell_booksCategory,sell_utilitiesCategory,sell_novelsCategory,sell_instumentsCategory;

    public SellFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View home_sell_view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_sell,container,false);
        setupSellCategories(home_sell_view);
        return home_sell_view;

    }

    private void setupSellCategories(View home_sell_view) {
        sell_booksCategory= (LinearLayout) home_sell_view.findViewById(R.id.sell_booksCategory);
        sell_booksCategory.setOnClickListener(this);
        sell_utilitiesCategory= (LinearLayout) home_sell_view.findViewById(R.id.sell_utilitiesCategory);
        sell_utilitiesCategory.setOnClickListener(this);
        sell_novelsCategory= (LinearLayout) home_sell_view.findViewById(R.id.sell_novelsCategory);
        sell_novelsCategory.setOnClickListener(this);
        sell_instumentsCategory= (LinearLayout) home_sell_view.findViewById(R.id.sell_instumentsCategory);
        sell_instumentsCategory.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sell_booksCategory :
                showCommingSoonDialog();
                break;
            case R.id.sell_utilitiesCategory :
                showCommingSoonDialog();
                break;
            case R.id.sell_novelsCategory :
                showCommingSoonDialog();
                break;
            case R.id.sell_instumentsCategory :
                showCommingSoonDialog();
                break;
        }

    }

    private void showCommingSoonDialog(){

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.Sell_Coming_soon));
            builder.setMessage(getString(R.string.sell_coming_doon_message));

            String positiveText = getString(android.R.string.ok);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // positive button logic
                            Context mContext = getActivity().getApplicationContext();
                            ((MainHomeActivity)getActivity()).openCustomTab(getString(R.string.sell_poll_link));
                        }
                    });

            String negativeText = getString(android.R.string.cancel);
            builder.setNegativeButton(negativeText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // negative button logic
                        }
                    });

            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();
    }




}
