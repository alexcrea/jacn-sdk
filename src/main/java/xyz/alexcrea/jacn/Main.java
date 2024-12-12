package xyz.alexcrea.jacn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.sdk.NeuroSDK;
import xyz.alexcrea.jacn.sdk.NeuroSDKBuilder;

import java.util.HashMap;

// Only temporary for manual test.
// Test will get on test folder when randy is published & CI-able
public class Main {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws JsonProcessingException {
        Action test = new Action("test", "a", result -> {
            System.out.println("temp");
            return null;
        });

        HashMap<?, ?> temp = objectMapper.readValue("{\"test\": {\"test\": \"test\"}}", HashMap.class);
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