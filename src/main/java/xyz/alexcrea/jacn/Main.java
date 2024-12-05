package xyz.alexcrea.jacn;

import com.google.gson.Gson;
import xyz.alexcrea.jacn.action.Action;

import java.util.HashMap;

// Only temporary for manual test.
// Test will get on test folder when randy is published & CI-able
public class Main {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        Action test = new Action("test", "a", result -> {
            System.out.println("temp");
            return null;
        });

        HashMap<?, ?> temp = gson.fromJson("{\"test\": {\"test\": \"test\"}}", HashMap.class);
        System.out.println(temp.get("test").getClass());

        NeuroSDK SDK = new NeuroSDKBuilder("Test 42")
                .addActionsOnConnect(test)
                .setOnConnect((handshake -> System.out.println("connected")))
                .setOnClose((reason) -> System.out.println("closed: " + reason))
                .setOnError((error) -> {
                    System.out.println("error running websocket");
                    error.printStackTrace();
                })
                .build();

        if (!SDK.unregisterActions(test)) System.out.println("sadge");
        if (!SDK.registerActions(test)) System.out.println("sadge");
    }

}