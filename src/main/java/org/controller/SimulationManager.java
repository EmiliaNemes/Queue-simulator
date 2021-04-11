package org.controller;

import org.model.Server;
import org.model.Task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.*;

public class SimulationManager implements Runnable{

    private int numberOfClients;
    private int numberOfServers;
    private int timeLimit; // maximum processing time
    private static AtomicInteger currentTime = new AtomicInteger();
    private int minArrivalTime;
    private int maxArrivalTime;
    private int minProcessingTime;
    private int maxProcessingTime; // max service time
    public static AtomicInteger numberOfProcessedClients = new AtomicInteger();
    public static AtomicInteger totalWaitingTime = new AtomicInteger();
    private static String inputFile;
    private static String outputFile;

    private List<Task> generatedTasks; // toate Task-urile generate
    private Scheduler scheduler; // responsabil de managementul cozilor si de distribuirea clientilor
    private List<Server> servers; // toate serverele generate

    public SimulationManager(){
        FileReader fileReader = new FileReader(inputFile);
        numberOfClients = fileReader.getNumberOfClients();
        numberOfServers = fileReader.getNumberOfServers();
        timeLimit = fileReader.getSimulationInterval();
        minArrivalTime = fileReader.getMinArrivalTime();
        maxArrivalTime = fileReader.getMaxArrivalTime();
        minProcessingTime = fileReader.getMinProcessingTime();
        maxProcessingTime = fileReader.getMaxProcessingTime();

        generateNRandomTasks(numberOfClients);
        generateNServers(numberOfServers);
        generateNThreads(numberOfServers);
        scheduler = new Scheduler(numberOfServers, numberOfClients, servers);
    }

    private void generateNRandomTasks(int N){
        generatedTasks = new ArrayList<Task>();

        int arrivalTime = 0;
        int processingTime = 0;
        Task task;

        for(int i = 1; i <= N; i++){
            Random rand = new Random();
            arrivalTime = rand.nextInt(maxArrivalTime - minArrivalTime) + minArrivalTime; // se genereaza un numar random in intervalul minArrivalTime si maxArrivalTime
            processingTime = rand.nextInt(maxProcessingTime - minProcessingTime) + minProcessingTime; // se genereaza un numar random in intervalul minProcessingTime si maxProcessingTime
            task = new Task(0, arrivalTime, processingTime); // se face un Task nou cu numerele generate
            generatedTasks.add(task); // se adauga in lista de task-uri
        }
        Collections.sort(generatedTasks); // se sorteaza lista de task-uri (pe baza timpilor de sosire)

        for(int i = 0; i < N; i++){
            generatedTasks.get(i).setID(i+1); // i se asigneaza un ID fiecarui task
        }
    }

    private void generateNServers(int N){
        servers = Collections.synchronizedList(new ArrayList<Server>()); // se face o lista de servere goala
        for(int i = 0; i < N; i++){
            Server server = new Server(); // se face un server gol
            servers.add(server); // se adauga in lista de servere
        }
    }

    private void generateNThreads(int N){
        for(int i = 0; i < N; i++){
            Thread thread = new Thread(servers.get(i)); // se face un thread nou, cu cun server
            thread.start(); // se porneste thread-ul
        }
    }

    private void printInOutputFile(PrintWriter file){
        file.println("Time " + currentTime.get()); // se scrie in fisier timpul curent al simularii
        file.print("Waiting clients: ");
        int ok = 0;
        for(Task r: generatedTasks){ // se scriu in fisier clientii in asteptare
            if(ok == 1){
                file.print("; ");
            }
            file.print(r);
            ok = 1;
        }
        file.println();

        int k = 1;
        for(Server s: servers){ // se scriu in fisier clientii fiecarei cozi
            ok = 0;
            file.print("Queue " + k + ": ");
            if(s.getTasks().isEmpty()){
                file.print("closed"); // daca coada este goala se scrie ca este "closed"
            } else {
                for (Task t : s.getTasks()) {
                    if (ok == 1) {
                        file.print("; ");
                    }
                    file.print(t);
                    ok = 1;
                }
            }
            k++;
            file.println();
        }
        file.println();
        file.println();
    }

