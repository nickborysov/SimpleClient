package niks.simpleclient;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Client
 * <p>
 * Async task for connecting and messaging with server.
 * Connect to server and messaging while is not cancel.
 */

public class Client extends AsyncTask<Void, String, Void> {

    private String mHostIpAddress;
    private int mHostPort;
    private ArrayList<String> mMessagesFromServer;
    private MainActivity mActivity;
    private final String STATE_CONNECTION = "connection";
    private final String STATE_MESSAGING = "messaging";
    private int mTimeout = 1000;
    private int mBufferSize = 254;

    /**
     * Create client
     *
     * @param activity      for update message list and show status
     * @param hostIpAddress server ip address
     * @param hostPort      server port
     */
    public Client(MainActivity activity, String hostIpAddress,
                  int hostPort) {
        super();
        mHostIpAddress = hostIpAddress;
        mHostPort = hostPort;
        mActivity = activity;
        mMessagesFromServer = new ArrayList<>();
    }

    /**
     * Call when called in doInBackground. Can change UI. Sets state.
     *
     * @param values first element has state
     */
    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        switch (values[0]) {
            case STATE_CONNECTION:
                mActivity.setClientInfo("Connecting to " + mHostIpAddress + ":" + mHostPort);
                break;
            case STATE_MESSAGING:
                mActivity.setClientInfo("Messaging with server");
                mActivity.onListViewUpdate();
                break;
        }
    }

    /**
     * Connect to server and messaging
     *
     * @param args void
     * @return void
     */
    @Override
    protected Void doInBackground(Void... args) {

        //Connecting to server
        Socket socket = null;
        publishProgress(STATE_CONNECTION);
        // While client is not cancel try to connect to server
        while (!this.isCancelled()) {
            try {
                socket = new Socket();
                socket.setReceiveBufferSize(mBufferSize);
                socket.setSendBufferSize(mBufferSize);
                socket.connect(new InetSocketAddress(mHostIpAddress, mHostPort), mTimeout);
                if (socket.isConnected()) {
                    break;
                }
            } catch (IOException e) {
                // Do nothing, because client try connect to server more
            }
        }
        // If client canceled and is not connected return
        if (!socket.isConnected()) {
            System.err.println("Error connection to server");
            return null;
        }
        startMessaging(socket);
        return null;
    }

    /**
     * Start messaging with server on socket
     *
     * @param socket with connection to server
     */
    private void startMessaging(Socket socket) {
        // Create In and Out streams
        BufferedReader socketIn = null;
        PrintWriter socketOut = null;
        try {
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Can't get input and output streams");
        }

        // Send start message
        String StartMessage = ("Hello! I am " + android.os.Build.MODEL);
        socketOut.println(StartMessage);
        // Set state
        publishProgress(STATE_MESSAGING);
        long startTime = System.currentTimeMillis(); // Time of beginning messaging
        try {
            // While server is not cancel exchange messages
            while (!this.isCancelled()) {
                // Get message
                String message = socketIn.readLine();
                // Send response with time from beginning
                socketOut.println("Client received \"" + message +
                        "\" at " + (System.currentTimeMillis() - startTime) + " ms");
                // Add message to list
                mMessagesFromServer.add(message);
            }
        } catch (IOException e) {
            System.err.println("Error while read line");
        }
        try {
            socketOut.close();
            socketIn.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error while close: " + e.getMessage());
        }
    }

    /**
     * Call when client cancel. Clear message list and update UI
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        mMessagesFromServer.clear();
        mActivity.onListViewUpdate();
    }

    /**
     * Returns ArrayList with messages from server
     *
     * @return ArrayList with messages from server
     */
    public ArrayList<String> getMessagesFromServer() {
        return mMessagesFromServer;
    }
}
