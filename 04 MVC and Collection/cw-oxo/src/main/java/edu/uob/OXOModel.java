package edu.uob;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class OXOModel {

    private ArrayList<ArrayList<OXOPlayer>> cells;
    private ArrayList<OXOPlayer> players;
    private int currentPlayerNumber;
    private OXOPlayer winner;
    private boolean gameDrawn;
    private int winThreshold;

    public OXOModel(int numberOfRows, int numberOfColumns, int winThresh) {
        winThreshold = winThresh;
        cells = new ArrayList<ArrayList<OXOPlayer>>();

        initialiseCells(numberOfRows, numberOfColumns);

        players = new ArrayList<OXOPlayer>();
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public void addPlayer(OXOPlayer player) {
        players.add(player);
    }

    public void removePlayer(OXOPlayer player){ players.remove(player); }

    public OXOPlayer getPlayerByNumber(int number) {
        return players.get(number);
    }

    public OXOPlayer getWinner() {
        return winner;
    }

    public void setWinner(OXOPlayer player) {
        winner = player;
    }

    public int getCurrentPlayerNumber() {
        return currentPlayerNumber;
    }

    public void setCurrentPlayerNumber(int playerNumber) {
        currentPlayerNumber = playerNumber;
    }

    public int getNumberOfRows() {
        return cells.size();
    }

    public int getNumberOfColumns() {
        return cells.get(0).size();
    }

    public OXOPlayer getCellOwner(int rowNumber, int colNumber) {
        return cells.get(rowNumber).get(colNumber);
    }

    public void setCellOwner(int rowNumber, int colNumber, OXOPlayer player) {
        cells.get(rowNumber).set(colNumber,player);
    }

    public void setWinThreshold(int winThresh) {
        winThreshold = winThresh;
    }

    public int getWinThreshold() {
        return winThreshold;
    }

    public void setGameDrawn() {
        gameDrawn = true;
    }

    public boolean isGameDrawn() {
        return gameDrawn;
    }

    public void resetGameDrawn(){
        this.gameDrawn = false;
    }

    public void initialiseCells(int numberOfRows, int numberOfColumns){
        for(int i=0;i<numberOfRows;i++){
            cells.add(new ArrayList<OXOPlayer>());
            for(int j=0;j<numberOfColumns;j++)
                cells.get(i).add(null);
        }
    }

    public void addRow(){
        cells.add(new ArrayList<OXOPlayer>());
        for(int i = 0; i < cells.get(0).size(); i++){
            cells.get(cells.size() - 1).add(null);
        }
    }

    public void addColumn(){
        for(int i=0;i<cells.size();i++){
            cells.get(i).add(null);
        }
    }

    public void removeRow(){

        cells.remove(cells.size()-1);

    }

    public void removeColumn(){

        for(int i=0;i<cells.size();i++){
            cells.get(i).remove(cells.get(i).size()-1);
        }

    }

}