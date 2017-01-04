import java.util.LinkedList;

public class Process implements Comparable<Process> {
    int currentBurstTime;   // Randomly calculated CPU burst time(constant for particular burst)
    int arrivalTime;        // arrival time for this process, measured in cycles
                            // elapsed since the start of the simulation.
    int burstTimeCPUMax;    // This is the maximum CPU burst time that can be allowed
                            // for this process. It is used to randomly calculate
                            // the length of each CPU burst.
    int totalCPUTime;       // required CPU time for this process. Constant for a specific process
                            // for the duration of the simulation.
    int finishingTime;      // The time that a process finishes its task, measured in
                            // cycles since the start of the simulation.
    int turnAroundTime;
    int blockedBurstTime;   //calculated blocked burst time(constant for particular IO burst)
    int remainingBlockedBurstTime;
    int remainingCPUBurstTime;
    
    //total time process has spent in blocked state.
    int IOTime;             //IO time keeps track of the time this process has spent waiting for IO.
    int burstIOMultiplier;
    int waitingTime;
    int remainingCPUTime;
    private ProcessState state;

    Process(int arrivalTime, int burstCPUTime, int totalCPUTime, int burstIOTimeMultiplier) {
        this.arrivalTime = arrivalTime;
        burstTimeCPUMax = burstCPUTime;
        this.totalCPUTime = totalCPUTime;
        burstIOMultiplier = burstIOTimeMultiplier;
    }

    public String toString()
    {
        return "(" + arrivalTime + " " + burstTimeCPUMax + " " + totalCPUTime + " " + burstIOMultiplier + ")";
    }

    public String printState(int quantum, Algorithm currentAlgorithm) {
        String output = null;
        switch (state) {
            case RUNNING:
 
                if (currentAlgorithm == Algorithm.RR) {
                    output = String.format("running%3d", quantum);
                }
                else {
                    output = String.format("running%3d", remainingCPUBurstTime);
                }
                
                break;

            case READY:
                output = String.format("ready%3d", 0);
                break;
            case BLOCKED:
                output = String.format("blocked%3d", remainingBlockedBurstTime);
                break;
            case NONEXISTENT:
                output = "unstarted  0";
                break;
            case FINISHED:
                output = String.format("terminated%3d", 0);
                break;
            default:
                break;
        }

        return output;
    }

    public int compareTo(Process other) {

        return -1 * other.arrivalTime - arrivalTime;
    }

    public void setReady(LinkedList<Process> queue) {
        if (state == ProcessState.RUNNING) {
            //System.out.println("Process was preempted.");
        }
        if (state == ProcessState.READY) {
            System.err.println("Process entered READY state when already in the READY state. This should never happen.");
        }
        if (state == ProcessState.FINISHED) {
            System.err.println("Process entered READY state from the FINISHED state. This should never happen.");
        }
        state = ProcessState.READY;
        queue.add(this);

    }

    public void setRunning(int randomNumber) {
        //System.out.println("The random number to be used to calculate the CPU Burst Time is: " + randomNumber);
        // Calculate new CPU Burst Time only if the last burst was not finished:
        if (remainingCPUBurstTime == 0) {
            // This process was not previously preempted. Calculate new CPU Burst Time:
            remainingCPUBurstTime = randomOS(randomNumber);
            //System.out.print("Randomly calculated CPU burst time: " + remainingCPUBurstTime);
            if (remainingCPUBurstTime > remainingCPUTime) {
                remainingCPUBurstTime = remainingCPUTime;
                //System.out.print(", changed to match remaining CPU Time of: " + remainingCPUTime);
            }
            currentBurstTime = remainingCPUBurstTime;
            //System.out.println(".");
        }
        else {
            // This process was preempted. We do not need to calculate a new CPU Burst Time.
            // Confirm that randomNumber sent was zero:
            if (randomNumber != 0) {
                // Unexpected result.
                System.out.println("setRunning(): Process had a non-zero CPU Burst time (" +
                        remainingCPUBurstTime + "), yet was sent a random number.");
            }
        }
        // Update state of process:
        if (state == ProcessState.READY || state == ProcessState.BLOCKED) {
            state = ProcessState.RUNNING;
        }
        else {
            System.err.println("setRunning(): Process entered RUNNING state from the " +
                    state + " state. It should only enter the RUNNING state from the READY state.");
        }
    }

    public void setBlocked() {
        if (state != ProcessState.RUNNING) {
            System.err.println("Process entered BLOCKED state from a state that was not RUNNNING.");
        }
        state = ProcessState.BLOCKED;
        remainingBlockedBurstTime = currentBurstTime * burstIOMultiplier;
        blockedBurstTime = remainingBlockedBurstTime;
        currentBurstTime = 0;
    }

    public void setFinished(int finishTime) {
        if (state != ProcessState.RUNNING) {
            System.err.println("Process entered FINISHED state from a state that was not RUNNNING.");
        }
        finishingTime = finishTime;
        state = ProcessState.FINISHED;
    }
    
    public ProcessState getState() {
        return this.state;
    }

    public int randomOS(int num) {
        return num % burstTimeCPUMax + 1;
    }

    public void reset() {
        this.state = ProcessState.NONEXISTENT;
        remainingCPUTime = totalCPUTime;
        waitingTime = 0;
        remainingCPUBurstTime = 0;
        remainingBlockedBurstTime = 0;
        IOTime = 0;
    }
    
    public void printStatistics(int processNumber) {
        System.out.println("Process " + processNumber + ":");
        System.out.println("\t(A,B,C,M) = (" +
                arrivalTime + "," +
                burstTimeCPUMax + "," +
                totalCPUTime + "," +
                burstIOMultiplier + ")");
        System.out.println("\tFinishing time: " + finishingTime);
        System.out.println("\tTurnaround time: " + turnAroundTime);
        System.out.println("\tI/O time: " + IOTime);
        System.out.println("\tWaiting time: " + waitingTime);
    }
}
