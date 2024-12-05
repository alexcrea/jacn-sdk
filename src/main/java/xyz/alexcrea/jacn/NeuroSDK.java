package xyz.alexcrea.jacn;

import com.google.gson.Gson;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;

import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The instance used to register actions
 */
@SuppressWarnings({"unused"})
public class NeuroSDK {

    private static final Gson gson = new Gson();

    private final @NotNull String gameName;

    private volatile @NotNull NeuroSDKState state;
    private final @NotNull NeuroWebsocket websocket;

    private final List<Action> actionsToRegisterOnConnect;

    // We have no assumption on thread. so we just lock on register/unregister
    private final ReentrantReadWriteLock registerLock;
    private final HashMap<String, Action> registeredActions;

    /**
     * Create a neuro sdk via a builder
     *
     * @param builder the builder to base from
     */
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

    private void onConnect(@NotNull ServerHandshake handshake) {
        if (handshake.getHttpStatus() < 200 || handshake.getHttpStatus() >= 300) {
            this.state = NeuroSDKState.ERROR;
            return;
        }
        if (!startup()) {
            System.err.println("Could not startup ");

            websocket.close(1, "Could not startup the ");
        }
    }

    private void onClose(String s) {
        if (this.state == NeuroSDKState.ERROR) return;
        this.state = NeuroSDKState.CLOSED;
    }

    private void onConnectError(ConnectException e) {
        this.state = NeuroSDKState.ERROR;
    }

    /**
     * Get the current state of the neuro sdk
     *
     * @return the neuro sdk state
     */
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
    public boolean internalUnregisterAction(@NotNull Action action) {
        return registeredActions.remove(action.getName(), action);
    }

    /**
     * Get an action by its name.
     *
     * @param name the name of the action
     * @return the action registered with this name. null if not registered
     */
    @Nullable
    public Action getAction(@NotNull String name) {
        registerLock.readLock().lock();
        Action action = registeredActions.get(name);
        registerLock.readLock().unlock();

        return action;
    }

    /**
     * Get a list of actions from a list of action names.
     * <p>
     * As this function only add action that are present.
     * the returned list may be smaller than the provided list of names.
     *
     * @param names name of action
     * @return List of actions related to name.
     * as big or smaller than the provided list of names
     */
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

    /**
     * Get a list of actions from a list of action names.
     * <p>
     * As this function only add action that are present.
     * the returned list may be smaller than the provided list of names.
     *
     * @param names name of action
     * @return List of actions related to name.
     * as big or smaller than the provided list of names
     */
    @NotNull
    public List<Action> getActions(@NotNull String... names) {
        return getActions(List.of(names));
    }

    private boolean sendCommand(@NotNull String command, @Nullable Map<String, Object> data, boolean bypassConnected) {
        if (!bypassConnected && !NeuroSDKState.CONNECTED.equals(this.state)) return false;

        HashMap<String, Object> toSendMap = new HashMap<>();
        toSendMap.put("command", command);
        toSendMap.put("game", this.gameName);
        if (data != null) {
            toSendMap.put("data", data);
        }

        String toSend = gson.toJson(toSendMap);
        websocket.send(toSend);
        return true;
    }

