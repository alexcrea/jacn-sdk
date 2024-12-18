package xyz.alexcrea.jacn.sdk;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.sdk.proposed.ProposedFeature;

import java.util.List;

/**
 * Represent a class that can interact with the Neuro SDK
 */
@SuppressWarnings({"unused"})
public interface NeuroSDKInterface {

    /**
     * Get the game name
     *
     * @return the game name
     */
    @NotNull String getGameName();

    /**
     * Get the current state of the Neuro sdk
     *
     * @return the Neuro sdk state
     */
    @NotNull NeuroSDKState getState();

    /**
     * Get an action by its name.
     *
     * @param name the name of the action
     * @return the action registered with this name. null if not registered
     */
    @Nullable
    Action getAction(@NotNull String name);

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
    List<Action> getActions(@NotNull List<String> names);

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
    List<Action> getActions(@NotNull String... names);

    /**
     * This function is used to let Neuro know about something happening in game
     *
     * @param message A plaintext message that describe what is happening in the game.
     *                This information will be directly received by Neuro
     * @param silent  If true, the message will be added to Neuro context without prompting her to respond to it.
     *                <p>
     *                If false, Neuro will respond to the message directly,
     *                unless she is busy talking about someone else or to chat
     * @return if the command was successful
     */
    boolean sendContext(@NotNull String message, boolean silent);

    /**
     * Register a list of actions
     * <p>
     * please note: There is no guaranty on what thread the action result is called by or even if the action will be executed.
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    boolean registerActions(List<Action> actions);

    /**
     * Register a list of actions.
     * If you try to register an action that is already registered. it will be ignored
     * <p>
     * please note: There is no guaranty on what thread the action result is called by or even if the action will be executed.
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    boolean registerActions(@NotNull Action... actions);

    /**
     * Unregister a list of actions.
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    boolean unregisterActions(List<Action> actions);

    /**
     * Unregister a list of actions
     *
     * @param actions list of action to register
     * @return if the command was successful
     */
    boolean unregisterActions(@NotNull Action... actions);

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
     * @param actions   list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            boolean ephemeral,
            @NotNull List<Action> actions);

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
     * @param actions   list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            boolean ephemeral,
            @NotNull Action... actions);

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in {@code state} and {@code query} parameters.
     *
     * @param state   An arbitrary string that describe the current state of the game.
     *                This can be plaintext, JSON, Markdown, or any other format.
     *                This information will be directly received by Neuro.
     * @param query   A plaintext message that tells Neuro what she is currently supposed to be doing
     *                (e.g "It is now your turn, Please perform an action.
     *                If you want to use any items, you should use them before picking up the shotgun.")
     * @param actions list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            @NotNull List<Action> actions);

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in {@code state} and {@code query} parameters.
     *
     * @param state   An arbitrary string that describe the current state of the game.
     *                This can be plaintext, JSON, Markdown, or any other format.
     *                This information will be directly received by Neuro.
     * @param query   A plaintext message that tells Neuro what she is currently supposed to be doing
     *                (e.g "It is now your turn, Please perform an action.
     *                If you want to use any items, you should use them before picking up the shotgun.")
     * @param actions list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @Nullable String state,
            @NotNull String query,
            @NotNull Action... actions);

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
     * @param actions   list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @NotNull String query,
            boolean ephemeral,
            @NotNull List<Action> actions);

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
     * @param actions   list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @NotNull String query,
            boolean ephemeral,
            @NotNull Action... actions);

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in the {@code query} parameters.
     *
     * @param query   A plaintext message that tells Neuro what she is currently supposed to be doing
     *                (e.g "It is now your turn, Please perform an action.
     *                If you want to use any items, you should use them before picking up the shotgun.")
     * @param actions list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @NotNull String query,
            @NotNull List<Action> actions);

    /**
     * This force Neuro to execute one of the following actions as soon as possible.
     * Note that this may take a bit if she is already talking.
     * Neuro will remember the context provided in the {@code query} parameters.
     *
     * @param query   A plaintext message that tells Neuro what she is currently supposed to be doing
     *                (e.g "It is now your turn, Please perform an action.
     *                If you want to use any items, you should use them before picking up the shotgun.")
     * @param actions list of possible action to force. one of them should get forced.
     * @return if the command was successful
     */
    boolean forceActions(
            @NotNull String query,
            @NotNull Action... actions);

    /**
     * Get all registered actions
     *
     * @return a list of all the registered actions
     */
    List<Action> getRegisteredActions();

    /**
     * Get if a proposed feature is enabled.
     * By default, no feature is enabled. see {@link NeuroSDKBuilder#addProposed The Builder} for more information.
     * @param feature the feature to test if enable
     * @return if the provided feature is enabled
     */
    boolean isEnable(@NotNull ProposedFeature feature);

}
