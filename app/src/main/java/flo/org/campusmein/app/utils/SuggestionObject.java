package flo.org.campusmein.app.utils;

/**
 * Created by Mayur on 25/06/17.
 */

public class SuggestionObject {

    private String id;
    private String title;
    private String subTitle;
    private String whereClause;
    private String tags;

    public SuggestionObject(String id, String title, String subTitle, String whereClause, String tags) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.whereClause = whereClause;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
