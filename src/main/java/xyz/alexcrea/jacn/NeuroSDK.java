package xyz.alexcrea.jacn;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.error.ActionException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * The instance used to register actions
 */
@SuppressWarnings({"unused"})
public class NeuroSDK {

    private @NotNull NeuroSDKState state;
    private final @NotNull NeuroWebsocket websocket;

    private final List<Action> actionsToRegisterOnConnect;

    /**
     * Create a neuro sdk via a builder
     * @param builder the builder to base from
     */
    public NeuroSDK(NeuroSDKBuilder builder) {
        this.state = NeuroSDKState.CONNECTING;
        this.actionsToRegisterOnConnect = new ArrayList<>(builder.getActions());

        URI uri = URI.create("ws://" + builder.getAddress() + ":" + builder.getPort());
        this.websocket = new NeuroWebsocket(uri, this, builder, this::onConnect, this::onClose);
    }

    private void onConnect(@NotNull ServerHandshake handshake){
        if(handshake.getHttpStatus() < 200 || handshake.getHttpStatus() >= 300){
            this.state = NeuroSDKState.ERROR;
            return;
        }
        // register the startup actions
        for (Action action : actionsToRegisterOnConnect) {
            try {
                registerAction(action);
            } catch (ActionException e) {
                e.printStackTrace();
            }
        }

        // Finally, set the state to connected
        this.state = NeuroSDKState.CONNECTED;
    }

    private void onClose(String s) {
        if(this.state == NeuroSDKState.ERROR) return;
        this.state = NeuroSDKState.CLOSED;
    }


    /**
     * Get the current state of the neuro sdk
     * @return the neuro sdk state
     */
    public NeuroSDKState getState() {
        return state;
    }

    /**
     * TODO the javadoc
     * @param action
     * @throws ActionException
     */
    public void registerAction(Action action) throws ActionException {
        //TODO

    }

    /**
     * TODO the javadoc
     * @param action
     * @throws ActionException
     */
    public void unregisterAction(Action action) throws ActionException {
        //TODO

    }

}
