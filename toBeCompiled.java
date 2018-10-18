import java.io.*;
import java.net.*;

/* 
/\/\/\/ README \/\/\/\
1º Compilar
javac toBeCompiled.java

=> CASO FOR TESTAR NO MESMO HOST! Estrutura: java Start <IP_LOCAL> <CLIENT_PORT> <SERVER_PORT>
1º abrir um terminal-> java Start 127.0.0.1 9999 9998
2º abrir segundo terminal -> java Start 127.0.0.1 9998 9999

PS: caso for testar em hosts separados, só é necessário especificar o IP do outro cliente
Exemplo: java Start 192.168.0.101

*/

class Start {
    public static void main(String[] args){
        String ip, port, serverPort;
        if(args.length < 3){
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            ip = "";
            port = "9999";
            serverPort = "9999";
            try{
                while(ip.equals("")){
                    System.out.println("What's your friend's IP? ");
                    ip = br.readLine();
                }
            }catch(Exception e){System.out.println("Class(Start) Function(main)\nErro: " + e);}
        }else{
            ip = args[0];
            port = args[1];
            serverPort = args[2];
        }
        
        Chat chat = new Chat();
        chat.setIp(ip);
        chat.setPort(Integer.parseInt(port));
        chat.setServerPort(Integer.parseInt(serverPort));

        Thread t = new Thread(chat);
        t.start();
    }
}

class Chat implements Runnable{
    // server
    private int serverPort;

    // client
    private int port;
    private String ip;

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public void run(){
        try{
            // server
            ServerSocket serverSocket = new ServerSocket(serverPort);
            
            // client
            System.out.println("WARNING! Before continue make sure the other client is up!");
            System.out.println("PRESS ANY KEY TO CONTINUE!");
            System.in.read();

            Socket socket = new Socket(ip, port);          

            // Close socket connections
            CloseSockets close = new CloseSockets();
            close.setServerSocket(serverSocket);
            close.setClientSocket(socket);

            // server stuff
            Listener listener = new Listener();
            listener.setServerSocket(serverSocket);
            listener.setClose(close);
            Thread sThread = new Thread(listener); // thread for listener
            sThread.start();

            // client stuff
            Sender sender = new Sender();
            sender.setSocket(socket);
            sender.setClose(close);
            Thread cThread = new Thread(sender); // thread for sender
            cThread.start();

        }catch(Exception e){System.out.println("Class(Chat) Function(run)\nErro: " + e);}
    }
}

// to receive messages
class Listener implements Runnable{
    ServerSocket serverSocket;
    CloseSockets close;

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public void setClose(CloseSockets close) {
        this.close = close;
    }
    
    public void run(){
        try{
            Socket socket = serverSocket.accept();
            System.out.println("Connection established!");
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String temp;
            while((temp=in.readLine()) != null){
                if(temp.equals("FIM")){
                    Thread tClose = new Thread(close);
                    tClose.start();
                }
                System.out.println("> " + temp);
            }
        }catch(Exception e){System.out.println("Class(Listener) Function(run)\nErro: " + e);}
    }
}

// to send messages
class Sender implements Runnable{
    Socket socket;
    CloseSockets close;

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    public void setClose(CloseSockets close) {
        this.close = close;
    }
    public void run(){
        try{
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String temp;
            while((temp=br.readLine()) != null){	
                if(temp.equals("FIM")){
                    out.writeBytes(temp);
                    Thread tClose = new Thread(close);
                    tClose.start();
                }else{
                    out.writeBytes(temp + '\n'); 
                }
            }
        }catch(Exception e){System.out.println("Class(Sender) Function(run)\nErro: " + e);}
    }
}

class CloseSockets implements Runnable{
    
    ServerSocket serverSocket;
    Socket clientSocket;

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run(){
        try{
            serverSocket.close();
            clientSocket.close();
            System.exit(0);
        }catch(Exception e){System.out.println("Class(CloseSockets) Function(run)\nErro: " + e);}
    }
}
