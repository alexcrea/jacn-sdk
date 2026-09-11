package xyz.alexcrea.jacn.example.game;

import org.jetbrains.annotations.NotNullByDefault;

@NotNullByDefault
public enum TicTacToeCaseState {
    EMPTY(" "),
    PLAYER1("X"),
    PLAYER2("O"),
    ;

    private final String PlayerRepresentation;

    TicTacToeCaseState(String PlayerRepresentation) {
        this.PlayerRepresentation = PlayerRepresentation;
    }

    public String getPlayerRepresentation() {
        return PlayerRepresentation;
    }

}
