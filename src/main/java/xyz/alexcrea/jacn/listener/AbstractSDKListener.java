package xyz.alexcrea.jacn.listener;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.sdk.ForceActionPriority;
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
@NotNullByDefault
public abstract class AbstractSDKListener implements NeuroSDKListener, NeuroSDKInterface {

    private @Nullable NeuroSDK sdk = null;

    @Override
    @ApiStatus.Internal
    public final boolean setNeuroSDK(NeuroSDK sdk) {
        if (this.sdk != null) return false;

        this.sdk = sdk;
        return true;
    }

    @Override
    public final @Nullable NeuroSDK getSDK() {
        return this.sdk;
    }

    @Override
    public @Nullable ActionResult onActionRequest(ActionRequest request, NeuroSDK sdk) {
        // Empty but let it be overridden
        return null;
    }

    @Override
    public void onAfterResult(ActionRequest request, ActionResult result, NeuroSDK sdk) {
        // Empty but let it be overridden
    }

    /**
     * Called when a connection handshake was successfully done with a websocket.
     * This function is, probably, only called from the websocket thread.
     *
     * @param handshake the handshake of this connection
     * @param sdk       the sdk instance related to this connection
     */
    public void onConnectSuccess(ServerHandshake handshake, NeuroSDK sdk) {

    }

    /**
     * Called when a connection handshake was unsuccessfully done with a websocket.
     * Please know it is not the only way a connection can fail. it is recommended to also implement {@link #onConnectError}
     * This function is, probably, only called from the websocket thread.
     *
     * @param handshake the handshake of this connection
     * @param sdk       the sdk instance related to this connection
     */
    public void onConnectFailed(ServerHandshake handshake, NeuroSDK sdk) {

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
    public void onClose(String reason, boolean remote, int code, NeuroSDK sdk) {

    }

    /**
     * Called when a websocket exception has happened.
     * This function is, probably, only called from the websocket thread.
     *
     * @param exception the exception
     * @param sdk       the sdk instance related to this connection
     */
    public void onError(Exception exception, NeuroSDK sdk) {

    }

    /**
     * Called when we could not connect to the websocket.
     * Please know it is not the only way a connection can fail. it is recommended to also implement {@link #onConnectFailed}
     * This function is, probably, only called from the websocket thread.
     *
     * @param exception the exception
     * @param sdk       the sdk instance related to this connection
     */
    public void onConnectError(ConnectException exception, NeuroSDK sdk) {

    }

    @Override
    public final void onConnect(ServerHandshake handshake) {
        if (this.sdk == null) return;

        int status = handshake.getHttpStatus();
        if ((status < 200 || status >= 300) && (status != 101)) {
            onConnectFailed(handshake, this.sdk);
            return;
        }

        onConnectSuccess(handshake, this.sdk);
    }

    @Override
    public final void onClose(String reason, boolean remote, int code) {
        if (this.sdk == null) return;

        onClose(reason, remote, code, this.sdk);
    }

    @Override
    public final void onError(Exception exception) {
        if (this.sdk == null) return;

        onError(exception, this.sdk);
        if (exception instanceof ConnectException connectException) {
            onConnectError(connectException, this.sdk);
        }
    }

    @Override
    public final String getGameName() {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getGameName();
    }

    @Override
    public final NeuroSDKState getState() {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getState();
    }

    @Override
    public final @Nullable Action getAction(String name) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getAction(name);
    }

    @Override
    public final List<Action> getActions(List<String> names) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getActions(names);
    }

    @Override
    public final List<Action> getActions(String ... names) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getActions(names);
    }

    @Override
    public final boolean sendContext(String message, boolean silent) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.sendContext(message, silent);
    }

    @Override
    public final boolean registerActions(List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.registerActions(actions);
    }

    @Override
    public final boolean registerActions(Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.registerActions(actions);
    }

    @Override
    public final boolean unregisterActions(List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.unregisterActions(actions);
    }

    @Override
    public final boolean unregisterActions(Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.unregisterActions(actions);
    }

    @Override
    public boolean forceActions(String query, List<Action> actions, ForceActionPriority priority) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, actions, priority);
    }

    @Override
    public boolean forceActions(@Nullable String state, String query, List<Action> actions, ForceActionPriority priority) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, actions, priority);
    }

    @Override
    public boolean forceActions(@Nullable String state, String query, boolean ephemeral, List<Action> actions, ForceActionPriority priority) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, ephemeral, actions, priority);
    }

    @Override
    public final boolean forceActions(@Nullable String state, String query, boolean ephemeral, List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, String query, boolean ephemeral, Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, String query, List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, actions);
    }

    @Override
    public final boolean forceActions(@Nullable String state, String query, Action... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(state, query, actions);
    }

    @Override
    public final boolean forceActions(String query, boolean ephemeral, List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(String query, boolean ephemeral, Action ... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, ephemeral, actions);
    }

    @Override
    public final boolean forceActions(String query, List<Action> actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, actions);
    }

    @Override
    public final boolean forceActions(String query, Action ... actions) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.forceActions(query, actions);
    }

    @Override
    public final List<Action> getRegisteredActions() {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.getRegisteredActions();
    }

    @Override
    public boolean isEnable(ProposedFeature feature) {
        if (sdk == null) throw new IllegalStateException("NeuroSDK not initialized");
        return sdk.isEnable(feature);
    }

}
