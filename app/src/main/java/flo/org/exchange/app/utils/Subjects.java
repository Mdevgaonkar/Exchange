package flo.org.exchange.app.utils;

import java.util.ArrayList;

/**
 * Created by Mayur on 10/12/16.
 */

public class Subjects {
        public String year;
        public String subject;
        public String subjectShort;
        public int semester;
        public String objectId;
        public ArrayList<branch> branchs= new ArrayList<branch>();


        public class branch {
            public String branchShort;
            public String branch;
            public String objectId;
        }
    }
