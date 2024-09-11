package chatroom.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

class Client extends Thread {
    private final Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    private void handleServerDisconnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        super.run();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                InputStream inputStream = socket.getInputStream();
                DataInputStream dataInputStream = new DataInputStream(inputStream);
                String message = dataInputStream.readUTF();
                System.out.println(message);
            } catch (IOException e) {
                System.out.println("Server disconnected");
                handleServerDisconnection();
                System.out.println("Enter any message to stop execution...");
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        final String ipAddress = "localhost";
        final int serverPort = 5050;
        try (Socket socket = new Socket(ipAddress, serverPort)) {
            Client client = new Client(socket);
            client.start();
            while (true) {
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                String message = scanner.nextLine();
                dataOutputStream.writeUTF(String.format("%s: %s", name, message));
            }
        } catch (IOException e) {
            System.out.println("Execution stopped");
        }
        scanner.close();
    }
}
