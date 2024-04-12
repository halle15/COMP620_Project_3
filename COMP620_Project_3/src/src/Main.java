package src;

import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    /*
     * Runtime:
     * 
     * should request user input for a file should then request a user for a key
     * file
     * 
     * should construct graph, display path
     */

    public static void main(String[] args) {
        
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.WARNING); Handler[] handlers =
        rootLogger.getHandlers(); if (handlers[0] instanceof ConsoleHandler) {
        handlers[0].setLevel(Level.WARNING); }

        Scanner scanner = new Scanner(System.in);

        // Prompt for graph and key files
        System.out.println("This application uses a logger, defaulting at WARN level.");
        System.out.println("Enter the name of the input file");
        String graphFile = scanner.nextLine();
        System.out.println("Enter the name of the key file");
        String keyFile = scanner.nextLine();

        // Initialize the DungeonMap
        DungeonMap dungeonMap = new DungeonMap(graphFile, keyFile);
        
        System.out.println("Building and solving dungeon...");
        
        System.out.println("A path is: \n" + dungeonMap.solveDungeon(dungeonMap.startVertex, dungeonMap.endVertex));
        
        
        scanner.close();

        /*
         * old manual testing Logger rootLogger = Logger.getLogger("");
         * rootLogger.setLevel(Level.FINE); Handler[] handlers =
         * rootLogger.getHandlers(); if (handlers[0] instanceof ConsoleHandler) {
         * handlers[0].setLevel(Level.FINE); }
         * 
         * System.out.print("\u001B[36m");
         * 
         * DungeonMap dM = new DungeonMap("testGraph1.txt", "testKey1.txt", Level.ALL);
         * 
         * 
         * 
         * 
         * 
         * 
         * dM = new DungeonMap("shortGraph1.txt", "shortKey1.txt");
         * 
         * dM.printAdjacencyMatrix();
         * 
         * dM.printKeyLocations();
         * 
         * rootLogger.info("Checking where key is for room 2");
         * 
         * rootLogger.info(dM.findRoomsWithKey(2).toString());
         * 
         * dM.solveDungeon(dM.startVertex, dM.endVertex); /* testing double take
         * dM.grabKey(3);
         */

        /*
         * testing removing key rootLogger.info(dM.isKey(3).toString());
         * 
         * dM.removeRoomKey(3);
         * 
         * rootLogger.info(dM.isKey(3).toString());
         */

    }
}
