package flo.org.campusmein.app.Home.listing;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import flo.org.campusmein.R;
import flo.org.campusmein.app.Home.ProductView.productView;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.RealmUtils.RealmController;
import flo.org.campusmein.app.utils.campusExchangeApp;
import flo.org.campusmein.app.utils.cartObject;

/**
 * Created by Mayur on 15/12/2016
 */
public class productListingAdapter extends RecyclerView.Adapter<productListingAdapter.MyViewHolder> {

    private Context mContext;
    private List<Products> productList;

    private static final String TYPE_BOOK ="B";
    private static final String TYPE_INSTRUMENT ="I";
    private static final String TYPE_COMBOPACK ="C";

    private static final String PRODUCT_TYPE = "type";
    private static final String PRODUCT_OBJECT_ID = "objectId";
    private static final String BIC_OBJECT_ID = "bicObjectId";

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView title, count;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);

        }

    }


    public productListingAdapter(Context mContext, List<Products> productList) {
        this.mContext = mContext;
        this.productList = productList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Products product = productList.get(position);
        setPriceText(product, holder);
        if (product.type.equals(TYPE_BOOK)){
            holder.title.setText(product.book.title);

//            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
            // loading product image using Glide library
//            holder.thumbnail.setImageResource(R.drawable.ic_book_default);
            try{
                Glide.with(mContext).load(product.book.photofile).thumbnail(0.5f)
                        .crossFade()
                        .placeholder(R.drawable.ic_book_default)
                        .error(R.drawable.ic_book_default)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.thumbnail);
            }catch (NullPointerException e) {
                holder.thumbnail.setImageResource(R.drawable.ic_book_default);
            }
        }else if(product.type.equals(TYPE_INSTRUMENT)){
            holder.title.setText(product.instrument.instrumentName);

//            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
            // loading product image using Glide library
//            holder.thumbnail.setImageResource(R.drawable.ic_instrument_default);
            try{
                Glide.with(mContext).load(product.instrument.photoFile).thumbnail(0.5f)
                        .crossFade()
                        .placeholder(R.drawable.ic_instrument_default)
                        .error(R.drawable.ic_instrument_default)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.thumbnail);
            }catch (NullPointerException e) {
                holder.thumbnail.setImageResource(R.drawable.ic_instrument_default);
            }
        }else if(product.type.equals(TYPE_COMBOPACK)){
            holder.title.setText(product.combopack.title);
//            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
            // loading product image using Glide library
            holder.thumbnail.setImageResource(R.drawable.ic_combo_default);
            try{
            Glide.with(mContext).load(product.combopack.photoUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .placeholder(R.drawable.ic_combo_default)
                    .error(R.drawable.ic_combo_default)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.thumbnail);
            }catch (NullPointerException e) {
                holder.thumbnail.setImageResource(R.drawable.ic_combo_default);
            }
        }

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductView(product);
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProductView(product);
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow, product);
            }
        });
    }

    private void setPriceText(Products product, MyViewHolder holder) {
        int price = product.listPrice;
        if(price>0){
            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
        }else if(price<0){
            holder.count.setText(mContext.getString(R.string.price_text_coming_soon));
        }else {
            holder.count.setText(mContext.getString(R.string.price_text_free));
        }

    }


    public void openProductView(Products product){
        Intent productView = new Intent(mContext,productView.class);
        productView.putExtra(PRODUCT_TYPE, product.type);
        productView.putExtra(PRODUCT_OBJECT_ID, product.objectId);
        if (product.type.equals(TYPE_BOOK)){
            productView.putExtra(BIC_OBJECT_ID, product.book.objectId);
        }else if(product.type.equals(TYPE_INSTRUMENT)){
            productView.putExtra(BIC_OBJECT_ID, product.instrument.objectId);
        }else if(product.type.equals(TYPE_COMBOPACK)){
            productView.putExtra(BIC_OBJECT_ID, product.combopack.objectId);
        }
        mContext.startActivity(productView);
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view, final Products product) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_product, popup.getMenu());

        setColorWishlistIcon(popup,product);

        Drawable drawablecart = popup.getMenu().getItem(1).getIcon();
        if(drawablecart != null) {
            drawablecart.mutate();
            drawablecart.setColorFilter(mContext.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_add_cart:
                            addToCart(product.objectId);
                        ((productListingActivity)mContext).invalidateOptionsMenu();
                        return true;
                    case R.id.action_add_favourite:
                        addOrRemoveWish(product);
                        return true;
                    default:
                }
                return false;
            }
        });

        MenuPopupHelper menuHelper = new MenuPopupHelper(mContext, (MenuBuilder) popup.getMenu(), view);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();

//        popup.show();
    }

    private void setColorWishlistIcon(PopupMenu popup, Products product){
        if(RealmController.getInstance().isItemInWishlist(product.objectId)) {
            Drawable drawable = popup.getMenu().getItem(0).getIcon();
            popup.getMenu().getItem(0).setTitle("Remove From Wishlist");
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(mContext.getResources().getColor(R.color.colorRed), PorterDuff.Mode.SRC_ATOP);
            }
        }else {
            Drawable drawable = popup.getMenu().getItem(0).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(mContext.getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    private void addOrRemoveWish(Products product) {
        if(RealmController.getInstance().isItemInWishlist(product.objectId)) {
            RealmController.getInstance().removewishListItem(product.objectId);
            Toast.makeText(mContext, R.string.removedFromWishlist,Toast.LENGTH_SHORT).show();
        }else {
            RealmController.getInstance().addToWishList(product.objectId);
            Toast.makeText(mContext, R.string.addedToWishlist,Toast.LENGTH_SHORT).show();
//                            ActivityCompat.invalidateOptionsMenu();
        }
    }


    private void addToCart(String s) {
        if(isItemInCart(s)) {
            Toast.makeText(mContext, R.string.alreadyInCart,Toast.LENGTH_SHORT).show();
        }else {
            cartObject item = new cartObject();
            item.setId((int) (RealmController.getInstance().getItems().size()+ System.currentTimeMillis()));
            item.setProductId(s);
            item.setQuantity(1);
            Toast.makeText(mContext, R.string.addedToCart,Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
