package xyz.alexcrea.jacn;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.function.Consumer;

/**
 * The websocket for the neuro sdk.
 */
public class NeuroWebsocket extends WebSocketClient {

    private final @NotNull NeuroSDK parent;

    private final @NotNull Consumer<ServerHandshake> onWebsocketOpen;
    private final @NotNull Consumer<ServerHandshake> onWebsocketOpenInternal;

    private final @NotNull Consumer<String> onWebsocketClose;
    private final @NotNull Consumer<String> onWebsocketCloseInternal;

    private final @NotNull Consumer<Exception> onWebsocketError;

    public NeuroWebsocket(@NotNull URI serverUri, @NotNull NeuroSDK parent, @NotNull NeuroSDKBuilder builder,
                          @NotNull Consumer<ServerHandshake> onWebsocketOpenInternal,
                          @NotNull Consumer<String> onWebsocketCloseInternal) {
        super(serverUri);
        this.parent = parent;

        this.onWebsocketOpen = builder.getOnWebsocketOpen();
        this.onWebsocketOpenInternal = onWebsocketOpenInternal;

        this.onWebsocketClose = builder.getOnWebsocketClose();
        this.onWebsocketCloseInternal = onWebsocketCloseInternal;

        this.onWebsocketError = builder.getOnWebsocketError();
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
        onWebsocketError.accept(e);
    }
}
