package io.mobeacon.sdk.model;

import com.google.gson.annotations.Expose;

/**
 * Created by maxulan on 19.07.15.
        */

public class SDKContext {
    @Expose
    private String sdkVersion;                  // mobeacon SDK version
    @Expose
    private String sdkInstallDateTime;          // SDK installation date & time in the format "yyyy-MM-dd HH:mm:ss"

    @Expose
    private String appKey;                      // application key from Mobeacon Web Console
    @Expose
    private String appBundle;                   // bundle(package name) eg. "com.company.myapp"
    @Expose
    private String appVersion;                  // app version for eg. "2.0.1"

    @Expose
    private String appInstallDateTime;          // App installation date & time in the format "yyyy-MM-dd HH:mm:ss"

    @Expose
    private String sessionStartDateTime;        // App session start date & time in the format "yyyy-MM-dd HH:mm:ss"

    @Expose
    private String OS;                          // hardcoded "Android"
    @Expose
    private String OSVersion;                   // host version  eg. "5.1"
    @Expose
    private String lang;                        // language  eg. "en"
    @Expose
    private String country;                     // country  eg. "UK"
    @Expose
    private String provider;                    // network provider eg. "Verizon"

    @Expose
    private String deviceManufacturer;          //LG, Samsung etc.
    @Expose
    private String deviceModel;                 //eg. Nexus
    @Expose
    private String deviceHardwareVersion;       //eg. 5
    @Expose
    private String devicePlatformId;            //Android ID
    @Expose
    private String deviceHardwareId;            //unique id of device (IMEI/MEID/Serial ID)
    @Expose
    private String deviceType;                  //Smartphone, Tablet

    @Expose
    private int screenWidth;                    //Physical width of the screen in pixels.
    @Expose
    private int screenHeight;                   //Physical height of the screen in pixels.
    @Expose
    private int screenPPI;                      //Screen size as pixels per linear inch.

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getSdkInstallDateTime() {
        return sdkInstallDateTime;
    }

    public void setSdkInstallDateTime(String sdkInstallDateTime) {
        this.sdkInstallDateTime = sdkInstallDateTime;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppBundle() {
        return appBundle;
    }

    public void setAppBundle(String appBundle) {
        this.appBundle = appBundle;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getSessionStartDateTime() {
        return sessionStartDateTime;
    }

    public void setSessionStartDateTime(String sessionStartDateTime) {
        this.sessionStartDateTime = sessionStartDateTime;
    }

    public String getOS() {
        return OS;
    }

    public void setOS(String OS) {
        this.OS = OS;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceHardwareVersion() {
        return deviceHardwareVersion;
    }

    public void setDeviceHardwareVersion(String deviceHardwareVersion) {
        this.deviceHardwareVersion = deviceHardwareVersion;
    }

    public String getDevicePlatformId() {
        return devicePlatformId;
    }

    public void setDevicePlatformId(String devicePlatformId) {
        this.devicePlatformId = devicePlatformId;
    }

    public String getDeviceHardwareId() {
        return deviceHardwareId;
    }

    public void setDeviceHardwareId(String deviceHardwareId) {
        this.deviceHardwareId = deviceHardwareId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getScreenPPI() {
        return screenPPI;
    }

    public void setScreenPPI(int screenPPI) {
        this.screenPPI = screenPPI;
    }

    public String getAppInstallDateTime() {
        return appInstallDateTime;
    }

    public void setAppInstallDateTime(String appInstallDateTime) {
        this.appInstallDateTime = appInstallDateTime;
    }
}
