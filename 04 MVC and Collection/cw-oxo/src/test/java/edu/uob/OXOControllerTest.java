package edu.uob;

import edu.uob.OXOMoveException.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class OXOControllerTest {
    private OXOModel model;
    private OXOController controller;

    // Make a new "standard" (3x3) board before running each test case (i.e. this method runs before every `@Test` method)
    // In order to test boards of different sizes, winning thresholds or number of players, create a separate test file (without this method in it !)
    @BeforeEach
    void setup() {
        model = new OXOModel(3, 3, 3);
        model.addPlayer(new OXOPlayer('X'));
        model.addPlayer(new OXOPlayer('O'));
        controller = new OXOController(model);
    }

    // This next method is a utility function that can be used by any of the test methods to _safely_ send a command to the controller
    void sendCommandToController(String command) {
        // Try to send a command to the server - call will timeout if it takes too long (in case the server enters an infinite loop)
        // Note: this is ugly code and includes syntax that you haven't encountered yet
        String timeoutComment = "Controller took too long to respond (probably stuck in an infinite loop)";
        assertTimeoutPreemptively(Duration.ofMillis(1000), ()-> controller.handleIncomingCommand(command), timeoutComment);
    }

    @Test
    void testAddRowCol(){

        int originalRow = model.getNumberOfRows();
        int originalCol = model.getNumberOfColumns();

        String messageRow = "addRow method did not increase row size";
        String messageCol = "addColumn method did not increase column size";

        controller.addRow();
        assertEquals((originalRow +1), model.getNumberOfRows(), messageRow);

        controller.addColumn();
        assertEquals((originalCol +1), model.getNumberOfColumns(), messageCol);

        controller.removeRow();
        controller.removeColumn();

        // if checkGameDrawn = true, allowed to add rows and columns
        model.setGameDrawn();
        controller.addRow();
        assertEquals((originalRow +1), model.getNumberOfRows(), messageRow);

        controller.addColumn();
        assertEquals((originalCol +1), model.getNumberOfColumns(), messageCol);

        controller.removeRow();
        controller.removeColumn();
        controller.resetDraw(model);

        // if winner != null, not allowed to add rows and columns
        model.setWinner(model.getPlayerByNumber(model.getCurrentPlayerNumber()));

        controller.addRow();
        assertEquals((originalRow), model.getNumberOfRows(), messageRow);

        controller.addColumn();
        assertEquals((originalCol), model.getNumberOfColumns(), messageCol);

        controller.reset();

    }

    @Test
    void testRemoveRowCol(){

        int originalRow = model.getNumberOfRows();
        int originalCol = model.getNumberOfColumns();

        String messageRow = "removeRow method did not decrease row size";
        String messageCol = "removeColumn method did not decrease column size";

        controller.removeRow();
        assertEquals((originalRow - 1), model.getNumberOfRows(), messageRow);

        controller.removeColumn();
        assertEquals((originalCol - 1), model.getNumberOfColumns(), messageCol);

        controller.addRow();
        controller.addColumn();



        // if checkGameDrawn = true,  allowed to remove rows and columns
        model.setGameDrawn();
        controller.removeRow();
        assertEquals((originalRow - 1), model.getNumberOfRows(), messageRow);

        controller.removeColumn();
        assertEquals((originalCol - 1), model.getNumberOfColumns(), messageCol);

        controller.addRow();
        controller.addColumn();

        controller.resetDraw(model);



        // if winner != null, not allowed to remove rows and columns
        model.setWinner(model.getPlayerByNumber(model.getCurrentPlayerNumber()));

        controller.removeRow();
        assertEquals((originalRow), model.getNumberOfRows(), messageRow);

        controller.removeColumn();
        assertEquals((originalCol), model.getNumberOfColumns(), messageCol);

        controller.reset();
    }

    @Test
    void testCheckWinDiagonal(){

        OXOPlayer firstPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
        String messageNoWinner = "first player did not win the game - Diagonal";

        // Test first diagonal, win threshold (3) - 3x3 board
        sendCommandToController("a1");
        sendCommandToController("b1");
        sendCommandToController("b2");
        sendCommandToController("c1");
        sendCommandToController("c3");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();


        // Test second diagonal, win threshold (3) - 3x3 board
        sendCommandToController("a3");
        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("c3");
        sendCommandToController("c1");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();


        // Test first diagonal, win threshold (3) - 2x2 board
        controller.removeRow();
        controller.removeColumn();

        sendCommandToController("a1");
        sendCommandToController("b1");
        sendCommandToController("b2");
        sendCommandToController("a2");

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        // Test second diagonal, win threshold (3) - 2x2 board
        sendCommandToController("a2");
        sendCommandToController("b2");
        sendCommandToController("b1");
        sendCommandToController("a1");

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        controller.addRow();
        controller.addColumn();

        // Test first diagonal, win threshold (2) - 3x3 board
        controller.decreaseWinThreshold();
        sendCommandToController("a1");
        sendCommandToController("b1");
        sendCommandToController("b2");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();



        // Test first diagonal, win threshold (4) - 4x4 board
        controller.addRow();
        controller.addColumn();
        controller.increaseWinThreshold();
        sendCommandToController("a1");
        sendCommandToController("b1");
        sendCommandToController("b2");
        sendCommandToController("c1");
        sendCommandToController("c3");
        sendCommandToController("d2");
        sendCommandToController("d4");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();


        // Reset board to 3x3, win threshold (3)
        controller.removeColumn();
        controller.removeRow();

    }


    @Test
    void testCheckWinVertical(){

        OXOPlayer firstPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());

        String messageNoWinner = "first player did not win the game - Vertical";

        // Test vertical, win threshold (3) - 3x3 board
        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("b1");
        sendCommandToController("c3");
        sendCommandToController("c1");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test vertical, win threshold (3) - 2x2 board
        controller.removeRow();
        controller.removeColumn();

        sendCommandToController("a1");
        sendCommandToController("b1");
        sendCommandToController("a2");
        sendCommandToController("b2");

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        controller.addRow();
        controller.addColumn();

        // Test vertical, win threshold (2) - 3x3 board
        controller.decreaseWinThreshold();
        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("b1");


        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test vertical, win threshold (4) - 4x3 board
        controller.addRow();
        controller.increaseWinThreshold();
        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("b1");
        sendCommandToController("c3");
        sendCommandToController("c1");
        sendCommandToController("d3");
        sendCommandToController("d1");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Reset board to 3x3, win threshold (3)
        controller.removeRow();

    }

    @Test
    void testCheckWinHorizontal(){

        OXOPlayer firstPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
        String messageNoWinner = "First player did not win the game - Horizontal";

        // Test horizontal, win threshold (3) - 3x3 board
        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("a2");
        sendCommandToController("c3");
        sendCommandToController("a3");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test horizontal, win threshold (3) - 2x2 board
        controller.removeRow();
        controller.removeColumn();

        sendCommandToController("a1");
        sendCommandToController("b1");
        sendCommandToController("a2");
        sendCommandToController("b2");

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        controller.addRow();
        controller.addColumn();


        // Test horizontal, win threshold (2) - 3x3 board
        controller.decreaseWinThreshold();
        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("a2");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test horizontal, win threshold (4) - 4x4 board
        controller.increaseWinThreshold();
        controller.addRow();
        controller.addColumn();

        sendCommandToController("a1");
        sendCommandToController("b2");
        sendCommandToController("a2");
        sendCommandToController("c3");
        sendCommandToController("a3");
        sendCommandToController("c4");
        sendCommandToController("a4");

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Reset board to 3x3, win threshold (3)
        controller.removeRow();
        controller.removeColumn();

    }


    @Test
    void checkDraw(){

        String messageNotDrawn = "Drawn detection not working";

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a3");  // Second Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c2");  // First Player

        assertEquals(model.isGameDrawn(), true, messageNotDrawn);

    }

    @Test
    void checkReset(){

        String messageNotDrawn = "Drawn detection not working";

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a3");  // Second Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c2");  // First Player

        assertEquals(model.isGameDrawn(), true, messageNotDrawn);


        String messageNotReset = "Reset not working";

        controller.reset();

        //check cells if they are empty after reset
        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j));
            }
        }

        assertEquals(false, model.isGameDrawn(), messageNotReset);

    }


    @Test
    void testTogglePlayer(){

        int firstPlayer = model.getCurrentPlayerNumber();

        controller.togglePlayer();

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getPlayerByNumber(firstPlayer + 1));
    }

    @Test
    void testAddMorePlayer(){

        model.addPlayer(new OXOPlayer('A'));

        String messageNoPlayerAdded = "Player not added";
        assertEquals(3, model.getNumberOfPlayers(), messageNoPlayerAdded);

        sendCommandToController("a1");   //First Player
        sendCommandToController("b2");   //Second Player
        sendCommandToController("c3");   //Third Player
        sendCommandToController("a2");   //First Player
        sendCommandToController("b1");   //Second Player
        sendCommandToController("b3");   //Third Player
        sendCommandToController("c2");   //First Player
        sendCommandToController("a3");   //Second Player
        sendCommandToController("c1");   //Third Player

        assertEquals(model.getCellOwner(2,0), model.getPlayerByNumber(model.getNumberOfPlayers() - 1));    // c1


    }
    @Test
    void testThrowsInvalidIdentifierLengthException(){

        String throwsComment = "Invalid Identifier Length";
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("dd1"), throwsComment);
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("d111"), throwsComment);
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("aa99"), throwsComment);
    }

    @Test
    void testThrowsOutsideCellRange(){

        String throwsComment = "Outside Cell Range";
        assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("d4"), throwsComment);
        assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("a4"), throwsComment);
    }



    @Test
    void testThrowsInvalidIdentifierCharacterException(){

        String throwsComment = "Invalid Character";
        assertThrows(InvalidIdentifierCharacterException.class, ()->sendCommandToController("1a"), throwsComment);
        assertThrows(InvalidIdentifierCharacterException.class, ()->sendCommandToController("9z"), throwsComment);
    }

    @Test
    void testThrowsCellAlreadyTakenException(){

        String throwsComment = "Cell Already Taken";
        sendCommandToController("a1");     // First Player takes cell a1

        assertThrows(CellAlreadyTakenException.class, ()->sendCommandToController("a1"), throwsComment);   // Second player tries to take owned cell position, throws exception
        controller.reset();
    }

    @Test
    void testHandleIncomingCommands(){

        OXOPlayer firstPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());

        //Initialization - no winner should be declared
        assertEquals(null, model.getWinner(), "Initiation with winner - error");

        //Initialization - all do not have CellOwner
        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j), "Cell has an owner - error");
            }
        }

        assertEquals(false, model.isGameDrawn(), "gameDrawn set to true error");






        int originalRow = model.getNumberOfRows();
        int originalCol = model.getNumberOfColumns();
        int originalWinThreshold = model.getWinThreshold();

        //Play Game - 2 players (3x3 Board) (win threshold - 3)
        sendCommandToController("a1"); // First player
        sendCommandToController("b1"); // Second player
        sendCommandToController("a2"); // First player
        sendCommandToController("b2"); // Second player
        sendCommandToController("a3"); // First player

        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");

        // not allowed to add or remove row and columns if winner = true
        controller.addRow();
        controller.addColumn();

        assertEquals(originalRow, model.getNumberOfRows(),"Row size has not remained the same - error");
        assertEquals(originalCol, model.getNumberOfColumns(),"Column size has not remained the same - error");


        controller.reset();

        //After reset, check the class attributes should be null
        assertEquals(null, model.getWinner(), "Reset with winner - error");

        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j), "Cell has an owner - error");
            }
        }





        //Play Game - 2 players (3x3 Board, Result: Draw, AddRow and AddColumn until winner) (reduce win threshold from 3 to 2)
        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("a2"); // First player
        sendCommandToController("b1"); // Second player
        sendCommandToController("b3"); // First player
        sendCommandToController("c2"); // Second player
        sendCommandToController("c1"); // First player
        sendCommandToController("b2"); // Second player
        sendCommandToController("c3"); // First player

        assertEquals(model.isGameDrawn(), true, "Game is not Drawn - error");

        controller.addRow();
        controller.addColumn();

        sendCommandToController("c4"); // Second player
        sendCommandToController("d3"); // First player

        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");
        controller.reset();

        //After using reset, board size and threshold should remain the same
        assertEquals(originalRow + 1, model.getNumberOfRows(),"Row size has not remained the same - error");
        assertEquals(originalCol + 1, model.getNumberOfColumns(),"Column size has not remained the same - error");

        //reset board to (3x3 board)
        controller.removeRow();
        controller.removeColumn();



        //Play Game - 2 players (3x3 Board, Result: CheckHorizontalWin) (reduce win threshold from 3 to 2)
        controller.decreaseWinThreshold();

        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b1"); // First player


        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");
        controller.reset();




        //Play Game - 2 players (3x3 Board, Result: CheckVerticalWin) (reduce win threshold from 3 to 2)
        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b1"); // First player

        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");
        controller.reset();


        //Play Game - 2 players (3x3 Board, Result: CheckDiagonalWin (First Diagonal)) (reduce win threshold from 3 to 2)
        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b2"); // First player

        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");
        controller.reset();

        //Play Game - 2 players (3x3 Board, Result: CheckDiagonalWin (Second Diagonal)) (reduce win threshold from 3 to 2)
        sendCommandToController("a3"); // First player
        sendCommandToController("a2"); // Second player
        sendCommandToController("b2"); // First player

        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");
        controller.reset();



        //Reset win threshold from 2 to 3
        controller.increaseWinThreshold();
        assertEquals(originalWinThreshold, model.getWinThreshold(), "Win threshold has not increased remained the same - error");


        //Play with 3 players - (4x3 Board) (win threshold - 4)
        controller.increaseWinThreshold();
        controller.addRow();
        model.addPlayer(new OXOPlayer('A'));


        sendCommandToController("a1");   //First Player
        sendCommandToController("a2");   //Second Player
        sendCommandToController("a3");   //Third Player
        sendCommandToController("b1");   //First Player
        sendCommandToController("b2");   //Second Player
        sendCommandToController("b3");   //Third Player
        sendCommandToController("c2");   //First Player
        sendCommandToController("c1");   //Second Player
        sendCommandToController("c3");   //Third Player
        sendCommandToController("d1");   //First Player
        sendCommandToController("d2");   //Second Player
        sendCommandToController("d3");   //Third Player

        assertEquals(model.getWinner(), model.getPlayerByNumber(model.getCurrentPlayerNumber()), "Player 'A' was supposed to win but didn't");


    }

}
