package xyz.alexcrea.jacn;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.error.WebsocketException;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Create a new builder to create a Neuro sdk instance
 */
@SuppressWarnings({"unused"})
public class NeuroSDKBuilder {

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final short DEFAULT_PORT = 8000;

    private @NotNull String gameName;

    private String address = DEFAULT_ADDRESS;
    private short port = DEFAULT_PORT;

    private Consumer<ServerHandshake> onConnect;
    private Consumer<String> onClose;
    private Consumer<Exception> onError;

    private List<Action> actionList;

    /**
     * Create a new builder for
     * <p>
     * Default builder has localhost as address and 42 as port.
     * It also log error to system err on connect and print stacktrace on error.
     * No default action is prepared to be registered by default
     *
     * @param gameName The game name.
     *                 You should use the game's display name, including any spaces and symbols
     *                 (e.g "Buckshot Roulette").
     *                 The server will not include this field
     */
    public NeuroSDKBuilder(@NotNull String gameName) {
        this.gameName = gameName;

        this.onConnect = handshake -> {
            short status = handshake.getHttpStatus();
            if(status == 101) return;

            if (status < 200 || status >= 300) {
                System.err.println("Got error while connecting to the websocket: " +
                        "(" + handshake.getHttpStatus() + ") " + handshake.getHttpStatusMessage() +
                        "\nIs Neuro or randy open ?");
            }
        };
        this.onClose = string -> {
            System.out.println("Closed: " + string);
        };
        this.onError = error -> {
            if(error instanceof ConnectException) {
                System.err.println("Error connecting to the websocket");
                System.err.println("Is Neuro or Randy open ?");
                return;
            }

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
     *
     * @param onOpen the consumer to execute on open
     * @return this
     */
    public NeuroSDKBuilder setOnConnect(@NotNull Consumer<ServerHandshake> onOpen) {
        this.onConnect = onOpen;
        return this;
    }

    /**
     * Set the consumer to be executed on websocket close
     * Default to do nothing
     * <p>
     * Action will not be able to get registered after this
     *
     * @param onClose the consumer to execute on open
     * @return this
     */
    public NeuroSDKBuilder setOnClose(@NotNull Consumer<String> onClose) {
        this.onClose = onClose;
        return this;
    }

    /**
     * Get the consumer to be executed on websocket error.
     * Default to print stacktrace of the error.
     *
     * @param onError the consumer to execute on error
     * @return this
     */
    public NeuroSDKBuilder setOnError(@NotNull Consumer<Exception> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Set the actions that will be registered if the websocket connected successfully.
     * <p>
     * please note: There is no guaranty on what thread the action result is called by.
     *
     * @param actions the list of actions to get registered
     * @return this
     */
    public NeuroSDKBuilder setActionsOnConnect(@NotNull List<Action> actions) {
        this.actionList = new ArrayList<>(actions);
        return this;
    }

    /**
     * Set the actions that will be registered if the websocket connected successfully.
     * <p>
     * please note: There is no guaranty on what thread the action result is called by.
     *
     * @param actions the list of actions to get registered
     * @return this
     */
    public NeuroSDKBuilder setActionsOnConnect(@NotNull Action... actions) {
        return setActionsOnConnect(List.of(actions));
    }

    /**
     * Add action to be registered if the websocket get connected successfully
     * <p>
     * please note: There is no guaranty on what thread the action result is called by.
     *
     * @param actions the list of actions to get registered
     * @return this
     */
    public NeuroSDKBuilder addActionsOnConnect(@NotNull List<Action> actions) {
        this.actionList.addAll(actions);
        return this;
    }

    /**
     * Add action to be registered if the websocket get connected successfully
     * <p>
     * please note: There is no guaranty on what thread the action result is called by.
     *
     * @param actions the list of actions to get registered
     * @return this
     */
    public NeuroSDKBuilder addActionsOnConnect(@NotNull Action... actions) {
        return addActionsOnConnect(List.of(actions));
    }

    /**
     * Set the webserver expected port
     *
     * @param port the webserver port
     * @return this
     */
    public NeuroSDKBuilder setPort(short port) {
        this.port = port;
        return this;
    }

    /**
     * Set the expected websocket address
     *
     * @param address the websocket address
     * @return this
     */
    public NeuroSDKBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Get the expected websocket address
     *
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
     * Excepted action that planned to be registered on open that will get registered
     * just before this action is executed.
     *
     * @return the consumer to execute on open
     */
    @NotNull
    public Consumer<ServerHandshake> getOnConnect() {
        return onConnect;
    }

    /**
     * Get the consumer to be executed on websocket close
     * Default to do nothing
     * <p>
     * Action will not be able to get registered after this
     *
     * @return the consumer to execute on open
     */
    @NotNull
    public Consumer<String> getOnClose() {
        return onClose;
    }

    /**
     * Get the consumer to be executed on websocket error.
     * Default to print stacktrace of the error.
     *
     * @return the consumer to execute on error
     */
    @NotNull
    public Consumer<Exception> getOnError() {
        return onError;
    }

    /**
     * List of action that will get registered on websocket connect
     *
     * @return the list of actions
     */
    @NotNull
    public List<Action> getActions() {
        return actionList;
    }

    /**
     * Get the game name.
     *
     * @return the game name
     */
    public @NotNull String getGameName() {
        return gameName;
    }

    /**
     * Set the game name.
     * You should use the game's display name, including any spaces and symbols
     * (e.g "Buckshot Roulette").
     * The server will not include this field
     *
     * @param gameName the game name
     * @return this
     */
    public NeuroSDKBuilder setGameName(@NotNull String gameName) {
        this.gameName = gameName;
        return this;
    }

    /**
     * Create and open a Neuro sdk with the builder properties.
     * Will also try to connect to it in a non-blocking way:
     * You can continue execution. {@link #setOnConnect onConnect} will get executed when the websocket is open.
     * <p>
     * No guaranty on when will the websocket will be ready.
     * But if neuro do not run or address/port is invalid then it will never get connected.
     *
     * @return the Neuro sdk instance with websocket opening.
     */
    @NotNull
    @NonBlocking
    public NeuroSDK build() {
        return new NeuroSDK(this);
    }

}
