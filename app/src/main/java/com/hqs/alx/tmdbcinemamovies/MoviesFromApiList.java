package com.hqs.alx.tmdbcinemamovies;

/**
 * Created by Alex on 16/11/2017.
 */


// a class for movie list
public class MoviesFromApiList {

    private String title;
    private String description;
    private String image;
    private String dateReleased;
    private String id;
    private String bigImage;
    private String votesAverage;
    private String votesTotal;
    private boolean isAmovieOrSeries;
    private boolean isChecked = false;


    // a constractor - each movie must have these variables inserted
    public MoviesFromApiList(String title, String description, String image, String dateReleased, String id, boolean isChecked, String bigImage, String votesAverage, String votesTotal, boolean isAmovieOrSeries) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.dateReleased = dateReleased;
        this.id = id;
        this.isChecked = isChecked;
        this.bigImage = bigImage;
        this.votesAverage = votesAverage;
        this.votesTotal = votesTotal;
        this.isAmovieOrSeries = isAmovieOrSeries;
    }

    //getters for the values of each item
    public boolean isAmovieOrSeries() {
        return isAmovieOrSeries;
    }

    public String getVotesAverage() {
        return votesAverage;
    }

    public String getVotesTotal() {
        return votesTotal;
    }

    public String getBigImage() {
        return bigImage;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public String getDateReleased() {
        return dateReleased;
    }

    public String getId() {
        return id;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
