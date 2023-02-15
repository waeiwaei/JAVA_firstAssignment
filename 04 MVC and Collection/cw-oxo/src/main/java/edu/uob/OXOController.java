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
    private static int firstIndex = 0;


    public OXOController(OXOModel model) {
        gameModel = model;
    }

    public void handleIncomingCommand(String command) throws OXOMoveException {

        System.out.println(gameModel.getCurrentPlayerNumber());

        //convert the co-ordinates given into a range index - using private method
        //split the string into 2 parts - alphabet and number
        List <String> value = commandParse(command);

        //convert the value of the character into an integer value : aA = 10, bB = 11
        //we want to fit it within the 2D board dimensions
            //Question - how does the getNumericValue work, and if we are resizing the board
            //how should this code work for e.g. 'AA' rows or 'AAA'
        int rowIndex = (Character.getNumericValue(value.get(0).charAt(0))) - 10;
        int colIndex = Integer.parseInt(value.get(1)) - 1;

        //check the rowIndex and colIndex are within the scope of the board
        //else display error
        if(checkRowColRange(rowIndex, colIndex)){

            //we want to then update model on cell ownership
            if(gameModel.getCellOwner(rowIndex, colIndex) == null){

                //set cell owner of the row and column index
                gameModel.setCellOwner(rowIndex, colIndex, gameModel.getPlayerByNumber(currentPlayer));
            }

        }else{
            System.out.println("Error on screen");
        }

        //checks if the currentPlayer is more than the number of players
        if(currentPlayer < gameModel.getNumberOfPlayers() - 1){

            //we want to switch between players
            currentPlayer++;

        }else{
            //we re-set the currentPlayer to the first player when it goes beyond the number of players
            currentPlayer = firstIndex;
            gameModel.setCurrentPlayerNumber(currentPlayer);
            System.out.println(gameModel.getCurrentPlayerNumber());
        }

        gameModel.setCurrentPlayerNumber(currentPlayer);

    }

    public void addRow() {}
    public void removeRow() {}
    public void addColumn() {}
    public void removeColumn() {}
    public void increaseWinThreshold() {}
    public void decreaseWinThreshold() {}
    public void reset() {}

    private List<String> commandParse(String str) {

        //we want to extract the x and y index's from the command (str)
        //we create a new ArrayList to store the index's to store patterns which match
        //the regex identifies patterns in the command with 0-9 | a-z | A-Z ('+', one or more),
        List<String> list = new ArrayList<String>();
        Pattern pt = Pattern.compile("[0-9]+|[a-z]+|[A-Z]+");
        Matcher extract = pt.matcher(str);

        while (extract.find()) {
            list.add(extract.group());
        }

        return list;
    }


    public Boolean checkRowColRange(int rowIndex, int colIndex){

        //we need to check if it is within the range of the 2D array board
        if(rowIndex <= gameModel.getNumberOfRows()){
            System.out.println("Row index - " + rowIndex);
        }else{
            System.out.println("Error - row index is beyond board");
            return false;
        }

        if(colIndex <= gameModel.getNumberOfColumns()){
            System.out.println("Column index - " + colIndex);
        }else{
            System.out.println("Error - Col index is beyond board");
            return false;
        }

        return true;
    }

    }
