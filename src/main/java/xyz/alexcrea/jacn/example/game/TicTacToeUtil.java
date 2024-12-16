package xyz.alexcrea.jacn.example.game;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.sdk.NeuroSDK;

import java.util.Scanner;

/**
 * Function common to the two example that I did not wanted to introduce into the game class
 */
public class TicTacToeUtil {

    /**
     * Let the cli player play
     *
     * @param game  the tic-tac-toe game
     * @param sdk   the neuro sdk
     * @param sc    the user input scanner
     * @param state the case state owned by the playing player
     * @return if this play caused a win.
     */
    public static boolean cliPlay(@NotNull TicTacToeGame game, @NotNull NeuroSDK sdk,
                                  @NotNull Scanner sc, @NotNull TicTacToeCaseState state) {
        System.out.println(game.gameState());

        System.out.println("It is your turn. please input the row and column as \"row column\"");
        boolean hasWon = readCliPlay(game, sdk, sc, state);
        System.out.println(game.gameState(false));

        return hasWon;
    }

    /**
     * Read player input until they provide a valid output in an empty case.
     *
     * @param game  the tic-tac-toe game
     * @param sdk   the neuro sdk
     * @param sc    the user input scanner
     * @param state the case state owned by the playing player
     * @return if this play caused a win.
     */
    private static boolean readCliPlay(@NotNull TicTacToeGame game, @NotNull NeuroSDK sdk,
                                       @NotNull Scanner sc, @NotNull TicTacToeCaseState state) {
        while (true) {
            String line = sc.nextLine();
            String[] vals = line.trim().split(" ");
            if (vals.length < 2) {
                System.out.println("Missing value");
                continue;
            }

            Integer row = parseIntOrNull(vals[0]);
            if (row == null || row <= 0 || row > 3) {
                System.out.println("Wrong row value");
                continue;
            }

            Integer col = parseIntOrNull(vals[1]);
            if (col == null || col <= 0 || col > 3) {
                System.out.println("Wrong colum value");
                continue;
            }

            TicTacToeLocation loc = new TicTacToeLocation(row - 1, col - 1);
            if (game.getState(loc) != TicTacToeCaseState.EMPTY) {
                System.out.println("There is already something here");
                continue;
            }

            if (!tryPlay(game, sdk, loc, state)) {
                System.out.println("It is not your turn ? somehow ?");
                return false;
            }
            sdk.sendContext("Player " + TicTacToeCaseState.PLAYER1.getPlayerRepresentation() + " just played " +
                    "row " + (loc.row() + 1) + " column " + (loc.column() + 1), true);

            return game.hasWon();
        }
    }

    // Just a function that return either an int or null if could not parse the provided string
    @Nullable
    @Contract(value = "null -> null", pure = true)
    private static Integer parseIntOrNull(@Nullable String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Play a turn for player owning case state
     *
     * @param game  the tic-tac-toe game
     * @param sdk   the neuro sdk
     * @param loc   the location to play
     * @param state the state owned by the player.
     * @return true if could play. false otherwise.
     */
    public static boolean tryPlay(@NotNull TicTacToeGame game, @NotNull NeuroSDK sdk,
                                  @NotNull TicTacToeLocation loc, @NotNull TicTacToeCaseState state) {
        // Check player's turn
        if (game.getTurn() != state) {
            return false;
        }

        // Try to play
        boolean hasWon = game.play(loc, state);
        if (hasWon) {
            // Remove all actions
            for (Action action : sdk.getRegisteredActions()) {
                sdk.unregisterActions(action);
            }

            return true;
        }

        // Remove the action related to this location
        Action action = sdk.getAction(loc.actionName());
        if (action != null) sdk.unregisterActions(action);

        // Set to the other player turn
        game.switchTurn();
        return true;
    }

}
