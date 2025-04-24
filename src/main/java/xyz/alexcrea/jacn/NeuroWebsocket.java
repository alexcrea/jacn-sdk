package xyz.alexcrea.jacn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.listener.NeuroSDKListener;
import xyz.alexcrea.jacn.sdk.NeuroSDK;
import xyz.alexcrea.jacn.sdk.NeuroSDKBuilder;
import xyz.alexcrea.jacn.sdk.NeuroSDKState;
import xyz.alexcrea.jacn.sdk.proposed.ProposedFeature;

import java.net.ConnectException;
import java.net.URI;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The websocket for the Neuro sdk api
 */
@ApiStatus.Internal
public class NeuroWebsocket extends WebSocketClient {

    private final static Logger logger = LoggerFactory.getLogger(NeuroWebsocket.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final @NotNull NeuroSDK parent;

    private final @NotNull List<NeuroSDKListener> listeners;

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

        this.listeners = new ArrayList<>(builder.getListeners());

        this.onWebsocketOpen = builder.getOnConnect();
        this.onWebsocketOpenInternal = onWebsocketOpenInternal;

        this.onWebsocketClose = builder.getOnClose();
        this.onWebsocketCloseInternal = onWebsocketCloseInternal;

        this.onConnectErrorInternal = onConnectErrorInternal;
        this.onWebsocketError = builder.getOnError();

        // Set the sdk to listeners
        for (NeuroSDKListener listener : this.listeners) {
            if (!listener.setNeuroSDK(parent)) {
                throw new RuntimeException("Could not set sdk to a listener");
            }
        }
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        onWebsocketOpenInternal.accept(serverHandshake);

        onWebsocketOpen.accept(serverHandshake);

        for (NeuroSDKListener listener : listeners) {
            try {
                listener.onConnect(serverHandshake);
            } catch (Exception e) {
                throw new RuntimeException("Caught an exception executing on open for listener", e);
            }
        }
    }

    private void actionExecuteFailed(@NotNull ActionRequest request, @Nullable String reason, @Nullable Exception e) {
        StringBuilder report = new StringBuilder("Could not execute action request ").append(request.from().getName());
        if (reason != null) {
            report.append(": ").append(reason);
        }

        logger.error(report.toString(), e);

        ActionResult failed;
        if (request.from().isReportFailure()) {
            if (e != null) {
                if (reason == null) {
                    report.append(": ");
                } else {
                    report.append("\n ");
                }

                report.append(e.getMessage());
            }

            failed = new ActionResult(request.id(), false, report.toString());
        } else {
            failed = new ActionResult(request.id(), true, "");
        }

        sendResult(failed);
    }

    private void executeActionRequest(@NotNull ActionRequest request) {
        // Do Action and get result
        ActionResult result = null;
        boolean fromCallback = false;
        try {
            Function<@NotNull ActionRequest, @Nullable ActionResult> onResult = request.from().getOnResult();
            if (onResult != null) {
                result = request.from().getOnResult().apply(request);
                fromCallback = true;
            }

        } catch (Exception e) {
            actionExecuteFailed(request, "Exception thrown while executing the action request on the action's callback", e);
        }

        NeuroSDKListener resultingListener = null;
        if (!fromCallback) {
            // Execute on listeners
            for (NeuroSDKListener listener : listeners) {
                try {
                    result = listener.onActionRequest(request, this.parent);
                    if (result != null) {
                        resultingListener = listener;
                        break;
                    }
                } catch (Exception e) {
                    actionExecuteFailed(request, "Exception thrown while executing the request on a listener", e);
                }
            }
        }

        if (result == null) {
            actionExecuteFailed(request, "All of the action request listeners and the action's callback returned null", null);
            return;
        }

        // Send result
        sendResult(result);

        // Do after result
        try {
            if (fromCallback) {
                BiConsumer<ActionRequest, ActionResult> onResult = request.from().getAfterResult();
                if (onResult != null) {
                    onResult.accept(request, result);
                }
            } else {
                resultingListener.onAfterResult(request, result, this.parent);
            }

        } catch (Exception e) {
            logger.error("Exception thrown while executing the after result for an action request from action {}", request.from().getName(), e);
        }

    }

    @Override
    public void onMessage(String message) {
        try {
            HashMap<?, ?> map = objectMapper.readValue(message, HashMap.class);
            if (map == null) {
                sendInvalidFeedbackUnknownID(message, "Could not parse json: " +
                        "\nmessage: " + message, null);
                return;
            }

            Object commandObj = map.get("command");
            if (commandObj == null) {
                sendInvalidFeedbackUnknownID(message, "Could not find command: " +
                        "\nmessage: " + message, null);
                return;
            }

            handleCommand(message, commandObj.toString(), map);
        } catch (JsonProcessingException e) {
            sendInvalidFeedbackUnknownID(message, "Could not parse json. it is malformed. message: " + message, e);
        }
    }

    private void handleCommand(@NotNull String message, @NotNull String command, @NotNull HashMap<?, ?> map) {
        switch (command) {
            case "action":
                handleIngoingAction(message, map);
                break;
            case "actions/reregister_all":
                handleReRegister();
                break;
            case "shutdown/graceful":
                //TODO
                break;
            case "shutdown/immediate":
                //TODO
                break;
            default:
                logger.error("Unknown incoming command: {}", command);
        }
    }

