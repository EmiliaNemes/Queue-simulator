package org.model;

import java.util.concurrent.atomic.AtomicInteger;

public class Task implements Comparable<Task>{

    private int ID;
    private int arrivalTime;
    private AtomicInteger processingTime;
    private AtomicInteger waitingTime;

    public Task(int ID, int arrivalTime, int processingTime){
        this.ID = ID;
        this.arrivalTime = arrivalTime;
        this.processingTime = new AtomicInteger(processingTime);
        this.waitingTime = new AtomicInteger();
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public AtomicInteger getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(int processingTime) {
        this.processingTime = new AtomicInteger(processingTime);
    }

    public int getWaitingTime() {
        return waitingTime.get();
    }

    public void setWaitingTime(AtomicInteger waitingTime) {
        this.waitingTime = waitingTime;
    }

    @Override
    public int compareTo(Task task) {
        return this.arrivalTime - task.arrivalTime; // sortarea Task-urilor se face in ordine crescatoare pe baza arrivalTime-ului
    }

    @Override
    public String toString(){
        return "(" + ID + "," + arrivalTime + "," + processingTime + ")"; // Task-ul este afisat in forma e tupla: (ID,arrivalTime,processingTime)
    }

}
