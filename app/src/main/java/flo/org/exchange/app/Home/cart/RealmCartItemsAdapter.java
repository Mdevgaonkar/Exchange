package flo.org.exchange.app.Home.cart;

/**
 * Created by Mayur on 21/12/16.
 */

import android.content.Context;


import flo.org.exchange.app.utils.cartObject;
import io.realm.RealmResults;

public class RealmCartItemsAdapter extends RealmModelAdapter<cartObject> {

    public RealmCartItemsAdapter(Context context, RealmResults<cartObject> realmResults, boolean automaticUpdate) {

        super(context, realmResults, automaticUpdate);
    }
}