package com.example.lenovossd.andoridriderapp.Model;

public class Rider {

    private String name,Email,Phone,Password,avatarUrl,rates;
    public Rider() {
        }


    public Rider(String name, String email, String phone, String password, String avatarUrl, String rates) {
        this.name = name;
        Email = email;
        Phone = phone;
        Password = password;
        this.avatarUrl = avatarUrl;
        this.rates = rates;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }
}