    private boolean sendCommand(@NotNull String command, @Nullable Map<String, Object> data) {
        return sendCommand(command, data, false);
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

        if (!sendCommand("startup", null, true)) {
            System.err.println("Could not send startup command");
            this.state = NeuroSDKState.ERROR;

            registerLock.writeLock().unlock();
            return false;
        }

        // register the startup actions
        if (!registerActions(actionsToRegisterOnConnect)) {
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

    /**
     * This function is used to let neuro know about something happening in game
     *
     * @param message A plaintext message that describe what is happening in the game.
     *                This information will be directly received by neuro
     * @param silent  If true, the message will be added to neuro context without prompting her to respond to it.
     *                <p>
     *                If false, Neuro will respond to the message directly,
     *                unless she is busy talking about someone else or to chat
     * @return if the command was successful
     */
    public boolean sendContext(@NotNull String message, boolean silent) {
        if (!NeuroSDKState.CONNECTED.equals(this.state)) return false;

        return sendCommand("context", Map.of(
                "message", message,
                "silent", silent
        ));
    }

    /**
     * Register a list of actions
     * <p>
     * please note: There is no guaranty on what thread the action result is called by or even if the action will be executed.
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    public boolean registerActions(List<Action> actions) {
        if (!NeuroSDKState.CONNECTED.equals(this.state)) return false;

        registerLock.readLock().lock();
        for (Action action : actions) {
            if (!internalRegisterAction(action)) {
                System.err.println("Could not register action " + action.getName());
            }
        }
        registerLock.readLock().unlock();

        return sendCommand("actions/register", Map.of("actions", actions));
    }

    /**
     * Register a list of actions.
     * If you try to register an action that is already registered. it will be ignored
     * <p>
     * please note: There is no guaranty on what thread the action result is called by or even if the action will be executed.
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    public boolean registerActions(@NotNull Action... actions) {
        return registerActions(List.of(actions));
    }

    /**
     * Unregister a list of actions.
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    public boolean unregisterActions(List<Action> actions) {
        if (!NeuroSDKState.CONNECTED.equals(this.state)) return false;

        registerLock.readLock().lock();
        for (Action action : actions) {
            if (!internalUnregisterAction(action)) {
                System.err.println("Could not unregister action " + action.getName());
            }
        }
        registerLock.readLock().unlock();

        List<String> actionNames = new ArrayList<>(registeredActions.keySet());
        return sendCommand("actions/unregister", Map.of("action_names", actionNames));
    }

    /**
     * Unregister a list of actions
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    public boolean unregisterActions(@NotNull Action... actions) {
        return registerActions(List.of(actions));
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     *
     * @param state     An arbitrary string that describe the current state of the game.
     *                  This can be plaintext, JSON, Markdown, or any other format.
     *                  This information will be directly received by Neuro.
     * @param query     A plaintext message that tells Neuro what she is currently supposed to be doing
     *                  (e.g "It is now your turn, Please perform an action.
     *                  If you want to use any items, you should use them before picking up the shotgun.")
     * @param ephemeral if false, the context provided in {@code state} and {@code query} parameters
     *                  will be remembered by Neuro after this action.
     *                  If true, Neuro will only remember it for the duration of the action.
     * @param action    list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
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

        return sendCommand("actions/force", Map.of("actions", toSend));
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     *
     * @param state     An arbitrary string that describe the current state of the game.
     *                  This can be plaintext, JSON, Markdown, or any other format.
     *                  This information will be directly received by Neuro.
     * @param query     A plaintext message that tells Neuro what she is currently supposed to be doing
     *                  (e.g "It is now your turn, Please perform an action.
     *                  If you want to use any items, you should use them before picking up the shotgun.")
     * @param ephemeral if false, the context provided in {@code state} and {@code query} parameters
     *                  will be remembered by Neuro after this action.
     *                  If true, Neuro will only remember it for the duration of the action.
     * @param action    list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            boolean ephemeral,
            @NotNull Action... action) {
        return forceActions(state, query, ephemeral, List.of(action));
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in {@code state} and {@code query} parameters.
     *
     * @param state  An arbitrary string that describe the current state of the game.
     *               This can be plaintext, JSON, Markdown, or any other format.
     *               This information will be directly received by Neuro.
     * @param query  A plaintext message that tells Neuro what she is currently supposed to be doing
     *               (e.g "It is now your turn, Please perform an action.
     *               If you want to use any items, you should use them before picking up the shotgun.")
     * @param action list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            @NotNull List<Action> action) {
        return forceActions(state, query, false, action);
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in {@code state} and {@code query} parameters.
     *
     * @param state  An arbitrary string that describe the current state of the game.
     *               This can be plaintext, JSON, Markdown, or any other format.
     *               This information will be directly received by Neuro.
     * @param query  A plaintext message that tells Neuro what she is currently supposed to be doing
     *               (e.g "It is now your turn, Please perform an action.
     *               If you want to use any items, you should use them before picking up the shotgun.")
     * @param action list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            @NotNull Action... action) {
        return forceActions(state, query, List.of(action));
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     *
     * @param query     A plaintext message that tells Neuro what she is currently supposed to be doing
     *                  (e.g "It is now your turn, Please perform an action.
     *                  If you want to use any items, you should use them before picking up the shotgun.")
     * @param ephemeral if false, the context provided in the  {@code query} parameters
     *                  will be remembered by Neuro after this action.
     *                  If true, Neuro will only remember it for the duration of the action.
     * @param action    list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @NotNull String query,
            boolean ephemeral,
            @NotNull List<Action> action) {
        return forceActions(null, query, ephemeral, action);
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     *
     * @param query     A plaintext message that tells Neuro what she is currently supposed to be doing
     *                  (e.g "It is now your turn, Please perform an action.
     *                  If you want to use any items, you should use them before picking up the shotgun.")
     * @param ephemeral if false, the context provided in the {@code query} parameters
     *                  will be remembered by Neuro after this action.
     *                  If true, Neuro will only remember it for the duration of the action.
     * @param action    list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @NotNull String query,
            boolean ephemeral,
            @NotNull Action... action) {
        return forceActions(query, ephemeral, List.of(action));
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in the {@code query} parameters.
     *
     * @param query  A plaintext message that tells Neuro what she is currently supposed to be doing
     *               (e.g "It is now your turn, Please perform an action.
     *               If you want to use any items, you should use them before picking up the shotgun.")
     * @param action list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @NotNull String query,
            @NotNull List<Action> action) {
        return forceActions(query, false, action);
    }

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in the {@code query} parameters.
     *
     * @param query  A plaintext message that tells Neuro what she is currently supposed to be doing
     *               (e.g "It is now your turn, Please perform an action.
     *               If you want to use any items, you should use them before picking up the shotgun.")
     * @param action list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    public boolean forceActions(
            @NotNull String query,
            @NotNull Action... action) {
        return forceActions(query, List.of(action));
    }


}
