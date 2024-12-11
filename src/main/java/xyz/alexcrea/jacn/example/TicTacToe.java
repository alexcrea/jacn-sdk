package xyz.alexcrea.jacn.example;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.NeuroSDK;
import xyz.alexcrea.jacn.NeuroSDKBuilder;
import xyz.alexcrea.jacn.NeuroSDKState;
import xyz.alexcrea.jacn.action.Action;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.example.game.TicTacToeCaseState;
import xyz.alexcrea.jacn.example.game.TicTacToeGame;
import xyz.alexcrea.jacn.example.game.TicTacToeLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TicTacToe {

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();

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

    private TicTacToeCaseState currentPlayer;
    private final TicTacToeGame game = new TicTacToeGame();
    private volatile NeuroSDK sdk;

    TicTacToe() {
        currentPlayer = TicTacToeCaseState.PLAYER1;
    }

    public void setSdk(NeuroSDK sdk) throws InterruptedException {
        this.sdk = sdk;

        synchronized (this) {
            wait(10000);
        }
        if (sdk.getState() != NeuroSDKState.CONNECTED) {
            System.err.println("Websocket took to much time to connect or an error occurred while connecting to it. " +
                    ("(Current state: " + sdk.getState()+")"));
            return;
        }

        startGame();
    }

    // Create one action per position
    // Can probably
    private @NotNull List<Action> getActionOnConnect() {
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
            @NotNull TicTacToeLocation loc,
            @NotNull ActionRequest request) {
        // Neuro tried to play.
        if (game.getState(loc) != TicTacToeCaseState.EMPTY) {
            // But it's on a case already used.
            // Just to be sure, unregister requested action
            sdk.registerActions(request.from());

            // And send a failure result with reason
            return new ActionResult(request, false, "Location is already used by a player.");
        }

        if (tryPlay(loc, TicTacToeCaseState.PLAYER2)) {
            // end turn
            synchronized (game) {
                game.notify();
            }

            // And say neuro she could play
            return new ActionResult(request, true, "You just played on " +
                    "row " + (loc.row() + 1) + " and column " + (loc.col() + 1));
        } else {
            // And neuro could not play
            return new ActionResult(request, false, "It is not your turn");
        }

    }

    private boolean tryPlay(@NotNull TicTacToeLocation loc, @NotNull TicTacToeCaseState state) {
        if (currentPlayer != state) {
            return false;
        }

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

        // Set the other player turn
        switch (state) {
            case PLAYER1 -> currentPlayer = TicTacToeCaseState.PLAYER2;
            case PLAYER2 -> currentPlayer = TicTacToeCaseState.PLAYER1;
        }

        return true;
    }

    public void onConnect(ServerHandshake handshake) {
        // Check status first
        int status = handshake.getHttpStatus();
        if ((status < 200 || status >= 300) && (status != 101)) {
            System.err.println("Got error while connecting to the websocket: " +
                    "(" + handshake.getHttpStatus() + ") " + handshake.getHttpStatusMessage() +
                    "\nIs Neuro or randy open ?");
            return;
        }

        System.out.println("Connected");
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

            hasWon = cliPlay(TicTacToeCaseState.PLAYER1, sc);
            if (hasWon) break;
            locations = game.getValidLocations();
            if (locations.isEmpty()) break;

            try {
                hasWon = neuroPlay(TicTacToeCaseState.PLAYER2);
                System.out.println(game.gameState());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        sc.close();
        sdk.close();
    }

    private boolean neuroPlay(@NotNull TicTacToeCaseState state) throws InterruptedException {
        // we own the game object so neuro can't play currently
        synchronized (game) {
            // We force actions all the possible actions
            sdk.forceActions(game.gameState(), "It is now your turn. " +
                            "You are currently player as the " + state.getPlayerRepresentation() + ". " +
                            "Please play on one of the empty Tic tac Toe case.",
                    sdk.getRegisteredActions());

            // We wait for neuro to play
            game.wait();
        }

        return game.hasWon();
    }

    private boolean cliPlay(@NotNull TicTacToeCaseState state, @NotNull Scanner sc) {
        System.out.println(game.gameState());

        System.out.println("It is your turn. please input the row and collum as \"row collum\"");
        return readCliPlay(sc, state);
    }

    private boolean readCliPlay(@NotNull Scanner sc, @NotNull TicTacToeCaseState state) {
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

            Integer col = parseIntOrNull(vals[0]);
            if (col == null || col <= 0 || col > 3) {
                System.out.println("Wrong colum value");
                continue;
            }

            TicTacToeLocation loc = new TicTacToeLocation(row - 1, col - 1);
            if (game.getState(loc) != TicTacToeCaseState.EMPTY) {
                System.out.println("There is already something here");
                continue;
            }

            if(!tryPlay(loc, state)){
                System.out.println("It is not your turn ? somehow ?");
                continue;
            }

            return game.hasWon();
        }
    }

    @Nullable
    @Contract(value = "null -> null", pure = true)
    private Integer parseIntOrNull(@Nullable String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
