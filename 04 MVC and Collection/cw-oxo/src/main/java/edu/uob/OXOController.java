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

        //Check whether the win threshold has been achieved
        //we can check for all the possible combinations of winning from that cell
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

        gameModel.addRow();
    }

    public void removeRow() {

        if(gameModel.isGameDrawn() == true || gameModel.getWinner() != null || gameModel.getNumberOfRows() == 1 || checkRemoveLastRowCausesWinDraw() == true){
            return;
        };

        //need to check if Row has values inside && gamewinner = null
        for(int i = 0; i < gameModel.getNumberOfColumns() - 1; i++) {
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

        gameModel.addColumn();
    }


    public void removeColumn() {

        if(gameModel.isGameDrawn() == true || gameModel.getWinner() != null || gameModel.getNumberOfColumns() == 1 || checkRemoveLastColumnCausesWinDraw() == true){
            return;
        };


        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            if(gameModel.getCellOwner(i,gameModel.getNumberOfColumns() - 1) != null && gameModel.getWinner() == null){
                return;
            }
        }

        gameModel.removeColumn(gameModel.getNumberOfRows() , gameModel.getNumberOfColumns());
    }


    public void increaseWinThreshold() {

        if(gameModel.getWinner() != null){
            return;
        };

        gameModel.setWinThreshold(gameModel.getWinThreshold()+1);
    }

    public void decreaseWinThreshold() {
        //win threshold can only be decreased before the start of a game
        int flag = 0;
        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                if(gameModel.getCellOwner(i,j) != null){
                    flag = 1;
                }
            }
        }

        if(gameModel.getWinner() != null){
            return;
        };

        if(flag == 0) {
            gameModel.setWinThreshold(gameModel.getWinThreshold() - 1);
        }
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

        //first diagonal across
        //if min(col,row) is less than winThreshold, the winThreshold cannot be reached by diagonal win
        //try implementing similar logic for vertical and horizontal win checking as well
        if (Math.min(gameModel.getNumberOfColumns(), gameModel.getNumberOfRows()) < gameModel.getWinThreshold()) {
            return;
        }

        //here, we use i, j as offsets to the pointer, instead of using the pointers directly (since, there would be two pairs of pointers required then)
        //to point to rowIndex and colIndex, simultaneously
        //Notice that (rowIndex-i,colIndex-i) goes back up along the "main diagonal"
        //So, we put conditions accordingly
        int i = 0, j = 0;
        while (rowIndex - i >= 0 && colIndex - i >= 0 && gameModel.getCellOwner(rowIndex - i, colIndex - i) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
            i++;
        }

        //In a similar way, we used the variable j for shoving offset in the positive direction of the "main diagonal"
        //Notice that (rowIndex+j,colIndex+j) goes down along the "main diagonal"
        while(rowIndex+j<gameModel.getNumberOfRows() && colIndex+j<gameModel.getNumberOfColumns() && gameModel.getCellOwner(rowIndex+j, colIndex+j) == gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())) {
            j++;
        }

        //finally, we find that j and i contains the no. of entries from the current one, how many consecutively have the
        //cell owner same as the current player
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


    public boolean checkWinDiagonalRemoveRow() {

        for(int i=0;i<gameModel.getNumberOfRows() - 1;i++){
            for(int j=0;j<gameModel.getNumberOfColumns();j++){
                int currThreshold = 0;
                for(int k=0;;k++){
                    if(gameModel.getCellOwner(i, j) == null || i+k >= gameModel.getNumberOfRows() || j+k >=gameModel.getNumberOfColumns() || gameModel.getCellOwner(i, j) != gameModel.getCellOwner(i+k, j+k)){
                        break;
                    }else {
                        currThreshold++;
                    }

                    if(currThreshold == gameModel.getWinThreshold()){
                        return true;
                    }
                }
            }
        }

        for(int i=0;i<gameModel.getNumberOfRows() - 1;i++){
            for(int j=0;j<gameModel.getNumberOfColumns();j++){
                int currThreshold = 0;
                for(int k=0;;k++){
                    if(gameModel.getCellOwner(i, j) == null || i+k>=gameModel.getNumberOfRows() || i-k < 0 || j-k <0 || gameModel.getCellOwner(i, j) != gameModel.getCellOwner(i+k, j-k)) {
                        break;
                    }else {
                        currThreshold++;
                    }

                    if(currThreshold == gameModel.getWinThreshold()){
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public boolean checkWinDiagonalRemoveColumn() {

        for(int i=0;i<gameModel.getNumberOfRows();i++){
            for(int j=0;j<gameModel.getNumberOfColumns() - 1;j++){
                int currThreshold = 0;
                for(int k=0;;k++){
                    if(gameModel.getCellOwner(i, j) == null || i+k >= gameModel.getNumberOfRows() || j+k >=gameModel.getNumberOfColumns() || gameModel.getCellOwner(i, j) != gameModel.getCellOwner(i+k, j+k)){
                        break;
                    }else {
                        currThreshold++;
                    }

                    if(currThreshold == gameModel.getWinThreshold()){
                        return true;
                    }
                }
            }
        }

        for(int i=0;i<gameModel.getNumberOfRows();i++){
            for(int j=0;j<gameModel.getNumberOfColumns() - 1;j++){
                int currThreshold = 0;
                for(int k=0;;k++){
                    if(gameModel.getCellOwner(i, j) == null || i+k>=gameModel.getNumberOfRows() || i-k < 0 || j-k <0 || gameModel.getCellOwner(i, j) != gameModel.getCellOwner(i+k, j-k)) {
                        break;
                    }else {
                        currThreshold++;
                    }

                    if(currThreshold == gameModel.getWinThreshold()){
                        return true;
                    }
                }
            }
        }

        return false;
    }


    public boolean checkWinHorizontalRemoveRow() {
        int rows, columns;

        for (rows = 0; rows < gameModel.getNumberOfRows() - 1; rows++) {
            int currThreshold = 0;
            for (columns = 0; columns < gameModel.getNumberOfColumns(); columns++) {
                if (gameModel.getCellOwner(rows, columns) == null || gameModel.getCellOwner(rows, columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())) {
                    currThreshold = 0;
                }else {
                    currThreshold++;
                }

                if (currThreshold == gameModel.getWinThreshold()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkWinHorizontalRemoveColumn() {
        int rows, columns;

        for (rows = 0; rows < gameModel.getNumberOfRows(); rows++) {
            int currThreshold = 0;
            for (columns = 0; columns < gameModel.getNumberOfColumns() - 1; columns++) {
                if (gameModel.getCellOwner(rows, columns) == null || gameModel.getCellOwner(rows, columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())) {
                    currThreshold = 0;
                }else {
                    currThreshold++;
                }

                if (currThreshold == gameModel.getWinThreshold()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkWinVerticalRemoveRow() {
        int rows, columns;

        for(columns = 0; columns < gameModel.getNumberOfColumns(); columns++){
            int currThreshold = 0;
            for(rows = 0; rows < gameModel.getNumberOfRows() - 1; rows++){
                if(gameModel.getCellOwner(rows,columns) == null || gameModel.getCellOwner(rows,columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                    currThreshold = 0;
                } else {
                    currThreshold++;
                }
                if(currThreshold == gameModel.getWinThreshold()){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkWinVerticalRemoveColumn() {
        int rows, columns;

        for(columns = 0; columns < gameModel.getNumberOfColumns() - 1; columns++){
            int currThreshold = 0;
            for(rows = 0; rows < gameModel.getNumberOfRows(); rows++){
                if(gameModel.getCellOwner(rows,columns) == null || gameModel.getCellOwner(rows,columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                    currThreshold = 0;
                } else {
                    currThreshold++;
                }
                if(currThreshold == gameModel.getWinThreshold()){
                    return true;
                }
            }
        }

        return false;
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

    public boolean checkRemoveLastColumnCausesWinDraw(){

        if(checkWinHorizontalRemoveColumn()){
            return true;
        }else if(checkWinVerticalRemoveColumn()){
            return true;
        }else if(checkWinDiagonalRemoveColumn()){
            return true;
        }else if(checkDrawRemoveColumn()){
            return true;
        }

        return false;
    }

    public boolean checkRemoveLastRowCausesWinDraw(){

        if(checkWinHorizontalRemoveRow()){
            return true;
        }else if(checkWinVerticalRemoveRow()){
            return true;
        }else if(checkWinDiagonalRemoveRow()){
            return true;
        }else if(checkDrawRemoveRow()){
            return true;
        }

        return false;
    }

    }
