import java.util.ArrayList;
import java.util.*;

public class Simulation {
    boolean isVerbose;
    int quantum = 2;    // This is the maximum number of cycles that each process gets.
    int remainingQuantum = 0;   // This is the remaining number of cycles for this quantum.
    ArrayList<Integer> randomNumbers = null;
    boolean isRunningProcess = false;
    int isBlockedProcess = 0;
    Process currentRunningProcess = null;
    //keeps track of number of unfinished processes.
    int processCounter = 0;
    int currentTime = 0;
    Algorithm currentAlgorithm;
    ArrayList<Process> processes;//list of all finished and unfinished processes.
    LinkedList<Process> readyProcesses = new LinkedList<>();

    // Data members for statistical purposes:
    int finishingTime;      // Holds the time when the simulation finished.
    double utilizationCPU;  // The fraction of the time that the CPU was in use.
    double utilizationIO;   // The fraction of the time that the IO happening.
    double throughput;      // The number of processes completed per hundred cycles.
    double averageTurnaroundTime;   // The average turnaroundTime across all processes.
    double averageWaitingTime;      // The average waitingTime across all processes.
    int numCyclesCPURunning = 0;    // The number of cycles that the CPU is running for.
    int numCyclesIOHappening = 0;    // The number of cycles that IO is happening.
    
    public Simulation(ArrayList<Process> processes, Algorithm scheduling, ArrayList<Integer> randomNumbers, boolean isVerbose) {
        this.isVerbose = isVerbose;
        this.processes = processes;
        processCounter = processes.size();
        currentAlgorithm = scheduling;
        this.randomNumbers = randomNumbers;
        for (int i = 0; i < processes.size(); i++) {
            processes.get(i).reset();
        }
    }

    public void startSimulation() {
        if (isVerbose) {
            printStates(0);
        }
        // Main simulation loop
        while (processCounter > 0) {
            // While processes are still unfinished:
            updateStates(); // move processes to their new states as applicable.
            //if there is no running process and one is ready run next process
            if (!isRunningProcess && !readyProcesses.isEmpty()) {
                chooseAndRunNextProcess();
            }
            if (isVerbose && processCounter > 0) {
                printStates(currentTime+1);
            }
            incrementTime(); // increment time recalculate process time quantities.
        }
        calculateStatistics();
        outputDataSummary();
    }

    private void chooseAndRunNextProcess() {
    	 if (currentRunningProcess != null) {
             System.out.println("Boolean 'isRunningProcess' was false, "
                     + "but variable 'currentRunningProcess was not null.");
         }
    	if(currentAlgorithm == Algorithm.FCFS||currentAlgorithm == Algorithm.RR){
    		currentRunningProcess = readyProcesses.poll();
    	}
        if(currentAlgorithm == Algorithm.SJF){
        	int min = readyProcesses.get(0).remainingCPUTime;
        	
        	Process minCPUTime = readyProcesses.get(0);
        	int index = 0;
        	for(int i = 1;i< readyProcesses.size();i++){
        		if(readyProcesses.get(i).remainingCPUTime < min){
        			min = readyProcesses.get(i).remainingCPUTime;
        			minCPUTime = readyProcesses.get(i); 
        			index = i;
        			    
        		} 
        	}
        	minCPUTime = readyProcesses.remove(index);
            currentRunningProcess = minCPUTime;
        }
        
        if (currentAlgorithm == Algorithm.UP) {
        	// This simulation is Uniprocessing.
        	if(isRunningProcess ||isBlockedProcess!=0){
        		return;
        	}
        	currentRunningProcess = readyProcesses.poll();
            // Only set running the next ready process if there is no
            // active (i.e. no blocked or running process) currently, then return.
        }
        // Check if this process needs to calculate a new 'remainingCPUBurstTime':
        if (currentRunningProcess.remainingCPUBurstTime == 0) {
            // This process needs a random number to calculate a new 'remainingCPUBurstTime':
            currentRunningProcess.setRunning(randomNumbers.remove(0));
        } else {
            // This process must have been preempted and will continue with
            // its current value of 'remainingCPUBurstTime'.
            // It does not need a random number.
            currentRunningProcess.setRunning(0);
        }
        isRunningProcess = true;
        if (currentAlgorithm == Algorithm.RR) {
            remainingQuantum = quantum;
            if (remainingQuantum > currentRunningProcess.remainingCPUBurstTime) {
                remainingQuantum = currentRunningProcess.remainingCPUBurstTime;
                //System.out.println("quantum was set to " + remainingQuantum + ", to match the remainingCPUBurstTime");
            }
        }
    }
    public void printStates(int time) {
        System.out.printf("Before cycle %4d:", time);
        for (int i = 0; i < processes.size(); i++) {
            System.out.print(String.format("%14s", processes.get(i).printState(remainingQuantum, currentAlgorithm)));
        }
        System.out.println(".");
    }

