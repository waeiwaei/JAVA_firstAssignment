package edu.uob;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//will update the model, which holds the data of the status of the game
//will sit in between both the Model and View classes, to update each one
public class OXOController {
    OXOModel gameModel;
    private static int currentPlayer = 0;

    public OXOController(OXOModel model) {
        gameModel = model;
    }

    public void handleIncomingCommand(String command) throws OXOMoveException {

        System.out.println(gameModel.getCurrentPlayerNumber());

        //convert the co-ordinates given into a range index - using private method
        //split the string into 2 parts - alphabet and number
        int value[] = commandParse(command);

        //convert the value of the character into an integer value : aA = 10, bB = 11
        //we want to fit it within the 2D board dimensions
        int rowIndex = value[0] - 1;
        int colIndex = value[1] - 1;

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

        int number = gameModel.getNumberOfPlayers();
        currentPlayer = (currentPlayer + 1) % number;

        gameModel.setCurrentPlayerNumber(currentPlayer);

    }


    public void addRow() {}
    public void removeRow() {}
    public void addColumn() {}
    public void removeColumn() {}
    public void increaseWinThreshold() {}
    public void decreaseWinThreshold() {}
    public void reset() {

        for(int i = 0; i < gameModel.getNumberOfRows(); i++){
            for(int j = 0; j < gameModel.getNumberOfColumns(); j++){
                gameModel.setCellOwner(i,j,null);
            }
        }
    }

    private int[] commandParse(String str) {

        int list[] = new int[2];
        int index = 0;
        //we want to extract the x and y index's from the command (str)
        //we create a new ArrayList to store the index's to store patterns which match
        //the regex identifies patterns in the command with 0-9 | a-z | A-Z ('+', one or more),
        str = str.toUpperCase();

        Pattern pt = Pattern.compile("[0-9]+|[a-z]+|[A-Z]+");
        Matcher extract = pt.matcher(str);

        while (extract.find()) {
            String strExtract = extract.group();

            if(index == 0) {

                list[index] = colIndexConv(strExtract);

            }

            if(index == 1){
                list[index] = Integer.parseInt(strExtract);
            }

            index++;
        }

        return list;
    }

    static int colIndexConv(String s) {
        int colValue = 0;
        for (int i = 0; i < s.length(); i++){
            colValue *= 26;
            colValue += s.charAt(i) - 'A' + 1;
        }
        return colValue;
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

        int index;
        int columns = gameModel.getNumberOfRows();

        //first diagonal across
        for(index = 0; index < gameModel.getNumberOfRows(); index++){
            if(gameModel.getCellOwner(index,index) == null ||gameModel.getCellOwner(index,index) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                break;
            }
        }

        if(index == gameModel.getNumberOfRows()){
            gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
            return;
        }

        for(index = 0; index < gameModel.getNumberOfRows(); index++){
            if(gameModel.getCellOwner(index, ((columns-1)-index)) == null || gameModel.getCellOwner(index, ((columns-1)-index)) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                break;
            }
        }

        if(index == gameModel.getNumberOfRows()){
            gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
            return;
        }

    }

    private void checkWinVertical() {
        int rows, columns;

        for(rows = 0; rows < gameModel.getNumberOfRows(); rows++){
            for(columns = 0; columns < gameModel.getNumberOfColumns(); columns++){
                if(gameModel.getCellOwner(columns, rows) == null || gameModel.getCellOwner(columns, rows) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                  break;
                }
            }

            if(columns == gameModel.getNumberOfRows()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
                return;
            }
        }
    }

    private void checkWinHorizontal() {
        int rows, columns;

        for(rows = 0; rows < gameModel.getNumberOfRows(); rows++){
            for(columns = 0; columns < gameModel.getNumberOfColumns(); columns++){
                if(gameModel.getCellOwner(rows, columns) == null || gameModel.getCellOwner(rows, columns) != gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber())){
                    break;
                }
            }

            if(columns == gameModel.getNumberOfRows()){
                gameModel.setWinner(gameModel.getPlayerByNumber(gameModel.getCurrentPlayerNumber()));
                return;
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


    }
