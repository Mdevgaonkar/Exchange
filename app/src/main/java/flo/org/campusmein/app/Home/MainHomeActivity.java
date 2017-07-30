package flo.org.campusmein.app.Home;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.MatrixCursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.Buy.BuyFragment;
import flo.org.campusmein.app.Home.WishList.wishlistView;
import flo.org.campusmein.app.Home.cart.cartView;
import flo.org.campusmein.app.Home.orderPlacement.ordersView;
import flo.org.campusmein.app.Home.search.Search;
import flo.org.campusmein.app.Home.sell.SellFragment;
import flo.org.campusmein.app.utils.Person;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.SuggestionRealmObject;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartObject;
import flo.org.campusmein.app.utils.cartViewUtils.BadgeDrawable;
import flo.org.campusmein.app.utils.chromeCustomTab.CustomTabActivityHelper;
import flo.org.campusmein.app.utils.chromeCustomTab.WebviewFallback;
import io.realm.RealmResults;

public class MainHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    View.OnClickListener, View.OnTouchListener{
    private int count = 0;
    private long startMillis=0;

    private static final String TAG = MainHomeActivity.class.getSimpleName();
    Person person;

    Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView my_profile;
    private TextView personNameInHeader, personCollegeInHeader;
    private CircularImageView personPhotoInHeader;
    private NavigationView navigationView;

    private DrawerLayout drawer;

//    cartIcon options
    private LayerDrawable mCartMenuIcon;
    private int mCartCount;


//    Chrome custom activity helper
    private CustomTabActivityHelper mCustomTabActivityHelper;
    private SimpleCursorAdapter busStopCursorAdapter;
    String[] sggstn_str_arr;

    ArrayList<SuggestionRealmObject> suggestionRealmObjectArrayList;
    MatrixCursor cursor;

    private static final String PRODUCT_TITLE = "productTitle";
    private static final String PRODUCT_STATUS = "productStatus";
    private static final String PRODUCT_WHERE_CLAUSE = "productWhereClause";
    private static final String PRODUCT_CLASS = "productClass";
    private static final String PRODUCT_POLL = "productPoll";
    private static final String PRODUCT_POLL_URL = "productPollUrl";
    private static final String PRODUCT_TYPE = "type";
    private static final String SRC_STR_KEY = "src"; // added to provide all previous orders directly from sidebar


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        person = new Person(this);
        mCustomTabActivityHelper = new CustomTabActivityHelper();

