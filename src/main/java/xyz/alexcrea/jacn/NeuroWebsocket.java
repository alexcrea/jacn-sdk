package xyz.alexcrea.jacn;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;

import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The websocket for the Neuro sdk api
 */
@ApiStatus.Internal
public class NeuroWebsocket extends WebSocketClient {

    private static final Gson gson = new Gson();

    private final @NotNull NeuroSDK parent;

    private final @NotNull Consumer<ServerHandshake> onWebsocketOpen;
    private final @NotNull Consumer<ServerHandshake> onWebsocketOpenInternal;

    private final @NotNull Consumer<String> onWebsocketClose;
    private final @NotNull Consumer<String> onWebsocketCloseInternal;

    private final @NotNull Consumer<ConnectException> onConnectErrorInternal;
    private final @NotNull Consumer<Exception> onWebsocketError;

    public NeuroWebsocket(@NotNull URI serverUri, @NotNull NeuroSDK parent, @NotNull NeuroSDKBuilder builder,
                          @NotNull Consumer<ServerHandshake> onWebsocketOpenInternal,
                          @NotNull Consumer<String> onWebsocketCloseInternal,
                          @NotNull Consumer<ConnectException> onConnectErrorInternal) {
        super(serverUri);
        this.parent = parent;

        this.onWebsocketOpen = builder.getOnConnect();
        this.onWebsocketOpenInternal = onWebsocketOpenInternal;

        this.onWebsocketClose = builder.getOnClose();
        this.onWebsocketCloseInternal = onWebsocketCloseInternal;

        this.onConnectErrorInternal = onConnectErrorInternal;
        this.onWebsocketError = builder.getOnError();
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        onWebsocketOpenInternal.accept(serverHandshake);

        onWebsocketOpen.accept(serverHandshake);
    }

    private void actionExecuteFailed(@NotNull ActionRequest request, @NotNull String reason) {
        String report = "Could not execute action request " + request.from().getName() + ": " + reason;
        System.err.println(report);

        ActionResult failed;
        if (request.from().isReportFailure()) {
            failed = new ActionResult(request.id(), false, report);
        } else {
            failed = new ActionResult(request.id(), true, "");
        }

        sendResult(failed);
    }

