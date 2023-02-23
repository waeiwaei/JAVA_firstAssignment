package edu.uob;

import edu.uob.OXOMoveException.*;

public class OXOController {
    OXOModel gameModel;
    private int currentPlayer = 0;

    public OXOController(OXOModel model) {
        gameModel = model;
    }

    public void handleIncomingCommand(String command) throws OXOMoveException {

        if(gameModel.getWinner() != null){
            return;
        };

        if(command.length() != 2) {
            throw new OXOMoveException.InvalidIdentifierLengthException(command.length());
        }

        if(Character.isLetter(command.charAt(0))==false){
            RowOrColumn row = RowOrColumn.ROW;
            throw new OXOMoveException.InvalidIdentifierCharacterException(row, command.charAt(0));
        }

        if(!(command.charAt(1)>='0' && command.charAt(1)<='9')){
            RowOrColumn col = RowOrColumn.COLUMN;
            throw new OXOMoveException.InvalidIdentifierCharacterException(col, command.charAt(1));
        }

        command = command.toUpperCase();
        int rowIndex = command.charAt(0) - 'A';
        int colIndex = command.charAt(1) - '1';

        if(rowIndex >= gameModel.getNumberOfRows()){
            RowOrColumn row = RowOrColumn.ROW;
            throw new OXOMoveException.OutsideCellRangeException(row, rowIndex+1);
        }
        if(colIndex >= gameModel.getNumberOfColumns()){
            RowOrColumn column = RowOrColumn.COLUMN;
            throw new OXOMoveException.OutsideCellRangeException(column, colIndex+1);
        }

        if(gameModel.getCellOwner(rowIndex, colIndex) != null) {
            throw new OXOMoveException.CellAlreadyTakenException(rowIndex + 1, colIndex + 1);
        }

        gameModel.setCellOwner(rowIndex, colIndex, gameModel.getPlayerByNumber(currentPlayer));

        checkWin(gameModel, rowIndex, colIndex);

        if(gameModel.getWinner() != null){
            return;
        }

        checkDraw();

        if(gameModel.isGameDrawn() == true){
            return;
        }

        togglePlayer();

    }


    public void addRow() {

        if(gameModel.isGameDrawn() == true){
            togglePlayer();
            resetDraw(gameModel);
        }

        if(gameModel.getNumberOfRows() < 9){
            gameModel.addRow();
        }
    }

    public void removeRow() {

        if(gameModel.isGameDrawn() == true || gameModel.getWinner() != null || gameModel.getNumberOfRows() == 1 || checkDrawRemoveRow() == true){
            return;
        };

        for(int i = 0; i < gameModel.getNumberOfColumns(); i++) {
            if (gameModel.getCellOwner(gameModel.getNumberOfRows() - 1, i) != null && gameModel.getWinner() == null) {
                return;
            }
        }

        gameModel.removeRow();
    }

    public void addColumn() {

        if(gameModel.isGameDrawn() == true){
            togglePlayer();
            resetDraw(gameModel);
        }

        if(gameModel.getNumberOfColumns() < 9) {
            gameModel.addColumn();
        }
    }


    public void removeColumn() {

        if(gameModel.isGameDrawn() == true || gameModel.getWinner() != null || gameModel.getNumberOfColumns() == 1 || checkDrawRemoveColumn() == true){
            return;
        };


        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            if(gameModel.getCellOwner(i,gameModel.getNumberOfColumns() - 1) != null && gameModel.getWinner() == null){
                return;
            }
        }

