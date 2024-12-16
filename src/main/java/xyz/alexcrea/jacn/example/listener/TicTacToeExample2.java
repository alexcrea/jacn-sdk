package xyz.alexcrea.jacn.example.listener;

import org.jetbrains.annotations.NotNull;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.OptionMapAction;
import xyz.alexcrea.jacn.example.game.TicTacToeCaseState;
import xyz.alexcrea.jacn.example.game.TicTacToeGame;
import xyz.alexcrea.jacn.example.game.TicTacToeLocation;
import xyz.alexcrea.jacn.example.game.TicTacToeUtil;
import xyz.alexcrea.jacn.sdk.NeuroSDK;
import xyz.alexcrea.jacn.sdk.NeuroSDKBuilder;
import xyz.alexcrea.jacn.sdk.NeuroSDKState;

import java.util.List;
import java.util.Scanner;

/**
 * This is an example of how to use the neuro sdk with listener
 * For the listener part. see {@link xyz.alexcrea.jacn.example.listener.TicTacToeListener}
 */
public class TicTacToeExample2 {

    public static void main(String[] args) throws InterruptedException {
        // Instantiate game objects
        TicTacToeGame game = new TicTacToeGame();
        TicTacToeListener listener = new TicTacToeListener(game);

        // Create & build the sdk
        // We synchronize to ensures that this thread hold the execution context first.
        // If we do not, in a world, the following could occur (even if it is extremely unlikely due to timing. but we never know)
        // SDK init -> websocket init -> connection success & synchronized & notify call -> this thread synchronized & wait
        NeuroSDK sdk;
        synchronized (game) {
            sdk = new NeuroSDKBuilder("Tic Tac Toe")
                    // If you want to disable default callback //for me: TODO maybe allow nullable and maybe re think of default behavior
                    //.setOnConnect((handshake) -> {})
                    //.setOnClose((reason) -> {})
                    //.setOnError((error) -> {})

                    // If needed, change the websocket ip & port.
                    //.setAddress("example.com")
                    //.setPort(42)

                    // We may register action as soon as it start
                    //.addActionsOnConnect(action to register on startup here)

                    // Add the listener(s)
                    // You can add multiples listener if you prefer to divide your code
                    .addListeners(listener)
                    // And finally build
                    .build();

            // Wait for Neuro to connect before starting the game. or to fail to connect (see the Listener on connect & on connect error & failed)
            // On a normal game that allow neuro as same input as the behind the screen player it is not needed.
            // But in case of this example, it is.
            game.wait(10000);
        }

        // See if the websocket got connected
        if (sdk.getState() != NeuroSDKState.CONNECTED) {
            // Close websocket just in case
            sdk.close("Could not properly connect to the websocket");

            System.out.println("Solo CLI play is not implemented for this example. and we could not connect to a websocket...");
            return;
        }

        // Game loop
        // We always want to hold the game lock when the CLI player do not wait.
        synchronized (game) {
            TicTacToeExample2.gameLoop(game, sdk);
        }

        // Game finished. we close websocket
        sdk.close("Game finished");
    }

    // Assumed synchronized with game.
    private static void gameLoop(@NotNull TicTacToeGame game, @NotNull NeuroSDK sdk) throws InterruptedException {
        boolean hasWin = false;
        Scanner sc = new Scanner(System.in);

        // Instantiate the play action
        Action play = new Action("play", "Play a position in the tic tac toe grid.");
        play.setSchemaFromResource("example/tictactoe_pos.json");

        while (!hasWin) {
            // Check at least 1 empty location remaining to play
            List<TicTacToeLocation> possibleLocations = game.getValidLocations();
            if (possibleLocations.isEmpty()) break; // End by tie if break

            // CLI play
            hasWin = TicTacToeUtil.cliPlay(game, sdk, sc, TicTacToeCaseState.PLAYER1);
            if (hasWin) break;

            // Check at least 1 empty location remaining to play
            possibleLocations = game.getValidLocations();
            if (possibleLocations.isEmpty()) break; // End by tie if break

            hasWin = forceNeuroPlay(game, possibleLocations, sdk);

            // We just wait for a very small amount of time to let potential other Neuro action trying to synchronise discover it's not there turn
            // (it allow then to synchronise with game object)
            game.wait(0, 500);
        }

        // close the player input scanner.
        sc.close();

        // Send Neuro a last message indicating the result of the game.
        StringBuilder stb = new StringBuilder(game.gameState()).append("\n");
        if (hasWin) {
            TicTacToeCaseState winnerState;
            switch (game.getTurn()) {
                case PLAYER1 -> winnerState = TicTacToeCaseState.PLAYER2;
                case PLAYER2 -> winnerState = TicTacToeCaseState.PLAYER1;
                default -> throw new IllegalStateException("Unexpected value: " + game.getTurn());
            }

            if (winnerState == TicTacToeCaseState.PLAYER1) { // That mean player 2 just played.
                stb.append("You just win the game ! Congratulation.");
            } else {
                stb.append("Oh no... You just lost the game.");
            }
        } else { // nobody has won. a tie has happened.
            stb.append("The game resulted in a tie.");
        }

        sdk.sendContext(stb.toString(), false);
        sdk.close("Game finished");
    }

    /**
     * Send a force action to let neuro play and wait for her play.
     *
     * @param game              the tic-tac-toe game
     * @param possibleLocations list of possible location neuro can play
     * @param sdk               the neuro sdk
     * @return If the play of neuro resulted in a win.
     * @throws InterruptedException cause by wait on game
     */
    private static boolean forceNeuroPlay(@NotNull TicTacToeGame game,
                                          @NotNull List<TicTacToeLocation> possibleLocations,
                                          @NotNull NeuroSDK sdk) throws InterruptedException {
        // Check Neuro's turn obviously
        if (game.getTurn() != TicTacToeCaseState.PLAYER2) {
            System.err.println("It is not Neuro's turn.");
            return false;
        }

        // Create option action
        OptionMapAction<TicTacToeLocation> playAction = new OptionMapAction<>(
                "play",
                "Play a position in the tic tac toe grid.",
                "Tic Tac Toe position",
                "Selected legal location for a tic tac toe game");
        for (TicTacToeLocation loc : possibleLocations) {
            playAction.setOption(loc.actionName(), loc);
        }

        // Register and force play action
        if (!sdk.registerActions(playAction)) {
            System.err.println("Could not register play action");
            return false;
        }
        if (!sdk.forceActions(game.gameState(), "It is your turn. please play on a empty tic-tac-toe case.", playAction)) {
            System.err.println("Could not send send force play action");
            return false;
        }

        // Wait for neuro to play.
        // We should be on a synchronised with game section so we need to wait on game.
        game.wait();

        return game.hasWon();
    }

}
