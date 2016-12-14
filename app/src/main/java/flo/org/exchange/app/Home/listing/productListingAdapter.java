package flo.org.exchange.app.Home.listing;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
 * Created by Mayur on 01/12/16.
 */

public class productListingAdapter extends RecyclerView.Adapter<productListingAdapter.itemViewHolder>{
    private Context mContext;

    List<Products> products;


    @Override
    public itemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View item_card_view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card_view, parent, false);
        itemViewHolder item_card = new itemViewHolder(item_card_view);
        return item_card;
    }

    public productListingAdapter(Context mContext, List<Products> productList) {
        this.mContext = mContext;
        this.products = productList;
    }

    @Override
    public void onBindViewHolder(itemViewHolder holder, int position) {

        holder.productTitle.setText("");
        holder.productSubTitle.setText("");
        holder.productPrice.setText("");
        // loading album cover using Glide library
        Glide.with(mContext).load("").into(holder.productPhoto);

    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class itemViewHolder extends RecyclerView.ViewHolder {
        CardView itemCard,productCard;
        ImageView productPhoto;
        TextView productTitle, productSubTitle,  productPrice;

        itemViewHolder(View productView) {
            super(productView);
            final Context mycontext=productView.getContext();
            itemCard = (CardView)productView.findViewById(R.id.product_card_view);
            productPhoto = (ImageView) productView.findViewById(R.id.product_photo);
            productTitle = (TextView)productView.findViewById(R.id.profile_name);
            productSubTitle = (TextView) productView.findViewById(R.id.product_publisher);
            productPrice= (TextView) productView.findViewById(R.id.product_price);


            productView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent openProduct=new Intent(mycontext,productDescription.class);
//                    mycontext.startActivity(openProduct);
                }
            });
        }
    }

}