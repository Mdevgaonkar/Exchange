package flo.org.exchange.app.Home.listing;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import flo.org.exchange.R;
import flo.org.exchange.app.utils.Products;

/**
 * Created by Ravi Tamada on 18/05/16.
 */
public class productListingAdapter extends RecyclerView.Adapter<productListingAdapter.MyViewHolder> {

    private Context mContext;
    private List<Products> productList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, price;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.product_name);
            price = (TextView) view.findViewById(R.id.product_price);
            thumbnail = (ImageView) view.findViewById(R.id.product_photo);
        }
    }


    public productListingAdapter(Context mContext, List<Products> productList) {
        this.mContext = mContext;
        this.productList = productList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_card_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Products product = productList.get(position);
        holder.title.setText(product.book.title);
        Log.d("Product Title",product.book.title);
        holder.price.setText("Rs. "+product.listPrice+"/-");
        // loading album cover using Glide library
        Glide.with(mContext).load(product.book.photofile).into(holder.thumbnail);

    }
    /**
     * holder.overflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow);
            }
        });*/


    /**
     * Showing popup menu when tapping on 3 dots

    private void showPopupMenu(View view) {
        // inflate menu
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_album, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener());
        popup.show();
    }*/

    /**
     * Click listener for popup menu items

    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {

        public MyMenuItemClickListener() {
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_add_favourite:
                    Toast.makeText(mContext, "Add to favourite", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.action_play_next:
                    Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                    return true;
                default:
            }
            return false;
        }
    }
     */

    @Override
    public int getItemCount() {
        return productList.size();
    }
}