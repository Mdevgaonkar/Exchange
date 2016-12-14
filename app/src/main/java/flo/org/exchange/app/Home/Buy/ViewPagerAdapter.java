package flo.org.exchange.app.Home.Buy;

/**
 * Created by Mayur on 09/06/16.
 */
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import flo.org.exchange.R;
import flo.org.exchange.app.Home.MainHomeActivity;
import flo.org.exchange.app.utils.buyFragmentVariables;


public class ViewPagerAdapter extends PagerAdapter {

    private Context mContext;

    private static final String PRODUCT_TITLE = "productTitle";
    private static final String PRODUCT_STATUS = "productStatus";
    private static final String PRODUCT_WHERE_CLAUSE = "productWhereClause";
    private static final String PRODUCT_CLASS = "productClass";
    private static final String PRODUCT_POLL = "productPoll";
    private static final String PRODUCT_POLL_URL = "productPollUrl";

    private ArrayList<String> carousalImages;
    private ArrayList<buyFragmentVariables.carousal.items> carousalItems;

    private ArrayList<String> urls;

    public ViewPagerAdapter(Context mContext, ArrayList<buyFragmentVariables.carousal.items> carousalItems) {
        this.mContext = mContext;

        if(carousalItems != null){
            this.carousalItems = carousalItems;
        }

//        this.carousalImages = carousalImages;
//        if(urls != null) {
//            this.urls = urls;
//        }
    }

    @Override
    public int getCount() {
        if (carousalItems != null) {
            return carousalItems.size();
        }else return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.buy_home_pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.img_pager_item);
        imageView.setImageResource(R.drawable.sidebar_header_background);
        try{    Glide.with(mContext)
//                .load(mResources[position])
//                .load(carousalImages.get(position))
                .load(carousalItems.get(position).photoUrl)
                .thumbnail(0.5f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
        }catch (NullPointerException e){
            Log.d("introImages","can't load image");
            Glide.with(mContext).load(R.drawable.sidebar_header_background).into(imageView);
        }
//        imageView.setImageResource(Integer.parseInt(mResources[position]));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(mContext,"you clicked page "+position, Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext,""+carousalItems.get(position).Status, Toast.LENGTH_SHORT).show();
                switch (carousalItems.get(position).screenType){
                    case 1:         //list View
                        openProductStoreList(
                                carousalItems.get(position).title,
                                carousalItems.get(position).Status,
                                carousalItems.get(position).whereClause,
                                carousalItems.get(position).poll,
                                carousalItems.get(position).pollUrl,
                                carousalItems.get(position).___class
                        );
                        break;
                    case 2:         //Product View
                        break;
                    case 3:         //Combo View
                        break;
                    case 4:         //WebView Chrome custom Tab
                        ((MainHomeActivity)mContext).openCustomTab(carousalItems.get(position).whereClause);
                        break;
                }

            }
        });

        container.addView(itemView);

        return itemView;
    }

    private void openProductStoreList(String productTitle, int productListStatus, String whereClause, boolean poll, String pollUrl, String ___class) {
        Intent openProductStore = new Intent(mContext, flo.org.exchange.app.Home.listing.productListingActivity.class);
//        openProductStore.putExtra(PRODUCT_TYPE, productType);
        openProductStore.putExtra(PRODUCT_TITLE, productTitle);
        openProductStore.putExtra(PRODUCT_STATUS, productListStatus);
        openProductStore.putExtra(PRODUCT_WHERE_CLAUSE, whereClause);
        openProductStore.putExtra(PRODUCT_CLASS,___class);
        openProductStore.putExtra(PRODUCT_POLL,poll);
        openProductStore.putExtra(PRODUCT_POLL_URL,pollUrl);
        mContext.startActivity(openProductStore);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }


}