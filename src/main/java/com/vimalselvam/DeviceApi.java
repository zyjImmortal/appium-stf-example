package com.vimalselvam;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;

/**
 * This class provides the capability to connect or disconnect device.
 */
public class DeviceApi {
    private OkHttpClient client;
    private JsonParser jsonParser;
    private Gson gson;
    private List<String> devices;
    private STFService stfService;
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    public DeviceApi(STFService stfService) {
        this.client = new OkHttpClient();
        this.jsonParser = new JsonParser();
        this.stfService = stfService;
        this.gson = new Gson();
        this.devices = new ArrayList<String>();
    }

    public List<String> deviceList() {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "/devices")
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();
            JsonArray devicesObject = jsonObject.getAsJsonArray("devices");
            for (Iterator<JsonElement> it = devicesObject.iterator(); it.hasNext(); ) {
                JsonElement element = it.next();
                JsonObject device = element.getAsJsonObject();
                boolean present = device.get("present").getAsBoolean();
                boolean ready = device.get("ready").getAsBoolean();
                boolean using = device.get("using").getAsBoolean();
                LOGGER.info("Device Serial:" + device.get("serial").getAsString() + ",present:" + present + ",ready:" + ready + ",using:" + using);
                if (ready && !using && present) {
                    devices.add(device.get("serial").getAsString());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return devices;
    }

    public boolean connectDevice(String deviceSerial) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "devices/" + deviceSerial)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            JsonObject deviceObject = jsonObject.getAsJsonObject("device");
            boolean present = deviceObject.get("present").getAsBoolean();
            boolean ready = deviceObject.get("ready").getAsBoolean();
            boolean using = deviceObject.get("using").getAsBoolean();
            JsonElement ownerElement = deviceObject.get("owner");
            boolean owner = !(ownerElement instanceof JsonNull);

            if (!present || !ready || using || owner) {
                LOGGER.severe("Device is in use");
                return false;
            }

            return addDeviceToUser(deviceSerial);
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

    private boolean isDeviceFound(JsonObject jsonObject) {
        if (!jsonObject.get("success").getAsBoolean()) {
            LOGGER.severe("Device not found");
            return false;
        }
        return true;
    }

    private boolean addDeviceToUser(String deviceSerial) {
        RequestBody requestBody = RequestBody.create(JSON, "{\"serial\": \"" + deviceSerial + "\"}");
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "user/devices")
                .post(requestBody)
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            LOGGER.info("The device <" + deviceSerial + "> is locked successfully");
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

    public boolean releaseDevice(String deviceSerial) {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + stfService.getAuthToken())
                .url(stfService.getStfUrl() + "user/devices/" + deviceSerial)
                .delete()
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            JsonObject jsonObject = jsonParser.parse(response.body().string()).getAsJsonObject();

            if (!isDeviceFound(jsonObject)) {
                return false;
            }

            LOGGER.info("The device <" + deviceSerial + "> is released successfully");
            return true;
        } catch (IOException e) {
            throw new IllegalArgumentException("STF service is unreachable", e);
        }
    }

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        String STF_SERVICE_URL = "http://localhost:7100";  // Change this URL
        String ACCESS_TOKEN = "ba3074f108a44ce39192d37b514a1217ec0b4f77baab423b97d273dc5369be4f";
        STFService stfService = new STFService(STF_SERVICE_URL, ACCESS_TOKEN);
        DeviceApi deviceApi = new DeviceApi(stfService);
        deviceApi.deviceList();
    }
}
