package com.playposse.egoeater.backend.firebase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * A class with utility methods for dealing with Firebase.
 */
public class FirebaseServerAction {

    private static final Logger log = Logger.getLogger(FirebaseServerAction.class.getName());

    public enum FirebasePriority {
        normal,
        high,
    }

    public static final int MAX_FIREBASE_MESSAGE_SIZE = 3_500;

    private static final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String APP_ID = "AAAAAwmSP4o:APA91bEHeJhtauN9ZS0SUyB-OwmJiN2IYCUJYjehOc4grYnTddA-2oUu-USewx-ntJEhC802UK1qzvaKNL0-Yis-ZOxUHyRm8MrxWrV6OPt8_8i0dW20y6D3k9_oopR0vwBIe5OpCQc7";

    private static final String ALL_DEVICES_DESTINATION = "/topics/allDevices";

    protected static final String TYPE_KEY = "type";

    protected static String sendMessageToAllDevices(
            JSONObject data,
            FirebasePriority priority,
            @Nullable String collapseKey)
            throws IOException {
        return sendMessageToDevice(ALL_DEVICES_DESTINATION, data, priority, collapseKey);
    }

    protected static String sendMessageToDevice(
            String firebaseToken,
            JSONObject data)
            throws IOException {

        return sendMessageToDevice(firebaseToken, data, FirebasePriority.normal);
    }

    protected static String sendMessageToDevice(
            String firebaseToken,
            JSONObject data,
            FirebasePriority priority)
            throws IOException {

        return sendMessageToDevice(firebaseToken, data, priority, null);
    }

    private static String sendMessageToDevice(
            String firebaseToken,
            JSONObject data,
            FirebasePriority priority,
            @Nullable String collapseKey)
            throws IOException {

        JSONObject rootNode = new JSONObject();
        rootNode.put("to", firebaseToken);
        rootNode.put("data", data);
        rootNode.put("priority", priority.name());
        if (collapseKey != null) {
            rootNode.put("collapse_key", collapseKey);
        }

        log.info("Firebase payload: " + rootNode.toString());
        String response = sendMessage(rootNode.toString());
        log.info("Firebase response: " + response);
        return response;
    }

    private static String sendMessage(String httpPayload) throws IOException {
        URL url = new URL(FIREBASE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true);

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", "key=" + APP_ID);
        connection.setRequestMethod("POST");

        OutputStream output = connection.getOutputStream();
        output.write(httpPayload.getBytes("UTF-8"));
        output.close();

        int httpResult = connection.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK) {
            return readStream(connection.getInputStream());
        } else {
            String errorMsg = readStream(connection.getErrorStream());
            throw new IOException("Request to Firebase failed: "
                    + connection.getResponseCode() + " "
                    + connection.getResponseMessage() + "\n"
                    + errorMsg);
        }
    }

    private static String readStream(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));

        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        reader.close();
        return sb.toString();
    }
}
