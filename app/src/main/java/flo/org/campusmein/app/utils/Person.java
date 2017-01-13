package flo.org.campusmein.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mayur on 30/10/16.
 */

public class Person {

    public static final String PROFILE_PREFS_FILE = "profileprefs";
    public static final String PERSON_NAME = "personname";
    public static final String PERSON_EMAIL = "personemail";
    public static final String PERSON_PIC_URL = "personpicurl";
    public static final String PERSON_COLLEGE = "personcollege";
    public static final String PERSON_COURSE = "personcourse";
    public static final String PERSON_YEAR = "personyear";
    public static final String PERSON_PHONE = "personphone";
    public static final String PERSON_PRESENT = "personpresent";
    public static final String PERSON_INFO_COLLECTED = "personinfocollected";
    public static final String PERSON_OBJECT_ID = "personobjectid";
    public static final String PERSON_COLLEGE_OBJECT_ID = "personcollegeobjectid";
    public static final String PERSON_COURSEO_BJECT_ID = "personcourseobjectid";
    public static final String PERSON_COLLEGE_LOCATION = "personcollegelocation";
    public static final String PERSON_COLLEGE_SHORT = "personcollegeshort";
    public static final String PERSON_COURSE_SHORT = "personcourseshort";
    public static final String PERSON_AUTH_CODE = "authcode";

    String personName;
    String personPhotoUrl;
    String personEmail;
    String phoneNumber;
    String collegeName;
    String academicYear;
    String course;
    String personPresent;
    String personInfoCollected;
    String personObjectId;
    String personCollegeObjectId;
    String personCourseoBjectId;
    String personCollegeLocation;
    String personCollegeShort;
    String personCourseShort;

    String college, courseYear,lastLogin, userStatus, profilepic, name, contactNumber, objectId, email;


    private Context context;
    private String authCode;

    public Person(Context context){
        this.context=context;
    }

    public String getPersonName() {

        return readPreferences(context,PERSON_NAME,"null");
    }

    public void setPersonName(String personName) {
        this.personName = personName;
        setPreferences(context, PERSON_NAME, personName);
    }

    public String getPersonObjectId() {

        return readPreferences(context,PERSON_OBJECT_ID,"null");
    }

    public void setPersonObjectId(String personObjectId) {
        this.personObjectId = personObjectId;
        setPreferences(context, PERSON_OBJECT_ID, personObjectId);
    }

    public String getPersonCollegeObjectId() {

        return readPreferences(context,PERSON_COLLEGE_OBJECT_ID,"null");
    }

    public void setPersonCollegeObjectId(String personCollegeObjectId) {
        this.personCollegeObjectId = personCollegeObjectId;
        setPreferences(context, PERSON_COLLEGE_OBJECT_ID, personCollegeObjectId);
    }

    public String getPersonCollegeLocation() {

        return readPreferences(context,PERSON_COLLEGE_LOCATION,"null");
    }

    public void setPersonCollegeLocation(String personCollegeLocation) {
        this.personCollegeLocation = personCollegeLocation;
        setPreferences(context, PERSON_COLLEGE_LOCATION, personCollegeLocation);
    }

    public String getPersonCollegeShort() {

        return readPreferences(context,PERSON_COLLEGE_SHORT,"null");
    }

    public void setPersonCollegeShort(String personCollegeShort) {
        this.personCollegeShort = personCollegeShort;
        setPreferences(context, PERSON_COLLEGE_SHORT, personCollegeShort);
    }

    public String getPersonCourseoBjectId() {

        return readPreferences(context,PERSON_COURSEO_BJECT_ID,"null");
    }

    public void setPersonCourseoBjectId(String personCourseoBjectId) {
        this.personCourseoBjectId = personCourseoBjectId;
        setPreferences(context, PERSON_COURSEO_BJECT_ID, personCourseoBjectId);
    }

    public String getPersonCourseShort() {

        return readPreferences(context,PERSON_COURSE_SHORT,"null");
    }

    public void setPersonCourseShort(String personCourseShort) {
        this.personCourseShort = personCourseShort;
        setPreferences(context, PERSON_COURSE_SHORT, personCourseShort);
    }

    public String getPersonPhotoUrl() {

        return readPreferences(context,PERSON_PIC_URL,"null");
    }

    public void setPersonPhotoUrl(String personPhotoUrl) {
        this.personPhotoUrl = personPhotoUrl;
        setPreferences(context, PERSON_PIC_URL, personPhotoUrl);
    }


    public String getPersonEmail() {
        return readPreferences(context,PERSON_EMAIL,"null");
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
        setPreferences(context,PERSON_EMAIL,personEmail);
    }

    public String getPhoneNumber() {
        return readPreferences(context,PERSON_PHONE,"");
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        setPreferences(context, PERSON_PHONE,phoneNumber);
    }

    public String getCollegeName() {
        return readPreferences(context,PERSON_COLLEGE,"null");
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
        setPreferences(context,PERSON_COLLEGE,collegeName);

    }

    public String getAcademicYear() {
        return readPreferences(context,PERSON_YEAR,"null");
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
        setPreferences(context,PERSON_YEAR,academicYear);
    }

    public String getCourse() {
        return readPreferences(context,PERSON_COURSE,"null");
    }

    public void setCourse(String course) {
        this.course = course;
        setPreferences(context,PERSON_COURSE,course);
    }

    public String getPersonPresent() {
        return readPreferences(context, PERSON_PRESENT,"false");
    }

    public void setPersonPresent(String personPresent) {
        this.personPresent = personPresent;
        setPreferences(context,PERSON_PRESENT,personPresent);
    }

    public String getPersonInfoCollected() {
        return readPreferences(context,PERSON_INFO_COLLECTED,"false");
    }

    public void setPersonInfoCollected(String personInfoCollected) {
        this.personInfoCollected = personInfoCollected;
        setPreferences(context,PERSON_INFO_COLLECTED,personInfoCollected);
    }


    private static void setPreferences(Context actContext, String preferenceName, String preferenceValue){

        SharedPreferences savedPreferences = actContext.getSharedPreferences(PROFILE_PREFS_FILE,actContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putString(preferenceName,preferenceValue);
        editor.apply();

    }

    private static String readPreferences(Context actContext, String preferenceName, String preferenceDefaultValue){

        SharedPreferences savedPreferences = actContext.getSharedPreferences(PROFILE_PREFS_FILE,actContext.MODE_PRIVATE);
        return savedPreferences.getString(preferenceName,preferenceDefaultValue);

    }


    public void setPersonAuthCode(String authCode) {
        this.authCode = authCode;
        setPreferences(context,PERSON_AUTH_CODE,authCode);
    }

    public String getPersonAuthCode() {
        return readPreferences(context,PERSON_AUTH_CODE,"secretPasskey");
    }
}
