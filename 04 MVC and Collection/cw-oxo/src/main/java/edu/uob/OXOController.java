package edu.uob;

import edu.uob.OXOMoveException.*;


//will update the model, which holds the data of the status of the game
//will sit in between both the Model and View classes, to update each one
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

        //convert the value of the character into an integer value : aA = 10, bB = 11
        //we want to fit it within the 2D board dimensions
        command = command.toUpperCase();
        int rowIndex = command.charAt(0) - 'A';
        int colIndex = command.charAt(1)- '1';

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

        //Check whether the win threshold has been achieved
        //we can check for all the possible combinations of winning from that cell
        checkWin(gameModel, currentPlayer);

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

        if(gameModel.getWinner() != null){
            return;
        };

        if(gameModel.isGameDrawn() == true){
            togglePlayer();
            resetDraw(gameModel);
        }

        gameModel.addRow();
    }

    public void removeRow() {

        if(gameModel.getWinner() != null || gameModel.getNumberOfRows() == 1){
            return;
        };

        gameModel.removeRow();
    }
    public void addColumn() {

        if(gameModel.getWinner() != null){
            return;
        };

        if(gameModel.isGameDrawn() == true){
            togglePlayer();
            resetDraw(gameModel);
        }

        gameModel.addColumn();
    }
    public void removeColumn() {

        if(gameModel.getWinner() != null || gameModel.getNumberOfColumns() == 1){
            return;
        };

        gameModel.removeColumn();
    }
    public void increaseWinThreshold() {

        if(gameModel.getWinner() != null){
            return;
        };

        gameModel.setWinThreshold(gameModel.getWinThreshold()+1);
    }
    public void decreaseWinThreshold() {

        if(gameModel.getWinner() != null){
            return;
        };

        gameModel.setWinThreshold(gameModel.getWinThreshold()-1);
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


    public void checkWin(OXOModel gameModel, Integer currentPlayer){

        checkWinHorizontal();

        if(gameModel.getWinner() == null){
            checkWinVertical();
        }

        if(gameModel.getWinner() == null){
            checkWinDiagonal();
        }
    }

    private void checkWinDiagonal() {

        //first diagonal across
        if(Math.min(gameModel.getNumberOfColumns(), gameModel.getNumberOfRows()) < gameModel.getWinThreshold()){
            return;
        }

        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                int currentThreshold = 0;
                for(int k = 0;;k++){
                    if(gameModel.getCellOwner(i, j) == null || i+k >= gameModel.getNumberOfRows() || j+k >=gameModel.getNumberOfColumns() || gameModel.getCellOwner(i, j) != gameModel.getCellOwner(i+k, j+k)){
                        break;
                    }else{
                        currentThreshold++;
                    }

                    if(currentThreshold == gameModel.getWinThreshold()){
                        gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
                        return;
                    }
                }
            }
        }

        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                int currentThreshold = 0;
                for(int k = 0;;k++){
                    if(gameModel.getCellOwner(i, j) == null ||  i+k>=gameModel.getNumberOfRows() ||  j - k >=gameModel.getNumberOfRows() || i + k < 0 || j-k < 0 || gameModel.getCellOwner(i, j) != gameModel.getCellOwner(i+k, j-k)){
                        break;
                    }else{
                        currentThreshold++;
                    }

                    if(currentThreshold == gameModel.getWinThreshold()){
                        gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
                        return;
                    }
                }
            }
        }

    }

    private void checkWinVertical() {
        int rows, columns;

    for(columns = 0; columns < gameModel.getNumberOfColumns(); columns++){
        int currThreshold = 0;
        for(rows = 0; rows < gameModel.getNumberOfRows(); rows++){
            if(gameModel.getCellOwner(rows, columns) == null || gameModel.getCellOwner(rows, columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
              currThreshold = 0;
            }else{
                currThreshold++;
            }

            if(currThreshold == gameModel.getWinThreshold()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
                return;
            }
        }

    }
    }

    private void checkWinHorizontal() {
        int rows, columns;

        for(rows = 0; rows < gameModel.getNumberOfRows(); rows++){
            int currentTreshold = 0;
            for(columns = 0; columns < gameModel.getNumberOfColumns(); columns++){
                if(gameModel.getCellOwner(rows, columns) == null || gameModel.getCellOwner(rows, columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                    currentTreshold = 0;
                }
                else{
                    currentTreshold++;
                }

                if(currentTreshold == gameModel.getWinThreshold()){
                    gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
                    return;
                }
            }
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


    //resets the "game draw" of the state
    public void resetDraw(OXOModel gameModel){

        gameModel.resetGameDrawn();

        return;

    }

    public void togglePlayer(){
        int number = gameModel.getNumberOfPlayers();
        currentPlayer = (currentPlayer + 1) % number;

        gameModel.setCurrentPlayerNumber(currentPlayer);
        
    }

    }
