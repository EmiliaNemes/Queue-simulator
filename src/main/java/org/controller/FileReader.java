package org.controller;

import java.io.File;
import java.util.Scanner;

public class FileReader {
    private int numberOfClients;
    private int numberOfServers;
    private int simulationInterval;
    private int minArrivalTime;
    private int maxArrivalTime;
    private int minProcessingTime;
    private int maxProcessingTime; // max service time

    public FileReader(String filePath) {
        try {
            File file = new File(filePath); // se deschide fisierul primit ca parametru
            Scanner scanner = new Scanner(file);

            int i = 0;
            while(scanner.hasNextLine()){ // se verifica daca mai sunt linii in fisier
                switch (i) {
                    case 0 : String line = scanner.nextLine(); // se citeste numarul clientilor de pe prima linie ca si String
                             this.numberOfClients = Integer.parseInt(line);  // Stringul se converteste in int
                             break;
                    case 1 : line = scanner.nextLine(); // se citeste numarul cozilor/serverelor de pe urmatoarea linie
                             this.numberOfServers = Integer.parseInt(line); // Stringul se converteste in int
                             break;
                    case 2 : line = scanner.nextLine(); // se citeste durata simularii de pe urmatoarea linie
                             this.simulationInterval = Integer.parseInt(line); // Stringul se converteste in int
                             break;
                    case 3 : line = scanner.nextLine(); // se citesc de pe urmatoarea linie capetele intervalului in care trebuie sa fie timpul de sosire
                             String[] strings = line.split(","); // min si max arrival time-urile sunt separate prin virgula, se face split pe Stringul care contine tot randul din fisier pe baza virgulei
                             this.minArrivalTime = Integer.parseInt(strings[0]); // primul numar este minArrivalTime
                             this.maxArrivalTime = Integer.parseInt(strings[1]); // al doilea numar este maxArrivalTime
                             break;
                    case 4 : line = scanner.nextLine(); // se citesc de pe urmatoarea linie capetele intervalului in care trebuie sa fie timpul de procesare
                             strings = line.split(","); // min si max process time-urile sunt separate prin virgula, se face split pe Stringul care contine tot randul din fisier pe baza virgulei
                             this.minProcessingTime = Integer.parseInt(strings[0]); // primul numar este minProcessingTime
                             this.maxProcessingTime = Integer.parseInt(strings[1]); // al doilea numar este maxProcessingTime
                             break;
                }
                i++;
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public int getNumberOfClients() {
        return numberOfClients;
    }

    public int getNumberOfServers() {
        return numberOfServers;
    }

    public int getSimulationInterval() {
        return simulationInterval;
    }

    public int getMinArrivalTime() {
        return minArrivalTime;
    }

    public int getMaxArrivalTime() {
        return maxArrivalTime;
    }

    public int getMinProcessingTime() {
        return minProcessingTime;
    }

    public int getMaxProcessingTime() {
        return maxProcessingTime;
    }

}
