package xyz.alexcrea.jacn.example.callback;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.example.game.TicTacToeCaseState;
import xyz.alexcrea.jacn.example.game.TicTacToeGame;
import xyz.alexcrea.jacn.example.game.TicTacToeLocation;
import xyz.alexcrea.jacn.example.game.TicTacToeUtil;
import xyz.alexcrea.jacn.sdk.ForceActionPriority;
import xyz.alexcrea.jacn.sdk.NeuroSDK;
import xyz.alexcrea.jacn.sdk.NeuroSDKBuilder;
import xyz.alexcrea.jacn.sdk.NeuroSDKState;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This is an example of how to use the neuro sdk with only callbacks.
 * This example is currently dirty and should get a rewrite. it is recommended to see {@link xyz.alexcrea.jacn.example.listener.TicTacToeExample2} instead.
 */
@NotNullByDefault
public class TicTacToeExample1 {

    private final static Logger logger = LoggerFactory.getLogger(TicTacToeExample1.class);

    public static void main(String[] args) {
        TicTacToeExample1 game = new TicTacToeExample1();

        // Create the neuro SDK
        NeuroSDK sdk = new NeuroSDKBuilder("Tic Tac Toe")
                .setOnConnect(game::onConnect)
                .setActionsOnConnect(game.getActionOnConnect())
                .build();

        try {
            game.setSdk(sdk);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private final TicTacToeGame game = new TicTacToeGame();
    private volatile @Nullable NeuroSDK sdk;

    public void setSdk(NeuroSDK sdk) throws InterruptedException {
        this.sdk = sdk;

        synchronized (this) {
            wait(10000);
        }
        if (sdk.getState() != NeuroSDKState.CONNECTED) {
            logger.info("Websocket took to much time to connect or an error occurred while connecting to it. (Current state: {})", sdk.getState());
            return;
        }

        startGame();
    }

    private NeuroSDK getSdk() {
        var sdk = this.sdk;
        if(sdk == null) throw new IllegalStateException("Neuro sdk not yet initialized");
        return sdk;
    }

    // Create one action per position
    // Can probably
    private List<Action> getActionOnConnect() {
        List<Action> actions = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                // Prepare the actions to play on every row & column
                TicTacToeLocation loc = new TicTacToeLocation(row, col);
                Action action = new Action(
                        loc.actionName(),
                        "Play on the tic tac toe game the following position: " +
                                "Row " + (row + 1) + ", Col " + (col + 1),
                        request -> neuroTicTacToeAction(loc, request)
                );

                actions.add(action);
            }
        }

        return actions;
    }

    // Called when neuro request an action
    private ActionResult neuroTicTacToeAction(
            TicTacToeLocation loc,
            ActionRequest request) {
        // Neuro tried to play.
        if (game.getState(loc) != TicTacToeCaseState.EMPTY) {
            // But it's on a case already used.
            // Just to be sure, unregister requested action
            getSdk().registerActions(request.from());

            // And send a failure result with reason
            return new ActionResult(request, false, "Location is already used by a player.");
        }

        var error = TicTacToeUtil.tryPlay(game, getSdk(), loc, TicTacToeCaseState.PLAYER2);
        if(error.isPresent()){
            // And neuro could not play
            return new ActionResult(request, false, "It is not your turn");
        }

        // end turn
        synchronized (game) {
            game.notify();
        }

        // Finally respond neuro she was able to play
        return new ActionResult(request, true, "You just played on " +
                "row " + (loc.row() + 1) + " and column " + (loc.column() + 1));
    }

    public void onConnect(ServerHandshake handshake) {
        // Check status first
        int status = handshake.getHttpStatus();
        if ((status < 200 || status >= 300) && (status != 101)) {
            logger.error("Got error while connecting to the websocket: ({}) {}\nIs Neuro or randy open ?", handshake.getHttpStatus(), handshake.getHttpStatusMessage());
            return;
        }

        logger.info("Connected");
        synchronized (this) {
            notify();
        }
    }

    private void startGame() {
        Scanner sc = new Scanner(System.in);
        boolean hasWon = false;

        while (!hasWon) {
            List<TicTacToeLocation> locations = game.getValidLocations();
            if (locations.isEmpty()) break;

            hasWon = TicTacToeUtil.cliPlay(game, getSdk(), sc, TicTacToeCaseState.PLAYER1);
            if (hasWon) break;
            locations = game.getValidLocations();
            if (locations.isEmpty()) break;

            try {
                hasWon = neuroPlay(TicTacToeCaseState.PLAYER2);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        sc.close();
        getSdk().close();
    }

    private boolean neuroPlay(TicTacToeCaseState state) throws InterruptedException {
        // we own the game object so neuro can't play currently
        synchronized (game) {
            // We force action all the possible actions to make her select one of them
            NeuroSDK sdk = getSdk();
            sdk.forceActions(game.gameState(), "It is now your turn. " +
                            "You are currently player as the " + state.getPlayerRepresentation() + ". " +
                            "Please play on one of the empty Tic tac Toe case.",
                    sdk.getRegisteredActions(),
                    ForceActionPriority.MEDIUM // make neuro's speak shorter for a quicker respond
                    );

            // We wait for neuro to play
            game.wait();
        }

        return game.hasWon();
    }

}
