package flo.org.campusmein.app.Home.orderPlacement;

import android.content.Context;
import android.content.Intent;
import android.graphics.Movie;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import flo.org.campusmein.R;
import flo.org.campusmein.app.utils.Products;
import flo.org.campusmein.app.utils.orders;

/**
 * Created by Mayur on 07/01/17.
 */

public class ordersListAdapter extends RecyclerView.Adapter<ordersListAdapter.MyViewHolder> {

    private List<orders> ordersList;
    private Context mContext;
    private String ORD_ID_KEY = "ord_id";

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView ord_itemTitle, ord_date, ord_price, ord_status;
        public ImageView ord_ItemImage;
        public LinearLayout order_cardView;

        public MyViewHolder(View view) {
            super(view);
            order_cardView = (LinearLayout) view.findViewById(R.id.order_cardView);
            ord_itemTitle = (TextView) view.findViewById(R.id.ord_itemTitle);
            ord_date = (TextView) view.findViewById(R.id.ord_date);
            ord_price = (TextView) view.findViewById(R.id.ord_price);
            ord_status = (TextView) view.findViewById(R.id.ord_status);
            ord_ItemImage = (ImageView) view.findViewById(R.id.ord_ItemImage);
        }
    }


    public ordersListAdapter(List<orders> ordersList, Context mContext) {
        this.ordersList = ordersList;
        this.mContext = mContext;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ordered_product_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final orders order = ordersList.get(position);
        String ordered_date = order.getOrderedDate();
        long ordered_date_long = Long.valueOf(ordered_date);
        Date date = new Date(ordered_date_long);

        SimpleDateFormat DayFormat = new SimpleDateFormat("EEE");
        SimpleDateFormat MonthFormat = new SimpleDateFormat("MMM");
        SimpleDateFormat DateFormat = new SimpleDateFormat("dd");
        SimpleDateFormat YearFormat = new SimpleDateFormat("yyyy");

        String ord_date_string = DayFormat.format(date)+", "+MonthFormat.format(date)+" "+DateFormat.format(date)+" "+YearFormat.format(date);
        String s = ord_date_string.toUpperCase();
        holder.ord_date.setText(s);

        Log.d("ord_price",""+order.getOrderCost());
        holder.ord_price.setText(mContext.getString(R.string.rupeesSymbol)+order.getOrderCost()+mContext.getString(R.string.priceEndSymbol));

        Products product = order.getProduct();
        Log.d("type",product.type);
        if(product.type.equals(mContext.getString(R.string.bookType))){
            Log.d("ord_itemTitle",product.book.title);
            holder.ord_itemTitle.setText(product.book.title);
            Glide.with(mContext)
                    .load(product.book.photofile)
                    .crossFade()
                    .placeholder(R.drawable.ic_book_default)
                    .error(R.drawable.ic_book_default)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ord_ItemImage);
        }
        else if(product.type.equals(mContext.getString(R.string.instrumentType))){
            Log.d("ord_itemTitle",product.instrument.instrumentName);
            holder.ord_itemTitle.setText(product.instrument.instrumentName);
            Glide.with(mContext)
                    .load(product.instrument.photoFile)
                    .crossFade()
                    .placeholder(R.drawable.ic_instrument_default)
                    .error(R.drawable.ic_instrument_default)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ord_ItemImage);
        }
        else if(product.type.equals(mContext.getString(R.string.comboType))){
            Log.d("ord_itemTitle",product.combopack.title);
            holder.ord_itemTitle.setText(product.combopack.title);
            Glide.with(mContext)
                    .load(product.combopack.photoUrl)
                    .crossFade()
                    .placeholder(R.drawable.ic_combo_default)
                    .error(R.drawable.ic_combo_default)
                    .thumbnail(0.5f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.ord_ItemImage);
        }
        int status = order.getStatus();
        switch (status){
            case -2:
                holder.ord_status.setText(R.string.delayed);
                holder.ord_status.setTextColor(mContext.getResources().getColor(R.color.colorRed));
                break;
            case -1:
                holder.ord_status.setText(R.string.pending);
                holder.ord_status.setTextColor(mContext.getResources().getColor(android.R.color.tertiary_text_light));
                break;
            case 0:
                holder.ord_status.setText(R.string.canceled);
                holder.ord_status.setTextColor(mContext.getResources().getColor(R.color.colorRed));
                break;
            case 1:
                holder.ord_status.setText(R.string.deliveryReady);
                holder.ord_status.setTextColor(mContext.getResources().getColor(R.color.colorOrange));
                break;
            case 2:
                String delivered_date = order.getDeliveredDate();
                long status_date_long = Long.valueOf(delivered_date);
                Date statusDate = new Date(status_date_long);
                String statusString = mContext.getString(R.string.delivered)+" "+DayFormat.format(statusDate)+", "+MonthFormat.format(statusDate)+" "+DateFormat.format(statusDate)+" "+YearFormat.format(statusDate);
                String s1 = statusString.toUpperCase();
                holder.ord_status.setText(s1);
                holder.ord_status.setTextColor(mContext.getResources().getColor(R.color.colorGreen));
                break;
        }

        holder.order_cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewOrder = new Intent(mContext,orderDetailsView.class);
                viewOrder.putExtra(ORD_ID_KEY,order.getObjectId());
                mContext.startActivity(viewOrder);
            }
        });

    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }
}