        toolbar = (Toolbar) findViewById(R.id.tab_action_bar);
        setSupportActionBar(toolbar);



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorAccent));
        tabLayout.setSelectedTabIndicatorHeight(10);


        fetchNewestVersion();

        setUpNavHeaderDetails();
        if(Boolean.valueOf(campusExchangeApp.getInstance().getUniversalPerson().getPersonPresent())){
            validateUserLogin();
        }else{
            showSnack("Please login to stay updated on best deals.", Snackbar.LENGTH_SHORT);
        }

    }



    private void setUpNavHeaderDetails() {
        my_profile = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.myProfile);
        personNameInHeader = (TextView) navigationView.getHeaderView(0).findViewById(R.id.persoNameInHeader);
        personCollegeInHeader = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personCollegeInHeader);
        personPhotoInHeader = (CircularImageView) navigationView.getHeaderView(0).findViewById(R.id.personPhotoInHeader);
        personPhotoInHeader.setOnTouchListener(this);
        if(Boolean.valueOf(person.getPersonPresent())){
            my_profile.setImageResource(R.drawable.ic_mode_edit_black_24dp);
            if(!person.getPersonName().equals("null")){
                personNameInHeader.setText(person.getPersonName());
                Log.e(TAG, "display name: " + person.getPersonName());
            }else {
                personNameInHeader.setText(R.string.retryLogin);
            }

            if(!person.getCollegeName().equals("null")){
                personCollegeInHeader.setText(person.getCollegeName());
            }else {
                personCollegeInHeader.setText("");
            }

            if(!person.getPersonPhotoUrl().equals("null")){
                try{
                    Glide.with(getApplicationContext()).load(Uri.parse(person.getPersonPhotoUrl()))
                            .thumbnail(0.5f)
                            .crossFade()
                            .error(R.mipmap.ic_launcher)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(personPhotoInHeader);
                }catch (NullPointerException e) {
                    personPhotoInHeader.setImageResource(R.drawable.ic_default_avatar);
                }
            }else {
                personPhotoInHeader.setImageResource(R.drawable.ic_default_avatar);
            }

        }else{
            personNameInHeader.setText(R.string.defaultNameText);
            personCollegeInHeader.setText("");
            my_profile.setImageResource(R.drawable.ic_chevron_right_black_24dp);
            personPhotoInHeader.setImageResource(R.drawable.ic_default_avatar);
        }
        my_profile.setOnClickListener(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mCustomTabActivityHelper.bindCustomTabsService(this);
        setUpNavHeaderDetails();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomTabActivityHelper.unbindCustomTabsService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpNavHeaderDetails();
        if(mCartMenuIcon!=null){
            int cartSize = RealmController.getInstance().getItems().size();
            setBadgeCount(this, mCartMenuIcon, String.valueOf(cartSize));
        }

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new BuyFragment(), getResources().getString(R.string.buy));
//        adapter.addFragment(new SellFragment(), getResources().getString(R.string.sell));
//        adapter.addFragment(new Chats(), getResources().getString(R.string.chatsTab));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.myProfile :
                if(Boolean.valueOf(person.getPersonPresent())){
                    Intent profile_options_screen = new Intent(this, flo.org.campusmein.app.Login.profile_options.class);
                    startActivity(profile_options_screen);
                }else{
                    Intent login_screen = new Intent(this, flo.org.campusmein.app.Login.login.class);
                    startActivity(login_screen);
                }
                break;

        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_home_with_search, menu);
        mCartMenuIcon = (LayerDrawable) menu.findItem(R.id.action_cart).getIcon();
        int cartSize = RealmController.getInstance().getItems().size();
        setBadgeCount(this, mCartMenuIcon, String.valueOf(cartSize));

        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.colorCard), PorterDuff.Mode.SRC_ATOP);
            }
        }

        // Associate searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        setupSearch(menu);

        return true;
    }

    private void setupSearch(Menu menu) {

        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        final Search s = new Search(getApplicationContext(),this);

        suggestionRealmObjectArrayList = new ArrayList<>();
        s.setNewSuggestions(suggestionRealmObjectArrayList);
        searchView.setSuggestionsAdapter(s.getSuggestionAdapter());


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                String wClause = "attribbutes%20LIKE%20%27%25"+query+"%25%27";
                String wClause = "book.author%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"book.edition%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"book.description%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"book.title%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"book.ISBN%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"book.publicationYear%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"book.publisher%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"instrument.instrumentName%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"instrument.description%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"instrument.instrumentSubtitle%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"instrument.type%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"combopack.title%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"combopack.description%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"combopack.subTitle%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"specialization.branchShort%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"specialization.branch%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27%20OR%20"
                        +"attribbutes%20LIKE%20%27%25"+URLEncoder.encode(query)+"%25%27";

                gatherExtrasForSearchIntent(wClause,query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

//                Toast.makeText(getApplicationContext(),"You searched for "+newText,Toast.LENGTH_LONG).show();

                RealmResults<SuggestionRealmObject> sggstns =s.FindInDataset(newText);
                suggestionRealmObjectArrayList.clear();
                for (SuggestionRealmObject realmObject: sggstns){
                    suggestionRealmObjectArrayList.add(realmObject);
                }
                Log.d("quick"," \n size:-"+suggestionRealmObjectArrayList.size()+" "+suggestionRealmObjectArrayList.toString());
                s.setNewSuggestions(suggestionRealmObjectArrayList);
                searchView.setSuggestionsAdapter(s.getSuggestionAdapter());
                return false;
            }
        });
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                SuggestionRealmObject sObj = suggestionRealmObjectArrayList.get(position);
                String title= sObj.getTitle();
                String whereClause = sObj.getWhereClause();
                gatherExtrasForSearchIntent(whereClause,title);
                return false;
            }
        });

    }


    public static void setBadgeCount(Context context, LayerDrawable icon, String count) {

        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_cart){
            openCart();
        }

        return super.onOptionsItemSelected(item);
    }


    public void gatherExtrasForSearchIntent(String whereClause, String title){
        int status = 0;
        String ___class = "products";
        String type = "A";
//        String title = "Search";
        boolean poll =  false;
        String pollUrl = null;

            openProductStoreList(title,status,whereClause,poll,pollUrl,___class,type);


    }

    private void openProductStoreList(String productTitle, int productListStatus, String whereClause, boolean poll, String pollUrl, String productClass, String type) {
        Intent openProductStore = new Intent(getApplicationContext(), flo.org.campusmein.app.Home.listing.productListingActivity.class);
        openProductStore.putExtra(PRODUCT_TYPE, type);
        openProductStore.putExtra(PRODUCT_TITLE, productTitle);
        openProductStore.putExtra(PRODUCT_STATUS, productListStatus);
        openProductStore.putExtra(PRODUCT_WHERE_CLAUSE, whereClause);
        openProductStore.putExtra(PRODUCT_CLASS,productClass);
        openProductStore.putExtra(PRODUCT_POLL,poll);
        openProductStore.putExtra(PRODUCT_POLL_URL,pollUrl);
        startActivity(openProductStore);

    }

    public void openCart() {
        Intent cart = new Intent(this,cartView.class);
        startActivity(cart);
    }

    private void addToCart(String s) {
        if(isItemInCart(s)) {
            Toast.makeText(this,"Already present",Toast.LENGTH_LONG).show();
        }else {
            cartObject item = new cartObject();
            item.setId((int) (RealmController.getInstance().getItems().size()+ System.currentTimeMillis()));
            item.setProductId(s);
            item.setQuantity(1);

            campusExchangeApp.getInstance().getRealm().beginTransaction();
            campusExchangeApp.getInstance().getRealm().copyToRealm(item);
            campusExchangeApp.getInstance().getRealm().commitTransaction();
        }
    }

    private boolean isItemInCart(String s) {
        if (RealmController.getInstance().hasItems()){
            if(RealmController.getInstance().getCartObjectWithProductId(s) == null){
                return false;
            }else {
                return true;
            }
        }else {
            return false;
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();


        switch (id){
            case R.id.home:
                break;
            case R.id.myOrders:
                Intent myOrders = new Intent(this, ordersView.class);
                myOrders.putExtra(SRC_STR_KEY,"profile_options");
                startActivity(myOrders);
                break;
            case R.id.mySells:
                showSnack(getString(R.string.option_comming_soon),Snackbar.LENGTH_LONG);
                break;
            case R.id.myWishlist:
                Intent myWishlist = new Intent(this, wishlistView.class);
                startActivity(myWishlist);
                break;
//            case R.id.notifications:
//                break;
            case R.id.quick_help:
                showHelpConfirmationDialog();
                break;
            case R.id.about_us:
                openCustomTab(getString(R.string.about_us_link));
                break;
//            case R.id.settings:
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public void openCustomTab(String urlFromCall) {
        String url = urlFromCall;

        int color = getColor("#0097a7");
        int secondaryColor = getColor("#0097a7");

        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(color);
        intentBuilder.setSecondaryToolbarColor(secondaryColor);

//        if (mShowActionButtonCheckbox.isChecked()) {
//            //Generally you do not want to decode bitmaps in the UI thread. Decoding it in the
//            //UI thread to keep the example short.
//            String actionLabel = getString(R.string.label_action);
//            Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                    android.R.drawable.ic_menu_share);
//            PendingIntent pendingIntent =
//                    createPendingIntent(ActionBroadcastReceiver.ACTION_ACTION_BUTTON);
//            intentBuilder.setActionButton(icon, actionLabel, pendingIntent);
//        }

//        if (mAddMenusCheckbox.isChecked()) {
//            String menuItemTitle = getString(R.string.menu_item_title);
//            PendingIntent menuItemPendingIntent =
//                    createPendingIntent(ActionBroadcastReceiver.ACTION_MENU_ITEM);
//            intentBuilder.addMenuItem(menuItemTitle, menuItemPendingIntent);
//        }


        intentBuilder.addDefaultShareMenuItem();

//        if (mToolbarItemCheckbox.isChecked()) {
//            //Generally you do not want to decode bitmaps in the UI thread. Decoding it in the
//            //UI thread to keep the example short.
//            String actionLabel = getString(R.string.label_action);
//            Bitmap icon = BitmapFactory.decodeResource(getResources(),
//                    android.R.drawable.ic_menu_share);
//            PendingIntent pendingIntent =
//                    createPendingIntent(ActionBroadcastReceiver.ACTION_TOOLBAR);
//            intentBuilder.addToolbarItem(TOOLBAR_ITEM_ID, icon, actionLabel, pendingIntent);
//        }

        intentBuilder.setShowTitle(true);



        intentBuilder.enableUrlBarHiding();



        intentBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back_black_24dp));

        intentBuilder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
        intentBuilder.setExitAnimations(this, android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        CustomTabActivityHelper.openCustomTab(
                this, intentBuilder.build(), Uri.parse(url), new WebviewFallback());
    }

    private int getColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (NumberFormatException ex) {
            Log.i(TAG, "Unable to parse Color: " + color);
            return Color.LTGRAY;
        }
    }

    private void showSnack(String snackString, int period) {
        String message;
        message = snackString;
        Snackbar snackbar = Snackbar.make(drawer, message, period);
        snackbar.show();
    }

    private void emailUsForHelp() {

        String[] TO = {getString(R.string.helpdeskEmail)};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.helpQueryMailSubject)+campusExchangeApp.getInstance().getUniversalPerson().getPersonName()+campusExchangeApp.getInstance().getUniversalPerson().getPersonCollegeShort()+" "+campusExchangeApp.getInstance().getUniversalPerson().getPersonCourseShort());
        String EmailBody = "\n\n"+campusExchangeApp.getInstance().getUniversalPerson().getPersonName()
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

    private void showHelpConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.help_Confirmation));
        builder.setMessage(getString(R.string.help_Confirmation_dialog_text));

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        emailUsForHelp();
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

    private void validateUserLogin(){

        String validateUserLoginString = "https://api.backendless.com/test/users/isvalidusertoken/"+campusExchangeApp.getInstance().getUniversal_Credentials().getUserToken();

        StringRequest validateLogin = new StringRequest(Request.Method.GET,
                validateUserLoginString,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ValidateLogin_response", response);
                        if (!Boolean.valueOf(response)) {
                            loginUser();
                        }
//                        else {
//                            showSnack(getString(R.string.validLoginGreetings), Snackbar.LENGTH_SHORT);
//                        }
                    }
                },
                new Response.ErrorListener(){

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showSnack(getString(R.string.pleaseRestartApp), Snackbar.LENGTH_INDEFINITE);
                    }
                }){
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                headers.put("Content-Type","application/json");
                headers.put("application-type","REST");
                Log.d("device_Headers", headers.toString());
                return headers;
            }
        };
