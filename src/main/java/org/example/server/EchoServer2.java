package org.example.server;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.*;

@JsonInclude(JsonInclude.Include.NON_NULL) // Ignorar campos nulos

public class EchoServer2 {
    static Scanner entrada = new Scanner(System.in);

    //endereco padrao: 127.0.0.1
//   private static final int PORT = 10008; //10008
    private static final int THREAD_POOL_SIZE = 10;


    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        System.out.println("Digite a porta do servidor: ");
        int porta = entrada.nextInt();
        entrada.nextLine();
        try (

                ServerSocket serverSocket = new ServerSocket(porta)
        ) {
            System.out.println("Servidor iniciado na porta: " + porta);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Conexão aceita de: " + clientSocket.getRemoteSocketAddress());
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }
}