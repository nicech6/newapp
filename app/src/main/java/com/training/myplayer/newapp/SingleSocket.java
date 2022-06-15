package com.training.myplayer.newapp;

import java.io.IOException;
import java.net.Socket;

public class SingleSocket {
    private Socket mSocket;

    private SingleSocket() {
    }

    private static class Holder {
        static SingleSocket SIGNAL = new SingleSocket();
    }

    public static SingleSocket getInstance() {
        return Holder.SIGNAL;
    }

    public Socket getSocket() {
        if (mSocket == null) {
            try {
                mSocket = new Socket(UdpServerSocket.INSTANCE.getIpv4(), 2000);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mSocket;
    }

    public void disConnect() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}