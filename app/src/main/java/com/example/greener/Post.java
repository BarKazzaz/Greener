package com.example.greener;

import java.util.HashMap;
import java.util.Map;

public class Post {
    public String date, id, postImage, postContent, time, username, profilePic;

    public Post() {

    }

    public Post(String date, String id, String postImage, String postContent, String time, String user, String profilePicUrl) {
        this.date = date;
        this.id = id;
        this.postImage = postImage;
        this.postContent = postContent;
        this.time = time;
        this.username = user;
        this.profilePic = profilePicUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, Object> toMap() {
        HashMap map = new HashMap();
        map.put("id", this.id);
        map.put("date", this.date);
        map.put("postImage", this.postImage);
        map.put("postContent", this.postContent);
        map.put("time", this.time);
        map.put("username", this.username);
        map.put("profilePic", this.profilePic);

        return map;
    }
}
