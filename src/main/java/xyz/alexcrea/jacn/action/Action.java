package xyz.alexcrea.jacn.action;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represent any action to send to neuro
 */
@SuppressWarnings({"unused"})
public class Action {

    private final @NotNull String name;
    private final @NotNull String description;

    private @Nullable Function<@NotNull ActionRequest, @Nullable ActionResult> onResult;
    private @Nullable BiConsumer<@NotNull ActionRequest, @NotNull ActionResult> afterResult;

    private boolean reportFailure;

    private @Nullable JsonSchema schema;

    /**
     * Represent any action to send to neuro
     *
     * @param name        The name of the action, which is a unique identifier.
     *                    This should be a lowercase string, with words seperated by underscore or dashes
     *                    (e.g "join_friend_lobby", "use_item")
     * @param description A plaintext description of what this action does.
     *                    This information is directly received by Neuro.
     * @param schema      a simple JSON schema to parse
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
                  @Nullable JsonSchema schema,
                  @Nullable Function<@Nullable ActionRequest, ActionResult> onResult) {
        this.name = name.toLowerCase();
        this.description = description;
        this.schema = schema;

        this.onResult = onResult;
        this.afterResult = null;

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
     * Represent any action to send to neuro
     *
     * @param name        The name of the action, which is a unique identifier.
     *                    This should be a lowercase string, with words seperated by underscore or dashes
     *                    (e.g "join_friend_lobby", "use_item")
     * @param description A plaintext description of what this action does.
     *                    This information is directly received by Neuro.
     * @param schema      a simple JSON schema to parse to receive response from neuro
     */
    public Action(@NotNull String name,
                  @NotNull String description,
                  @Nullable JsonSchema schema) {
        this(name, description, schema, null);
    }

    /**
     * Represent any action to send to neuro
     *
     * @param name        The name of the action, which is a unique identifier.
     *                    This should be a lowercase string, with words seperated by underscore or dashes
     *                    (e.g "join_friend_lobby", "use_item")
     * @param description A plaintext description of what this action does.
     *                    This information is directly received by Neuro.
     */
    public Action(@NotNull String name,
                  @NotNull String description) {
        this(name, description, null, null);
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
    public @Nullable Function<@NotNull ActionRequest, @Nullable ActionResult> getOnResult() {
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
    public Action setOnResult(@Nullable Function<@NotNull ActionRequest, @Nullable ActionResult> onResult) {
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
    public @Nullable BiConsumer<@NotNull ActionRequest, @Nullable ActionResult> getAfterResult() {
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
    public Action setAfterResult(@Nullable BiConsumer<@NotNull ActionRequest, @NotNull ActionResult> afterResult) {
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


    private static final JsonSchemaFactory jsonSchemaFactory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    /**
     * Get the simple JSON schema of this action.
     *
     * @return the json schema.
     */
    public @Nullable JsonSchema getSchema() {
        return schema;
    }

    /**
     * Set a simple JSON schema from a document present on the URI location.
     *
     * @param uri the simple JSON schema location
     * @return this
     */
    @NotNull
    public Action setSchemaFromURI(@Nullable URI uri) {
        if (uri == null) {
            this.schema = null;
            return this;
        }

        this.schema = jsonSchemaFactory.getSchema(uri);
        return this;
    }

    /**
     * Set a simple JSON schema from a document present on the URI location.
     *
     * @param uri the simple JSON schema location
     * @return this
     */
    @NotNull
    public Action setSchemaFromURI(@Nullable String uri) {
        if (uri == null) {
            this.schema = null;
            return this;
        }

        return setSchemaFromURI(URI.create(uri));
    }

    /**
     * Set the simple JSON schema from a restore file.
     *
     * @param resourcePath the path to the resource file
     * @return this
     */
    public @NotNull Action setSchemaFromResource(@Nullable String resourcePath) {
        if (resourcePath == null) {
            this.schema = null;
            return this;
        }

        URL resource = getClass().getClassLoader().getResource(resourcePath);
        if(resource == null){
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }
        try {
            return this.setSchemaFromURI(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the expected simple JSON Schema
     *
     * @param schema the simple JSON schema to validate
     * @return this
     */
    @NotNull
    public Action setSchema(@Nullable JsonSchema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Set the expected simple JSON Schema from raw string
     *
     * @param rawSchema the simple JSON schema to validate as a plain string
     * @return this
     */
    @NotNull
    public Action setSchemaRaw(@Nullable String rawSchema) {
        if (rawSchema == null) {
            this.schema = null;
            return this;
        }

        this.schema = jsonSchemaFactory.getSchema(rawSchema);
        return this;
    }

    /**
     * Return as a map of object that will be sent as json to Neuro
     *
     * @return the map object representing this action
     */
    @ApiStatus.Internal
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("description", description);
        if (schema != null) {
            map.put("schema", schema.getSchemaNode());
        }

        return map;
    }
}
