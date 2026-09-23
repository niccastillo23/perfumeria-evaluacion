package com.perfumeria.profile;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "profiles", schema = "profile_schema")
public class Profile {

    @Id
    private String username;
    private String fullName;
    private String bio;
    private String memberSince;
    
    @Column(columnDefinition = "text")
    private String preferencesJson;

    public Profile() {}

    public Profile(String username, String fullName, String bio, String memberSince, String preferencesJson) {
        this.username = username;
        this.fullName = fullName;
        this.bio = bio;
        this.memberSince = memberSince;
        this.preferencesJson = preferencesJson;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getMemberSince() { return memberSince; }
    public void setMemberSince(String memberSince) { this.memberSince = memberSince; }
    public String getPreferencesJson() { return preferencesJson; }
    public void setPreferencesJson(String preferencesJson) { this.preferencesJson = preferencesJson; }
}
