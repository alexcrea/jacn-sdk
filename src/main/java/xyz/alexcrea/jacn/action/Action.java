package xyz.alexcrea.jacn.action;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represent any action to send to neuro
 */
public class Action {

    private static final Gson gson = new Gson();

    private final @NotNull String name;
    private final @NotNull String description;

    private @NotNull Function<@NotNull ActionRequest, @Nullable ActionResult> onResult;
    private @NotNull BiConsumer<@NotNull ActionRequest, @NotNull ActionResult> afterResult;

    private boolean reportFailure;

    //private @NotNull Consumer<ActionFailed> onFailed;

    private @Nullable String data;

    //TODO add schema correctly

    /**
     * Represent any action to send to neuro
     *
     * @param name        The name of the action, which is a unique identifier.
     *                    This should be a lowercase string, with words seperated by underscore or dashes
     *                    (e.g "join_friend_lobby", "use_item")
     * @param description A plaintext description of what this action does.
     *                    This information is directly received by Neuro.
     * @param data        the JSON schema to parse (type and name is temporary) TODO
     * @param onResult    action called when send by Neuro and successfully parsed.
     *                    please note:
     *                    <p>
     *                    1) There is no guaranty on what thread the action result is called by.
     *                    <p>
     *                    2)The function should return fast: Neuro will be frozen until a result is sent by.
     *                    If you need to do work after the result is returned. please use {@link #setAfterResult afterResult} consumer
     *                    <p>
     *                    3) If this function throw an exception or return null. it will be reported as a failure if
     *                    {@link #setReportFailure reportFailure} is true
     *                    Else it will be reported as a silent success (empty message) (default to false).
     *                    If the action was present in an action force. reporting it as a failure will make Neuro instantly retry the action force.
     *                    Please be careful with this as true as it could freeze Neuro if it throws an exception.
     *                    Behavior may change in the future to make it more safe (exception may not be reported as failure for example).
     *                    If you think of a better/safer system please propose it.
     */
    public Action(@NotNull String name,
                  @NotNull String description,
                  @Nullable String data,
                  @NotNull Function<@NotNull ActionRequest, ActionResult> onResult) {
        this.name = name.toLowerCase();
        this.description = description;
        this.data = data;

        this.onResult = onResult;
        this.afterResult = (ignored1, ignored2) -> {
        };

        this.reportFailure = false;
    }

    /**
     * Represent any action to send to neuro
     *
     * @param name        The name of the action, which is a unique identifier.
     *                    This should be a lowercase string, with words seperated by underscore or dashes
     *                    (e.g "join_friend_lobby", "use_item")
     * @param description A plaintext description of what this action does.
     *                    This information is directly received by Neuro.
     * @param onResult    action called when send by Neuro and successfully parsed.
     *                    please note:
     *                    <p>
     *                    1) There is no guaranty on what thread the action result is called by.
     *                    <p>
     *                    2)The function should return fast: Neuro will be frozen until a result is sent by.
     *                    If you need to do work after the result is returned. please use {@link #setAfterResult afterResult} consumer
     *                    <p>
     *                    3) If this function throw an exception or return null. it will be reported as a failure if
     *                    {@link #setReportFailure reportFailure} is true
     *                    Else it will be reported as a silent success (empty message) (default to false).
     *                    If the action was present in an action force. reporting it as a failure will make Neuro instantly retry the action force.
     *                    Please be careful with this as true as it could freeze Neuro if it throws an exception.
     *                    Behavior may change in the future to make it more safe (exception may not be reported as failure for example).
     *                    If you think of a better/safer system please propose it.
     */
    public Action(@NotNull String name,
                  @NotNull String description,
                  @NotNull Function<@NotNull ActionRequest, ActionResult> onResult) {
        this(name, description, null, onResult);
    }

    /**
     * Get the action name.
     *
     * @return the action name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Get the action description.
     *
     * @return the action description
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * Get the consumer to call on action decided by neuro.
     * see {@link #setOnResult} for detail
     *
     * @return the action executed on result
     */
    public @NotNull Function<@NotNull ActionRequest, @Nullable ActionResult> getOnResult() {
        return onResult;
    }

