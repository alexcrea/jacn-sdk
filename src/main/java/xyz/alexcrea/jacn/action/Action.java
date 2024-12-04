package xyz.alexcrea.jacn.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.ActionFailed;

import java.util.function.Consumer;

/**
 * Represent any action to send to neuro
 */
public class Action {

    private final @NotNull String id;
    private final @NotNull ActionType type;
    private @NotNull Consumer<ActionResult> onResult;

    private @NotNull Consumer<ActionFailed> onFailed;

    private @Nullable String data;

    //TODO add other things when API release

    /**
     * Represent any action to send to neuro
     *
     * @param id       the action id
     * @param type     the action type
     * @param onResult called when action is successful.
     *                 please note: There is no guaranty on what thread the action result is called by.
     * @param data     the JSON schema to parse (type and name is temporary)
     */
    public Action(@NotNull String id,
                  @NotNull ActionType type,
                  @NotNull Consumer<ActionResult> onResult,
                  @Nullable String data) {
        this.id = id;
        this.type = type;
        this.onResult = onResult;
        this.onFailed = (failed) -> {
            //TODO see what to do when api doc is released
        };
        this.data = data;
    }

    /**
     * Represent any action to send to neuro
     *
     * @param id       the action id
     * @param type     the action type
     * @param onResult called when action is successful
     */
    public Action(@NotNull String id,
                  @NotNull ActionType type,
                  @NotNull Consumer<ActionResult> onResult) {
        this(id, type, onResult, null);
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Get the action type.
     *
     * @return the action type
     */
    public @NotNull ActionType getType() {
        return type;
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
     * Get the JSON schema.
     *
     * @return the JSON schema
     */
    public @Nullable String getData() {
        return data;
    }

    /**
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

}
