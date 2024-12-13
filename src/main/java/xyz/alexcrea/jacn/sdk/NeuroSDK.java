package xyz.alexcrea.jacn.sdk;

import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.NeuroWebsocket;
import xyz.alexcrea.jacn.action.Action;

import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The instance used to communicate with the Neuro SDK API
 */
@SuppressWarnings({"unused"})
public class NeuroSDK implements NeuroSDKInterface {

    private final @NotNull String gameName;

    private volatile @NotNull NeuroSDKState state;
    private final @NotNull NeuroWebsocket websocket;

    private final List<Action> actionsToRegisterOnConnect;

    // We have no assumption on thread. so we just lock on register/unregister
    private final ReentrantReadWriteLock registerLock;
    private final HashMap<String, Action> registeredActions;

    /**
     * Create and connect to Neuro sdk websocket via a builder
     *
     * @param builder the builder to base the websocket from
     */
    @NonBlocking
    public NeuroSDK(@NotNull NeuroSDKBuilder builder) {
        this.gameName = builder.getGameName();

        this.state = NeuroSDKState.CONNECTING;
        this.actionsToRegisterOnConnect = new ArrayList<>(builder.getActions());

        this.registerLock = new ReentrantReadWriteLock();
        this.registeredActions = new HashMap<>();

        // create and connected the websocket
        URI uri = URI.create("ws://" + builder.getAddress() + ":" + builder.getPort());
        this.websocket = new NeuroWebsocket(uri, this, builder,
                this::onConnect, this::onClose, this::onConnectError);

        this.websocket.connect();
    }

    @Override
    public @NotNull String getGameName() {
        return gameName;
    }

    private void onConnect(@NotNull ServerHandshake handshake) {
        if ((handshake.getHttpStatus() < 200 || handshake.getHttpStatus() >= 300) && (handshake.getHttpStatus() != 101)) {
            this.state = NeuroSDKState.ERROR;
            return;
        }
        if (!startup()) {
            System.err.println("Could not startup the websocket");

            websocket.close(CloseFrame.PROTOCOL_ERROR, "Could not startup the websocket");
        }
    }

    private void onClose(String s) {
        if (this.state == NeuroSDKState.ERROR) return;
        this.state = NeuroSDKState.CLOSED;
    }

    private void onConnectError(ConnectException e) {
        this.state = NeuroSDKState.ERROR;
    }

    @Override
    public @NotNull NeuroSDKState getState() {
        return state;
    }

    /**
     * Register an action if the action is not currently registered.
     * This method do not lock, but you need to lock first before using it.
     *
     * @param action the action to unregister
     * @return true if no action of the same id was previously registered.
     */
    private boolean internalRegisterAction(@NotNull Action action) {
        return registeredActions.putIfAbsent(action.getName(), action) == null;
    }

    /**
     * Unregister and action.
     * This method do not lock, but you need to lock first before using it.
     *
     * @param action the action to unregister
     * @return false if it was not registered. true otherwise.
     */
    private boolean internalUnregisterAction(@NotNull Action action) {
        return registeredActions.remove(action.getName(), action);
    }

    @Override
    @Nullable
    public Action getAction(@NotNull String name) {
        registerLock.readLock().lock();
        Action action = registeredActions.get(name);
        registerLock.readLock().unlock();

        return action;
    }

    @Override
    @NotNull
    public List<Action> getActions(@NotNull List<String> names) {
        registerLock.readLock().lock();

        List<Action> actions = new ArrayList<>();
        for (String name : names) {
            Action action = getAction(name);
            if (action != null) actions.add(action);
        }

        registerLock.readLock().unlock();
        return actions;
    }

    @Override
    @NotNull
    public List<Action> getActions(@NotNull String... names) {
        return getActions(List.of(names));
    }

    /**
     * Send a startup command.
     * clearing every action and registering all the startup actions
     *
     * @return if the command was successful
     */
    public boolean startup() {
        registerLock.writeLock().lock();
        // Clear previous actions if any
        this.registeredActions.clear();

        if (!websocket.sendCommand("startup", null, true)) {
            System.err.println("Could not send startup command");
            this.state = NeuroSDKState.ERROR;

            registerLock.writeLock().unlock();
            return false;
        }

        // register the startup actions
        if (!internalRegisterActions(actionsToRegisterOnConnect)) {
            System.err.println("Could not register startup actions");
            this.state = NeuroSDKState.ERROR;

            registerLock.writeLock().unlock();
            return false;
        }
        registerLock.writeLock().unlock();

        // set the state to connected when startup is done
        this.state = NeuroSDKState.CONNECTED;
        return true;
    }