    /**
     * Sets the consumer to call on action decided by neuro.
     *
     * @param onResult action called when send by Neuro and successfully parsed.
     *                 please note:
     *                 <p>
     *                 1) There is no guaranty on what thread the action result is called by.
     *                 <p>
     *                 2)The function should return fast: Neuro will be frozen until a result is sent by.
     *                 If you need to do work after the result is returned. please use {@link #setAfterResult afterResult} consumer
     *                 <p>
     *                 3) If this function throw an exception or return null. it will be reported as a failure if
     *                 {@link #setReportFailure reportFailure} is true
     *                 Else it will be reported as a silent success (empty message) (default to false).
     *                 If the action was present in an action force. reporting it as a failure will make Neuro instantly retry the action force.
     *                 Please be careful with this as true as it could freeze Neuro if it throws an exception.
     *                 Behavior may change in the future to make it more safe (exception may not be reported as failure for example).
     *                 If you think of a better/safer system please propose it.
     * @return this
     */
    @NotNull
    public Action setOnResult(@NotNull Function<@NotNull ActionRequest, @Nullable ActionResult> onResult) {
        this.onResult = onResult;
        return this;
    }

    /**
     * Get the consumer called after result returned, not null and not exception thrown.
     * <p>
     * Will be called on the same thread as {@link #setOnResult onResult} function.
     *
     * @return the action that will be executed after result send to Neuro
     */
    public @NotNull BiConsumer<@NotNull ActionRequest, @NotNull ActionResult> getAfterResult() {
        return afterResult;
    }

    /**
     * Set the consumer called after result returned, not null and not exception thrown.
     * <p>
     * Will be called on the same thread as {@link #setOnResult onResult} function.
     *
     * @param afterResult the action that will be executed after result send to Neuro
     * @return this
     */
    @NotNull
    public Action setAfterResult(@NotNull BiConsumer<@NotNull ActionRequest, @NotNull ActionResult> afterResult) {
        this.afterResult = afterResult;
        return this;
    }


    /**
     * Get if we should report to the Neuro API
     * <p>
     * for more information. see {@link #setReportFailure}
     *
     * @return if we should report failure to the Neuro API
     */
    public boolean isReportFailure() {
        return reportFailure;
    }

    /**
     * Set if we should report to the Neuro API
     * <p>
     * If {@link #setOnResult onResult} throw an exception or return null. it will be reported as a failure if set to true
     * Else it will be reported as a silent success (empty message) (default to false).
     * If the action was present in an action force. reporting it as a failure will make Neuro instantly retry the action force.
     * Please be careful with this as true as it could freeze Neuro if {@link #setOnResult onResult} throws an exception.
     * Behavior may change in the future to make it more safe (exception may not be reported as failure for example).
     * If you think of a better/safer system please propose it.
     *
     * @param reportFailure if we should report failure to the Neuro API
     * @return this
     */
    @NotNull
    public Action setReportFailure(boolean reportFailure) {
        this.reportFailure = reportFailure;
        return this;
    }

    /*/**
     * Get the consumer to call on JSON schema validation fail.
     *
     * @return the action executed on JSON parsing fail
     */
    /*public @NotNull Consumer<ActionFailed> getOnFailed() {
        return onFailed;
    }*/

    /*/**
     * Set the consumer to call on JSON schema validation fail.
     *
     * @param onFailed the action executed on JSON parsing fail
     * @return this
     */
    /*@NotNull
    public Action setOnFailed(@NotNull Consumer<ActionFailed> onFailed) {
        this.onFailed = onFailed;
        return this;
    }*/

    /**
     * TODO
     * Get the JSON schema.
     *
     * @return the JSON schema
     */
    public @Nullable String getData() {
        return data;
    }

    /**
     * TODO
     * Set the JSON schema.
     *
     * @param data the JSON schema
     * @return this
     */
    @NotNull
    public Action setData(@Nullable String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return gson.toJson(Map.of(
                "name", this.name,
                "description", this.description,
                "schema", data == null ? "{}" : data)); // TODO better data
    }

}
