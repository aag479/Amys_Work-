import java.util.Scanner;
import java.util.Collections;
import java.io.File;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        ArrayList<Process> processList = new ArrayList<Process>();
        Scanner input = null;

        File file;
        boolean isVerbose = false;
        try {
            switch(args.length) {
                case 0:
                    printUsage();
                    return;
                case 1:
                    file = new File(args[0]);
                    break;
                case 2:
                    if (args[0].equalsIgnoreCase("--verbose")) {
                        isVerbose = true;
                    }
                    else {
                        printUsage();
                        return;
                    }
                    file = new File(args[1]);
                    break;
                default:
                    printUsage();
                    return;
            }
            input = new Scanner(file);
        }
        catch (Exception ex) {
            printUsage();
            return;
        }

        //Scanner randomNumbers= new Scanner(args[1]);
        int numProcesses = input.nextInt();
        for (int i = 0; i < numProcesses; i++) {
            String firstToken = input.next();
            int arrivalTime = Integer.parseInt(firstToken.substring(1));

            int burstCPUTime = input.nextInt();
            //System.out.println(arrivalTime);
            int totalCPUTime = input.nextInt();
            //System.out.println(totalCPUTime);
            String lastToken = input.next();
            int burstIOTimeMultiplier = Integer.parseInt(lastToken.substring(0, lastToken.length() - 1));
            //System.out.println(arrivalTime + " " + burstCPUTime + " " + totalCPUTime + " " + burstIOTimeMultiplier);
            Process newProcess = new Process(arrivalTime, burstCPUTime, totalCPUTime, burstIOTimeMultiplier);
            processList.add(newProcess);
        }
        
        // Output the original list:
        System.out.print("The original input was: " + numProcesses);
        for (Process process : processList) {
            System.out.print(" " + process);
        }
        System.out.println();
        
        // Sort the processList based on arrivalTime:
        Collections.sort(processList);
        
        // Output the sorted list:
        System.out.print("The (sorted) input is:  " + numProcesses);
        for (Process process : processList) {
            System.out.print(" " + process);
        }
        System.out.println();
        System.out.println();
        
        if (isVerbose) {
            System.out.println("This detailed printout gives the state and remaining burst for each process\n");
        }
        // Process the random-numbers.txt file:
        File file2 = new File("random-numbers.txt");
        try {
            input = new Scanner(file2);
        } catch (Exception ex) {
            System.out.println("file problems");
        }
        
        ArrayList<Integer> randomNumbers = new ArrayList<>();
        while (input.hasNext()) {
            int randomNumber = input.nextInt();
            randomNumbers.add(randomNumber);
        }
        ArrayList<Integer> randomNumbers2 = (ArrayList<Integer>) randomNumbers.clone();
        ArrayList<Integer> randomNumbers3 = (ArrayList<Integer>) randomNumbers.clone();
        ArrayList<Integer> randomNumbers4 = (ArrayList<Integer>) randomNumbers.clone();
        Simulation newSimulation1 = new Simulation(processList, Algorithm.FCFS, randomNumbers, isVerbose);
        newSimulation1.startSimulation();
        Simulation newSimulation2 = new Simulation(processList, Algorithm.RR, randomNumbers2, isVerbose);
        newSimulation2.startSimulation();
        Simulation newSimulation3 =new Simulation(processList, Algorithm.UP, randomNumbers3, isVerbose);
        newSimulation3.startSimulation();
        Simulation newSimulation4 =new Simulation(processList,Algorithm.SJF, randomNumbers4, isVerbose);
        newSimulation4.startSimulation();
    }
    
    public static void printUsage() {
        System.out.println("Usage: <program-name> <input-filename>");
        System.out.println("Usage: <program-name> --verbose <input-filename>");
    }

}

