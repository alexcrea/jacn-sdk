package xyz.alexcrea.jacn.action;

import org.jetbrains.annotations.NotNull;

public record ActionResult (
        @NotNull Action from,
        boolean success
        // TODO add other things when API release
){
}
