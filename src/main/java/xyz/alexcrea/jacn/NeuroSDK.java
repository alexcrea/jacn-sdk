package xyz.alexcrea.jacn;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.error.ActionException;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The instance used to register actions
 */
@SuppressWarnings({"unused"})
public class NeuroSDK {

    private @NotNull NeuroSDKState state;
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
    public NeuroSDK(NeuroSDKBuilder builder) {
        this.state = NeuroSDKState.CONNECTING;
        this.actionsToRegisterOnConnect = new ArrayList<>(builder.getActions());

        this.registerLock = new ReentrantReadWriteLock();
        this.registeredActions = new HashMap<>();

        // Connected the websocket
        URI uri = URI.create("ws://" + builder.getAddress() + ":" + builder.getPort());
        this.websocket = new NeuroWebsocket(uri, this, builder, this::onConnect, this::onClose);
    }

    private void onConnect(@NotNull ServerHandshake handshake) {
        if (handshake.getHttpStatus() < 200 || handshake.getHttpStatus() >= 300) {
            this.state = NeuroSDKState.ERROR;
            return;
        }
        // register the startup actions
        for (Action action : actionsToRegisterOnConnect) {
            try {
                if(!registerAction(action)){
                    System.err.println("Could not register startup action: " + action.getId());
                }
            } catch (ActionException e) {
                e.printStackTrace();
            }
        }

        // Finally, set the state to connected
        this.state = NeuroSDKState.CONNECTED;
    }

    private void onClose(String s) {
        if (this.state == NeuroSDKState.ERROR) return;
        this.state = NeuroSDKState.CLOSED;
    }

    /**
     * Get the current state of the neuro sdk
     *
     * @return the neuro sdk state
     */
    public NeuroSDKState getState() {
        return state;
    }

    /**
     * Register an action if the action is not currently registered
     * <p>
     * please note: There is no guaranty on what thread the action result is called by.
     * @param action the action to unregister
     * @return false if it could not register. true otherwise.
     * @throws ActionException If an exception occur while registering the action
     */
    public boolean registerAction(@NotNull Action action) throws ActionException {
        registerLock.writeLock().lock();
        boolean success = registeredActions.putIfAbsent(action.getId(), action) == null;
        registerLock.writeLock().unlock();

        if (!success) return false;

        // TODO send to websocket the action registering
        return true;
    }

    /**
     * TODO the javadoc
     *
     * @param action the action to unregister
     * @return false if it is not registered. true otherwise.
     * @throws ActionException If an exception occur while unregister the action
     */
    public boolean unregisterAction(@NotNull Action action) throws ActionException {
        registerLock.writeLock().lock();
        boolean success = registeredActions.remove(action.getId(), action);
        registerLock.writeLock().unlock();

        //TODO send to websocket unregistering
        if (!success) return false;

        return true;
    }

    @Nullable
    public Action popAction(@NotNull String id) throws ActionException {
        registerLock.writeLock().lock();
        Action action = registeredActions.get(id);
        if (action == null) {
            registerLock.writeLock().unlock();
            return null;
        }

        unregisterAction(action);
        registerLock.writeLock().unlock();

        return action;
    }

    @Nullable
    public Action getAction(@NotNull String id) {
        registerLock.readLock().lock();
        Action action = registeredActions.get(id);
        registerLock.readLock().unlock();

        return action;
    }

}
