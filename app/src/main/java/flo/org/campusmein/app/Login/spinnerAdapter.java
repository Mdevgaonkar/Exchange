package flo.org.campusmein.app.Login;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Mayur on 03/11/16.
 */

public class spinnerAdapter extends ArrayAdapter<String> {

    public spinnerAdapter(Context context, int textViewResourceId, List<String> popupList) {
        super(context, textViewResourceId,popupList);
    }

    @Override
    public boolean isEnabled(int position){
        if(position == 0)
        {
            // Disable the first item from Spinner
            // First item will be use for hint
            return false;
        }
        else
        {
            return true;
        }
    }
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView tv = (TextView) view;
        if(position == 0){
            // Set the hint text color gray
            tv.setTextColor(Color.argb(38,0,0,0));
        }
        else {
            tv.setTextColor(Color.BLACK);
        }
        return view;
    }

}
