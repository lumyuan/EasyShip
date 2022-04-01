package com.pointer.wave.easyship.pojo;

public class VersionBen {

    private String versionName;
    private String versionCode;
    private String updateContent;
    private String downloadUrl;

    public VersionBen() {
    }

    public VersionBen(String versionName, String versionCode, String updateContent) {
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.updateContent = updateContent;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public void setUpdateContent(String updateContent) {
        this.updateContent = updateContent;
    }
}
