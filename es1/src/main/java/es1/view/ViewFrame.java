package es1.view;

import akka.actor.ActorRef;
import es1.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class that manage the GUI of the Game and all elements of it.
 */
public class ViewFrame extends JFrame {

    private final String[] players = {"2","3","4"};
    private final String[] numbers = {"1","2","3","4"};
    private final String[] namePlayer = {"Pippo", "Gino", "Topolino", "HumanPlayer"};
    private final JPanel panelNorth = new JPanel();
    private final JPanel panelCenter = new JPanel();
    private final JPanel panelSouth = new JPanel();
    private final JTextArea textArea = new JTextArea("Welcome to Mistermind");
    private final JComboBox<String> player = new JComboBox<>(players);
    private final JComboBox<String> number = new JComboBox<>(numbers);;
    private final JButton button = new JButton("Start");
    private final JButton stop = new JButton("Stop");;
    private final JButton send = new JButton("Send Attempt");
    private final JButton submitVictory = new JButton("Send Victory");
    private final JButton sendVictory = new JButton("Try To Win");
    private final JCheckBox human = new JCheckBox("Add Human Player");
    private final JTextField input = new JTextField();
    private final JTextField choosePlayer = new JTextField();
    private final JTextField code1 = new JTextField();
    private final JTextField code2 = new JTextField();
    private final JTextField code3 = new JTextField();
    private final JTextField code4 = new JTextField();
    private final JLabel name, selectPlayer, selectCode, chooseNPlayer, codes;
    private final JScrollPane sp;
    private final Random rand = new Random(); //instance of random class

    public ViewFrame(ActorRef arbiter) {
        super("Assignment3 - Esercizio 1");
        setSize(900, 400);

        textArea.setRows(10);
        textArea.setColumns(25);
        sp = new JScrollPane(textArea);
        input.setColumns(5);
        choosePlayer.setColumns(5);
        code1.setColumns(5);
        code2.setColumns(5);
        code3.setColumns(5);
        code4.setColumns(5);
        selectPlayer = new JLabel("Select Numbers of Player: ");
        selectCode = new JLabel("Select Numbers of Code to Guess: ");
        chooseNPlayer = new JLabel("Choose Player to Send Attempt: ");
        codes = new JLabel("Write all Codes of other Players: ");
        name = new JLabel("Name: "+namePlayer[rand.nextInt(4)]+" - Attempt: ");

        panelNorth.add(selectPlayer);
        panelNorth.add(player);
        panelNorth.add(selectCode);
        panelNorth.add(number);
        panelNorth.add(button);
        panelNorth.add(stop);
        panelCenter.add(sp);
        panelSouth.add(human);
        panelSouth.add(name);
        panelSouth.add(input);
        panelSouth.add(chooseNPlayer);
        panelSouth.add(choosePlayer);
        panelSouth.add(send);
        panelSouth.add(submitVictory);
        panelSouth.add(codes);
        panelSouth.add(code1);
        panelSouth.add(code2);
        panelSouth.add(code3);
        panelSouth.add(code4);
        panelSouth.add(sendVictory);
        name.setVisible(false);
        input.setVisible(false);
        send.setVisible(false);
        chooseNPlayer.setVisible(false);
        choosePlayer.setVisible(false);
        codes.setVisible(false);
        code1.setVisible(false);
        code2.setVisible(false);
        code3.setVisible(false);
        code4.setVisible(false);
        sendVictory.setVisible(false);

        button.addActionListener((ActionEvent ev) -> {
            if(human.isSelected()){
                ArrayList<Integer> code = new ArrayList<>();
                for(int i = 0; i < input.getText().length(); i++) {
                    code.add(Integer.parseInt(String.valueOf(input.getText().charAt(i))));
                }
                arbiter.tell(new StartMsg(Integer.parseInt(numbers[number.getSelectedIndex()]), Integer.parseInt(players[player.getSelectedIndex()]), this, namePlayer[rand.nextInt(4)], code), ActorRef.noSender());
            }else{
                arbiter.tell(new StartMsg(Integer.parseInt(numbers[number.getSelectedIndex()]), Integer.parseInt(players[player.getSelectedIndex()]), this), ActorRef.noSender());

            }
        });
        stop.addActionListener((ActionEvent ev) -> arbiter.tell(new StopMsg(), ActorRef.noSender()));
        human.addActionListener(event -> {
            JCheckBox cb = (JCheckBox) event.getSource();
            if (cb.isSelected()) {
                name.setVisible(true);
                name.setText("Name: "+namePlayer[rand.nextInt(4)]+" - Attempt: ");
                input.setVisible(true);
                send.setVisible(true);
                chooseNPlayer.setVisible(true);
                choosePlayer.setVisible(true);
            } else {
                name.setVisible(false);
                input.setVisible(false);
                send.setVisible(false);
                chooseNPlayer.setVisible(false);
                choosePlayer.setVisible(false);
            }
        });
        submitVictory.addActionListener((ActionEvent ev) -> {
            name.setVisible(false);
            input.setVisible(false);
            send.setVisible(false);
            chooseNPlayer.setVisible(false);
            choosePlayer.setVisible(false);
            human.setVisible(false);
            codes.setVisible(true);
            code1.setVisible(true);
            code2.setVisible(true);
            switch (Integer.parseInt(players[player.getSelectedIndex()])) {
                case 3:
                    code3.setVisible(true);
                    break;
                case 4:
                    code3.setVisible(true);
                    code4.setVisible(true);
                    break;
            }
            sendVictory.setVisible(true);
        });
        sendVictory.addActionListener(e -> {
            ArrayList<Pair<Integer, ArrayList<Integer>>> code = new ArrayList<>();
            if (code1.isVisible()) {
                ArrayList<Integer> codes1 = new ArrayList<>();
                for(int i = 0; i < code1.getText().length(); i++) {
                    codes1.add(Integer.parseInt(String.valueOf(code1.getText().charAt(i))));
                }
                code.add(new Pair<>(0, codes1));
            }
            if (code2.isVisible()) {
                ArrayList<Integer> codes2 = new ArrayList<>();
                for(int i = 0; i < code2.getText().length(); i++) {
                    codes2.add(Integer.parseInt(String.valueOf(code2.getText().charAt(i))));
                }
                code.add(new Pair<>(1, codes2));
            }
            if (code3.isVisible()) {
                ArrayList<Integer> codes3 = new ArrayList<>();
                for(int i = 0; i < code3.getText().length(); i++) {
                    codes3.add(Integer.parseInt(String.valueOf(code3.getText().charAt(i))));
                }
                code.add(new Pair<>(2, codes3));
            }
            arbiter.tell(new HumanWinMsg(code), ActorRef.noSender());
        });
        send.addActionListener(e -> {
            ArrayList<Integer> code = new ArrayList<>();
            for(int i = 0; i < input.getText().length(); i++) {
                code.add(Integer.parseInt(String.valueOf(input.getText().charAt(i))));
            }
            arbiter.tell(new HumanPlayerAttempt(Integer.parseInt(choosePlayer.getText()), code), ActorRef.noSender());
        });

        getContentPane().add(panelNorth, BorderLayout.NORTH);
        getContentPane().add(panelCenter, BorderLayout.CENTER);
        getContentPane().add(panelSouth, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(-1);
            }
        });
        setVisible(true);
    }

    /**
     * Method that connect the Scrollbar to the TextArea.
     * @param message - Message to append in the text area.
     */
    public void addEvent(String message){
        textArea.append( "\n" + message);
        JScrollBar vertical = sp.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );

    }
}