    private void handleIngoingAction(@NotNull String message, @NotNull HashMap<?, ?> map) {
        Object dataObj = map.get("data");
        if (!(dataObj instanceof Map<?, ?> data)) {
            sendInvalidFeedbackUnknownID(message, "Could not find command data" +
                    "\nmessage: " + message, null);
            return;
        }

        ActionRequest request = findRequest(data, message);
        if (request == null) return;

        executeActionRequest(request);
    }

    private void sendInvalidFeedbackUnknownID(@NotNull String message, @NotNull String errorToSend, @Nullable Exception e) {
        String id = findID(message);
        if (id == null) {
            logger.error(errorToSend, e);
            return;
        }

        sendInvalidFeedbackKnownID(id, errorToSend, e);
    }

    private void sendInvalidFeedbackKnownID(@NotNull String id, @NotNull String errorToSend, @Nullable Exception e) {
        ActionResult failedResult = new ActionResult(id, false, errorToSend);

        if (!sendResult(failedResult)) {
            String reason = "Could not send result feedback: " +
                    "\n" + errorToSend;

            logger.error(reason, e);
            close(CloseFrame.PROTOCOL_ERROR, reason);
        }
    }

    @Nullable
    private ActionRequest findRequest(@NotNull Map<?, ?> map, @NotNull String message) {
        Object idObj = map.get("id");
        if (idObj == null) {
            sendInvalidFeedbackUnknownID(message, "Could not find the id field on the message" +
                    "\nmessage: " + message, null);
            return null;
        }
        String id = idObj.toString();

        Object nameObj = map.get("name");
        if (nameObj == null) {
            sendInvalidFeedbackKnownID(id, "Could not find the action name on the message" +
                    "\nmessage: " + message, null);
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

        // Get data if exist
        JsonNode dataNode;
        if (action.getSchema() != null) {
            Object data = map.get("data");
            if (data == null) {
                sendResult(new ActionResult(id, false, "Please provide a JSON schema"));
                return null;
            }

            try {
                dataNode = objectMapper.readTree(data.toString());
            } catch (JsonProcessingException e) {
                sendResult(new ActionResult(id, false, "Please provide a well formated JSON schema"));
                return null;
            }

            // validate schema
            Set<ValidationMessage> validations = action.getSchema().validate(dataNode);
            if (!validations.isEmpty()) {
                StringBuilder stb = new StringBuilder("Provided schema is not valid:");
                for (ValidationMessage validation : validations) {
                    stb.append("\n").append(validation.getMessage());
                }

                sendResult(new ActionResult(id, false, stb.toString()));
                return null;
            }

        } else {
            dataNode = null;
        }

        return new ActionRequest(action, id, dataNode);
    }

    @Nullable
    private String findID(String message) {
        // Try to find the id in a very poor way
        int startIndex = message.indexOf("\"id\":");
        if (startIndex == -1) {
            logger.error("Did not find id field. message: {}", message);
            close(CloseFrame.PROTOCOL_ERROR, "Did not find id field");
            return null;
        }
        startIndex = message.indexOf("\"", startIndex + 5) + 1;
        if (startIndex == 0) {
            logger.error("Did not find start of id field. message: {}", message);
            close(CloseFrame.PROTOCOL_ERROR, "Did not find start of id field");
            return null;
        }

        int badEndIndex = message.indexOf("\"", startIndex);
        if (badEndIndex == -1) {
            logger.error("Did not find end of id field. message: {}", message);
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

    private void handleReRegister() {
        if (!parent.isEnable(ProposedFeature.RE_REGISTER_ALL)) {
            logger.error("""
                    Received re register all command even with re-register feature flag not present.
                    Are you using randy or similar ? if so that a normal behavior.
                    else you should probably either update the SDK or enable this ProposedFlag""");
            return;
        }

        if (!parent.reRegisterActions()) {
            logger.error("Could not re register the actions");
        }
    }

    @Override
    public void onClose(int closeCode, String reason, boolean remote) {
        onWebsocketCloseInternal.accept(reason);

        onWebsocketClose.accept(reason);

        for (NeuroSDKListener listener : listeners) {
            try {
                listener.onClose(reason, remote, closeCode);
            } catch (Exception e) {
                throw new RuntimeException("Caught an exception executing on close for listener", e);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        if (e instanceof ConnectException connectException) {
            onConnectErrorInternal.accept(connectException);
        }

        onWebsocketError.accept(e);

        for (NeuroSDKListener listener : listeners) {
            try {
                listener.onError(e);
            } catch (Exception e2) {
                throw new RuntimeException("Caught an exception executing on error for listener", e2);
            }
        }
    }

    public boolean sendCommand(@NotNull String command, @Nullable Map<String, Object> data, boolean bypassConnected) {
        if (!bypassConnected && !NeuroSDKState.CONNECTED.equals(this.parent.getState())) return false;

        HashMap<String, Object> toSendMap = new HashMap<>();
        toSendMap.put("command", command);
        toSendMap.put("game", this.parent.getGameName());
        if (data != null) {
            toSendMap.put("data", data);
        }

        try {
            send(objectMapper.writeValueAsString(toSendMap));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean sendCommand(@NotNull String command, @Nullable Map<String, Object> data) {
        return sendCommand(command, data, false);
    }

}
