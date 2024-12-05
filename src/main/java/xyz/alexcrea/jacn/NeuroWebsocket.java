package xyz.alexcrea.jacn;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.ConnectException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * The websocket for the neuro sdk.
 */
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

    @Override
    public void onMessage(String message) {
        //TODO important part lol
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
