package xyz.alexcrea.jacn.example.game;

public record TicTacToeLocation(int row, int column) {

    public String actionName(){
        return "play_row" + (row + 1) + "_col" + (column + 1);
    }

}
