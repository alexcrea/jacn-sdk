package xyz.alexcrea.jacn.sdk;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.listener.NeuroSDKListener;
import xyz.alexcrea.jacn.sdk.proposed.ProposedFeature;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Create a new builder to create a Neuro sdk instance
 */
@SuppressWarnings({"unused"})
public class NeuroSDKBuilder {

    private final static Logger logger = LoggerFactory.getLogger(NeuroSDKBuilder.class);

    private static final String DEFAULT_ADDRESS = "localhost";
    private static final short DEFAULT_PORT = 8000;

    private @NotNull String gameName;

    private @Nullable String address = null;
    private @Nullable Short port = null;

    private Consumer<ServerHandshake> onConnect;
    private Consumer<String> onClose;
    private Consumer<Exception> onError;

    private List<NeuroSDKListener> listeners;

    private List<Action> actionList;

    private final EnumSet<ProposedFeature> proposed;

    /**
     * Create a new builder for
     * <p>
     * Default builder has localhost as address and 8000 as port.
     * But will use environment variable if exist and no address is provided.
     * It also logs error to SLF4J logger on error level.
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
            if (status == 101) return;

            if (status < 200 || status >= 300) {
                logger.error("Got error while connecting to the websocket: ({}) {}\nIs Neuro or randy open ?",
                        handshake.getHttpStatus(),
                        handshake.getHttpStatusMessage());
            }
        };
        this.onClose = reason -> {
            logger.info("NeuroSDK Closed: {}", reason);
        };
        this.onError = error -> {
            if (error instanceof ConnectException) {
                logger.error("Error connecting to the websocket\n" +
                        "Is Neuro or Randy open ?");
                return;
            }

            logger.error("Got error while running the websocket: ", error);
        };

        this.listeners = new ArrayList<>();
        this.actionList = new ArrayList<>();

        this.proposed = EnumSet.noneOf(ProposedFeature.class);
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
    public NeuroSDKBuilder addActionsOnConnect(@NotNull Action... actions) {
        return addActionsOnConnect(List.of(actions));
    }

    /**
     * Set the webserver expected port
     *
     * @param port the webserver port
     * @return this
     */
    @NotNull
    public NeuroSDKBuilder setPort(@Nullable Short port) {
        this.port = port;
        return this;
    }

    /**
     * Set the expected websocket address
     *
     * @param address the websocket address
     * @return this
     */
    @NotNull
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
        return address != null ? address : DEFAULT_ADDRESS;
    }

    /**
     * Get if a custom address was set
     *
     * @return if a custom address was set
     */
    public boolean hasAddress() {
        return address != null;
    }

    /**
     * Set the webserver expected port
     *
     * @return the webserver port
     */
    public short getPort() {
        return port != null ? port : DEFAULT_PORT;
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
    @NotNull
    public NeuroSDKBuilder setGameName(@NotNull String gameName) {
        this.gameName = gameName;
        return this;
    }

    /**
     * Get the list of SDK listener of this builder
     *
     * @return the list of SDK Listeners
     */
    @NotNull
    public List<NeuroSDKListener> getListeners() {
        return listeners;
    }

    /**
     * Set the list of SDK listeners for this builder
     *
     * @param listeners the list of SDK listener
     * @return this
     */
    @NotNull
    public NeuroSDKBuilder setListeners(@NotNull List<NeuroSDKListener> listeners) {
        this.listeners = new ArrayList<>(listeners);
        return this;
    }

    /**
     * Add listeners to the current of listeners.
     *
     * @param listeners the listeners to add
     * @return this
     */
    @NotNull
    public NeuroSDKBuilder addListeners(@NotNull NeuroSDKListener... listeners) {
        this.listeners.addAll(List.of(listeners));
        return this;
    }

    /**
     * Get enabled proposed features.
     *
     * @return set of enabled proposed features
     */
    @ApiStatus.Experimental
    @NotNull
    public Set<ProposedFeature> getProposed() {
        return EnumSet.copyOf(proposed);
    }

    /**
     * Enabled an array of proposed features
     *
     * @param proposed the list of proposed feature to enable
     * @return this
     */
    @ApiStatus.Experimental
    @NotNull
    public NeuroSDKBuilder addProposed(@NotNull ProposedFeature... proposed) {
        this.proposed.addAll(List.of(proposed));
        return this;
    }

    /**
     * Disable an array of proposed features
     *
     * @param proposed the list of proposed feature to disable
     * @return this
     */
    @ApiStatus.Experimental
    @NotNull
    public NeuroSDKBuilder removeProposed(@NotNull ProposedFeature... proposed) {
        List.of(proposed).forEach(this.proposed::remove);
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
