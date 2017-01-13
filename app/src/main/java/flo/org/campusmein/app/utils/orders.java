package flo.org.campusmein.app.utils;

/**
 * Created by Mayur on 05/01/17.
 */

public class orders {

    public Products product = new Products();
    public PersonGSON buyerId = new PersonGSON();

    private String updateComments;
    private String reasonDelayed;
    private String reasonCanceled;
    private String notificationString;
    private String deliveryComments;

    private int quantity;
    private String orderCode;
    private int orderCost;


    private String deliveryExpected;
    private String orderedDate;
    private String deliveredDate;
    private String deliveryReadyDate;
    private String delayedDate;
    private String canceledDate;
    private String paymentMode;

    private int status;
    private String  objectId;

    public String getObjectId() {
        return objectId;
    }

    private postOrder postOrder = new postOrder();
    private updateOrder updateOrder = new updateOrder();

    public Products getProduct(){
        return this.product;
    }

    public PersonGSON getPerson(){
        return this.buyerId;
    }

    public String getUpdateComments() {
        return updateComments;
    }

    public void setUpdateComments(String updateComments) {
        this.updateComments = updateComments;
    }

    public String getReasonDelayed() {
        return reasonDelayed;
    }

    public void setReasonDelayed(String reasonDelayed) {
        this.reasonDelayed = reasonDelayed;
    }

    public String getReasonCanceled() {
        return reasonCanceled;
    }

    public void setReasonCanceled(String reasonCanceled) {
        this.reasonCanceled = reasonCanceled;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public int getOrderCost() {
        return orderCost;
    }

    public void setOrderCost(int orderCost) {
        this.orderCost = orderCost;
    }

    public String getOrderedDate() {
        return orderedDate;
    }

    public void setOrderedDate(String orderedDate) {
        this.orderedDate = orderedDate;
    }

    public String getNotificationString() {
        return notificationString;
    }

    public void setNotificationString(String notificationString) {
        this.notificationString = notificationString;
    }

    public String getDeliveryComments() {
        return deliveryComments;
    }

    public void setDeliveryComments(String deliveryComments) {
        this.deliveryComments = deliveryComments;
    }

    public String getDeliveryExpected() {
        return deliveryExpected;
    }

    public void setDeliveryExpected(String deliveryExpected) {
        this.deliveryExpected = deliveryExpected;
    }

    public String getDeliveredDate() {
        return deliveredDate;
    }

    public void setDeliveredDate(String deliveredDate) {
        this.deliveredDate = deliveredDate;
    }

    public String getDeliveryReadyDate() {
        return deliveryReadyDate;
    }

    public void setDeliveryReadyDate(String deliveryReadyDate) {
        this.deliveryReadyDate = deliveryReadyDate;
    }

    public String getDelayedDate() {
        return delayedDate;
    }

    public void setDelayedDate(String delayedDate) {
        this.delayedDate = delayedDate;
    }

    public String getCanceledDate() {
        return canceledDate;
    }

    public void setCanceledDate(String canceledDate) {
        this.canceledDate = canceledDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public orders.postOrder getPostOrder() {
        return postOrder;
    }

    public orders.updateOrder getUpdateOrder() {
        return updateOrder;
    }

    public class postOrder{

        public product product = new product();
        public buyerId buyerId = new buyerId();
        public int quantity;
        public int orderCost;
        public int orderCode;
        public String paymentMode;

        public String orderedDate;
        public String deliveryExpected;

        public int status;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getOrderCost() {
            return orderCost;
        }

        public void setOrderCost(int orderCost) {
            this.orderCost = orderCost;
        }

        public int getOrderCode() {
            return orderCode;
        }

        public void setOrderCode(int orderCode) {
            this.orderCode = orderCode;
        }

        public String getOrderedDate() {
            return orderedDate;
        }

        public void setOrderedDate(String orderedDate) {
            this.orderedDate = orderedDate;
        }

        public String getDeliveryExpected() {
            return deliveryExpected;
        }

        public void setDeliveryExpected(String deliveryExpected) {
            this.deliveryExpected = deliveryExpected;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getPaymentMode() {
            return paymentMode;
        }

        public void setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
        }

        public class product {
            public String ___class= "products";
            public String objectId;

            public String getObjectId() {
                return objectId;
            }

            public void setObjectId(String objectId) {
                this.objectId = objectId;
            }
        }

        public class buyerId{
            public String ___class="Users";
            public String objectId;

            public String getObjectId() {
                return objectId;
            }

            public void setObjectId(String objectId) {
                this.objectId = objectId;
            }
        }
    }

    public class updateOrder{
        private String reasonCanceled;
        private String canceledDate;
        private int status=0;

        public String getReasonCanceled() {
            return reasonCanceled;
        }

        public void setReasonCanceled(String reasonCanceled) {
            this.reasonCanceled = reasonCanceled;
        }

        public String getCanceledDate() {
            return canceledDate;
        }

        public void setCanceledDate(String canceledDate) {
            this.canceledDate = canceledDate;
        }

    }
}
