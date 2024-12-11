package xyz.alexcrea.jacn.action;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ActionRequest(
        @NotNull Action from,
        @NotNull String id,
        @Nullable JsonNode data
) {
}
