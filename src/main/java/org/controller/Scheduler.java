package org.controller;

import org.model.Server;
import org.model.Task;

import java.util.List;

public class Scheduler {
    private List<Server> servers;
    private int maxNoServers;
    private int maxTasksPerServer; // is the maximum number of tasks/clients

    public Scheduler(int maxNoServers, int maxTasksPerServer, List<Server> servers){
        this.servers = servers;
        this.maxNoServers = maxNoServers;
        this.maxTasksPerServer = maxTasksPerServer;
    }

    public void dispatchTask(Task t){
        int min = 1000;
        int minsIndex = 0;
        for(int i = 0; i< maxNoServers; i++){    // cautam serverul cu waitingPeriod-ul minim
            if(servers.get(i).getWaitingPeriod().get() < min){ // verificam daca waitingPeriod-ul Server-ului este mai mic decat minimul actual
                min = servers.get(i).getWaitingPeriod().get(); // se actualizeaza minimul
                minsIndex = i; // se actualizeaza indexul minimului
            }
        }
        servers.get(minsIndex).addTask(t); // adaugam task-ul la serverul cu waitingPeriod-ul minim
    }

}
