package com.vimalselvam.stf;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * @author zhouyajun
 * @date 2019/4/12
 */
public class WechatSpider {

    private AppiumDriverLocalService service;

    public AndroidDriver initDriver(){
        this.service = AppiumDriverLocalService.buildDefaultService();
        this.service.start();
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "ANDROID");
        capabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "ANDROID");
        capabilities.setCapability(MobileCapabilityType.APP, "");
        capabilities.setCapability(MobileCapabilityType.FULL_RESET, false);
        capabilities.setCapability(MobileCapabilityType.NO_RESET, true);
        capabilities.setCapability("appPackage", "com.tencent.mm");
        capabilities.setCapability("appActivity", ".ui.LauncherUI");

        capabilities.setCapability("recreateChromeDriverSessions", true);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("androidProcess", "com.tencent.mm:tools");
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        return new AndroidDriver(this.service.getUrl(), capabilities);
    }

    public static void main(String[] args) {
        WechatSpider spider = new WechatSpider();
        AndroidDriver driver = spider.initDriver();
    }
}
