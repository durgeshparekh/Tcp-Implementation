package com.durgeshparekh.tcp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

class TcpClient {
    private static final String TAG = TcpClient.class.getSimpleName();
    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    TcpClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mBufferOut != null) {
                    Log.e(TAG, "Sending: " + message);
                    mBufferOut.println(message);
                    mBufferOut.flush();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    /**
     * Close the connection and release the members
     */
    void stopClient() {

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    void run(String serverIP) {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            Log.e("TCP Client", "C: Connecting...");

            Log.e(TAG, "run: "+ serverAddr );
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, 13002);

            try {
                Log.e(TAG, "IsConnected: "+ socket.isConnected() );
                //sends the message to the server
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                        true);

                //receives the message which the server sends back
                mBufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    mServerMessage = mBufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(mServerMessage);
                    }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