    public void incrementTime() {
        currentTime++;
        if (isRunningProcess) {
            remainingQuantum--;
            numCyclesCPURunning++;
        }

        //search for any blocked processes and increment IO time.
        boolean isIOinUse = false;
        for (Process process : processes) {
            if (process.getState() == ProcessState.BLOCKED) {
                //this process is waiting for IO.
                isIOinUse = true;
                process.IOTime++;
                process.remainingBlockedBurstTime--;
            } else if (process.getState() == ProcessState.READY) {
                process.waitingTime++;
            }
        }
        if (isIOinUse) {
            numCyclesIOHappening++;
        }
        //decrement times for running process. 
        if (isRunningProcess) {
            if (currentRunningProcess.getState() == ProcessState.RUNNING) {
                //System.out.println("I am happening");
                currentRunningProcess.remainingCPUBurstTime--;
                currentRunningProcess.remainingCPUTime--;
                //System.out.println(currentRunningProcess.remainingCPUTime);
            } else {
                System.err.println("Boolean 'isRunningProcess' is true, yet the currentRunningProcessState is not RUNNING");
            }
        }
    }

    public void updateStates() {
        //check if current runnning process has finished its cpu burst time.
        if (isRunningProcess && currentRunningProcess.getState() == ProcessState.RUNNING
                && currentRunningProcess.remainingCPUBurstTime == 0) {
            //this is end of current cpu burst, check if any more cpu time to go.
            if (currentRunningProcess.remainingCPUTime <= 0) {
                currentRunningProcess.setFinished(currentTime);
                processCounter--;
            } else {
                //this process still has more CPU time to go.
                currentRunningProcess.setBlocked();
                isBlockedProcess++;
            }
            isRunningProcess = false;
            currentRunningProcess = null;
        }
        for (Process process : processes) {
            // For each process:
            // Check if this is the running process and if it needs to be preempted (RR algorithm only).
            if (process == currentRunningProcess && currentAlgorithm == Algorithm.RR) {
                if (isRunningProcess && remainingQuantum == 0) {
                    //this process has finished its quantum, it will be preempted by the next
                    //ready process.
                    currentRunningProcess.setReady(readyProcesses);
                    isRunningProcess = false;
                    currentRunningProcess = null;
                }
            }
            // Check if this process has finished being blocked.
            if (process.getState() == ProcessState.BLOCKED
                    && process.remainingBlockedBurstTime == 0) {
            	if(currentAlgorithm == Algorithm.UP){
            		process.setRunning(randomNumbers.remove(0));
            		isRunningProcess = true;
            		currentRunningProcess = process;
            	}
            	else{
            		process.setReady(readyProcesses);
            	}
                isBlockedProcess--;
                // If UP algorithm. Set running. Otherweise set ready
            }
            // Check if this process is ready to run:
            if (process.arrivalTime <= currentTime) {
                if (process.getState() == ProcessState.NONEXISTENT) {
                    process.setReady(readyProcesses);
                }
            }
        }
    }

    public void calculateStatistics() {
        int numProcesses = 0;
        int sumWaitingTimes = 0;    // The sum of the waiting times of all processes.
        int sumTurnaroundTimes = 0;    // The sum of the waiting times of all processes.
        for (Process process : processes) {
            process.turnAroundTime = process.finishingTime - process.arrivalTime;
            sumTurnaroundTimes += process.turnAroundTime;
            sumWaitingTimes += process.waitingTime;
            numProcesses++;
        }
        averageTurnaroundTime = (float) sumTurnaroundTimes/numProcesses;
        averageWaitingTime = (double) sumWaitingTimes/numProcesses;
        finishingTime = currentTime - 1;
        utilizationCPU = (double) numCyclesCPURunning/finishingTime;
        utilizationIO = (double) numCyclesIOHappening/finishingTime;
        throughput = (double) numProcesses/((double) finishingTime/100);
    }

    public void outputDataSummary() {
        System.out.println("The scheduling algorithm used was " + currentAlgorithm);
        System.out.println();
        
        int processNumber = 0;
        for (Process process : processes) {
            process.printStatistics(processNumber);
            System.out.println();
            processNumber++;
        }
        
        System.out.println("Summary Data:");
        System.out.println("\tFinishing time: " + finishingTime);
        System.out.printf("\tCPU Utilization: %.6f\n", utilizationCPU);
        System.out.printf("\tI/O Utilization: %.6f\n", utilizationIO);
        System.out.printf("\tThroughput: %.6f processes per hundred cycles\n", throughput);
        System.out.printf("\tAverage turnaround time: %.6f\n", averageTurnaroundTime);
        System.out.printf("\tAverage waiting time: %.6f\n", averageWaitingTime);
    }
}
