package com.vimalselvam.stf;

import com.vimalselvam.DeviceApi;
import com.vimalselvam.STFService;
import io.appium.java_client.MobileBy;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.remote.MobileCapabilityType;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class AndroidTest {
    private static final String STF_SERVICE_URL = "http://localhost:7100";  // Change this URL
    private static final String ACCESS_TOKEN = "ba3074f108a44ce39192d37b514a1217ec0b4f77baab423b97d273dc5369be4f";  // Change this access token

    private AndroidDriver androidDriver;
    private String deviceSerial;
    private AppiumDriverLocalService service;
    private DeviceApi deviceApi;

    @Factory(dataProvider = "parallelDp")
    public AndroidTest(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    private void createAppiumService() {
        this.service = AppiumDriverLocalService.buildDefaultService();
        this.service.start();
    }

    private void connectToStfDevice() throws MalformedURLException, URISyntaxException {
        STFService stfService = new STFService(STF_SERVICE_URL,
                ACCESS_TOKEN);
        this.deviceApi = new DeviceApi(stfService);
        this.deviceApi.connectDevice(this.deviceSerial);
    }

    @BeforeClass
    public void setup() throws MalformedURLException, URISyntaxException {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(MobileCapabilityType.DEVICE_NAME, "ANDROID");
        desiredCapabilities.setCapability(MobileCapabilityType.PLATFORM_NAME, "ANDROID");
        desiredCapabilities.setCapability("udid", this.deviceSerial);
        desiredCapabilities.setCapability(MobileCapabilityType.APP,
                new File("src/test/resources/ApiDemos-debug.apk").getAbsolutePath());

        connectToStfDevice();
        createAppiumService();
        androidDriver = new AndroidDriver(this.service.getUrl(), desiredCapabilities);
    }

    @Test
    public void currentActivityTest() throws InterruptedException {
        Assert.assertEquals(androidDriver.currentActivity(), ".ApiDemos", "Activity not match");
    }

    @Test(dependsOnMethods = {"currentActivityTest"})
    public void scrollingToSubElement() {
        androidDriver.findElementByAccessibilityId("Views").click();
        AndroidElement list = (AndroidElement) androidDriver.findElement(By.id("android:id/list"));
        MobileElement radioGroup = list
                .findElement(MobileBy
                        .AndroidUIAutomator("new UiScrollable(new UiSelector()).scrollIntoView("
                                + "new UiSelector().text(\"Radio Group\"));"));
        Assert.assertNotNull(radioGroup.getLocation());
    }

    @AfterClass
    public void tearDown() {
        if (androidDriver != null) {
            androidDriver.quit();
        }

        if (this.service.isRunning()) {
            service.stop();
            this.deviceApi.releaseDevice(this.deviceSerial);
        }
    }

    @DataProvider
    public Object[][] parallelDp() {
        return new Object[][] {
            {"3JU5T18331000564"},   // Change the device serial
            {"6LB6KNEADMVKPF9S"},    // Change the device serial
        };
    }
}
