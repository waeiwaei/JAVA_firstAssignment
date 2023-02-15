package edu.uob;

import java.awt.*;
import java.io.Serial;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class OXOGame extends Frame implements WindowListener, ActionListener, MouseListener, KeyListener {
    @Serial private static final long serialVersionUID = 4493180057657097249L;
    private static Font FONT = new Font("SansSerif", Font.PLAIN, 14);

    OXOController controller;
    TextField inputBox;
    OXOView view;

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        new OXOGame(250, 300);
    }

    //Game class which initates the game
    public OXOGame(int width, int height) {
        super("OXO Board");
        //creates a model object which takes in parameters of the information about the board
        //information is stored within the private attributes of the OXOModel class
        OXOModel model = new OXOModel(3, 3, 3);

        //Adding a player onto an array of players within the model class
        //also creates 2 new player instances with the 'X' and 'O'
        model.addPlayer(new OXOPlayer('X'));
        model.addPlayer(new OXOPlayer('O'));

        //creates a new controller instance - used to implement
        // the logic in the game, taking in the model created as a parameter
        //at this point, we have so far added 2 new players into the class
        controller = new OXOController(model);

        //textfield for users to input small amount of text
        //it adds a specified action listener to receive action
        //events from the text field and keys entered onto the object
        inputBox = new TextField("");
        inputBox.addActionListener(this);
        inputBox.setFont(FONT);
        inputBox.addKeyListener(this);

        //creates a new view instance of the display of the data from the model
        //which currently only contains the two players
        view = new OXOView(model);
        view.addMouseListener(this);
        view.addKeyListener(this);


        //creates a new panel in which the program can stick
        //components onto the panel
        Panel contentPane = new Panel();

        //setting the layout of the components e.g. inputbox and the view
        //onto the layout
        contentPane.setLayout(new BorderLayout());
        contentPane.add(inputBox, BorderLayout.SOUTH);
        contentPane.add(view, BorderLayout.CENTER);
        this.setLayout(new GridLayout(1, 1));
        this.add(contentPane);
        this.setSize(width, height);
        this.setVisible(true);
        this.addWindowListener(this);
    }

    //Object of borders within a container
    public Insets getInsets() {
        return new Insets(30, 7, 7, 7);
    }

    //will register a MOVE, and update the model data, subsequently
    //it will then call repaint, which will trigger the paintComponent method
    //to redraw the board
    public void actionPerformed(ActionEvent event) {
        try {
            String command = inputBox.getText();
            inputBox.setText("");
            controller.handleIncomingCommand(command);
            view.repaint();
        } catch (OXOMoveException exception) {
            System.out.println("Game move exception: " + exception);
        }
    }

    //changes the board of tictactoe when mouse is pressed
    //somehow removes rows and columns
    public void mousePressed(MouseEvent event) {
        if (event.getX() < 35) {
            if (event.isPopupTrigger()) controller.removeRow();
            else if (event.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK) controller.removeRow();
            else controller.addRow();
        }
        if (event.getY() < 35) {
            if (event.isPopupTrigger()) controller.removeColumn();
            else if (event.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK) controller.removeColumn();
            else controller.addColumn();
        }
        view.repaint();
    }

    public void keyPressed(KeyEvent event) {
        inputBox.setText(inputBox.getText().replace("=",""));
        inputBox.setText(inputBox.getText().replace("-",""));
        view.repaint();
    }

    public void keyReleased(KeyEvent event) {
        inputBox.setText(inputBox.getText().replace("=",""));
        inputBox.setText(inputBox.getText().replace("-",""));
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) controller.reset();
        view.repaint();
    }

    public void keyTyped(KeyEvent event) {
        if (event.getKeyChar() == '=') controller.increaseWinThreshold();
        if (event.getKeyChar() == '-') controller.decreaseWinThreshold();
        view.repaint();
    }

    public void mouseClicked(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
    public void windowActivated(WindowEvent event) {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event) {}
    public void windowClosed(WindowEvent event) {}
    public void windowOpened(WindowEvent event) {}

    public void windowClosing(WindowEvent e) {
        this.dispose();
        System.exit(0);
    }
}