//        JsonObjectRequest validateUserLogin = new JsonObjectRequest(
//                Request.Method.GET,
//                validateUserLoginString,
//                null,
//                new Response.Listener<JSONObject>() {
//                    @Override
//                    public void onResponse(JSONObject response) {
//
//                        String validity = response.toString();
//                        Log.d("ValidateLogin_response",validity);
//                        if(Boolean.valueOf(validity)){
//                            showSnack("Welcome Back", Snackbar.LENGTH_SHORT);
//                        }else {
//                            loginUser();
//                        }
//
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d("ValidateLogin_volley",error.toString());
//                        showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
//                    }
//                }){
//            /**
//             * Passing some request headers
//             * */
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
//                headers.put("Content-Type","application/json");
//                headers.put("application-type","REST");
//                Log.d("device_Headers", headers.toString());
//                return headers;
//            }
//        };

        campusExchangeApp.getInstance().addToRequestQueue(validateLogin,TAG);

    }

    private void loginUser(){
        String loginObjString = "{" +
                " \"login\" : \""+campusExchangeApp.getInstance().getUniversalPerson().getPersonEmail()+"\"," +
                " \"password\" : \""+campusExchangeApp.getInstance().getUniversalPerson().getPersonAuthCode()+"\"" +
                "}";
        String LoginUserString = "https://api.backendless.com/test/users/login";

        try {
            JSONObject loginObject = new JSONObject(loginObjString);


            JsonObjectRequest loginUser = new JsonObjectRequest(
                    Request.Method.POST,
                    LoginUserString,
                    loginObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if(response.isNull("user-token")){
                                loginUser();
                            }else {
                                try {
                                    Log.d("login_user_token",response.getString("user-token"));
                                    campusExchangeApp.getInstance().getUniversal_Credentials().setUserToken(response.getString("user-token"));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.d("Logn_user_token","unsuccessful");
                                    showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                                }
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Login_volley",error.toString());
                            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
                        }
                    }){
                /**
                 * Passing some request headers
                 * */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers= campusExchangeApp.getInstance().getCredentialsHashMap();
                    headers.put("Content-Type","application/json");
                    headers.put("application-type","REST");
                    Log.d("device_Headers", headers.toString());
                    return headers;
                }
            };

            campusExchangeApp.getInstance().addToRequestQueue(loginUser,TAG);
        } catch (JSONException e) {
            showSnack(getString(R.string.pleaseRestartApp),Snackbar.LENGTH_INDEFINITE);
            e.printStackTrace();
            Log.d("Login_JSON_exception", e.toString());
        }
    }

    private void fetchNewestVersion(){
        JsonObjectRequest fetchVariables = new JsonObjectRequest(
                Request.Method.GET,
                getString(R.string.versionVariables),
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Float_newVersionFloat","response");
                        try {
                            String newVersion = response.getString("appVersion");
                            String unsupportedVersion = response.getString("unsupportedVersion");
                            String dataset_upd_dt = response.getString("dataset_upd_dt");

                            float newVersionFloat = Float.valueOf(newVersion);
                            Log.d("Float_newVersionFloat",newVersionFloat+"");

                            float unsupportedVersionFloat = Float.valueOf(unsupportedVersion);
                            Log.d("Float_unsupported",unsupportedVersionFloat+"");

                            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                            String presentVersion = pInfo.versionName;
                            float presentVersionFloat = Float.valueOf(presentVersion);

                            Log.d("Float_presentVersion",presentVersionFloat+"");

                            if(presentVersionFloat!=newVersionFloat){
                                if(presentVersionFloat <= unsupportedVersionFloat){
                                    showDialogToUpdate(1);
                                }else {
                                    showDialogToUpdate(0);
                                }
                            }

                            if(!dataset_upd_dt.equals(campusExchangeApp.getInstance().getUniversalPerson().getDataset_upd_dt())){
                                Search searchDataSet = new Search(getApplicationContext(), MainHomeActivity.this);
                                searchDataSet.getNewDataset(dataset_upd_dt);

                            }else{
                                Toast.makeText(getApplicationContext(), "dataset up to date", Toast.LENGTH_SHORT).show();
                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("JSON Exception",e.toString());
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            Log.d("PackageMgrException",e.toString());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("volley error",error.toString());
                        showSnack("Please Update App", Snackbar.LENGTH_SHORT);
                    }
                }
        );

        campusExchangeApp.getInstance().addToRequestQueue(fetchVariables,"FetchingVariables");

    }

    private void showDialogToUpdate(int i) {
        AlertDialog.Builder updateAppDialogBuilder = new AlertDialog.Builder(this);
        updateAppDialogBuilder.setTitle(getString(R.string.AppUpdate));
        if(i==0){
            updateAppDialogBuilder.setMessage(getString(R.string.AppUpdateMessage));
        }else {
            updateAppDialogBuilder.setMessage(getString(R.string.AppUpdateMessageCumpulsory));
        }


        String positiveText = getString(android.R.string.ok);
        updateAppDialogBuilder.setPositiveButton(positiveText, null);

        if(i==0){
            String negativeText = getString(android.R.string.cancel);
            updateAppDialogBuilder.setNegativeButton(negativeText, null);
        }


        AlertDialog dialog = updateAppDialogBuilder.create();
        if(i==0){
            dialog.setCancelable(true);
        }else if(i==1){
            dialog.setCancelable(false);
        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positive_Btn = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                positive_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateAppIntent();
                    }
                });
            }
        });
        // display dialog
        dialog.show();
    }

    private void updateAppIntent() {
        Log.d("RateApp","Clicked");
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Log.d("packageName",this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    private void showDevSignature(){

        AlertDialog.Builder devSignature_DialogBuilder;

        devSignature_DialogBuilder = new AlertDialog.Builder(this);
        devSignature_DialogBuilder.setTitle(getString(R.string.app_name));
        devSignature_DialogBuilder.setMessage("You have unlocked developer Signature");

        LinearLayout devSignature_layout = new LinearLayout(this);
        devSignature_layout.setOrientation(LinearLayout.VERTICAL);
        devSignature_layout.setGravity(Gravity.CENTER);
        devSignature_layout.setPadding(dpTopixels(8),dpTopixels(8),dpTopixels(8),dpTopixels(8));

        LinearLayout devSignature_sub_layout = new LinearLayout(this);
        devSignature_sub_layout.setOrientation(LinearLayout.HORIZONTAL);
        devSignature_sub_layout.setGravity(Gravity.CENTER);

        LinearLayout campusMe_icon_layout = new LinearLayout(this);
        campusMe_icon_layout.setOrientation(LinearLayout.HORIZONTAL);
        campusMe_icon_layout.setGravity(Gravity.RIGHT);
        LinearLayout dev_icon_layout = new LinearLayout(this);
        dev_icon_layout.setOrientation(LinearLayout.HORIZONTAL);
        dev_icon_layout.setGravity(Gravity.LEFT);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpTopixels(64),
                dpTopixels(64)
        );
        params.weight = 1;
        params.setMargins(0,0,0,dpTopixels(8));

        CircularImageView campusMe_icon = new CircularImageView(this);
        campusMe_icon.setImageResource(R.mipmap.ic_launcher);
        campusMe_icon.setBorderColor(getResources().getColor(R.color.colorPrimary));
        campusMe_icon.setBorderWidth(dpTopixels(3));
        campusMe_icon.setLayoutParams(params);

        CircularImageView dev_icon = new CircularImageView(this);
        dev_icon.setImageResource(R.drawable.dev_icon);
        dev_icon.setBorderColor(getResources().getColor(R.color.colorPrimary));
        dev_icon.setBorderWidth(dpTopixels(3));
        dev_icon.setLayoutParams(params);


        TextView dev_name = new TextView(this);
        dev_name.setText("\nMayur Devgaonkar\ndmayur57@gmail.com\n");
        dev_name.setGravity(Gravity.CENTER);
        dev_name.setTextColor(getResources().getColor(android.R.color.primary_text_light));
        dev_name.setTypeface(null, Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dev_name.setTextAppearance(android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Medium);
        } else {
            dev_name.setTextAppearance(this, android.support.v7.appcompat.R.style.TextAppearance_AppCompat_Medium);
        }

        LinearLayout.LayoutParams TextParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        TextParams.setMargins(0,0,0,dpTopixels(8));


        campusMe_icon_layout.addView(campusMe_icon);
        dev_icon_layout.addView(dev_icon);
        devSignature_sub_layout.addView(campusMe_icon_layout);
        devSignature_sub_layout.addView(dev_icon_layout);
        devSignature_layout.addView(devSignature_sub_layout);
        devSignature_layout.addView(dev_name);
        devSignature_DialogBuilder.setView(devSignature_layout);

        AlertDialog dialog = devSignature_DialogBuilder.create();
        // display dialog
        dialog.show();

        dev_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCustomTab("https://www.facebook.com/mayur.devgaonkar");
            }
        });
        campusMe_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCustomTab("https://www.facebook.com/mayur.devgaonkar");
            }
        });

    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (view.getId()){
            case R.id.personPhotoInHeader:
                int eventaction = motionEvent.getAction();
                if (eventaction == MotionEvent.ACTION_UP) {

                    //get system current milliseconds
                    long time= System.currentTimeMillis();


                    //if it is the first time, or if it has been more than 3 seconds since the first tap ( so it is like a new try), we reset everything
                    if (startMillis==0 || (time-startMillis> 3000) ) {
                        startMillis=time;
                        count=1;
                    }
                    //it is not the first, and it has been  less than 3 seconds since the first
                    else{ //  time-startMillis< 3000
                        count++;
                        Log.d("Dev_count",""+count);
                    }

                    if (count==5) {
                        //do whatever you need
                        Log.d("Dev_Signature","Shown");
                        showDevSignature();
                    }
                    return true;
                }
                break;
        }

        return true;
    }

    public int dpTopixels(int dps){
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps,
                getResources().getDisplayMetrics());
    }






}
