package xyz.alexcrea.jacn.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ActionRequest(
        @NotNull Action from,
        @NotNull String id,
        @Nullable String data //TODO correct JSON object
) {
}