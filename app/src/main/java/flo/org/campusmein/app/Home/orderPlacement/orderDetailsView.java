package flo.org.campusmein.app.Home.orderPlacement;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Login.spinnerAdapter;
import flo.org.campusmein.app.utils.ConnectivityReceiver;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.orders;

public class orderDetailsView extends AppCompatActivity
            implements ConnectivityReceiver.ConnectivityReceiverListener,View.OnClickListener{

    private CoordinatorLayout activity_orderDetails;
    private LinearLayout progressLayout,details_view;
    private CardView errorLayout;
    private boolean NETWORK_STATE= false;
    private String order_object_id;
    private static final String ORD_ID_KEY = "ord_id";
    private static final String ___CLASS = "orders";
    private static final String LOAD_RELATIONS ="loadRelations=buyerId.course%2Cproduct%2Cproduct.book%2Cproduct.combopack%2Cproduct.combopack.books%2Cproduct.combopack.instruments%2Cproduct.instrument%2CbuyerId%2CbuyerId.college";
    private static final String SLASH = "/";
    private static final String QUERY = "?";
    private static final String TAG = orderDetailsView.class.getSimpleName();
    private orders order;

    private TextView ordDt_date,ordDt_itemTitle,ordDt_itemSubtitle,ordDt_qty,ordDt_gr_total,ordDt_payment_mode,ordDt_code,ordDt_id,ordDt_status,ordDt_status_comments,ordDt_status_refresh,ordDt_delivery_address;
    private LinearLayout ordDt_cancel_btn,ordDt_contact_us_btn,divider;
    private CardView ordDt_receive_view;

    private SimpleDateFormat DayFormat = new SimpleDateFormat("EEE");
    private SimpleDateFormat MonthFormat = new SimpleDateFormat("MMM");
    private SimpleDateFormat DateFormat = new SimpleDateFormat("dd");
    private SimpleDateFormat YearFormat = new SimpleDateFormat("yyyy");
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_view);
        setupCoordinatorLayout();
        setupActionbar();
        checkConnection();
        setupErrorLayout();
        setupProgressLayout();
        setupDetailsView();
        setupSourceBundle();



    }

    private void setupSourceBundle() {
        Intent sourceIntent = getIntent();
        Bundle sourceBundle = sourceIntent.getExtras();
        order_object_id = sourceBundle.getString(ORD_ID_KEY,"");
    }

    private void loadView(){
        if(NETWORK_STATE){
            if(!order_object_id.equals("")){
                fecthOrderDetails(order_object_id);
            }else {
                showSnack("Some Error Occured. Please Retry");
            }
        }else {
            show_errorLayout();
        }
    }

    private void fecthOrderDetails(String order_object_id) {

        String orderRequest = getString(R.string.baseBackendUrl);
        orderRequest = orderRequest+___CLASS+SLASH+order_object_id+QUERY+LOAD_RELATIONS;
        show_progressLayout();

        JsonObjectRequest getProduct = new JsonObjectRequest(
                Request.Method.GET,
                orderRequest,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Gson gson = campusExchangeApp.getInstance().getGson();
                        String responseData = response.toString();
                        order = gson.fromJson(responseData,orders.class);
                        Log.d("Response", response.toString());
                        show_details_view();
                        uptadeUI(order);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        show_errorLayout();
                    }
                }){

            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                Log.d("Headers", headers.toString());
                return headers;
            }
        };

        campusExchangeApp.getInstance().addToRequestQueue(getProduct,TAG);

    }

    private void uptadeUI(orders order) {
        setOrderDate(order);
        setItemTitles(order);
        setQtyPrice(order);
        setPaymentMode(order);
        setOrderStatus(order);
        setDeliveryAddress();

    }

    private void setDeliveryAddress() {
        ordDt_delivery_address.setText(campusExchangeApp.getInstance().getUniversalPerson().getCollegeName()+",\n"+campusExchangeApp.getInstance().getUniversalPerson().getPersonCollegeLocation());
    }

    private void setOrderStatus(orders order) {
        ordDt_id.setText(order_object_id);
        int status = order.getStatus();
        switch (status){
            case -2:
                ordDt_status.setText(R.string.delayed);
                ordDt_status.setTextColor(getResources().getColor(R.color.colorRed));
                ordDt_status_comments.setText(order.getReasonDelayed());
                ordDt_status_comments.append("\n\n"+getString(R.string.expected_on_or_before_t)+getDateInText(order.getDeliveryExpected()));
                show_receive_view(order);
                break;
            case -1:
                ordDt_status.setText(R.string.pending);
                ordDt_status.setTextColor(getResources().getColor(android.R.color.tertiary_text_light));
                ordDt_status_comments.setText(getString(R.string.expected_on_or_before_t)+getDateInText(order.getDeliveryExpected()));
                show_receive_view(order);
                break;
            case 0:
                ordDt_status.setText(R.string.canceled);
                ordDt_status.setTextColor(getResources().getColor(R.color.colorRed));
                ordDt_status_comments.setText(order.getReasonCanceled());
                ordDt_status_comments.append("\n\n"+getString(R.string.canceled_on)+getDateInText(order.getCanceledDate()));
                hide_receive_view();
                hide_cancel_order_button();
                break;
            case 1:
                ordDt_status.setText(R.string.deliveryReady);
                ordDt_status.setTextColor(getResources().getColor(R.color.colorOrange));
                ordDt_status_comments.setText(order.getUpdateComments());
                ordDt_status_comments.append("\n\n"+getString(R.string.expected_on_or_before_t)+getDateInText(order.getDeliveryExpected()));
                show_receive_view(order);
                break;
            case 2:
                ordDt_status.setText(R.string.delivered);
                ordDt_status.setTextColor(getResources().getColor(R.color.colorGreen));
                ordDt_status_comments.setText(getString(R.string.delivered_on_t)+getDateInText(order.getDeliveredDate()));
                hide_receive_view();
                hide_cancel_order_button();
                break;
        }
    }

    private void hide_cancel_order_button() {
        ordDt_cancel_btn.setVisibility(View.GONE);
        divider.setVisibility(View.GONE);
    }

    private void setPaymentMode(orders order) {
        ordDt_payment_mode.setText(getString(R.string.payment_by)+order.getPaymentMode());
    }

    private void setQtyPrice(orders order) {
        ordDt_qty.setText(getString(R.string.quantity_n)+order.getQuantity());
        ordDt_gr_total.setText(getString(R.string.grand_total_n)+getString(R.string.rupeesSymbol)+order.getOrderCost()+getString(R.string.priceEndSymbol));
    }

    public void setItemTitles(orders order) {
        Products product = order.getProduct();
        if(product.type.equals(getString(R.string.bookType))){
            Log.d("ord_itemTitle",product.book.title);
            ordDt_itemTitle.setText(product.book.title);
            ordDt_itemSubtitle.setText(getString(R.string.byString)+product.book.author);
        }
        else if(product.type.equals(getString(R.string.instrumentType))){
            Log.d("ord_itemTitle",product.instrument.instrumentName);
            ordDt_itemTitle.setText(product.instrument.instrumentName);
            ordDt_itemSubtitle.setText(getString(R.string.byString)+product.instrument.instrumentSubtitle);

        }
        else if(product.type.equals(getString(R.string.comboType))){
            Log.d("ord_itemTitle",product.combopack.title);
            ordDt_itemTitle.setText(product.combopack.title);
            ordDt_itemSubtitle.setText(getString(R.string.byString)+product.combopack.subTitle);
        }

    }

    private void setOrderDate(orders order) {
        String ordered_date = order.getOrderedDate();
        ordDt_date.setText(getDateInText(ordered_date));
    }

    @NonNull
    private String getDateInText(String dateStr){
        if(dateStr!=null){
        long ordered_date_long = Long.valueOf(dateStr);
        Date date = new Date(ordered_date_long);
        String ord_date_string = DayFormat.format(date)+", "+MonthFormat.format(date)+" "+DateFormat.format(date)+" "+YearFormat.format(date);
        return ord_date_string.toUpperCase();
        }else {
            return "";
        }
    }

    private void setupDetailsView(){
        details_view = (LinearLayout) findViewById(R.id.ordDt_view);
        hide_details_view();

        ordDt_date = (TextView) findViewById(R.id.ordDt_date);
        ordDt_itemTitle = (TextView) findViewById(R.id.ordDt_itemTitle);
        ordDt_itemSubtitle = (TextView) findViewById(R.id.ordDt_itemSubtitle);
        ordDt_qty = (TextView) findViewById(R.id.ordDt_qty);
        ordDt_gr_total = (TextView) findViewById(R.id.ordDt_gr_total);
        ordDt_payment_mode = (TextView) findViewById(R.id.ordDt_payment_mode);
        ordDt_id = (TextView) findViewById(R.id.ordDt_id);
        ordDt_code = (TextView) findViewById(R.id.ordDt_code);
        ordDt_status = (TextView) findViewById(R.id.ordDt_status);
        ordDt_status_comments = (TextView) findViewById(R.id.ordDt_status_comments);
        ordDt_status_refresh = (TextView) findViewById(R.id.ordDt_status_refresh);
        ordDt_status_refresh.setOnClickListener(this);
        ordDt_delivery_address = (TextView) findViewById(R.id.ordDt_delivery_address);

        ordDt_cancel_btn = (LinearLayout) findViewById(R.id.ordDt_cancel_btn);
        ordDt_cancel_btn.setOnClickListener(this);
        ordDt_contact_us_btn = (LinearLayout) findViewById(R.id.ordDt_contact_us_btn);
        ordDt_contact_us_btn.setOnClickListener(this);
        divider = (LinearLayout) findViewById(R.id.divider);
        divider.setVisibility(View.VISIBLE);
        ordDt_receive_view = (CardView) findViewById(R.id.ordDt_receive_view);
        hide_receive_view();


    }

    private void hide_receive_view() {
        ordDt_receive_view.setVisibility(View.GONE);
    }

    private void show_receive_view(orders order) {
        ordDt_receive_view.setVisibility(View.VISIBLE);
        ordDt_code.setText(""+order.getOrderCode());
    }

    private void hide_details_view() {
        details_view.setVisibility(View.GONE);
    }

    private void show_details_view() {
        details_view.setVisibility(View.VISIBLE);
        hide_errorLayout();
        hide_progressLayout();
    }

    private  void setupCoordinatorLayout(){
        activity_orderDetails = (CoordinatorLayout) findViewById(R.id.activity_orderDetails);
    }

    private  void setupProgressLayout(){
        progressLayout = (LinearLayout) findViewById(R.id.ordDt_progressLayout);
        hide_progressLayout();
    }

    private void show_progressLayout() {
        progressLayout.setVisibility(View.VISIBLE);
        hide_details_view();
        hide_errorLayout();
    }

    private void hide_progressLayout() {
        progressLayout.setVisibility(View.GONE);
    }

    private void setupErrorLayout(){
        errorLayout = (CardView) findViewById(R.id.ordDt_error_Layout);
        hide_errorLayout();
    }

    private void show_errorLayout() {
        errorLayout.setVisibility(View.VISIBLE);
        hide_progressLayout();
        hide_details_view();
    }

    private void hide_errorLayout() {
        errorLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadView();
    }

    @Override
    public void onClick(View view) {
        if (NETWORK_STATE) {
            switch (view.getId()) {
                case R.id.ordDt_status_refresh:
                    loadView();
                    break;

                case R.id.ordDt_cancel_btn:
                    cancelOrderConfirmation(order_object_id);
                    break;

                case R.id.ordDt_contact_us_btn:
                    emailUsForHelp();
                    break;
            }
        }else {
            showSnack(getString(R.string.NetworkFaliure));
        }

    }

    private void emailUsForHelp() {

        String[] TO = {getString(R.string.helpdeskEmail)};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, campusExchangeApp.getInstance().getUniversalPerson().getPersonCollegeShort()+" "+campusExchangeApp.getInstance().getUniversalPerson().getPersonCourseShort()+" "+ getString(R.string.orderid_helpdesk_mail_subject)+ order_object_id);
        String EmailBody = "Your Order-ID is "+order_object_id
                +"\n\n"+campusExchangeApp.getInstance().getUniversalPerson().getPersonName()
                +"\n\n"+campusExchangeApp.getInstance().getUniversalPerson().getCollegeName()
                +",\n"+campusExchangeApp.getInstance().getUniversalPerson().getPersonCollegeLocation()+"."
                +"\n\n"+campusExchangeApp.getInstance().getUniversalPerson().getPersonCourseShort()
                +"\n\n"+"Your Query ->";


        emailIntent.putExtra(Intent.EXTRA_TEXT,EmailBody );

        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }

    }


    public int dpTopixels(int dps){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
    }

    private void cancelOrderConfirmation(final String order_object_id) {

        AlertDialog.Builder cancelOrder_DialogBuilder;
        final Spinner cancellation_reason = new Spinner(this);
        final EditText cncl_reason = new EditText(this);

        cancelOrder_DialogBuilder = new AlertDialog.Builder(this);
        cancelOrder_DialogBuilder.setTitle(getString(R.string.OrderCancellation));

        LinearLayout cancellation_layout = new LinearLayout(this);
        cancellation_layout.setOrientation(LinearLayout.VERTICAL);
        cancellation_layout.setPadding(dpTopixels(12),dpTopixels(12),dpTopixels(12),dpTopixels(12));


//        cancellation_reason.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        spinnerAdapter SpinnerAdapter = new spinnerAdapter(this,android.R.layout.simple_spinner_dropdown_item, Arrays.asList(getResources().getStringArray(R.array.cancelReasons)));
        cancellation_reason.setAdapter(SpinnerAdapter);


        cncl_reason.setHint(R.string.custom_reason);
        cncl_reason.setBackground(getResources().getDrawable(R.drawable.filled_background_rounded_rectangle));
        cncl_reason.setPadding(dpTopixels(8),dpTopixels(8),dpTopixels(8),dpTopixels(8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        int pixelsLR = dpTopixels(6);
        int pixelsTB = dpTopixels(6);
        params.setMargins(pixelsLR,pixelsTB,pixelsLR,pixelsTB);
        cncl_reason.setLayoutParams(params);

        cancellation_layout.addView(cancellation_reason);
        cancellation_layout.addView(cncl_reason);
        cancelOrder_DialogBuilder.setView(cancellation_layout);

        String positiveText = getString(android.R.string.ok);
        cancelOrder_DialogBuilder.setPositiveButton(positiveText,null);


        String negativeText = getString(android.R.string.cancel);
        cancelOrder_DialogBuilder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                        dialog.dismiss();
                    }
                });

        final AlertDialog dialog = cancelOrder_DialogBuilder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // positive button logic
                        String reason_canelled;
                        if(cancellation_reason.getSelectedItemPosition()==0 && cncl_reason.getText().toString().isEmpty()){
                            Toast.makeText(getApplicationContext(),"Please select or mention a reason to cancel the order", Toast.LENGTH_LONG).show();
                        }else {
                            if(cncl_reason.getText().toString().isEmpty()){

                                reason_canelled = cancellation_reason.getSelectedItem().toString();
                            }else {
                                reason_canelled = cancellation_reason.getSelectedItem().toString()+"\n"+cncl_reason.getText().toString();
                            }

                            dialog.dismiss();
                            cancelOrder(order_object_id, reason_canelled);
                        }
                    }
                });
            }
        });
        // display dialog
        dialog.show();

    }

    private void cancelOrder(final String order_object_id, String reason_cancelled){
        showProgressDialog(getString(R.string.cancellationProgress));
        orders ordDt_order = new orders();
        orders.updateOrder updateOrder = ordDt_order.getUpdateOrder();
        updateOrder.setReasonCanceled(reason_cancelled);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        updateOrder.setCanceledDate(String.valueOf(timestamp.getTime()));


        Gson gtoj = campusExchangeApp.getInstance().getGson();
        String jsonObj = gtoj.toJson(updateOrder);
        Log.d("updateOrder",jsonObj);
        try {
            JSONObject jsonOrder = new JSONObject(jsonObj);
            String orderCancelRequest = getString(R.string.baseBackendUrl);
            orderCancelRequest = orderCancelRequest + ___CLASS + SLASH + order_object_id;


            JsonObjectRequest getProduct = new JsonObjectRequest(
                    Request.Method.PUT,
                    orderCancelRequest,
                    jsonOrder,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                if (response.getString("objectId").equals(order_object_id)) {
                                    showSnack(getString(R.string.orderCancellationSuccessful));
                                    loadView();
                                } else {
                                    showSnack(getString(R.string.orderCancellationError));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            hideProgressDialog();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            showSnack(getString(R.string.orderCancellationError));
                            loadView();
                            hideProgressDialog();
                        }
                    }) {

                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = campusExchangeApp.getInstance().getCredentialsHashMap();
                    Log.d("Headers", headers.toString());
                    return headers;
                }
            };

            campusExchangeApp.getInstance().addToRequestQueue(getProduct, TAG);
        }catch (JSONException e) {
            showSnack(getString(R.string.orderCancellationError));
            loadView();
            hideProgressDialog();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void checkConnection() {
        NETWORK_STATE = ConnectivityReceiver.isConnected();
//        showSnack(isConnected);

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.d("networkChanged",isConnected?"Connected":"Not Connected");
        NETWORK_STATE = isConnected;
        if (isConnected) {
            showSnack(getString(R.string.good_connection));
        } else {
            showSnack(getString(R.string.NetworkFaliure));
        }
    }

    private void showSnack(String snackString) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(activity_orderDetails, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void setupActionbar() {
        Toolbar activityToolbar = (Toolbar) findViewById(R.id.action_bar_cart_listings);
        setSupportActionBar(activityToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.orderDetailsTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProgressDialog(String showString) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(showString);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.setMessage(showString);
        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(NETWORK_STATE){
                    campusExchangeApp.getInstance().getmRequestQueue().cancelAll(TAG);
                }
                finish();
            }
        });
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}
