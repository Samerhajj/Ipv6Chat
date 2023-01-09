package il.ac.kinneret.mjmay.ipv6chat;

import il.ac.kinneret.mjmay.ipv6chat.model.ListeningTask;
import il.ac.kinneret.mjmay.ipv6chat.model.SendTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;

import java.io.IOException;
import java.net.*;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

public class IPv6Controller implements Initializable {

    public TextField tfOtherIP;
    @FXML
    public ComboBox cbMyIP;
    @FXML
    public TextField tfMyPort;
    public TextField tfOtherPort;
    public Button bStart;
    public TextField tfMessage;
    public TextArea taLog;
    ServerSocket serverSocket;
    ListeningTask task;
    Thread listeningThread;

    /**
     * Start or stop listening to the messages coming.
     * @param event Ignored
     */
    public void toggleListen(ActionEvent event) {
        // if we're not lis tening, then listen
        if (bStart.getText().equals("Start")) {
            /// TODO: Fill in the start listening code, using the ListeningTask class
                try{
                    serverSocket = new ServerSocket(Integer.parseInt(tfMyPort.getText()));
                    taLog.appendText("Started Listening on : " + cbMyIP.getValue() + "\n");
                    task = new ListeningTask(serverSocket);
                    listeningThread = new Thread(task);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                    task.messageProperty().addListener(new ChangeListener<String>() {
                        @Override
                        public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                                    taLog.appendText(t1 + "\n");
                        }
                    });
            listeningThread.start();

            bStart.setText("Stop");
            bStart.setTextFill(Paint.valueOf("#f5210c"));
        } else if (bStart.getText().equals("Stop")) {

            /// TODO: Fill in the stop listening code
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listeningThread.interrupt();
            taLog.appendText("Stopped Listening + \n");
            bStart.setText("Start");
            bStart.setTextFill(Paint.valueOf("#2b8030"));
        }
    }

    /**
     * Uses the SendTask class to send a message using IPv6
     * @param event Ignored
     */
    public void sendMessage(ActionEvent event) {
        /// TODO: Fill in the send message code, using the SendTask class
        try {
            SendTask.sendMessage(tfOtherIP.getText(), Integer.parseInt(tfOtherPort.getText()), tfMessage.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the GUI with the available IPv6 addresses and such
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<InetAddress> interfaces = FXCollections.observableArrayList(getLocalIPs());
        cbMyIP.setItems(interfaces);

        tfMyPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                validatePort();
            }
        });

        tfOtherPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                validateOtherPort();
            }
        });

        tfOtherIP.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                validateOtherIP();
            }
        });
    }

    /**
     * Gets all of the available IPv6 addresses on the computer
     * @return A Vector with the addresses in it
     */
    public static Vector<InetAddress> getLocalIPs() {
        // make a list of addresses to choose from
        // add in the usual ones
        Vector<InetAddress> adds = new Vector<InetAddress>();
        try {
            adds.add(Inet6Address.getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            adds.add(Inet6Address.getByAddress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}));
        } catch (UnknownHostException ex) {
            // something is really weird - this should never fail
            System.out.println("Can't find IP addresses ::0 and ::1 " + ex.getMessage());
            ex.printStackTrace();
            return adds;
        }

        try {
            // get the local IP addresses from the network interface listing
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // see if it has an IPv4 address
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    // go over the addresses and add them
                    InetAddress add = addresses.nextElement();
                    // make sure it's an IPv6 address
                    if (!add.isLoopbackAddress() && add.getClass() == Inet6Address.class) {
                        adds.addElement(add);
                    }
                }
            }
        } catch (SocketException ex) {
            // can't get local addresses, something's wrong
            System.out.println("Can't get network interface information: " + ex.getLocalizedMessage());
        }
        return adds;
    }

    /**
     * Validate that the address in the multicast address box is valid
     *
     * @return True if the address is valid.  false otherwise
     */
    private boolean validateOtherIP() {

        if (tfOtherIP.getText().startsWith("/")) {
            tfOtherIP.setText(tfOtherIP.getText().substring(1));
        }
        try {
            Inet6Address otherAddress = (Inet6Address) Inet6Address.getByName(tfOtherIP.getText());

            setValidColor(tfOtherIP);
            return true;
        }
        catch (Exception ex) {
            setInvalidColor(tfOtherIP);
            return false;
        }
        }


    /**
     * Changes the textfield's background to show it's valid
     * @param tf  The textfield to modify
     */
    private void setValidColor(TextField tf) {
        tf.setStyle("-fx-background-color: white");
    }

    /**
     * Changes the textfield's background to show it's invalid
     * @param tf  The textfield to modify
     */
    private void setInvalidColor(TextField tf) {
        tf.setStyle("-fx-background-color: salmon");
    }

    /**
     * Checks if the port value given is ok
     * @return True if the port is valid.  False otherwise
     */
    private boolean validatePort() {
        int port = 0;
        try {
            port = Integer.parseInt(tfMyPort.getText());
            setValidColor(tfMyPort);
            if (port > 0 && port < 65535) {
                return true;
            }
        } catch (Exception ex) {
            setInvalidColor(tfMyPort);
        }
        return false;
    }

    /**
     * Checks if the port for the other computer is valid.
     * @return True if the port is valid.  False otherwise.
     */
    private boolean validateOtherPort() {
        int port = 0;
        try {
            port = Integer.parseInt(tfOtherPort.getText());
            setValidColor(tfOtherPort);
            if (port > 0 && port < 65535) {
                return true;
            }
        } catch (Exception ex) {
            setInvalidColor(tfOtherPort);
        }
        return false;
    }
}
