package org.model;

import org.controller.SimulationManager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.*;

public class Server implements Runnable{
    private LinkedBlockingQueue<Task> tasks;
    private AtomicInteger waitingPeriod; // intervalul dupa care s-ar elibera serverul
    private boolean condition = true; //devine false doar dupa ce toate Task-urile generate sunt procesate

    public Server(){
        this.tasks = new LinkedBlockingQueue<Task>();
        this.waitingPeriod = new AtomicInteger();
    }

     synchronized public void addTask(Task newTask){
        tasks.add(newTask); // se adauga Task-ul primit ca parametru in lista de Task-uri
        waitingPeriod.addAndGet(newTask.getProcessingTime().get()); // waitingPeriod se mareste cu processingTime-ul urmatorului client/task
         newTask.setWaitingTime(waitingPeriod); // timpul total de asteptare a clientului se seteaza
    }

    public void run(){
        while(condition) {
            if (!tasks.isEmpty()) { // se verifica daca exista clienti in coada
                Task currentTask = tasks.peek(); // se ia primul element din coada (dar nu se scoate)
                    try { // se pune thread-ul pe sleep pe atata secunde cat este timpul de procesare a Task-ului
                        Thread.sleep( currentTask.getProcessingTime().get() * 1000);
                    } catch (InterruptedException e) {
                        e.getMessage();
                    }
                try {
                    removeTask(currentTask); // dupa ce "se termina" procesarea Task-ului se scoate din coada (lista de Task-uri a serverului)
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }
    }

     synchronized public void removeTask(Task currentTask) throws InterruptedException {
        if(currentTask.getProcessingTime().get() == 0){ // se verifica daca timpul de procesare a Task-ului este zero
            tasks.take(); // se scoate Task-ul din coada
            SimulationManager.numberOfProcessedClients = new AtomicInteger(SimulationManager.numberOfProcessedClients.incrementAndGet()); // clientul se ia in considerare in calcularea avarage time-ului dor daca acesta a fost procesat
            SimulationManager.totalWaitingTime = new AtomicInteger(SimulationManager.totalWaitingTime.addAndGet(currentTask.getWaitingTime())); // total waiting time-ul unui cient se ia in considerare, doar daca acesta a fost procesat
        }
    }

    public LinkedBlockingQueue<Task> getTasks() {
        return tasks;
    }

    public AtomicInteger getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(AtomicInteger waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    public void setCondition(boolean condition) {
        this.condition = condition;
    }
}
