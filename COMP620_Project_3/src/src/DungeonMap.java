package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.*;

public class DungeonMap {

    private Logger logger = Logger.getLogger(DungeonMap.class.getName());

    int size;
    int startVertex;
    int endVertex;
    
    public int[][] adjacencyMatrix;
    private boolean[][] keyLocations;

    public DungeonMap(String graphFile, String keyFile) {
        logger.info("\u001B[36mBuilding dungeon...");
        buildDungeon(graphFile, keyFile);
    }

    private void buildDungeon(String graphFile, String keyFile) {
        int n = 0;
        
        
        try (BufferedReader br = new BufferedReader(new FileReader(graphFile))) {
            String line;
            // Skip first three lines as they are already used in constructor call, adjust if needed
            n = Integer.parseInt(br.readLine()); // Number of vertices
            startVertex = Integer.parseInt(br.readLine()); // Entrance vertex
            endVertex = Integer.parseInt(br.readLine()); // Destination vertex
            
            
            /*
             * Set up empty array
             * 
             */
            
            this.adjacencyMatrix = new int[n][n];
            this.keyLocations = new boolean[n][n];
            
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    
                    
                    adjacencyMatrix[i][j] = Integer.MAX_VALUE; // Initialize with max value to denote no direct path
                    keyLocations[i][j] = false; // Initialize all key locations to false
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
            logger.severe("Error reading file: " + graphFile) ;
        }
        
        
    }
    
    
    
    // Add a path from src to dest with a cost
    public void addPath(int src, int dest, int cost) {
        adjacencyMatrix[src][dest] = cost;
    }

    // Place a key that affects paths to a specific room
    public void placeKey(int src, int dest) {
        keyLocations[src][dest] = true;
    }

    // Get the cost of traveling from src to dest
    public int getCost(int src, int dest) {
        return adjacencyMatrix[src][dest];
    }

    // Update the cost of all paths going to a specific room based on a key pickup
    public void useKey(int room, int costReduction) {
        for (int i = 0; i < size; i++) {
            if (keyLocations[i][room]) {
                adjacencyMatrix[i][room] = Math.max(adjacencyMatrix[i][room] - costReduction, 0);
                keyLocations[i][room] = false; // Assume key is used and removed
            }
        }
    }
    
    public void printMatrix() {
        StringBuilder matrixString = new StringBuilder("Adjacency Matrix:\n");

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (adjacencyMatrix[i][j] == Integer.MAX_VALUE) {
                    matrixString.append("+ ");
                } else {
                    matrixString.append(adjacencyMatrix[i][j]).append(" ");
                }
            }
            matrixString.append("\n"); // Move to the next line after appending each row
        }

        logger.info(matrixString.toString());
    }


    // Check if a path exists between src and dest
    public boolean pathExists(int src, int dest) {
        return adjacencyMatrix[src][dest] != Integer.MAX_VALUE;
    }
}
