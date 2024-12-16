package xyz.alexcrea.jacn.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A simple action that map certain options for Neuro to chose 1 of them.
 *
 * @param <T> Type of option's values
 */
@SuppressWarnings({"unused"})
public class OptionMapAction<T> extends Action {

    private final static ObjectMapper mapper = new ObjectMapper();

    private final @NotNull Map<String, T> valueMap;
    private final @NotNull ObjectNode rootNode;
    private final @NotNull ArrayNode options;

    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable String schemaTitle, @Nullable String schemaDescription,
                           @Nullable Map<String, T> options,
                           @Nullable Function<@NotNull ActionRequest, ActionResult> onResult) {
        super(name, description, onResult);
        this.valueMap = Objects.requireNonNullElseGet(options, HashMap::new);

        this.rootNode = createObjectListSchema(this.valueMap, schemaTitle, schemaDescription);
        this.options = (ArrayNode) this.rootNode.get("properties").get("options").get("enum");

        super.setSchema(this.rootNode);
    }

    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable String schemaTitle, @Nullable String schemaDescription,
                           @Nullable Function<@NotNull ActionRequest, ActionResult> onResult) {
        this(name, description, schemaTitle, schemaDescription, null,  onResult);
    }

    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable String schemaTitle,
                           @Nullable Function<@NotNull ActionRequest, ActionResult> onResult) {
        this(name, description, schemaTitle, null,  onResult);
    }

    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable Function<@NotNull ActionRequest, ActionResult> onResult) {
        this(name, description, null,  onResult);
    }


    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable String schemaTitle, @Nullable String schemaDescription,
                           @Nullable Map<String, T> options) {
        this(name, description, schemaTitle, schemaDescription, options, null);
    }

    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable String schemaTitle, @Nullable String schemaDescription) {
        this(name, description, schemaTitle, schemaDescription, null,  null);
    }

    public OptionMapAction(@NotNull String name, @NotNull String description,
                           @Nullable String schemaTitle) {
        this(name, description, schemaTitle, null, null, null);
    }

    public OptionMapAction(@NotNull String name, @NotNull String description) {
        this(name, description, null, null, null, null);
    }

    private static <T> @NotNull ObjectNode createObjectListSchema(
            @NotNull Map<String, T> values,
            @Nullable String schemaTitle,
            @Nullable String schemaDescription) {

        ObjectNode jsonRoot = mapper.createObjectNode();
        jsonRoot.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        if(schemaTitle != null){
            jsonRoot.put("title", schemaTitle);
        }
        if(schemaDescription != null){
            jsonRoot.put("description", schemaDescription);
        }
        jsonRoot.put("type", "object");

        ObjectNode properties = jsonRoot.putObject("properties");
        ObjectNode options = properties.putObject("options");
        options.put("type", "string");
        ArrayNode optionsEnum = options.putArray("enum");

        for (String key : values.keySet()) {
            optionsEnum.add(key);
        }

        ArrayNode required = jsonRoot.putArray("required");
        required.add("options");

        return jsonRoot;
    }

    /**
     * Add an option to the option map of this action.
     * This method should only be called before the action is registered
     * or to replace a mapping of an already existing option.
     * if it called with a new option Neuro would not get update about this action,
     * this action will to be re-registered (unregister then re-register it)
     * @param option The key of this option
     * @param value  The mapped value of this option
     * @return if this option was already registered
     */
    public boolean setOption(@NotNull String option, @NotNull T value){
        T previous = this.valueMap.put(option, value);
        if(previous == null){
            this.options.add(option);
            super.setSchema(this.rootNode);
            return false;
        }
        return true;
    }

    //TODO test this one
    /**
     * Remove this option from the options mapping.
     * This method should only be called before the action is registered
     * or it will need to be re-registered to be updated on Neuro side.
     * @param option the option key to remove
     * @return if the option was present and removed
     */
    public boolean removeOption(@NotNull String option){
        if(this.valueMap.remove(option) == null)
            return false;

        // Get index of the value
        AtomicInteger accumulator = new AtomicInteger(0);
        AtomicInteger index = new AtomicInteger(0);

        this.options.elements().forEachRemaining(
                nodeToTest -> {
                    if(option.contentEquals(nodeToTest.asText())){
                        index.set(accumulator.get());
                    }
                    accumulator.getAndIncrement();
                }
        );

        this.options.remove(index.get());
        super.setSchema(this.rootNode);
        return true;
    }

    /**
     * Remove every option of this mapping
     * This method should only be called before the action is registered,
     * or it will need to be re-registered to be updated on Neuro side.
     */
    public void clearOptions(){
        if(!this.options.isEmpty()){
            this.options.removeAll();
            super.setSchema(this.rootNode);
        }
    }

    /**
     * Get the mapping associated with the option name
     * @param option the option name
     * @return the value associated to it. null if absent
     */
    @Nullable
    public T get(@NotNull String option){
        return this.valueMap.get(option);
    }

    /**
     * Get the selected value from an action request
     * @param request the source action request
     * @return the value associated to it. null if absent or could not find the value.
     */
    @Nullable
    public T get(@NotNull ActionRequest request){
        if(request.data() != null){
            return get(request.data().get("options").asText());
        }
        return null;
    }

    /**
     * Get the action count
     * @return the number of mapped actions
     */
    public int size(){
        return this.valueMap.size();
    }


}
