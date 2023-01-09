package il.ac.kinneret.mjmay.ipv6chat.model;

import javafx.concurrent.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

public class ListeningTask  extends Task<Void> {
    private final ServerSocket socket;

    public ListeningTask(ServerSocket socket) {
        this.socket = socket;
    }

    @Override
    protected Void call() throws Exception {

        /// TODO: Fill in the logic to listen for incoming messages on the server socket
        /// TODO: When a message arrives, use the message property of the Task to send it back to the GUI thread for printing
        /// Sample code:
        // updateMessage(
        //     String.format("%02d:%02d:%02d (from %s): %s", Calendar.getInstance().get(Calendar.HOUR),
        //                   Calendar.getInstance().get(Calendar.MINUTE),
        //                   Calendar.getInstance().get(Calendar.SECOND), clientAddress, line + "\n"));
        //              }


                while(!isCancelled()){
                    Socket clientSocket = socket.accept();
                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    BufferedReader brIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String line;
                    while((line= brIn.readLine())!=null)
                    {
                        updateMessage(
                                String.format("%02d:%02d:%02d (from %s): %s", Calendar.getInstance().get(Calendar.HOUR),
                                        Calendar.getInstance().get(Calendar.MINUTE),
                                        Calendar.getInstance().get(Calendar.SECOND), clientAddress, line + "\n"));

                    }
                }
        return null;
    }
}
