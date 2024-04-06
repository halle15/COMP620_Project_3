package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.*;

public class DungeonMap {

    private Logger logger = Logger.getLogger(DungeonMap.class.getName());

    int size;
    int startVertex;
    int endVertex;

    public int[][] adjacencyMatrix;
    
    /*
     * For int[a][b], describes that there is a key in room a that will reduce the weight for all edges traveling into b by adjacencyMatrix[a][b].
     */
    private int[][] keyLocations;

    public DungeonMap(String graphFile, String keyFile) {
        logger.info("Building dungeon from graphFile: " + graphFile + " and keyFile: " + keyFile);
        buildDungeon(graphFile, keyFile);
    }

    private void buildDungeon(String graphFile, String keyFile) {
        int n = 0;

        /*
         * Setting up graphs...
         */

        logger.info("Setting up adjacencyMatrix");

        try (BufferedReader br = new BufferedReader(new FileReader(graphFile))) {
            String line;
            // Skip first three lines as they are already used in constructor call, adjust
            // if needed
            n = Integer.parseInt(br.readLine()); // Number of vertices
            startVertex = Integer.parseInt(br.readLine()); // Entrance vertex
            endVertex = Integer.parseInt(br.readLine()); // Destination vertex

            /*
             * Set up empty array
             * 
             */

            this.size = n;

            this.adjacencyMatrix = new int[n][n];
            this.keyLocations = new int[n][n];

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {

                    adjacencyMatrix[i][j] = Integer.MAX_VALUE; // Initialize with max value to denote no direct path
                    keyLocations[i][j] = 0; // Initialize all key locations to false
                }
            }

            logger.info("Set adjacencyMatrix and keyLocations to starting values");

            /*
             * Read and add edges
             */

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int src = Integer.parseInt(parts[0].trim());
                int dest = Integer.parseInt(parts[1].trim());
                int weight = Integer.parseInt(parts[2].trim());
                adjacencyMatrix[src][dest] = weight; // Add edge with weight

                logger.info("Added edge from " + src + " to " + dest + " with weight " + weight);
            }
            
        } catch (IOException e) {
            logger.severe("Error reading file: " + graphFile);
        }
        
        logger.info("Finished creating adjacency matrix! Creating keys matrix...");
        
        applyKeysFromFile(keyFile);


    }
    
    private void applyKeysFromFile(String keyFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(keyFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int roomWithKey = Integer.parseInt(parts[0].trim()); // Room containing the key
                int weightReduction = Integer.parseInt(parts[1].trim()); // Weight reduction amount
                
                
                /* in room a there is a key that reduces the weight by b for all edges going to vertex c, d, e...
                * 
                * 
                * For keyLocations[a][c], describes that there is a key in room a that will reduce the weight b (keyLocations[a][c]) for all edges 
                * 
                * traveling into c by b or keyLocations[a][c].
                */
                
                for(int i = 2; i < parts.length; i++) {
                    int affectedRoom = Integer.parseInt(parts[i].trim()); // Room affected by the key
                    
                    keyLocations[roomWithKey][affectedRoom] = weightReduction;
                    logger.info("Added key for room " + affectedRoom + " inside of room " + roomWithKey + " for a weight reduction of " + weightReduction);
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        logger.info("Completed reading of keyfile and writing of keyLocations");
    }

    // Add a path from src to dest with a cost
    public void addPath(int src, int dest, int cost) {
        adjacencyMatrix[src][dest] = cost;
    }

    // Place a key that affects paths to a specific room
    public void placeKey(int src, int dest, int weight) {
        keyLocations[src][dest] = weight;
    }

    // Get the cost of traveling from src to dest
    public int getCost(int src, int dest) {
        return adjacencyMatrix[src][dest];
    }

    // Update the cost of all paths going to a specific room based on a key pickup
    public void grabKey(int room, int costReduction) {
        
        
        /*
         * First, fi
         * 
         */
    }

    public void printAdjacencyMatrix() {
        StringBuilder matrixString = new StringBuilder("Adjacency Matrix:\n");

        // Calculate the maximum number of digits in the matrix for formatting
        int maxDigits = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (adjacencyMatrix[i][j] != Integer.MAX_VALUE
                        && Integer.toString(adjacencyMatrix[i][j]).length() > maxDigits) {
                    maxDigits = Integer.toString(adjacencyMatrix[i][j]).length();
                }
            }
        }

        int indexDigits = Integer.toString(size - 1).length();
        maxDigits = Math.max(maxDigits, indexDigits);

        // Header with column indices
        String header = " ".repeat(indexDigits + 2); // Space for row index column
        for (int i = 0; i < size; i++) {
            header += String.format("| %-" + maxDigits + "d ", i);
        }
        matrixString.append(header).append("|\n");

        String separator = "_".repeat(header.length()); // Adjust the separator to match the header width
        matrixString.append(separator).append("\n");

        for (int i = 0; i < size; i++) {
            // Row index
            String rowIndex = String.format("%-" + (indexDigits + 2) + "d", i); // Left-hand side indices
            matrixString.append(rowIndex);

            for (int j = 0; j < size; j++) {
                matrixString.append("| ");
                if (adjacencyMatrix[i][j] == Integer.MAX_VALUE) {
                    matrixString.append("+".repeat(maxDigits));
                } else {
                    // Right-pad the number with spaces to align columns
                    String numberStr = String.format("%-" + maxDigits + "d", adjacencyMatrix[i][j]);
                    matrixString.append(numberStr);
                }
                matrixString.append(" ");
            }
            matrixString.append("|\n").append(separator).append("\n");
        }

        logger.info(matrixString.toString());
    }
    
    public void printKeyLocations() {
        StringBuilder matrixString = new StringBuilder("Key Locations Matrix:\n");

        // Calculate the maximum number of digits in the matrix for formatting
        int maxDigits = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (keyLocations[i][j] != 0 // Assuming 0 indicates no key effect, adjust if different
                        && Integer.toString(keyLocations[i][j]).length() > maxDigits) {
                    maxDigits = Integer.toString(keyLocations[i][j]).length();
                }
            }
        }

        int indexDigits = Integer.toString(size - 1).length();
        maxDigits = Math.max(maxDigits, indexDigits);

        // Header with column indices
        String header = " ".repeat(indexDigits + 2); // Space for row index column
        for (int i = 0; i < size; i++) {
            header += String.format("| %-" + maxDigits + "d ", i);
        }
        matrixString.append(header).append("|\n");

        String separator = "_".repeat(header.length()); // Adjust the separator to match the header width
        matrixString.append(separator).append("\n");

        for (int i = 0; i < size; i++) {
            // Row index
            String rowIndex = String.format("%-" + (indexDigits + 2) + "d", i); // Left-hand side indices
            matrixString.append(rowIndex);

            for (int j = 0; j < size; j++) {
                matrixString.append("| ");
                if (keyLocations[i][j] == 0) { // Adjust this condition if the meaning of 0 changes
                    matrixString.append("-".repeat(maxDigits)); // Use "-" to indicate no key effect
                } else {
                    // Right-pad the number with spaces to align columns
                    String numberStr = String.format("%-" + maxDigits + "d", keyLocations[i][j]);
                    matrixString.append(numberStr);
                }
                matrixString.append(" ");
            }
            matrixString.append("|\n").append(separator).append("\n");
        }

        logger.info(matrixString.toString()); // Change to logger.info if using a logging framework
    }
    
    /**
     * 
     * @return Returns ArrayList<Integer> of path of rooms to go to get to end
     */
    public ArrayList<Integer> findPath(){
        
        ArrayList<Integer> foundPath = new ArrayList<Integer>();
        
        int currentPosition = startVertex;
        
        logger.info("Starting pathfinding at " + startVertex + " with target " + endVertex);
        
        while(currentPosition != endVertex) {
            /*
             * Main implementation here.
             */
        }
        
        
        return foundPath;
    }


    // Check if a path exists between src and dest
    public boolean pathExists(int src, int dest) {
        return adjacencyMatrix[src][dest] != Integer.MAX_VALUE;
    }
}
