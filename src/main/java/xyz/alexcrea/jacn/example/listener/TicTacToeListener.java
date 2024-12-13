package xyz.alexcrea.jacn.example.listener;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.alexcrea.jacn.action.ActionRequest;
import xyz.alexcrea.jacn.action.ActionResult;
import xyz.alexcrea.jacn.example.game.TicTacToeCaseState;
import xyz.alexcrea.jacn.example.game.TicTacToeGame;
import xyz.alexcrea.jacn.example.game.TicTacToeLocation;
import xyz.alexcrea.jacn.example.game.TicTacToeUtil;
import xyz.alexcrea.jacn.listener.AbstractSDKListener;
import xyz.alexcrea.jacn.sdk.NeuroSDK;

import java.net.ConnectException;

/**
 * An example of a neuro sdk listener.
 * Please see javadoc of overridden function for more information.
 */
public class TicTacToeListener extends AbstractSDKListener {

    private final @NotNull TicTacToeGame game;

    public TicTacToeListener(@NotNull TicTacToeGame game) {
        this.game = game;
    }

    // This function is called when the connection was successful.
    @Override
    public void onConnectSuccess(@NotNull ServerHandshake handshake, @NotNull NeuroSDK sdk) {
        synchronized (game) {
            // Notify the game on connect success.
            game.notify();
        }
    }

    // Theses 2 functions may be called when the connection is not successful.
    @Override
    public void onConnectFailed(@NotNull ServerHandshake handshake, @NotNull NeuroSDK sdk) {
        synchronized (game) {
            // Notify the game on connect failed.
            game.notify();
        }
    }

    @Override
    public void onConnectError(@NotNull ConnectException exception, @NotNull NeuroSDK sdk) {
        synchronized (game) {
            // Notify the game on connect failed.
            game.notify();
        }
    }

    // If you want to be informed when the connection close. you can extend this function.
    @Override
    public void onClose(@NotNull String reason, boolean remote, int code, @NotNull NeuroSDK sdk) {
        // Nothing for this example
    }


    // If you want to be informed
    @Override
    public void onError(@NotNull Exception exception, @NotNull NeuroSDK sdk) {
        // Nothing for this example
    }

    // Called when neuro try to play
    // Null should only be return if this listener do not handle request linked to the provided action.
    // See more information on the javadoc of this function.
    //
    // We know the request should have validated the schema provided on resource tictactoe_pos (see action creation)
    @Override
    public @Nullable ActionResult onActionRequest(@NotNull ActionRequest request, @NotNull NeuroSDK sdk) {
        if (!request.from().getName().contentEquals("play")) return null;

        if (game.getTurn() != TicTacToeCaseState.PLAYER2) {
            // We do not want Neuro to retry to action so we send a success even if it failed
            return new ActionResult(request, true, null);
        }

        // acquire game lock to ensure the main game loop is waiting
        synchronized (game) {
            // check again now that we own the game lock
            if (game.getTurn() != TicTacToeCaseState.PLAYER2) {
                // We do not want Neuro to retry to action so we send a success even if it failed
                return new ActionResult(request, true, null);
            }

            // Fetch location from data
            int row = request.data().get("row").asInt() - 1;
            int column = request.data().get("column").asInt() - 1;
            TicTacToeLocation location = new TicTacToeLocation(row, column);

            // Check location is available
            TicTacToeCaseState current = game.getState(location);
            if (current != TicTacToeCaseState.EMPTY) {
                // IMPORTANT NOTE:
                // YOU SHOULD NOT SEND A FORCE ACTION WHILE ANOTHER FORCE ACTION IS BEING EXECUTED
                // HERE IT IS SENT TO FIX RANDY NOT RETRYING FORCE ACTION
                // THIS SHOULD BE DELETED AS FAST AS IT IS FIXED
                sdk.forceActions("ONLY FOR RANDY.", request.from());

                return new ActionResult(request, false, "This case (row: " + (row + 1) + ", column: " + (column + 1) + ") " +
                        "is already occupied by player " + current.getPlayerRepresentation());
            }

            // Finally, we play
            if (!TicTacToeUtil.tryPlay(game, sdk, location, TicTacToeCaseState.PLAYER2)) {
                // We do not want Neuro to retry to action so we send a success even if it failed
                return new ActionResult(request, true, null);
            }

            // We unregister the action
            sdk.unregisterActions(request.from());

            // We notify the game loop so the main loop can
            game.notify();

            return new ActionResult(request, true, "You just played on " +
                    "row " + (row + 1) + " and column " + (column + 1));
        }
    }

}
