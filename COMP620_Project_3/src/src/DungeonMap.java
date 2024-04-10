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
     * For int[a][b], describes that there is a key in room a that will reduce the
     * weight for all edges traveling into b by adjacencyMatrix[a][b].
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

                /*
                 * in room a there is a key that reduces the weight by b for all edges going to
                 * vertex c, d, e...
                 * 
                 * 
                 * For keyLocations[a][c], describes that there is a key in room a that will
                 * reduce the weight b (keyLocations[a][c]) for all edges
                 * 
                 * traveling into c by b or keyLocations[a][c].
                 */

                for (int i = 2; i < parts.length; i++) {
                    int affectedRoom = Integer.parseInt(parts[i].trim()); // Room affected by the key

                    keyLocations[roomWithKey][affectedRoom] = weightReduction;
                    logger.info("Added key for room " + affectedRoom + " inside of room " + roomWithKey
                            + " for a weight reduction of " + weightReduction);
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

    /**
     * Removes all weight reductions from this room's key NO MATTER WHAT. Utilize
     * this AFTER changing weights!
     * 
     * @param room The room to remove weight reductions from.
     */
    public void removeRoomKey(int room) {
        for (int i = 0; i < size; i++) {
            keyLocations[room][i] = 0;
        }

        logger.info("Removed key from room " + room + "!");
    }

    /**
     * 
     * @param room The room to search in
     * @return Returns list of rooms this room's key unlocks, or an empty list if
     *         there is no key
     */
    public ArrayList<Integer> isKey(int room) {

        logger.info("Checking for key in room " + room + "...");

        ArrayList<Integer> returnArray = new ArrayList<Integer>();

        for (int i = 0; i < size; i++) {
            if (keyLocations[room][i] != 0) {
                logger.info("Found key for room " + i);
                returnArray.add(i);
            }
        }
        if (returnArray.isEmpty()) {
            logger.info("No key found!");
        }
        return returnArray;
    }

    public int getKeyWeightReduction(int room) {
        for (int i = 0; i < size; i++) {
            if (keyLocations[room][i] != 0) {
                logger.info("Key value for key found in room " + room + " is a weight reduction of "
                        + keyLocations[room][i]);

                return keyLocations[room][i];
            }
        }

        return -1;
    }

    // Update the cost of all paths going to a specific room based on a key pickup
    public void grabKey(int room) {
        logger.info("Trying to take key in room " + room);

        ArrayList<Integer> keyRoomTo = isKey(room);

        if (!keyRoomTo.isEmpty()) {
            logger.info("Found key to rooms " + keyRoomTo.toString() + " which was in room " + room
                    + ". \n Updating weights...");

            for (Integer r : keyRoomTo) {
                for (int i = 0; i < size; i++) {
                    logger.info(
                            "Updating weight from room " + i + " to room " + r + " by " + getKeyWeightReduction(room)
                                    + ",\nNote that some may be at 0 already, so will see no difference.");

                    if (adjacencyMatrix[i][r] < Integer.MAX_VALUE) {
                        adjacencyMatrix[i][r] = Math.max(0, adjacencyMatrix[i][r] - getKeyWeightReduction(room));
                    }
                }
                // Math.min(0, adjacencyMatrix[i][r] - getKeyWeightReduction(room)

            }

            logger.info("Updated weights, now removing key...");

            removeRoomKey(room);

        } else {
            logger.warning("Tried to take key when one is not available! Possibly broken logic!");
        }

    }
    
    public boolean isPath(int from, int to) {
        return (adjacencyMatrix[from][to] != Integer.MAX_VALUE);
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


    // Check if a path exists between src and dest
    public boolean pathExists(int src, int dest) {
        return adjacencyMatrix[src][dest] != Integer.MAX_VALUE;
    }
    
    public ArrayList<Integer> solveDungeon(){
        
        // begin with finding the initial best path even given keys
        
        ArrayList<Integer> optimalPath = findOptimalPathUsingBellmanFord();
        
        // find all doors required in this, if we come across a door, we need to prioritize
        
        
        
        
        return null;
    }
    
    public ArrayList<Integer> findOptimalPathUsingBellmanFord() {
        int[] distances = new int[size];
        int[] predecessors = new int[size];
        ArrayList<Integer> path = new ArrayList<>();

        // Initialize distances and predecessors
        for (int i = 0; i < size; i++) {
            distances[i] = Integer.MAX_VALUE;
            predecessors[i] = -1; // -1 signifies no predecessor
        }
        distances[startVertex] = 0; // Distance to itself is 0
        logger.info("Initial distances and predecessors set. Start vertex: " + startVertex);

        // Relax edges repeatedly
        logger.info("Starting to relax edges...");
        for (int i = 1; i < size; i++) {
            for (int u = 0; u < size; u++) {
                for (int v = 0; v < size; v++) {
                    if (adjacencyMatrix[u][v] != Integer.MAX_VALUE && distances[u] != Integer.MAX_VALUE) {
                        int newDistance = distances[u] + adjacencyMatrix[u][v];
                        if (newDistance < distances[v]) {
                            distances[v] = newDistance;
                            predecessors[v] = u;
                            logger.info("Edge relaxed: " + u + " -> " + v + " with new distance: " + newDistance);
                        }
                    }
                }
            }
        }

        // Check for negative-weight cycles
        logger.info("Checking for negative-weight cycles...");
        for (int u = 0; u < size; u++) {
            for (int v = 0; v < size; v++) {
                if (adjacencyMatrix[u][v] != Integer.MAX_VALUE && distances[u] != Integer.MAX_VALUE && distances[u] + adjacencyMatrix[u][v] < distances[v]) {
                    logger.severe("Graph contains a negative-weight cycle. Cannot find an optimal path.");
                    return null; // Negative cycle detected, no solution
                }
            }
        }

        // Reconstruct path from startVertex to endVertex
        logger.info("Reconstructing path...");
        for (int at = endVertex; at != -1; at = predecessors[at]) {
            path.add(0, at); // Insert at beginning to reverse the path
            logger.info("Path so far: " + path);
        }

        // Check if a path exists
        if (path.isEmpty() || path.get(0) != startVertex) {
            logger.info("No path exists from " + startVertex + " to " + endVertex);
            return null;
        }

        // Log the final path
        logger.info("Optimal path found from " + startVertex + " to " + endVertex + ": " + path);

        // Return the path
        return path;
    }


}
