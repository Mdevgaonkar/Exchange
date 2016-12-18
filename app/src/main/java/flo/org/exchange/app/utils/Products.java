package flo.org.exchange.app.utils;

import java.util.ArrayList;
import java.util.Date;

import flo.org.exchange.app.Login.College;

/**
 * Created by Mayur on 02/12/16.
 */

public class Products {



    public String dateAdded;
    public String dateEnlisted;
    public String dateEvaluated;

    public String objectId;

    public int stock;
    public String type;


    public String reasonRemoved;
    public int condition;
    public Boolean removed;
    public Boolean enlisted;
    public Boolean evaluated;

    public int listPrice;
    public int mrp;


    public String disclaimerText;
    public String bannerUrl;

    public int priceGood;
    public int priceBad;
    public int priceNew;

    public Books book = new Books();
    public Instruments instrument = new Instruments();
    public Combopacks combopack = new Combopacks();
    public PersonGSON sellerId = new PersonGSON();

    public ArrayList<PersonGSON.college> college = new ArrayList<>();
    public ArrayList<Subjects> subject = new ArrayList<>();
    public String term;
    public ArrayList<College.Branch> specialization = new ArrayList<>();

    public boolean onForResell;




}