    @Override
    public boolean sendContext(@NotNull String message, boolean silent) {
        if (!NeuroSDKState.CONNECTED.equals(this.state)) return false;

        return websocket.sendCommand("context", Map.of(
                "message", message,
                "silent", silent
        ));
    }

    private boolean internalRegisterActions(List<Action> actions) {
        if (actions.isEmpty()) return true;

        registerLock.readLock().lock();
        List<Map<String, Object>> actionList = new ArrayList<>();
        for (Action action : actions) {
            if (!internalRegisterAction(action)) {
                System.err.println("Could not register action " + action.getName());
            }
            actionList.add(action.asMap());
        }
        registerLock.readLock().unlock();

        return websocket.sendCommand("actions/register", Map.of("actions", actionList), true);
    }

    @Override
    public boolean registerActions(List<Action> actions) {
        if (!NeuroSDKState.CONNECTED.equals(this.state)) return false;
        return internalRegisterActions(actions);
    }

    @Override
    public boolean registerActions(@NotNull Action... actions) {
        return registerActions(List.of(actions));
    }

    @Override
    public boolean unregisterActions(List<Action> actions) {
        if (!NeuroSDKState.CONNECTED.equals(this.state)) return false;
        if (actions.isEmpty()) return true;

        registerLock.readLock().lock();
        List<String> actionNames = new ArrayList<>();
        for (Action action : actions) {
            if (!internalUnregisterAction(action)) {
                System.err.println("Could not unregister action " + action.getName());
            }

            actionNames.add(action.getName());
        }
        registerLock.readLock().unlock();

        return websocket.sendCommand("actions/unregister", Map.of("action_names", actionNames));
    }

    @Override
    public boolean unregisterActions(@NotNull Action... actions) {
        return unregisterActions(List.of(actions));
    }

    @Override
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            boolean ephemeral,
            @NotNull List<Action> action) {
        List<String> actionNames = new ArrayList<>(registeredActions.keySet());

        HashMap<String, Object> toSend = new HashMap<>();
        if (state != null) toSend.put("state", state);
        toSend.put("query", query);
        toSend.put("ephemeral_context", ephemeral);
        toSend.put("action_names", actionNames);

        return websocket.sendCommand("actions/force", toSend);
    }

    @Override
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            boolean ephemeral,
            @NotNull Action... actions) {
        return forceActions(state, query, ephemeral, List.of(actions));
    }

    @Override
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            @NotNull List<Action> actions) {
        return forceActions(state, query, false, actions);
    }

    @Override
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            @NotNull Action... actions) {
        return forceActions(state, query, List.of(actions));
    }

    @Override
    public boolean forceActions(
            @NotNull String query,
            boolean ephemeral,
            @NotNull List<Action> actions) {
        return forceActions(null, query, ephemeral, actions);
    }

    @Override
    public boolean forceActions(
            @NotNull String query,
            boolean ephemeral,
            @NotNull Action... actions) {
        return forceActions(query, ephemeral, List.of(actions));
    }

    @Override
    public boolean forceActions(
            @NotNull String query,
            @NotNull List<Action> actions) {
        return forceActions(query, false, actions);
    }

    @Override
    public boolean forceActions(
            @NotNull String query,
            @NotNull Action... actions) {
        return forceActions(query, List.of(actions));
    }

    @Override
    public List<Action> getRegisteredActions() {
        return new ArrayList<>(this.registeredActions.values());
    }


    /**
     * Gracefully close the websocket
     *
     * @param reason Reason of why the sdk is closed
     */
    public void close(String reason) {
        if (this.state == NeuroSDKState.CONNECTED) {
            this.onClose(reason);
        }

        this.state = NeuroSDKState.CLOSED;
        this.websocket.close(CloseFrame.NORMAL, reason);
    }

    /**
     * Gracefully close the websocket
     */
    public void close() {
        close("Shutdown");
    }

}
