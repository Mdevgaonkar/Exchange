package flo.org.exchange.app.Login;

import java.util.ArrayList;

/**
 * Created by Mayur on 02/11/16.
 */

public class College {

    public String collegeName;
    public String location;
    public String objectId;
    public String collegeShort;
    public ArrayList<Branch> branches = new ArrayList<Branch>();




    public class Branch{
        public String branchShort;
        public String branch;
        public String objectId;

    }
}
