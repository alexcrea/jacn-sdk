package xyz.alexcrea.jacn.action;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Represent any action to send to neuro
 */
public class Action {

    private static final Gson gson = new Gson();

    private final @NotNull String name;
    private final @NotNull String description;
    private @NotNull Consumer<ActionResult> onResult;

    private @NotNull Consumer<ActionFailed> onFailed;

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
     * @param onResult    called when action is successful.
     *                    please note: There is no guaranty on what thread the action result is called by.
     */
    public Action(@NotNull String name,
                  @NotNull String description,
                  @Nullable String data,
                  @NotNull Consumer<ActionResult> onResult) {
        this.name = name.toLowerCase();
        this.description = description;
        this.data = data;

        this.onResult = onResult;
        this.onFailed = (failed) -> {
            //TODO see what to do when api doc is released
        };
    }

    /**
     * Represent any action to send to neuro
     *
     * @param name        The name of the action, which is a unique identifier.
     *                    This should be a lowercase string, with words seperated by underscore or dashes
     *                    (e.g "join_friend_lobby", "use_item")
     * @param description A plaintext description of what this action does.
     *                    This information is directly received by Neuro.
     * @param onResult    called when action is successful.
     *                    please note: There is no guaranty on what thread the action result is called by.
     */
    public Action(@NotNull String name,
                  @NotNull String description,
                  @NotNull Consumer<ActionResult> onResult) {
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
     *
     * @return the action executed on result
     */
    public @NotNull Consumer<ActionResult> getOnResult() {
        return onResult;
    }

    /**
     * Sets the consumer to call on action decided by neuro.
     *
     * @param onResult the action executed on result
     * @return this
     */
    @NotNull
    public Action setOnResult(@NotNull Consumer<ActionResult> onResult) {
        this.onResult = onResult;
        return this;
    }

    /**
     * Get the consumer to call on JSON schema validation fail.
     *
     * @return the action executed on JSON parsing fail
     */
    public @NotNull Consumer<ActionFailed> getOnFailed() {
        return onFailed;
    }

    /**
     * Set the consumer to call on JSON schema validation fail.
     *
     * @param onFailed the action executed on JSON parsing fail
     * @return this
     */
    @NotNull
    public Action setOnFailed(@NotNull Consumer<ActionFailed> onFailed) {
        this.onFailed = onFailed;
        return this;
    }

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
