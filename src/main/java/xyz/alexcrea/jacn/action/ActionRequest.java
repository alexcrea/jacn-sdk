package xyz.alexcrea.jacn.action;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/**
 * Represent an action requested from Neuro
 *
 * @param from the requested action
 * @param id   the action request id.
 *             It's only use is for the action result.
 * @param data A JSON that valid
 *             null if the action provided no schema.
 */
@NotNullByDefault
public record ActionRequest(
        Action from,
        String id,
        @Nullable JsonNode data
) {
}
