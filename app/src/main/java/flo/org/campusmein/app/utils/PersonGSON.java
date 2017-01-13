package flo.org.campusmein.app.utils;

/**
 * Created by Mayur on 22/11/16.
 */

public class PersonGSON {

    public String courseYear;
    public String lastLogin;
    public String userStatus;
    public String profilepic;
    public String name;
    public String contactNumber;
    public String objectId;
    public String email;
    public college college = new college();
    public course course = new course();

    public PersonGSON.postPerson getPostPerson() {
        return postPerson;
    }

    public postPerson postPerson = new postPerson();

    public String key_courseYear = "courseYear";
    public String key_lastLogin = "lastLogin";
    public String key_userStatus = "userStatus";
    public String key_profilepic = "profilepic";
    public String key_name = "name";
    public String key_contactNumber = "contactNumber";
    public String key_objectId = "objectId";
    public String key_email = "email";
    public String key_college = "college";
    public String key_course = "course";

    public class college{
        public String collegeName;
        public String location;
        public String objectId;
        public String collegeShort;
        public String ___class = "colleges";

        public String key_collegeName = "collegeName";
        public String key_location = "location";
        public String key_objectId = "objectId";
        public String key_collegeShort = "collegeShort";
        public String key____class = "___class";

        public String getKey_collegeName() {
            return key_collegeName;
        }

        public String getKey_location() {
            return key_location;
        }

        public String getKey_objectId() {
            return key_objectId;
        }

        public String getKey_collegeShort() {
            return key_collegeShort;
        }

        public String getKey____class() {
            return key____class;
        }


        public String get___class() {
            return ___class;
        }

        public void set___class(String ___class) {
            this.___class = ___class;
        }

        public String getCollegeName() {
            return collegeName;
        }

        public void setCollegeName(String collegeName) {
            this.collegeName = collegeName;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getCollegeShort() {
            return collegeShort;
        }

        public void setCollegeShort(String collegeShort) {
            this.collegeShort = collegeShort;
        }
    }

    public class course {
        public  String ___class = "branches";
        public  String branchShort;
        public  String branch;
        public  String objectId;

        public  String key____class = "___class";
        public  String key_branchShort = "branchShort";
        public  String key_branch = "branch";
        public  String key_objectId = "objectId";

        public String getKey____class() {
            return key____class;
        }

        public String getKey_branchShort() {
            return key_branchShort;
        }

        public String getKey_branch() {
            return key_branch;
        }

        public String getKey_objectId() {
            return key_objectId;
        }



        public String get___class() {
            return ___class;
        }

        public void set___class(String ___class) {
            this.___class = ___class;
        }

        public String getBranchShort() {
            return branchShort;
        }

        public void setBranchShort(String branchShort) {
            this.branchShort = branchShort;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String branch) {
            this.branch = branch;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }
    }

    public String getCourseYear() {
        return courseYear;
    }

    public void setCourseYear(String courseYear) {
        this.courseYear = courseYear;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKey_courseYear() {
        return key_courseYear;
    }

    public String getKey_lastLogin() {
        return key_lastLogin;
    }

    public String getKey_userStatus() {
        return key_userStatus;
    }

    public String getKey_profilepic() {
        return key_profilepic;
    }

    public String getKey_name() {
        return key_name;
    }

    public String getKey_contactNumber() {
        return key_contactNumber;
    }

    public String getKey_objectId() {
        return key_objectId;
    }

    public String getKey_email() {
        return key_email;
    }

    public String getKey_college() {
        return key_college;
    }

    public String getKey_course() {
        return key_course;
    }

    public class postPerson{
        private String courseYear;
        private String lastLogin;
        private String userStatus;
        private String profilepic;
        private String name;
        private String contactNumber;
        private String objectId;
        private String email;
        public college college = new college();
        public course course = new course();
        private String password;

        public String getCourseYear() {
            return courseYear;
        }

        public void setCourseYear(String courseYear) {
            this.courseYear = courseYear;
        }

        public String getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(String lastLogin) {
            this.lastLogin = lastLogin;
        }

        public String getUserStatus() {
            return userStatus;
        }

        public void setUserStatus(String userStatus) {
            this.userStatus = userStatus;
        }

        public String getProfilepic() {
            return profilepic;
        }

        public void setProfilepic(String profilepic) {
            this.profilepic = profilepic;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public postPerson.college getCollege() {
            return college;
        }

        public void setCollege(postPerson.college college) {
            this.college = college;
        }

        public postPerson.course getCourse() {
            return course;
        }

        public void setCourse(postPerson.course course) {
            this.course = course;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public class college {
            private String objectId;
            private String ___class = "colleges";


            public String getObjectId() {
                return objectId;
            }

            public void setObjectId(String objectId) {
                this.objectId = objectId;
            }

            public String get___class() {
                return ___class;
            }

            public void set___class(String ___class) {
                this.___class = ___class;
            }
        }

        public class course {
            private  String ___class = "branches";
            private  String objectId;

            public String get___class() {
                return ___class;
            }

            public void set___class(String ___class) {
                this.___class = ___class;
            }

            public String getObjectId() {
                return objectId;
            }

            public void setObjectId(String objectId) {
                this.objectId = objectId;
            }
        }
    }


}
