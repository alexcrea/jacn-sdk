package xyz.alexcrea.jacn;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.error.WebsocketException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Create a new builder to create a neuro sdk instance
 */
@SuppressWarnings({"unused"})
public class NeuroSDKBuilder {

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final short DEFAULT_PORT = 42;

    private String address = DEFAULT_ADDRESS;
    private short port = DEFAULT_PORT;

    private Consumer<ServerHandshake> onWebsocketOpen;
    private Consumer<String> onWebsocketClose;
    private Consumer<Exception> onWebsocketError;

    private List<Action> actionList;

    /**
     * Create a new builder for
     * <p>
     * Default builder has localhost as address and 42 as port.
     * It also log error to system err on connect and print stacktrace on error.
     * No default action is prepared to be registered by default
     */
    public NeuroSDKBuilder() {
        this.onWebsocketOpen = serverHandshake -> {
            short status = serverHandshake.getHttpStatus();
            if (status < 200 || status >= 300) {
                System.err.println("Got error while running the websocket: "+
                        "(" + serverHandshake.getHttpStatus() + ") " + serverHandshake.getHttpStatusMessage());
            }

        };
        this.onWebsocketClose = string -> {};
        this.onWebsocketError = error -> {
            new WebsocketException("Got error while running the websocket: ", error).printStackTrace();
        };

        this.actionList = new ArrayList<>();
    }

    /**
     * Set the consumer to be executed on websocket connect.
     * Default is a consumer that, if the return code is not 200, print an error to system err.
     * <p>
     * Action will only be able to be registered after this even is triggered.
     * Excepted action that planned to be registered on open that will get registered just before this action is executed.
     * @param onWebsocketOpen the consumer to execute on open
     */
    public void setOnWebsocketOpen(@NotNull Consumer<ServerHandshake> onWebsocketOpen) {
        this.onWebsocketOpen = onWebsocketOpen;
    }

    /**
     * Set the consumer to be executed on websocket close
     * Default to do nothing
     * <p>
     * Action will not be able to get registered after this
     * @param onWebsocketClose the consumer to execute on open
     */
    public void setOnWebsocketClose(@NotNull Consumer<String> onWebsocketClose) {
        this.onWebsocketClose = onWebsocketClose;
    }

    /**
     * Get the consumer to be executed on websocket error.
     * Default to print stacktrace of the error.
     * @param onWebsocketError the consumer to execute on error
     */
    public void setOnWebsocketError(@NotNull Consumer<Exception> onWebsocketError) {
        this.onWebsocketError = onWebsocketError;
    }

    /**
     * Set the actions that will be registered if the websocket connected successfully.
     * @param actions the list of actions to get registered
     */
    public void setActionsOnConnect(@NotNull List<Action> actions) {
        this.actionList = new ArrayList<>(actions);
    }

    /**
     * Set the actions that will be registered if the websocket connected successfully.
     * @param actions the list of actions to get registered
     */
    public void setActionsOnConnect(@NotNull Action... actions) {
        setActionsOnConnect(List.of(actions));
    }

    /**
     * Add action to be registered if the websocket get connected successfully
     * @param actions the list of actions to get registered
     */
    public void addActionsOnConnect(@NotNull List<Action> actions) {
        this.actionList.addAll(actions);
    }

    /**
     * Add action to be registered if the websocket get connected successfully
     * @param actions the list of actions to get registered
     */
    public void addActionsOnConnect(@NotNull Action... actions) {
        addActionsOnConnect(List.of(actions));
    }

    /**
     * Set the webserver expected port
     *
     * @param port the webserver port
     */
    public void setPort(short port) {
        this.port = port;
    }

    /**
     * Set the expected websocket address
     *
     * @param address the websocket address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get the expected websocket address
     * @return the expected address
     */
    @NotNull
    public String getAddress() {
        return address;
    }

    /**
     * Set the webserver expected port
     *
     * @return the webserver port
     */
    public short getPort() {
        return port;
    }

    /**
     * Get the consumer to be executed on websocket connect.
     * Default is a consumer that, if the return code is not 200, print an error to system err.
     * <p>
     * Action will only be able to be registered after this even is triggered.
     * Excepted action that planned to be registered on open that will get registered just before this action is executed.
     * @return the consumer to execute on open
     */
    @NotNull
    public Consumer<ServerHandshake> getOnWebsocketOpen() {
        return onWebsocketOpen;
    }

    /**
     * Get the consumer to be executed on websocket close
     * Default to do nothing
     * <p>
     * Action will not be able to get registered after this
     * @return  the consumer to execute on open
     */
    @NotNull
    public Consumer<String> getOnWebsocketClose() {
        return onWebsocketClose;
    }

    /**
     * Get the consumer to be executed on websocket error.
     * Default to print stacktrace of the error.
     * @return  the consumer to execute on error
     */
    @NotNull
    public Consumer<Exception> getOnWebsocketError() {
        return onWebsocketError;
    }

    /**
     * List of action that will get registered on websocket connect
     * @return the list of actions
     */
    @NotNull
    public List<Action> getActions() {
        return actionList;
    }

    /**
     * Create and open a neuro sdk with the builder properties.
     * Will also try to connect to it.
     * @return the neuro sdk instance with websocket opening.
     */
    @NotNull
    public NeuroSDK build() {
        return new NeuroSDK(this);
    }

}
