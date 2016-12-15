package flo.org.exchange.app.Home.listing;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import flo.org.exchange.R;
import flo.org.exchange.app.utils.Products;

/**
 * Created by Mayur on 15/12/2016
 */
public class productListingAdapter extends RecyclerView.Adapter<productListingAdapter.MyViewHolder> {

    private Context mContext;
    private List<Products> productList;

    private static final String TYPE_BOOK ="B";
    private static final String TYPE_INSTRUMENT ="I";
    private static final String TYPE_COMBOPACK ="C";

    public class MyViewHolder extends RecyclerView.ViewHolder {
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
        Products product = productList.get(position);
        if (product.type.equals(TYPE_BOOK)){
            holder.title.setText(product.book.title);
            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
            // loading album cover using Glide library
            Glide.with(mContext).load(product.book.photofile).into(holder.thumbnail);
        }else if(product.type.equals(TYPE_INSTRUMENT)){
            holder.title.setText(product.instrument.instrumentName);
            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
            // loading album cover using Glide library
            Glide.with(mContext).load(product.instrument.photofile).into(holder.thumbnail);
        }else if(product.type.equals(TYPE_COMBOPACK)){
            holder.title.setText(product.combopack.title);
            holder.count.setText(mContext.getString(R.string.rupeesSymbol)+product.listPrice+mContext.getString(R.string.priceEndSymbol));
            // loading album cover using Glide library
            Glide.with(mContext).load(product.combopack.photoUrl).into(holder.thumbnail);
        }


        holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
            }
        });
    }

    /**
     * Showing popup menu when tapping on 3 dots
     */
    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_product, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }

    /**
     * Click listener for popup menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Added to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext, "Added to Cart", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
