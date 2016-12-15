package flo.org.exchange.app.utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mayur on 02/12/16.
 */

public class Products {



    public String dateAdded;
    public int dateEnlisted;
    public int dateEvaluated;

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


    public Books book = new Books();
    public Instruments instrument = new Instruments();
    public Combopacks combopack = new Combopacks();
    public PersonGSON sellerId = new PersonGSON();

    public ArrayList<PersonGSON.college> college = null;
    public ArrayList<Subjects> subject = null;
    public String term;
    public ArrayList<PersonGSON.course> specialization = null;




}
