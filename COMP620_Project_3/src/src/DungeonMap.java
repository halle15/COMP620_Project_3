package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.*;

public class DungeonMap {

    private Logger logger = Logger.getLogger(DungeonMap.class.getName());

    int size;
    int startVertex;
    int endVertex;

    /*
     * Starting point; what is read in from our file initially.
     */
    public int[][] adjacencyMatrix;

    /*
     * We implement this as a better "memory" for pathing.
     */
    public int[][] floydWarshallMap;
    public int[][] floydWarshallNext;

    /*
     * For int[a][b], describes that there is a key in room a that will reduce the
     * weight for all edges traveling into b by adjacencyMatrix[a][b].
     */
    private int[][] keyLocations;

    public DungeonMap(String graphFile, String keyFile) {
        
        
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            handlers[0].setLevel(Level.ALL);
        }

        
        logger.info("Building dungeon from graphFile: " + graphFile + " and keyFile: " + keyFile);

        
        
        buildDungeon(graphFile, keyFile);
    }

    public DungeonMap(String graphFile, String keyFile, Level l) {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            handlers[0].setLevel(l);
        }


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

                logger.fine("Added edge from " + src + " to " + dest + " with weight " + weight);
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
                    logger.fine("Added key for room " + affectedRoom + " inside of room " + roomWithKey
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
            logger.warning("No key found!");
        }
        return returnArray;
    }

    public int getKeyWeightReduction(int room) {
        for (int i = 0; i < size; i++) {
            if (keyLocations[room][i] != 0) {
                logger.fine("Key value for key found in room " + room + " is a weight reduction of "
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
            logger.finest("Found key to rooms " + keyRoomTo.toString() + " which was in room " + room
                    + ". \n Updating weights...");

            for (Integer r : keyRoomTo) {
                for (int i = 0; i < size; i++) {
                    logger.finest(
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

        logger.info(matrixString.toString());
    }

    // Check if a path exists between src and dest
    public boolean pathExists(int src, int dest) {
        return adjacencyMatrix[src][dest] != Integer.MAX_VALUE;
    }

    public ArrayList<Integer> findRoomsWithKey(int room) {

        ArrayList<Integer> roomsWithRequiredKeys = new ArrayList<Integer>();

        logger.info("Checking for keys for room " + room);

        for (int i = 0; i < size; i++) {
            if (keyLocations[i][room] > 0) {
                logger.info("FOUND: A key for room " + room + " is in room " + i);
                roomsWithRequiredKeys.add(i);
            }
        }

        if (roomsWithRequiredKeys.isEmpty()) {
            logger.warning("This room does not have a key!");
        }
        
        return roomsWithRequiredKeys;
        
    }
    
    public boolean isRoomLocked(int roomFrom, int roomTo) {
        return(adjacencyMatrix[roomFrom][roomTo] > 0);
    }

    /**
     * 
     * 
     * @param path The path to search through to see if any doors are locked.
     * @return The rooms which are locked, returned in order.
     */
    public ArrayList<Integer> findLockedRoomsInPath(ArrayList<Integer> path) {

        ArrayList<Integer> lockedRooms = new ArrayList<Integer>();
        
        logger.info("Checking for locked rooms in our current path of " + path.toString());
      
        
        for(int r = 1; r < path.size(); r++) {
            
            logger.fine("Checking to see if " + path.get(r) + " is locked");
            
            if(isRoomLocked(path.get(r-1), path.get(r))) {
                logger.fine(path.get(r) + " is locked!");
                lockedRooms.add(path.get(r));
            }
            else {
                logger.fine(path.get(r) + " is not locked, continuing...");
            }
            
        }
        
        if(lockedRooms.isEmpty()) {
            logger.warning("No locked rooms!");
        }else {
            logger.fine("Found locked rooms: " + lockedRooms.toString() + " in path " + path.toString());
        }
        
        return lockedRooms;
    }

    public ArrayList<Integer> solveDungeon() {

        // begin with finding the initial best path even given keys

        ArrayList<Integer> optimalPath = findOptimalPathUsingBellmanFord();
        ArrayList<Integer> roomsRequiringKey = findLockedRoomsInPath(optimalPath);

        /*
         * 1. Find each room out of optimal path that requires a key(s) 2. If a room
         * requires keys (and it is not in the touched list), we will modify the
         * original optimal path list to implement a path to this key, then from the key
         * to the door. 2a. When we touch this key, we add this to a list of
         * "grabbed keys" so we do not grab a key twice. 2b. if a room requires multiple
         * keys, we will grab them in order, so we will go from: start -> (key1 -> key2
         * ->) door 2c. Before we finally add this to the total path, we must then check
         * if this path requires keys, possibly useful to create a doesPathNeedKey
         * function, or check by cost of path? 3. When we finalize step two, we
         * implement these paths before the succeeding door. Below shows a workthrough:
         *
         * Say we know our optimal path is:
         * 
         * 1 -> 3 -> 4
         * 
         * Lets say a key to room 3 is in room 2
         * 
         * We know at 3 we must find the key which is in room 2, so we can break the
         * problem down as
         * 
         * 
         * 1 -> (new path to room with key for 3) -> (new path from room to room 3) -> 3
         * -> 4
         * 
         * We should utilize the Floyd-Warshall algorithm
         * 
         * 
         * 
         * 
         *
         */
        
        
        
        
        /*
         * FUCK ALL THE ABOVE
         * 
         * FLOYD-WARSHSALL FOR THE MAP
         * 
         * GET THE OOPTIMAL MAP
         * 
         * CHECK WHERE WE NEED KEYS
         * 
         */

        return null;
    }

    public void runFloydWarshall() {

        int[][] dist = new int[size][size];
        int[][] next = new int[size][size];

        // Step 1: Initialize dist and next matrices
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    next[i][j] = -1;
                } else if (adjacencyMatrix[i][j] != Integer.MAX_VALUE) {
                    dist[i][j] = adjacencyMatrix[i][j];
                    next[i][j] = j;
                } else {
                    dist[i][j] = Integer.MAX_VALUE;
                    next[i][j] = -1;
                }
            }
        }
        logger.info("Initial distances and next hops set.");

        // Step 2: Run Floyd-Warshall, update dist and next
        logger.info("Starting Floyd-Warshall algorithm...");
        for (int k = 0; k < size; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (dist[i][k] != Integer.MAX_VALUE && dist[k][j] != Integer.MAX_VALUE
                            && dist[i][k] + dist[k][j] < dist[i][j]) {
                        dist[i][j] = dist[i][k] + dist[k][j];
                        next[i][j] = next[i][k];
                        logger.finest("Updating distance from " + i + " to " + j + " via " + k + " to new distance: "
                                + dist[i][j]);
                    }
                }
            }
        }

        logger.info("Saving our Floyd-Warshall map...");
        floydWarshallMap = dist;
        floydWarshallNext = next;
    }

    /**
     * This function utilizes the Floyd-Warshall algorithm to find an optimal path
     * which is possibly memoized.
     * 
     */
    public ArrayList<Integer> findOptimalPathUsingFloydWarshall() {
        ArrayList<Integer> path = new ArrayList<Integer>();

        path = memoizedOptimalPath(startVertex, endVertex);

        return path;
    }

    /**
     * This function utilizes the Floyd-Warshall algorithm to find an optimal path
     * which is possibly memoized.
     * 
     * @param currentVertex Vertex we are searching from.
     * @param targetVertex  Vertex we are attempting to go to.
     */
    public ArrayList<Integer> findOptimalPathUsingFloydWarshall(int startVertex, int endVertex) {
        ArrayList<Integer> path = new ArrayList<Integer>();

        path = memoizedOptimalPath(startVertex, endVertex);

        return path;
    }

    public ArrayList<Integer> memoizedOptimalPath() {
        ArrayList<Integer> path = new ArrayList<Integer>();

        logger.info("Reconstructing path from startVertex to endVertex...");
        int u = startVertex;
        if (floydWarshallNext[u][endVertex] != -1) { // There is a path
            while (u != endVertex) {
                path.add(u);
                u = floydWarshallNext[u][endVertex];
            }
            path.add(endVertex); // Add the end vertex to the path
            logger.info("Optimal path found: " + path);
        } else {
            logger.info("No path exists from " + startVertex + " to " + endVertex);
            return null;
        }

        return path;
    }

    public ArrayList<Integer> memoizedOptimalPath(int startVertex, int endVertex) {
        ArrayList<Integer> path = new ArrayList<Integer>();

        logger.info("Reconstructing path from startVertex to endVertex...");
        int u = startVertex;
        if (floydWarshallNext[u][endVertex] != -1) { // There is a path
            while (u != endVertex) {
                path.add(u);
                u = floydWarshallNext[u][endVertex];
            }
            path.add(endVertex); // Add the end vertex to the path
            logger.info("Optimal path found: " + path);
        } else {
            logger.info("No path exists from " + startVertex + " to " + endVertex);
            return null;
        }

        return path;
    }
    
 // Example method to update distances after collecting a key affecting node `a`
    public void updateDistancesForAffectedNodes(Set<Integer> affectedNodes) {
        // Only iterate over affected nodes as intermediate nodes
        for (int k : affectedNodes) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Check if the current path i->j can be improved by going through k
                    if (floydWarshallMap[i][k] != Integer.MAX_VALUE && floydWarshallMap[k][j] != Integer.MAX_VALUE) {
                        if (floydWarshallMap[i][j] > floydWarshallMap[i][k] + floydWarshallMap[k][j]) {
                            floydWarshallMap[i][j] = floydWarshallMap[i][k] + floydWarshallMap[k][j];
                            floydWarshallNext[i][j] = floydWarshallNext[i][k];
                            // Optionally, update affectedNodes if this change affects other nodes
                        }
                    }
                }
            }
        }
    }


    public void printFloydWarshallMap() {
        StringBuilder matrixString = new StringBuilder("Floyd-Warshall Matrix:\n");
        // Matrix logging logic (similar to printAdjacencyMatrix)...
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                // Adjust logging to accommodate potentially large numbers (including MAX_VALUE)
                if (floydWarshallMap[i][j] == Integer.MAX_VALUE) {
                    matrixString.append(String.format("%7s", "+∞"));
                } else {
                    matrixString.append(String.format("%7d", floydWarshallMap[i][j]));
                }
            }
            matrixString.append("\n");
        }
        logger.info(matrixString.toString());
    }

    /**
     * @deprecated
     * 
     * @return The array list with the optimal path in order..
     */
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
                if (adjacencyMatrix[u][v] != Integer.MAX_VALUE && distances[u] != Integer.MAX_VALUE
                        && distances[u] + adjacencyMatrix[u][v] < distances[v]) {
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

    /**
     * @deprecated possibly deprecated as there may be multiple keys for one room :(
     * 
     * @param room Room to find the key for.
     * @return Rooms that contain the key, or -1 if there is no room.
     */
    public int findRoomWithKey(int room) {

        for (int i = 0; i < size; i++) {
            if (keyLocations[i][room] > 0) {
                logger.info("FOUND: The key for room " + room + " is in room " + i);
                return i;
            }
        }

        logger.warning("This room does not have a key!");
        return -1;
    }

}
