package xyz.alexcrea.jacn.listener;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.sdk.NeuroSDK;
import xyz.alexcrea.jacn.sdk.NeuroSDKInterface;
import xyz.alexcrea.jacn.sdk.NeuroSDKState;
import xyz.alexcrea.jacn.sdk.proposed.ProposedFeature;

import java.net.ConnectException;
import java.util.List;

/**
 * An abstract implementation of a Neuro SDK Listener that implement basic sdk storage
 * and also allow to call sdk function from here
 */
@SuppressWarnings({"unused"})
public abstract class AbstractSDKListener implements NeuroSDKListener, NeuroSDKInterface {

    private NeuroSDK sdk;

    @Override
    @ApiStatus.Internal
    public final boolean setNeuroSDK(@NotNull NeuroSDK sdk) {
        if (this.sdk != null) return false;

        this.sdk = sdk;
        return true;
    }

    @Override
    public final @Nullable NeuroSDK getSDK() {
        return this.sdk;
    }

    @Override
    public @Nullable ActionResult onActionRequest(@NotNull ActionRequest request, @NotNull NeuroSDK sdk) {
        // Empty but let it be overridden
        return null;
    }

    @Override
    public void onAfterResult(@NotNull ActionRequest request, @NotNull ActionResult result, @NotNull NeuroSDK sdk) {
        // Empty but let it be overridden
    }

    /**
     * Called when a connection handshake was successfully done with a websocket.
     * This function is, probably, only called from the websocket thread.
     *
     * @param handshake the handshake of this connection
     * @param sdk       the sdk instance related to this connection
     */
    public void onConnectSuccess(@NotNull ServerHandshake handshake, @NotNull NeuroSDK sdk) {

    }

    /**
     * Called when a connection handshake was unsuccessfully done with a websocket.
     * Please know it is not the only way a connection can fail. it is recommended to also implement {@link #onConnectError}
     * This function is, probably, only called from the websocket thread.
     *
     * @param handshake the handshake of this connection
     * @param sdk       the sdk instance related to this connection
     */
    public void onConnectFailed(@NotNull ServerHandshake handshake, @NotNull NeuroSDK sdk) {

    }

    /**
     * Called when the websocket got closed for any reason.
     * There is no guaranty of what thread call this function.
     *
     * @param reason the reason of why the SDK got closed
     * @param remote if the connection was close by the remote connection
     * @param code   the close code
     * @param sdk    the sdk instance related to this connection
     */
    public void onClose(@NotNull String reason, boolean remote, int code, @NotNull NeuroSDK sdk) {

    }

    /**
     * Called when a websocket exception has happened.
     * This function is, probably, only called from the websocket thread.
     *
     * @param exception the exception
     * @param sdk       the sdk instance related to this connection
     */
    public void onError(@NotNull Exception exception, @NotNull NeuroSDK sdk) {

    }

    /**
     * Called when we could not connect to the websocket.
     * Please know it is not the only way a connection can fail. it is recommended to also implement {@link #onConnectFailed}
     * This function is, probably, only called from the websocket thread.
     *
     * @param exception the exception
     * @param sdk       the sdk instance related to this connection
     */
    public void onConnectError(@NotNull ConnectException exception, @NotNull NeuroSDK sdk) {

    }

    @Override
    public final void onConnect(@NotNull ServerHandshake handshake) {
        if (this.sdk == null) return;

        int status = handshake.getHttpStatus();
        if ((status < 200 || status >= 300) && (status != 101)) {
            onConnectFailed(handshake, this.sdk);
            return;
        }

        onConnectSuccess(handshake, this.sdk);
    }

    @Override
    public final void onClose(@NotNull String reason, boolean remote, int code) {
        if (this.sdk == null) return;

        onClose(reason, remote, code, this.sdk);
    }

    @Override
    public final void onError(@NotNull Exception exception) {
        if (this.sdk == null) return;

        onError(exception, this.sdk);
        if (exception instanceof ConnectException connectException) {
            onConnectError(connectException, this.sdk);
        }
    }

    @Override
    public final @NotNull String getGameName() {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getGameName();
    }

    @Override
    public final @NotNull NeuroSDKState getState() {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getState();
    }

    @Override
    public final @Nullable Action getAction(@NotNull String name) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getAction(name);
    }

    @Override
    public final @NotNull List<Action> getActions(@NotNull List<String> names) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getActions(names);
    }

    @Override
    public final @NotNull List<Action> getActions(@NotNull String... names) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getActions(names);
    }

    @Override
    public final boolean sendContext(@NotNull String message, boolean silent) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.sendContext(message, silent);
    }

    @Override
    public final boolean registerActions(List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.registerActions(actions);
    }

    @Override
    public final boolean registerActions(@NotNull Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.registerActions(actions);
    }

    @Override
    public final boolean unregisterActions(List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.unregisterActions(actions);
    }

    @Override
    public final boolean unregisterActions(@NotNull Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.unregisterActions(actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, @NotNull String query, boolean ephemeral, @NotNull List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, @NotNull String query, boolean ephemeral, @NotNull Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, @NotNull String query, @NotNull List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, @NotNull String query, @NotNull Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, actions);
    }

    @Override
    public final boolean forceActions(@NotNull String query, boolean ephemeral, @NotNull List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(@NotNull String query, boolean ephemeral, @NotNull Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(@NotNull String query, @NotNull List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, actions);
    }

    @Override
    public final boolean forceActions(@NotNull String query, @NotNull Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, actions);
    }

    @Override
    public final List<Action> getRegisteredActions() {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getRegisteredActions();
    }

    @Override
    public boolean isEnable(@NotNull ProposedFeature feature) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.isEnable(feature);
    }

}
