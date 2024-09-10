package chatroom.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

class Server extends Thread {
    private static final List<Socket> connectedClients = new Vector<>();
    private final Socket socket;

    public Server(Socket socket) {
        this.socket = socket;
    }

    private void handleClientDisconnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectedClients.remove(socket);
        Thread.currentThread().interrupt();
    }

    public String receiveMessage() {
        String message = "";
        try {
            InputStream inputStream = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            message = dataInputStream.readUTF();
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
            handleClientDisconnection();
        }
        return message;
    }

    public void broadcastMessage(String message) {
        for(Socket client : connectedClients) {
            try {
                OutputStream outputStream = client.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                dataOutputStream.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        super.run();

        while(true) {
            String message = receiveMessage();
            if(Thread.currentThread().isInterrupted()) {
                break;
            }
            broadcastMessage(message);
        }
    }

    public static void main(String[] args) {
        final int PORT = 5050;
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while(true) {
                Socket socket = serverSocket.accept();
                connectedClients.add(socket);
                Server serverInstance = new Server(socket);
                serverInstance.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