    private void executeActionRequest(@NotNull ActionRequest request) {
        // Do Action and get result
        ActionResult result;
        try {
            result = request.from().getOnResult().apply(request);
            if (result == null) {
                actionExecuteFailed(request, "result is returned null");
                return;
            }

        } catch (Exception e) {
            actionExecuteFailed(request, "Exception thrown while executing the request: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Send result
        sendResult(result);

        // Do after result
        try {
            request.from().getAfterResult().accept(request, result);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessage(String message) {
        try {
            HashMap<?, ?> map = gson.fromJson(message, HashMap.class);
            if (map == null) {
                sendInvalidFeedbackUnknownID(message, "Could not parse json: " +
                        "\nmessage: " + message);
                return;
            }

            Object commandObj = map.get("command");
            if (commandObj == null) {
                sendInvalidFeedbackUnknownID(message, "Could not find command: " +
                        "\nmessage: " + message);
                return;
            }

            String command = commandObj.toString();
            if (!command.contentEquals("action")) {
                sendInvalidFeedbackUnknownID(message, "Invalid command. only accept action command." +
                        "\nmessage: " + message);
                return;
            }

            Object dataObj = map.get("data");
            if (!(dataObj instanceof Map<?, ?> data)) {
                sendInvalidFeedbackUnknownID(message, "Could not find command data" +
                        "\nmessage: " + message);
                return;
            }

            ActionRequest request = findRequest(data, message);
            if (request == null) return;

            executeActionRequest(request);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();

            sendInvalidFeedbackUnknownID(message, "Could not parse json. it is malformed: " + e.getMessage() +
                    "\nmessage: " + message);
        }
    }

    private void sendInvalidFeedbackUnknownID(@NotNull String message, String errorToSend) {
        String id = findID(message);
        if (id == null) return;
        sendInvalidFeedbackKnownID(id, errorToSend);
    }

    private void sendInvalidFeedbackKnownID(@NotNull String id, String errorToSend) {
        ActionResult failedResult = new ActionResult(id, false, errorToSend);

        if (!sendResult(failedResult)) {
            System.err.println("Could not send result feedback: " +
                    "\n" + errorToSend);
            close(CloseFrame.PROTOCOL_ERROR, "Could not send result feedback: " +
                    "\n" + errorToSend);
        }
    }

    @Nullable
    private ActionRequest findRequest(@NotNull Map<?, ?> map, @NotNull String message) {
        Object idObj = map.get("id");
        if (idObj == null) {
            sendInvalidFeedbackUnknownID(message, "Could not find the id field on the message" +
                    "\nmessage: " + message);
            return null;
        }
        String id = idObj.toString();

        Object nameObj = map.get("name");
        if (nameObj == null) {
            sendInvalidFeedbackKnownID(id, "Could not find the action name on the message" +
                    "\nmessage: " + message);
            return null;
        }
        String name = nameObj.toString();

        // Try to find the action related to the message
        Action action = parent.getAction(name);
        if (action == null) {
            // This is kind of complicated:
            // We know we can't find the action (it is not registered on our side.)
            // But we can't report as failure as the Neuro side may retry if the action was force
            // So we report a success with no message just in case to avoid an infinite loop
            sendResult(new ActionResult(id, true, ""));
            return null;
        }

        // TODO json schema data (json schema response. nullable)
        Object data = map.get("data");
        String datatemp = data == null ? null : data.toString();


        return new ActionRequest(action, name, datatemp);
    }

    @Nullable
    private String findID(String message) {
        // Try to find the id in a very poor way
        int startIndex = message.indexOf("\"id\":");
        if (startIndex == -1) {
            System.err.println("Did not find id field" +
                    "\nmessage: " + message);
            close(CloseFrame.PROTOCOL_ERROR, "Did not find id field");
            return null;
        }
        startIndex = message.indexOf("\"", startIndex + 5) + 1;
        if (startIndex == 0) {
            System.err.println("Did not find start of id field" +
                    "\nmessage: " + message);
            close(CloseFrame.PROTOCOL_ERROR, "Did not find start of id field");
            return null;
        }

        int badEndIndex = message.indexOf("\"", startIndex);
        if (badEndIndex == -1) {
            System.err.println("Did not find end of id field" +
                    "\nmessage: " + message);
            close(CloseFrame.PROTOCOL_ERROR, "Did not find end of id field");
            return null;
        }

        return message.substring(startIndex, badEndIndex);
    }

    public boolean sendResult(@NotNull ActionResult result) {
        Map<String, Object> toSend = new HashMap<>();
        toSend.put("id", result.id());
        toSend.put("success", result.success());
        if (result.message() != null) toSend.put("message", result.message());

        return sendCommand("action/result", toSend);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        onWebsocketCloseInternal.accept(s);

        onWebsocketClose.accept(s);
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof ConnectException connectException) {
            onConnectErrorInternal.accept(connectException);
        }

        onWebsocketError.accept(e);
    }

    public boolean sendCommand(@NotNull String command, @Nullable Map<String, Object> data, boolean bypassConnected) {
        if (!bypassConnected && !NeuroSDKState.CONNECTED.equals(this.parent.getState())) return false;

        HashMap<String, Object> toSendMap = new HashMap<>();
        toSendMap.put("command", command);
        toSendMap.put("game", this.parent.getGameName());
        if (data != null) {
            toSendMap.put("data", data);
        }

        send(gson.toJson(toSendMap));
        return true;
    }

    public boolean sendCommand(@NotNull String command, @Nullable Map<String, Object> data) {
        return sendCommand(command, data, false);
    }

}
