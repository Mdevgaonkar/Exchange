package flo.org.campusmein.app.Home;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikhaellopez.circularimageview.CircularImageView;


import java.util.ArrayList;
import java.util.List;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.Buy.BuyFragment;
import flo.org.campusmein.app.Home.WishList.wishlistView;
import flo.org.campusmein.app.Home.cart.cartView;
import flo.org.campusmein.app.Home.orderPlacement.ordersView;
import flo.org.campusmein.app.Home.sell.SellFragment;
import flo.org.campusmein.app.utils.Person;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartObject;
import flo.org.campusmein.app.utils.cartViewUtils.BadgeDrawable;
import flo.org.campusmein.app.utils.chromeCustomTab.CustomTabActivityHelper;
import flo.org.campusmein.app.utils.chromeCustomTab.WebviewFallback;

public class MainHomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
                    View.OnClickListener {

    private static final String TAG = MainHomeActivity.class.getSimpleName();
    Person person;

    Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView my_profile;
    private TextView personNameInHeader, personCollegeInHeader;
    private CircularImageView personPhotoInHeader;
    private NavigationView navigationView;

//    cartIcon options
    private LayerDrawable mCartMenuIcon;
    private int mCartCount;


//    Chrome custom activity helper
    private CustomTabActivityHelper mCustomTabActivityHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        person = new Person(this);
        mCustomTabActivityHelper = new CustomTabActivityHelper();

        toolbar = (Toolbar) findViewById(R.id.tab_action_bar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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



        setUpNavHeaderDetails();
    }



    private void setUpNavHeaderDetails() {
        my_profile = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.myProfile);
        personNameInHeader = (TextView) navigationView.getHeaderView(0).findViewById(R.id.persoNameInHeader);
        personCollegeInHeader = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personCollegeInHeader);
        personPhotoInHeader = (CircularImageView) navigationView.getHeaderView(0).findViewById(R.id.personPhotoInHeader);
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
        adapter.addFragment(new SellFragment(), getResources().getString(R.string.sell));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_home, menu);
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

        return true;
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
                startActivity(myOrders);
                break;
            case R.id.mySells:
                break;
            case R.id.myWhishlist:
                Intent myWishlist = new Intent(this, wishlistView.class);
                startActivity(myWishlist);
                break;
            case R.id.notifications:
                break;
            case R.id.quick_help:
                break;
            case R.id.about_us:
                break;
            case R.id.settings:
                break;
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

//    private PendingIntent createPendingIntent(int actionSourceId) {
//        Intent actionIntent = new Intent(
//                this.getApplicationContext(), ActionBroadcastReceiver.class);
//        actionIntent.putExtra(ActionBroadcastReceiver.KEY_ACTION_SOURCE, actionSourceId);
//        return PendingIntent.getBroadcast(
//                getApplicationContext(), actionSourceId, actionIntent, 0);
//    }
}
