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

        // if winner != null, allowed add rows and columns
        model.setWinner(model.getPlayerByNumber(model.getCurrentPlayerNumber()));

        controller.addRow();
        assertEquals((originalRow + 1), model.getNumberOfRows(), messageRow);

        controller.addColumn();
        assertEquals((originalCol + 1), model.getNumberOfColumns(), messageCol);

        //max number of rows and columns allowed to be added 9x9 board
        for(int i = 0; i < 100; i++){
            controller.addRow();
            controller.addColumn();
        }

        assertEquals(9,model.getNumberOfRows(),"Max Rows are supposed to be 9 - error");
        assertEquals(9,model.getNumberOfColumns(),"Max Columns are supposed to be 9 - error");

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



        // if checkGameDrawn = true, not allowed to remove rows and columns
        model.setGameDrawn();
        controller.removeRow();
        assertEquals((originalRow), model.getNumberOfRows(), messageRow);

        controller.removeColumn();
        assertEquals((originalCol), model.getNumberOfColumns(), messageCol);

        controller.resetDraw(model);



        // if winner != null, not allowed to remove rows and columns
        model.setWinner(model.getPlayerByNumber(model.getCurrentPlayerNumber()));

        controller.removeRow();
        assertEquals((originalRow), model.getNumberOfRows(), messageRow);

        controller.removeColumn();
        assertEquals((originalCol), model.getNumberOfColumns(), messageCol);

        controller.reset();


        //test if cells are populated, not allowed to remove row and column
        sendCommandToController("c3");

        controller.removeRow();
        assertEquals(originalRow, model.getNumberOfRows(), "Not allowed to remove row if cell is populated - error");

        controller.removeColumn();
        assertEquals(originalCol, model.getNumberOfColumns(), "Not allowed to remove row if cell is populated - error");


        controller.reset();

        //if removing rows causes a draw state, not allowed to remove rows and columns
        String messageNotDrawn = "Drawn detection not working";
        int originalRowNo = model.getNumberOfRows();
        int originalColNo = model.getNumberOfColumns();

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a3");  // Second Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c2");  // First Player

        assertEquals(true, model.isGameDrawn(), messageNotDrawn);

        //not allowed to remove row and column if it causes a draw/win state
        controller.addRow();
        controller.addColumn();

        controller.removeRow();
        controller.removeColumn();
        assertEquals(originalRowNo, model.getNumberOfRows(), "Allowed to remove row, but not allowed to remove column - will lead to draw - error");
        assertEquals(originalColNo + 1, model.getNumberOfColumns(), "Allowed to remove row, but not allowed to remove column - will lead to draw - error");

        controller.addRow();
        controller.removeColumn();
        controller.removeRow();
        assertEquals(originalRowNo+1, model.getNumberOfRows(), "Allowed to remove column, but not allowed to remove row - will lead to draw - error");
        assertEquals(originalColNo, model.getNumberOfColumns(), "Allowed to remove column, but not allowed to remove row - will lead to draw - error");


        controller.reset();
        controller.removeRow();
        controller.removeColumn();

        controller.reset();

    }

    @Test
    void testCheckWinDiagonal(){

        OXOPlayer firstPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());
        String messageNoWinner = "first player did not win the game - Diagonal";

        // Test first diagonal, win threshold (3) - 3x3 board
        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("b2");  // First Player
        sendCommandToController("c1");  // Second Player
        sendCommandToController("c3");  // First Player

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();


        // Test second diagonal, win threshold (3) - 3x3 board
        sendCommandToController("a3");  // First Player
        sendCommandToController("a1");  // Second Player
        sendCommandToController("b2");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c1");  // First Player

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();


        // Test first diagonal, win threshold (3) - 2x2 board
        controller.removeRow();
        controller.removeColumn();

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("b2");  // First Player
        sendCommandToController("a2");  // Second Player

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        // Test second diagonal, win threshold (3) - 2x2 board
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b1");  // First Player
        sendCommandToController("a1");  // Second Player

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        controller.addRow();
        controller.addColumn();

        // Test first diagonal, (minimum win threshold - 3) - 3x3 board
        controller.decreaseWinThreshold();
        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("b2");  // First Player

        assertEquals(null, model.getWinner(), messageNoWinner);
        controller.reset();



        // Test first diagonal, win threshold (4) - 4x4 board
        controller.addRow();
        controller.addColumn();
        controller.increaseWinThreshold();
        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("b2");  // First Player
        sendCommandToController("c1");  // Second Player
        sendCommandToController("c3");  // First Player
        sendCommandToController("d2");  // Second Player
        sendCommandToController("d4");  // First Player - winner

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test second diagonal, win threshold (4) - 4x4 board
        sendCommandToController("a4");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a1");  // Second Player
        sendCommandToController("c2");  // First Player
        sendCommandToController("d2");  // Second Player
        sendCommandToController("d1");  // First Player - winner

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
        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c1");  // First Player

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test vertical, win threshold (3) - 2x2 board
        controller.removeRow();
        controller.removeColumn();

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        controller.addRow();
        controller.addColumn();

        // Test vertical, win threshold (minimum win threshold - 3) - 3x3 board
        controller.decreaseWinThreshold();
        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b1");  // First Player


        assertEquals(null, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test vertical, win threshold (4) - 4x3 board
        controller.addRow();
        controller.increaseWinThreshold();
        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("d3");  // Second Player
        sendCommandToController("d1");  // First Player - winner

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
        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("a3");  // First Player - winner

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test horizontal, win threshold (3) - 2x2 board
        controller.removeRow();
        controller.removeColumn();

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player

        assertEquals(null,model.getWinner(), messageNoWinner);
        controller.reset();

        controller.addRow();
        controller.addColumn();


        // Test horizontal, win threshold (minimum win threshold - 3)  - 3x3 board
        controller.decreaseWinThreshold();
        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("a2");  // First Player

        assertEquals(null, model.getWinner(), messageNoWinner);
        controller.reset();

        // Test horizontal, win threshold (4) - 4x4 board
        controller.increaseWinThreshold();
        controller.addRow();
        controller.addColumn();

        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("a3");  // First Player
        sendCommandToController("c4");  // Second Player
        sendCommandToController("a4");  // First Player - Winner

        assertEquals(firstPlayer, model.getWinner(), messageNoWinner);
        controller.reset();

        // Reset board to 3x3 and win threshold (3)
        controller.removeRow();
        controller.removeColumn();
        controller.reset();

    }


    @Test
    void checkDraw(){

        String messageNotDrawn = "Drawn detection not working";

        //Play Game - 3x3 Board (Win threshold - 3)
        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a3");  // Second Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c2");  // First Player

        assertEquals(true,model.isGameDrawn(), messageNotDrawn);
        controller.reset();

    }

    @Test
    void checkReset(){

        int originalNumberOfPlayers = model.getNumberOfPlayers();
        int originalWinThreshold = model.getWinThreshold();
        String messageNotDrawn = "Drawn detection not working";

        //Reset Drawn Game
        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a3");  // Second Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c2");  // First Player

        assertEquals(true,model.isGameDrawn(), messageNotDrawn);

        controller.reset();
        String messageNotReset = "Reset not working - error";

        //check cells if they are empty after reset
        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j), "Cells are supposed to be empty after reset - error");
            }
        }

        assertEquals(false, model.isGameDrawn(), messageNotReset);
        assertEquals(originalNumberOfPlayers, model.getNumberOfPlayers(), "Number of Players has changed");
        assertEquals(originalWinThreshold, model.getWinThreshold(), "Win threshold has changed");


        //Reset Win Game (3x3 Board) (2 Players)
        sendCommandToController("a1");  // First Player
        sendCommandToController("b2");  // Second Player
        sendCommandToController("a2");  // First Player
        sendCommandToController("c1");  // Second Player
        sendCommandToController("b3");  // First Player
        sendCommandToController("a3");  // Second Player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), messageNotReset);

        controller.reset();

        assertEquals(null, model.getWinner(), messageNotReset);

        //check cells if they are empty after reset
        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j));
            }
        }

        //Reset Drawn Game (3x3 Board) (3 Players)
        OXOPlayer thirdPlayer = new OXOPlayer('A');
        model.addPlayer(thirdPlayer);

        sendCommandToController("a1");  // First Player
        sendCommandToController("b1");  // Second Player
        sendCommandToController("a2");  // Third Player
        sendCommandToController("b2");  // First Player
        sendCommandToController("b3");  // Second Player
        sendCommandToController("a3");  // Third Player
        sendCommandToController("c1");  // First Player
        sendCommandToController("c3");  // Second Player
        sendCommandToController("c2");  // Third Player

        assertEquals(true, model.isGameDrawn(), messageNotDrawn);

        controller.reset();

        //check cells if they are empty after reset
        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j), "Cells are supposed to be clear after reset - error");
            }
        }

        assertEquals(false, model.isGameDrawn(), messageNotReset);

        model.removePlayer(thirdPlayer);

    }


    @Test
    void testTogglePlayer(){

        int firstPlayer = model.getCurrentPlayerNumber();

        controller.togglePlayer();

        assertEquals(model.getPlayerByNumber(firstPlayer + 1), model.getPlayerByNumber(model.getCurrentPlayerNumber()),"Was supposed to toggle to Player 2 - error");
        controller.reset();

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

        assertEquals(model.getCellOwner(2,0), model.getPlayerByNumber(model.getNumberOfPlayers() - 1));    // Cell c1
        controller.reset();


    }
    @Test
    void testThrowsInvalidIdentifierLengthException(){

        String throwsComment = "Invalid Identifier Length";
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("dd1"), throwsComment);
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("d111"), throwsComment);
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("aa99"), throwsComment);
        assertThrows(InvalidIdentifierLengthException.class, ()-> sendCommandToController("bb2"), throwsComment);
        controller.reset();

    }

    @Test
    void testThrowsOutsideCellRange(){

        String throwsComment = "Outside Cell Range";
        assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("d4"), throwsComment);
        assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("a4"), throwsComment);
        assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("r2"), throwsComment);
        assertThrows(OutsideCellRangeException.class, ()-> sendCommandToController("z9"), throwsComment);
        controller.reset();

    }



    @Test
    void testThrowsInvalidIdentifierCharacterException(){

        String throwsComment = "Invalid Character";
        assertThrows(InvalidIdentifierCharacterException.class, ()->sendCommandToController("1a"), throwsComment);
        assertThrows(InvalidIdentifierCharacterException.class, ()->sendCommandToController("9z"), throwsComment);
        assertThrows(InvalidIdentifierCharacterException.class, ()->sendCommandToController("@z"), throwsComment);
        assertThrows(InvalidIdentifierCharacterException.class, ()->sendCommandToController("))"), throwsComment);
        controller.reset();

    }

    @Test
    void testThrowsCellAlreadyTakenException(){

        String throwsComment = "Cell Already Taken";
        sendCommandToController("a1");  //  First Player takes cell a1

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
        sendCommandToController("a3"); // First player - winner

        assertEquals(model.getWinner(), firstPlayer, "Player one was supposed to win but didn't");

        // not allowed to add or remove row and columns if winner = true
        controller.addRow();
        controller.addColumn();

        assertEquals(originalRow + 1, model.getNumberOfRows(),"Row size has not remained the same - error");
        assertEquals(originalCol + 1, model.getNumberOfColumns(),"Column size has not remained the same - error");

        controller.reset();

        //After reset, check the class attributes should be null
        assertEquals(null, model.getWinner(), "Reset with winner - error");

        for(int i = 0; i < model.getNumberOfRows(); i++){
            for(int j = 0; j < model.getNumberOfColumns(); j++){
                assertEquals(null, model.getCellOwner(i , j), "Cell has an owner - error");
            }
        }

        controller.removeRow();
        controller.removeColumn();

        //Play Game - 2 players (3x3 Board) (Win Threshold - 3)
        //Win Diagonal - First Diagonal
        sendCommandToController("a1"); // First player
        sendCommandToController("a2"); // Second player
        sendCommandToController("b2"); // First player
        sendCommandToController("b3"); // Second player
        sendCommandToController("c3"); // First player - winner

        assertEquals(firstPlayer,model.getWinner(),"Player X was supposed to win - error");

        controller.reset();


        //Play Game - 2 players (4x4 Board) (Win Threshold - 3)
        //Win Diagonal - Second Diagonal
        controller.addRow();
        controller.addColumn();

        sendCommandToController("a2"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b3"); // First player
        sendCommandToController("d1"); // Second player
        sendCommandToController("c4"); // First player - winner

        assertEquals(firstPlayer,model.getWinner(),"Player X was supposed to win - error");

        controller.reset();

        controller.removeRow();
        controller.removeColumn();


        //Play Game - 2 players (3x4 Board) (Win Threshold - 3)
        controller.addColumn();

        sendCommandToController("a1"); // First player
        sendCommandToController("b2"); // Second player
        sendCommandToController("a2"); // First player
        sendCommandToController("b3"); // Second player
        sendCommandToController("c3"); // First player
        sendCommandToController("b4"); // Second player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Second Player was supposed to win - error");

        controller.reset();
        controller.removeColumn();


        //Play Game - 2 players (4x3 Board) (Win Threshold - 3)
        controller.addRow();

        sendCommandToController("a1"); // First player
        sendCommandToController("b1"); // Second player
        sendCommandToController("a2"); // First player
        sendCommandToController("c1"); // Second player
        sendCommandToController("b2"); // First player
        sendCommandToController("d1"); // Second player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Second Player was supposed to win - error");

        controller.reset();
        controller.removeRow();


        //Play Game - 2 players (3x3 Board, Result: Draw, AddRow and AddColumn until winner)
        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("a2"); // First player
        sendCommandToController("b1"); // Second player
        sendCommandToController("b3"); // First player
        sendCommandToController("c2"); // Second player
        sendCommandToController("c1"); // First player
        sendCommandToController("b2"); // Second player
        sendCommandToController("c3"); // First player

        assertEquals(true,model.isGameDrawn(),  "Game is not Drawn - error");

        controller.addRow();
        controller.addColumn();

        sendCommandToController("c4"); // Second player
        sendCommandToController("d3"); // First player - Winner

        assertEquals(firstPlayer,model.getWinner(),  "Player one was supposed to win but didn't");
        controller.reset();

        //After using reset, board size and threshold should remain the same
        assertEquals(originalRow + 1, model.getNumberOfRows(),"Row size has not remained the same - error");
        assertEquals(originalCol + 1, model.getNumberOfColumns(),"Column size has not remained the same - error");

        //reset board to (3x3 board)
        controller.removeRow();
        controller.removeColumn();



        //Play Game - 2 players (3x3 Board, Result: CheckHorizontalWin) (minimum win threshold - 3)
        controller.decreaseWinThreshold();

        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b1"); // First player


        assertEquals(null,model.getWinner(),  "Player one was supposed to win but didn't");
        controller.reset();




        //Play Game - 2 players (3x3 Board, Result: CheckVerticalWin) (Win Threshold - 3)
        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b1"); // First player
        sendCommandToController("b2"); // Second player
        sendCommandToController("c1"); // First player  - winner

        assertEquals(firstPlayer,model.getWinner(),  "Player one was supposed to win but didn't");
        controller.reset();


        //Play Game - 2 players (3x3 Board, Result: CheckDiagonalWin (First Diagonal) ((Win Threshold - 3)
        sendCommandToController("a1"); // First player
        sendCommandToController("a3"); // Second player
        sendCommandToController("b2"); // First player
        sendCommandToController("b1"); // Second player
        sendCommandToController("c3"); // First player - winner

        assertEquals(firstPlayer, model.getWinner(), "Player one was supposed to win but didn't");
        controller.reset();

        //Play Game - 2 players (3x3 Board, Result: CheckDiagonalWin (Second Diagonal) (Win Threshold - 3)
        sendCommandToController("a3"); // First player
        sendCommandToController("a2"); // Second player
        sendCommandToController("b2"); // First player
        sendCommandToController("b1"); // Second player
        sendCommandToController("c1"); // First player

        assertEquals(firstPlayer, model.getWinner(), "Player one was supposed to win but didn't");
        controller.reset();

        assertEquals(originalWinThreshold, model.getWinThreshold(), "Win threshold has not increased remained the same - error");

        //Play with 3 players - (4x3 Board) (win threshold - 4) (Winner - Player A)
        controller.increaseWinThreshold();
        controller.addRow();
        OXOPlayer thirdPlayer = new OXOPlayer('A');
        model.addPlayer(thirdPlayer);  // Third Player Added


        sendCommandToController("a1");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("a3");   // Third Player
        sendCommandToController("b1");   // First Player
        sendCommandToController("b2");   // Second Player
        sendCommandToController("b3");   // Third Player
        sendCommandToController("c1");   // First Player
        sendCommandToController("c2");   // Second Player
        sendCommandToController("c3");   // Third Player
        sendCommandToController("d1");   // First Player - winner

        // Once winner = true, commands will not change the state of the board
        sendCommandToController("d2");   // Second Player
        sendCommandToController("d3");   // Third Player

        assertEquals(null, model.getCellOwner(3,1), "Cell owner has been set after winner = true");
        assertEquals(null, model.getCellOwner(3,2), "Cell owner has been set after winner = true");

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Player 'A' was supposed to win but didn't");

        controller.reset();
        controller.decreaseWinThreshold();


        //Play with 3 players - (4x3 Board) (win threshold - 3) (Winner - Player O)
        controller.decreaseWinThreshold();

        sendCommandToController("a1");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("a3");   // Third Player
        sendCommandToController("d3");   // First Player
        sendCommandToController("c1");   // Second Player
        sendCommandToController("b3");   // Third Player
        sendCommandToController("b2");   // First Player
        sendCommandToController("b1");   // Second Player
        sendCommandToController("d1");   // Third Player
        sendCommandToController("c3");   // First Player - winner

        assertEquals(firstPlayer, model.getWinner(), "Player 'A' was supposed to win but didn't");

        controller.reset();
        controller.increaseWinThreshold();


        //Play with 3 players - (4x8 Board) (win threshold - 4) (Winner - Player A)
        //Horizontal Win
        for(int i = 0; i < 5;i++) {
            controller.addColumn();
        }


        sendCommandToController("a1");   // First Player
        sendCommandToController("b8");   // Second Player
        sendCommandToController("c2");   // Third Player
        sendCommandToController("d6");   // First Player
        sendCommandToController("c1");   // Second Player
        sendCommandToController("c3");   // Third Player
        sendCommandToController("c7");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("c4");   // Third Player
        sendCommandToController("d3");   // First Player
        sendCommandToController("a6");   // Second Player
        sendCommandToController("c5");   // Third Player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Player 'A' was supposed to win but didn't");
        controller.reset();

        //Play with 3 players - (4x8 Board) (win threshold - 4) (Winner - Player A)
        //First Diagonal Win
        sendCommandToController("b1");   // First Player
        sendCommandToController("a1");   // Second Player
        sendCommandToController("a4");   // Third Player
        sendCommandToController("b2");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("b5");   // Third Player
        sendCommandToController("c2");   // First Player
        sendCommandToController("c1");   // Second Player
        sendCommandToController("c6");   // Third Player
        sendCommandToController("d1");   // First Player
        sendCommandToController("d2");   // Second Player
        sendCommandToController("d7");   // Third Player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Player 'A' was supposed to win but didn't");
        controller.reset();


        //Play with 3 players - (4x8 Board) (win threshold - 4) (Winner - Player A)
        //Second Diagonal Win
        sendCommandToController("c5");   // First Player
        sendCommandToController("b2");   // Second Player
        sendCommandToController("a6");   // Third Player
        sendCommandToController("c6");   // First Player
        sendCommandToController("b3");   // Second Player
        sendCommandToController("b5");   // Third Player
        sendCommandToController("c7");   // First Player
        sendCommandToController("b4");   // Second Player
        sendCommandToController("c4");   // Third Player
        sendCommandToController("b6");   // First Player
        sendCommandToController("a4");   // Second Player
        sendCommandToController("d3");   // Third Player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Player 'A' was supposed to win but didn't");
        controller.reset();

        //Three players - (4x8 Board) (win threshold - 4) (Winner - Player A)
        //Vertical Win
        sendCommandToController("a8");   // First Player
        sendCommandToController("b2");   // Second Player
        sendCommandToController("a4");   // Third Player
        sendCommandToController("b7");   // First Player
        sendCommandToController("c2");   // Second Player
        sendCommandToController("b4");   // Third Player
        sendCommandToController("c6");   // First Player
        sendCommandToController("d2");   // Second Player
        sendCommandToController("c4");   // Third Player
        sendCommandToController("a2");   // First Player
        sendCommandToController("d5");   // Second Player
        sendCommandToController("d4");   // Third Player - winner

        assertEquals(model.getPlayerByNumber(model.getCurrentPlayerNumber()), model.getWinner(), "Player 'A' was supposed to win but didn't");
        controller.reset();

        //Re-set to 3x3 Board (win threshold - 3)
        controller.removeRow();

        for(int i = 0; i < 5;i++) {
            controller.removeColumn();
        }

        controller.decreaseWinThreshold();


        //Test game with 4 players
        OXOPlayer fourPlayer = new OXOPlayer('B');
        model.addPlayer(fourPlayer);


        //Four Players - (3x3 Board) (win threshold - 3)
        //Game Drawn
        sendCommandToController("a1");   // First Player
        sendCommandToController("b1");   // Second Player
        sendCommandToController("c1");   // Third Player
        sendCommandToController("b2");   // Fourth Player
        sendCommandToController("c2");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("a3");   // Third Player
        sendCommandToController("b3");   // Fourth Player
        sendCommandToController("c3");   // First Player - winner

        assertEquals(fourPlayer, model.getCellOwner(1, 1), "Cell Owner is supposed to be Player B");
        assertEquals(null, model.getWinner(), "Player 'X' was supposed to win but didn't");

        controller.reset();


        //Four Players - (3x3 Board) (win threshold - 3) (Player X)
        //First Diagonal Win
        sendCommandToController("a1");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("a3");   // Third Player
        sendCommandToController("b1");   // Fourth Player
        sendCommandToController("b2");   // First Player
        sendCommandToController("b3");   // Second Player
        sendCommandToController("c1");   // Third Player
        sendCommandToController("c2");   // Fourth Player
        sendCommandToController("c3");   // First Player - winner

        assertEquals(fourPlayer, model.getCellOwner(2, 1), "Cell Owner is supposed to be Player B");
        assertEquals(firstPlayer, model.getWinner(), "Player 'X' was supposed to win but didn't");

        controller.reset();


        //Four Players - (3x3 Board) (win threshold - 3) (Player X)
        //Second Diagonal Win
        sendCommandToController("c1");   // First Player
        sendCommandToController("b1");   // Second Player
        sendCommandToController("c2");   // Third Player
        sendCommandToController("a1");   // Fourth Player
        sendCommandToController("b2");   // First Player
        sendCommandToController("b3");   // Second Player
        sendCommandToController("c3");   // Third Player
        sendCommandToController("a2");   // Fourth Player
        sendCommandToController("a3");   // First Player - winner

        assertEquals(firstPlayer, model.getWinner(), "Player 'X' was supposed to win but didn't");

        controller.reset();

        //Four Players - (3x3 Board) (win threshold - 3) (Player X)
        //Vertical Win
        sendCommandToController("a1");   // First Player
        sendCommandToController("a2");   // Second Player
        sendCommandToController("a3");   // Third Player
        sendCommandToController("b3");   // Fourth Player
        sendCommandToController("b1");   // First Player
        sendCommandToController("b2");   // Second Player
        sendCommandToController("c2");   // Third Player
        sendCommandToController("c3");   // Fourth Player
        sendCommandToController("c1");   // First Player - winner

        assertEquals(firstPlayer, model.getWinner(), "Player 'X' was supposed to win but didn't");

        controller.reset();


        //Four Players - (3x3 Board) (win threshold - 3) (Player X)
        //Horizontal Win
        sendCommandToController("a1");   // First Player
        sendCommandToController("b3");   // Second Player
        sendCommandToController("c3");   // Third Player
        sendCommandToController("c2");   // Fourth Player
        sendCommandToController("a2");   // First Player
        sendCommandToController("b2");   // Second Player
        sendCommandToController("b1");   // Third Player
        sendCommandToController("c1");   // Fourth Player
        sendCommandToController("a3");   // First Player - winner

        assertEquals(firstPlayer, model.getWinner(), "Player 'X' was supposed to win but didn't");

        controller.reset();


        //Four Players - (4x8 Board) (win threshold - 4) (Player B)
        controller.increaseWinThreshold();

        for(int i = 0; i < 2; i++) {
            controller.addRow();
        }

        for(int i = 0; i < 5;i++) {
            controller.addColumn();
        }


        //First Diagonal Win
        sendCommandToController("b3");   // First Player
        sendCommandToController("b4");   // Second Player
        sendCommandToController("d6");   // Third Player
        sendCommandToController("b2");   // Fourth Player

        sendCommandToController("c2");   // First Player
        sendCommandToController("d2");   // Second Player
        sendCommandToController("c4");   // Third Player
        sendCommandToController("c3");   // Fourth Player


        sendCommandToController("d1");   // First Player
        sendCommandToController("e1");   // Second Player
        sendCommandToController("d3");   // Third Player
        sendCommandToController("d4");   // Fourth Player

        sendCommandToController("c5");   // First Player
        sendCommandToController("d5");   // Second Player
        sendCommandToController("e3");   // Third Player
        sendCommandToController("e5");   // Fourth Player - winner

        assertEquals(fourPlayer, model.getWinner(), "Player 'B' was supposed to win but didn't");

        controller.reset();

        //Second Diagonal Win
        sendCommandToController("b5");   // First Player
        sendCommandToController("c8");   // Second Player
        sendCommandToController("a2");   // Third Player
        sendCommandToController("b8");   // Fourth Player

        sendCommandToController("c4");   // First Player
        sendCommandToController("d8");   // Second Player
        sendCommandToController("b2");   // Third Player
        sendCommandToController("c7");   // Fourth Player


        sendCommandToController("d4");   // First Player
        sendCommandToController("d7");   // Second Player
        sendCommandToController("c2");   // Third Player
        sendCommandToController("d6");   // Fourth Player

        sendCommandToController("e4");   // First Player
        sendCommandToController("e7");   // Second Player
        sendCommandToController("d3");   // Third Player
        sendCommandToController("e5");   // Fourth Player - winner

        assertEquals(fourPlayer, model.getWinner(), "Player 'B' was supposed to win but didn't");

        controller.reset();

        //Vertical Win
        sendCommandToController("a4");   // First Player
        sendCommandToController("a5");   // Second Player
        sendCommandToController("a3");   // Third Player
        sendCommandToController("a2");   // Fourth Player

        sendCommandToController("b4");   // First Player
        sendCommandToController("b5");   // Second Player
        sendCommandToController("b3");   // Third Player
        sendCommandToController("b2");   // Fourth Player


        sendCommandToController("c4");   // First Player
        sendCommandToController("c5");   // Second Player
        sendCommandToController("c3");   // Third Player
        sendCommandToController("c2");   // Fourth Player

        sendCommandToController("d5");   // First Player
        sendCommandToController("d3");   // Second Player
        sendCommandToController("d4");   // Third Player
        sendCommandToController("d2");   // Fourth Player - winner

        assertEquals(fourPlayer, model.getWinner(), "Player 'B' was supposed to win but didn't");

        controller.reset();

        //Horizontal Win
        sendCommandToController("a4");   // First Player
        sendCommandToController("b4");   // Second Player
        sendCommandToController("d4");   // Third Player
        sendCommandToController("c4");   // Fourth Player

        sendCommandToController("b3");   // First Player
        sendCommandToController("b5");   // Second Player
        sendCommandToController("d5");   // Third Player
        sendCommandToController("c5");   // Fourth Player


        sendCommandToController("c3");   // First Player
        sendCommandToController("b6");   // Second Player
        sendCommandToController("d6");   // Third Player
        sendCommandToController("c6");   // Fourth Player

        sendCommandToController("d3");   // First Player
        sendCommandToController("d7");   // Second Player
        sendCommandToController("b7");   // Third Player
        sendCommandToController("c7");   // Fourth Player - winner

        assertEquals(fourPlayer, model.getWinner(), "Player 'B' was supposed to win but didn't");

        //Four Players - (9x9 Board) (win threshold - 4) (Player B)

        controller.reset();

    }

    @Test
    void testDecreaseWinThreshold(){

        //set win threshold from 3 to 5
        for(int i = 0; i < 2; i++) {
            controller.increaseWinThreshold();
        }

        //before game starts
        int originalThreshold = model.getWinThreshold();
        controller.decreaseWinThreshold();

        assertEquals(originalThreshold - 1, model.getWinThreshold(), "Win threshold did not decrease - error");

        //during game starts
        sendCommandToController("a1");
        controller.decreaseWinThreshold();
        assertEquals(originalThreshold - 1, model.getWinThreshold(), "Win threshold decreased once game started - error");

        model.setWinner(controller.gameModel.getPlayerByNumber(0));
        controller.decreaseWinThreshold();
        assertEquals(originalThreshold - 2, model.getWinThreshold(), "Win threshold decreased once game has been won - error");

        controller.reset();

        //decrease winthreshold - minimum win threshold decrease
        for(int i = 0; i < 1000; i++){
            controller.increaseWinThreshold();
        }

        for(int i = 0; i < 2000; i++){
            controller.decreaseWinThreshold();
        }

        assertEquals(3, model.getWinThreshold(), "Win threshold shouldn't be lower than the minimum of 3 - error");

    }

    @Test
    void testIncreaseWinThreshold(){

        //win threshold can be increased at any time - before game starts
        int original = model.getWinThreshold();

        controller.increaseWinThreshold();

        assertEquals(original + 1, model.getWinThreshold(), "Win threshold increase before game starts - error");

        //during game
        sendCommandToController("a1");
        controller.increaseWinThreshold();

        assertEquals(original + 2, model.getWinThreshold(), "Win threshold should be allowed to increase during game - error");

        //after game won not allowed to change win threshold
        model.setWinner(model.getPlayerByNumber(model.getCurrentPlayerNumber()));
        controller.increaseWinThreshold();

        assertEquals(original + 3, model.getWinThreshold(), "Win threshold should be allowed to increase after game is won - error");
        controller.reset();

        //after game draw
        model.setGameDrawn();
        controller.increaseWinThreshold();

        assertEquals(original + 4, model.getWinThreshold(), "Win threshold should be allowed to increase after game is drawn - error");
        controller.reset();

    }

    @Test
    void testUserInput(){

        OXOPlayer firstPlayer = model.getPlayerByNumber(model.getCurrentPlayerNumber());

        //Testing for CAPS lock is being treated the same
        sendCommandToController("A1"); // First Player
        sendCommandToController("b1"); // Second Player
        sendCommandToController("B2"); // First Player
        sendCommandToController("c1"); // Second Player
        sendCommandToController("C3"); // First Player - Winner

        assertEquals(firstPlayer, model.getWinner(), "First Player should win the game - error");

        sendCommandToController("c2");
        assertEquals(null, model.getCellOwner(2,1), "Second Player should not occupy cell C2 once winner is found - error");

        controller.reset();


    }

    @Test
    void testMinimumMaxRowCol(){

        int largeNumber = 999;

        for(int i = 0; i < largeNumber; i++){
            controller.addRow();
            controller.addColumn();
        }

        assertEquals(9,model.getNumberOfRows(),"Max number of rows is 9 - error");
        assertEquals(9,model.getNumberOfColumns(),"Max number of columns is 9 - error");

        for(int i = 0; i < largeNumber; i++){
            controller.removeRow();
            controller.removeColumn();
        }

        assertEquals(1,model.getNumberOfRows(),"Minimum number of rows is 1 - error");
        assertEquals(1,model.getNumberOfColumns(),"Minimum number of columns is 1 - error");

        controller.reset();

    }

    @Test
    void testCheckGameStarted(){

        assertEquals(false, controller.checkGameStart(), "Game hasn't started with no cells populated - error");

        sendCommandToController("c3");

        assertEquals(true, controller.checkGameStart(), "Game has started - error");
        controller.reset();

    }

}
