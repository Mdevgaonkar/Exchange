package flo.org.campusmein.app.utils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Mayur on 22/12/16.
 */

public class wishListObject extends RealmObject {

    @PrimaryKey
    private int id;

    private String productId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