    private void dispatchAndRemoveTasks(){
        int i = 0;
        List<Task> tasksToBeRemoved = new ArrayList<Task>(); // se face o lista cu task-urile care trebuie eliminate din lista clientilor in asteptare(generatedTasks)
        for(Task t: generatedTasks){
            if(t.getArrivalTime() == currentTime.get()){ // se cauta task-urile care au timpul de sosire egal cu timpul curent
                scheduler.dispatchTask(t); // task-ul respectiv este distribuit unei cozi
                tasksToBeRemoved.add(t); // task-ul se adauga in lista cu task-urile care trebuie eliminate din generatedTasks
            }
            i++;
        }

        for(Task t: tasksToBeRemoved){
            generatedTasks.remove(t); // se elimina task-urile din lista generatedTasks
        }
    }

    @Override
    public void run() {
        try {
            FileWriter FileEraser = new FileWriter(outputFile, false); // deschidem fisierul astfel incat sa se stearga continutul lui
        } catch (IOException e) {
            e.getMessage();
        }

        try(FileWriter fileWriter = new FileWriter(outputFile, true); // deschidem fisierul de output in mod append, ca sa-i tot adaugam linii
            BufferedWriter buffWriter = new BufferedWriter(fileWriter);
            PrintWriter file = new PrintWriter(buffWriter))
        {

        while(currentTime.get() <= timeLimit && areClients()){ // simularea se face pana cand nu am ajuns la timpul maxim de simulare sau pana cand se elibereaza toate cozile si nu mai sunt task-uri de procesat
            dispatchAndRemoveTasks(); // distribuire task-urilor cozilor si eliminarea lor din lista clientilor in asteptare
            Thread.sleep(1000); // punem thread-ul pe sleep pe o secunda, pentru a simula trecerea unei secunde (simularea timpului real)
            printInOutputFile(file); // scriem datele in fisierul de output
            currentTime.incrementAndGet(); // incrementam timpul curent al simularii
            changeServersTimes(); // schimbam time-urile in servere
        }
        float clientsNumber = numberOfProcessedClients.get();
        float waitingTime = totalWaitingTime.get();
        file.println("Avarage waiting time: " + waitingTime/clientsNumber); // calculam timpul mediu de asteptare pe baza clientilor procesati si a timpului lor de asteptare
        closeServers(); // inchiderea serverelor

        } catch (IOException | InterruptedException e) {
            e.getMessage();
        }
    }

    private boolean areClients(){
        boolean clientsInProcess = false;
        for(Server s: servers){
            if(!s.getTasks().isEmpty()){ // verificam daca cozile sunt goale
                clientsInProcess = true;
            }
        }
        return !generatedTasks.isEmpty() || clientsInProcess; // returnam true numai daca mai sunt clienti in asteptare sau mai sunt clienti in cozi
    }

     private void changeServersTimes(){
        for(Server s: servers){
            if(!s.getTasks().isEmpty()) { // verificam daca coada este goala
                if (s.getTasks().peek().getProcessingTime().get() > 0) {
                    s.getTasks().peek().setProcessingTime(s.getTasks().peek().getProcessingTime().get() - 1); // decrementam timpul de asteptare a primului client din coada
                    s.setWaitingPeriod(new AtomicInteger(s.getWaitingPeriod().get() - 1)); // decrementam timpul de eliberare a serverului
                }
            }
        }
    }

     private void closeServers(){
        for(Server s: servers){
            s.setCondition(false); // punem conditia de terminare a buclei while din clasa Server pe false
        }
    }

    public void setNumberOfProcessedClients(int numberOfProcessedClients) {
        this.numberOfProcessedClients = new AtomicInteger(numberOfProcessedClients);
    }

    public static void main(String[] args )
    {
        inputFile = args[0]; // stocam valoarea primului argument, care este fisierul de intrare in inputFile
        outputFile = args[1]; // stocam valoarea argumentului al doilea, care este fisierul de iesire in outputFile
        SimulationManager gen = new SimulationManager();
        Thread t = new Thread(gen); // facem thread-ul principal
        t.start(); // pornim thread-ul
    }
}