        gameModel.removeColumn();
    }


    public void increaseWinThreshold() {

        gameModel.setWinThreshold(gameModel.getWinThreshold()+1);
    }

    public void decreaseWinThreshold() {

        if(gameModel.getWinThreshold() == 3){
            return;
        }


        if(gameModel.getWinner() != null && checkGameStart() == true){
            gameModel.setWinThreshold(gameModel.getWinThreshold() - 1);
            return;
        }

        if(checkGameStart() == true){
            return;
        }


        gameModel.setWinThreshold(gameModel.getWinThreshold() - 1);

    }

    public boolean checkGameStart(){

        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                if(gameModel.getCellOwner(i,j) != null){
                    return true;
                }
            }
        }
        return false;
    }


    public void reset() {

        gameModel.setWinner(null);
        gameModel.resetGameDrawn();
        gameModel.setCurrentPlayerNumber(0);
        this.currentPlayer = 0;


        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                gameModel.setCellOwner(i , j,null);
            }
        }

        gameModel.setWinner(null);
    }


    public void checkWin(OXOModel gameModel, int rowIndex, int colIndex){

        checkWinHorizontal(rowIndex,colIndex);

        if(gameModel.getWinner() == null){
            checkWinVertical(rowIndex, colIndex);

        }

        if(gameModel.getWinner() == null){
            checkWinDiagonal(rowIndex,colIndex);
        }
    }



    private void checkWinDiagonal(int rowIndex, int colIndex) {

        if (Math.min(gameModel.getNumberOfColumns(), gameModel.getNumberOfRows()) < gameModel.getWinThreshold()) {
            return;
        }

        int i = 0, j = 0;
        while (rowIndex - i >= 0 && colIndex - i >= 0 && gameModel.getCellOwner(rowIndex - i, colIndex - i) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            i++;
        }

        while(rowIndex+j<gameModel.getNumberOfRows() && colIndex+j<gameModel.getNumberOfColumns() && gameModel.getCellOwner(rowIndex+j, colIndex+j) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())) {
            j++;
        }


        if( j + i - 1>=gameModel.getWinThreshold()) {
            gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
        }

        if (Math.min(gameModel.getNumberOfColumns(), gameModel.getNumberOfRows()) < gameModel.getWinThreshold()) {
            return;
        }


        int l = 0, m = 0;
        while (rowIndex + l < gameModel.getNumberOfRows() && colIndex - l >= 0 && gameModel.getCellOwner(rowIndex + l, colIndex - l) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            l++;
        }


        while(rowIndex - m >= 0 && colIndex + m < gameModel.getNumberOfColumns() && gameModel.getCellOwner(rowIndex - m, colIndex + m) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())) {
            m++;
        }


        if(l + m -1>=gameModel.getWinThreshold()) {
            gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
        }


    }



    private void checkWinVertical(int rowIndex, int colIndex) {
        int i = rowIndex, j = rowIndex;

        while(i>=0 && gameModel.getCellOwner(i, colIndex) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            i--;
        }

        while(j<gameModel.getNumberOfRows() && gameModel.getCellOwner(j,colIndex) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            j++;
        }

        if(j-i-1 >= gameModel.getWinThreshold()){
            gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
        }
    }

    private void checkWinHorizontal(int rowIndex, int colIndex) {
        int i = colIndex, j = colIndex;

        while(i>=0 && gameModel.getCellOwner(rowIndex, i) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            i--;
        }

        while(j<gameModel.getNumberOfColumns() && gameModel.getCellOwner(rowIndex, j) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            j++;
        }

        if(j-i-1 >= gameModel.getWinThreshold()){
            gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
        }
    }

    private void checkDraw() {

        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                if(gameModel.getCellOwner(i,j) == null){
                    return;
                }
            }
        }

        gameModel.setGameDrawn();

    }

    public boolean checkDrawRemoveRow(){

        for(int i = 0; i < gameModel.getNumberOfRows() - 1; i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                if(gameModel.getCellOwner(i,j) == null){
                    return false;
                }
            }
        }

        return true;
    }

    public boolean checkDrawRemoveColumn(){

        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns() - 1; j++){
                if(gameModel.getCellOwner(i,j) == null){
                    return false;
                }
            }
        }

        return true;
    }


    public void resetDraw(OXOModel gameModel){

        gameModel.resetGameDrawn();

    }

    public void togglePlayer(){
        int number = gameModel.getNumberOfPlayers();
        currentPlayer = (currentPlayer + 1) % number;

        gameModel.setCurrentPlayerNumber(currentPlayer);
        
    }


    }
