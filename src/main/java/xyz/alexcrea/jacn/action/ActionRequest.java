package xyz.alexcrea.jacn.action;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represent an action request from Neuro
 *
 * @param from the requested action
 * @param id   the action request id.
 *             It's only use is for the action result.
 * @param data A JSON that valid
 *             null if the action provided no schema.
 */
public record ActionRequest(
        @NotNull Action from,
        @NotNull String id,
        @Nullable JsonNode data
) {
}
