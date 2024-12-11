package xyz.alexcrea.jacn.example.game;

import org.jetbrains.annotations.NotNull;

public enum TicTacToeCaseState {
    EMPTY(" "),
    PLAYER1("X"),
    PLAYER2("O"),
    ;

    private final @NotNull String PlayerRepresentation;

    TicTacToeCaseState(@NotNull String PlayerRepresentation) {
        this.PlayerRepresentation = PlayerRepresentation;
    }

    public @NotNull String getPlayerRepresentation() {
        return PlayerRepresentation;
    }

}
