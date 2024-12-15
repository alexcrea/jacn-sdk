package xyz.alexcrea.jacn.listener;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.sdk.NeuroSDK;

/**
 * Represent a listener to even on the Neuro SDK
 */
@SuppressWarnings({"unused"})
public interface NeuroSDKListener {

    /**
     * Called when the neuro sdk is set.
     *
     * @param neuroSDK the neuro sdk
     * @return if the neuro sdk was successfully set. (should be false only if it was already present)
     */
    boolean setNeuroSDK(@NotNull NeuroSDK neuroSDK);

    /**
     * Get the current Neuro sdk.
     *
     * @return the current neuro sdk. null is not yet set.
     */
    @Nullable
    NeuroSDK getSDK();

    /**
     * Called when a handshake connection was done with a websocket.
     * This function is, probably, only called from the websocket thread.
     *
     * @param handshake the handshake of this connection
     */
    void onConnect(@NotNull ServerHandshake handshake);

    /**
     * Called when the websocket got closed for any reason.
     * There is no guaranty of what thread call this function.
     *
     * @param reason the reason of why the SDK got closed
     * @param remote if the connection was close by the remote connection
     * @param code   the close code
     */
    void onClose(@NotNull String reason, boolean remote, int code);

    /**
     * Called when a websocket exception has happened.
     * This function is, probably, only called from the websocket thread.
     *
     * @param exception the exception that caused
     */
    void onError(@NotNull Exception exception);

    /**
     * Called when Neuro request an action.
     * This function is, probably, only called from the websocket thread.
     * <p>
     * This function should return a result ASAP as Neuro will freeze until a result is provided.
     * An action may be triggered anytime. even if not requested by force action.
     * <p>
     * This function return a non-null value, it will stop other listener to process this action request.
     * If no listener return a non-null value, then it will be considered as a failed action request.
     * <p>
     * If you need to execute something that take more time after returned the result. see {@link #onAfterResult}
     *
     * @param request the requested action
     * @param sdk     the Neuro SDK
     * @return The resulting action result.
     * or null if the listener do not handle this Action Request
     */
    @Nullable
    ActionResult onActionRequest(@NotNull ActionRequest request, @NotNull NeuroSDK sdk);

    /**
     * Called after {@link #onActionRequest} returned a non-null result.
     * This function is, probably, only called from the websocket thread.
     *
     * @param request the requested action
     * @param result  the returned result
     * @param sdk     the Neuro SDK
     */
    void onAfterResult(@NotNull ActionRequest request, @NotNull ActionResult result, @NotNull NeuroSDK sdk);

}