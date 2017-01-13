package flo.org.campusmein.app.utils.RealmUtils;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import flo.org.campusmein.app.utils.cartObject;
import flo.org.campusmein.app.utils.wishListObject;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Mayur on 21/12/16.
 */

public class RealmController {
    private static RealmController instance;
    private final Realm realm;
    private static final String PRODUCT_ID = "productId";

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {

        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {

        return instance;
    }

    public Realm getRealm() {

        return realm;
    }

    //Refresh the realm istance
    public void refresh() {

        realm.refresh();
    }

    //clear all objects from cart
    public void clearCart() {

        realm.beginTransaction();
        realm.clear(cartObject.class);
        realm.commitTransaction();
    }

    //clear all objects from wishlist
    public void clearWishlist() {

        realm.beginTransaction();
        realm.clear(wishListObject.class);
        realm.commitTransaction();
    }

    //find all objects in the Book.class
    public RealmResults<cartObject> getItems() {

        return realm.where(cartObject.class).findAll();
    }


    //query a single item with the given id
    public cartObject getCartObjectWithId(String id) {

        return realm.where(cartObject.class).equalTo("id", id).findFirst();
    }

    //query a single item with the given product_id
    public cartObject getCartObjectWithProductId(String product_id) {
        return realm.where(cartObject.class).equalTo(PRODUCT_ID, product_id).findFirst();
    }

    //query quantity of a particular product
    public int getQuantityWithProductId(String product_id){
        return realm.where(cartObject.class).equalTo(PRODUCT_ID, product_id).findFirst().getQuantity();
    }

    //updateCartItemQuantity
    public void updateCartItemQuantity(String product_id,int quantity) {
        realm.beginTransaction();
        realm.where(cartObject.class).equalTo(PRODUCT_ID,product_id).findFirst().setQuantity(quantity);
        realm.commitTransaction();
    }

    //setDefaultCartItemQuantity
    public void setDefaultCartItemQuantity(String product_id) {
        realm.beginTransaction();
        realm.where(cartObject.class).equalTo(PRODUCT_ID,product_id).findFirst().setQuantity(1);
        realm.commitTransaction();
    }

    public void removeCartItem(String product_id){
        realm.beginTransaction();
        RealmResults<cartObject> c = realm.where(cartObject.class).equalTo(PRODUCT_ID,product_id).findAll();
        if(c.size()>0){
            c.remove(0);
        }
        realm.commitTransaction();
    }

    public void removewishListItem(String product_id){
        realm.beginTransaction();
        RealmResults<wishListObject> w = realm.where(wishListObject.class).equalTo(PRODUCT_ID,product_id).findAll();
        if(w.size()>0){
            w.remove(0);
        }
        realm.commitTransaction();
    }

    //check if cartObject.class is empty
    public boolean hasItems() {

        return !realm.allObjects(cartObject.class).isEmpty();
    }

    //check if cartObject.class is empty
    public boolean wishListhasItems() {
        return !realm.allObjects(wishListObject.class).isEmpty();
    }

    public RealmResults<wishListObject>  getAllWishedItems() {
        return realm.where(wishListObject.class).findAll();
    }

    public void addToWishList(String product_id){
        wishListObject itemWished = new wishListObject();
        itemWished.setId((int) (getAllWishedItems().size()+ System.currentTimeMillis()));
        itemWished.setProductId(product_id);
        realm.beginTransaction();
        realm.copyToRealm(itemWished);
        realm.commitTransaction();
    }

    public boolean isItemInWishlist(String s) {
        if (wishListhasItems()){
            if(getWishListObjectWithProductId(s) == null){
                return false;
            }else {
                return true;
            }
        }else {
            return false;
        }

    }

    private wishListObject getWishListObjectWithProductId(String s) {
        return realm.where(wishListObject.class).equalTo(PRODUCT_ID, s).findFirst();
    }

    //query example
    public RealmResults<cartObject> querycartObject(String productId) {

        return realm.where(cartObject.class)
                .contains(PRODUCT_ID, productId)
                .findAll();

    }
}
