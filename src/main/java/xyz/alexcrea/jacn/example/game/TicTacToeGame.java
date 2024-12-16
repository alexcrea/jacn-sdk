package xyz.alexcrea.jacn.example.game;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Just a normal tic-tac-toe game
 */
public class TicTacToeGame {

    private volatile TicTacToeCaseState turn;
    private final TicTacToeCaseState[][] states;

    public TicTacToeGame() {
        this.states = new TicTacToeCaseState[3][3];
        for (int i = 0; i < 3; i++) {
            Arrays.fill(this.states[i], TicTacToeCaseState.EMPTY);
        }

        this.turn = TicTacToeCaseState.PLAYER1;
    }

    @NotNull
    public List<TicTacToeLocation> getValidLocations() {
        ArrayList<TicTacToeLocation> result = new ArrayList<>();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (this.states[row][col] == TicTacToeCaseState.EMPTY) {
                    result.add(new TicTacToeLocation(row, col));
                }
            }
        }

        return result;
    }

    public TicTacToeCaseState getTurn() {
        return turn;
    }

    public void switchTurn() {
        switch (this.turn) {
            case PLAYER1 -> this.turn = TicTacToeCaseState.PLAYER2;
            case PLAYER2 -> this.turn = TicTacToeCaseState.PLAYER1;
        }
    }

    public boolean play(@NotNull TicTacToeLocation loc, @NotNull TicTacToeCaseState player) {
        this.states[loc.row()][loc.column()] = player;

        return hasWon();
    }

    public boolean hasWon() {
        // Row check
        for (int row = 0; row < 3; row++) {
            TicTacToeCaseState startState = this.states[row][0];
            if (startState == TicTacToeCaseState.EMPTY) continue;

            for (int col = 1; true; col++) {
                TicTacToeCaseState current = this.states[row][col];
                if(current != startState) break;

                if(col == 2) return true;
            }
        }

        // Col check
        for (int col = 0; col < 3; col++) {
            TicTacToeCaseState startState = this.states[0][col];
            if (startState == TicTacToeCaseState.EMPTY) continue;

            for (int row = 1; true; row++) {
                TicTacToeCaseState current = this.states[row][col];
                if(current != startState) break;

                if(row == 2) return true;
            }
        }

        // Diagonal check
        TicTacToeCaseState middle = this.states[1][1];
        if(middle == TicTacToeCaseState.EMPTY) return false;

        if(this.states[0][0] == middle && this.states[2][2] == middle) return true;

        return this.states[2][0] == middle && this.states[0][2] == middle;
    }


    public TicTacToeCaseState getState(@NotNull TicTacToeLocation loc) {
        return this.states[loc.row()][loc.column()];
    }

    /*
     * Make a state like the following
     *
     * Tic Tac Toe current game state:
     *  X | O | X
     *    | X |
     *  O | O |
     *
     */
    public @NotNull String gameState(boolean prefixed) {
        StringBuilder stb = new StringBuilder();
        if(prefixed){
            stb.append("Tic Tac Toe current game state:");
        }

        for (int row = 0; row < 3; row++) {
            stb.append('\n').append(this.states[row][0].getPlayerRepresentation());
            for (int col = 1; col < 3; col++) {
                stb.append('|').append(this.states[row][col].getPlayerRepresentation());
            }

        }

        return stb.toString();
    }

    public @NotNull String gameState() {
        return gameState(true);
    }

}
