package com.galosanchez.appuberclone.domain;

public class Client {

    private String key;
    private String name;
    private String email;
    private String image;

    public Client() {
    }

    public Client(String key, String name, String email) {
        this.key = key;
        this.name = name;
        this.email = email;
    }

    public Client(String key, String name, String email, String image) {
        this.key = key;
        this.name = name;
        this.email = email;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
