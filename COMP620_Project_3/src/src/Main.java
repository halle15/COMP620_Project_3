package src;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
        
    /* key reduces the weight of all edges going to a node by a cost.
    // graph.txt is 4 lines, first is n of vertices (from 0 to n-1)
    // second is start node
    // third is destination
    // all next are in form node a, node b, cost c where input is a,b,c to say:
    // to go from a to b for a cost of c
    */
    
    /*
     * keys.txt is list of keys formatted a,b,c.. (OPTIONALLY d, e, ....) where:
     * in room a there is a key that reduces the weight by b for all edges going to vertex c, d, e...
     */
    
    /*
     * need to output the most efficient path in a comma listed format of edges to travel to.
     * ex:
     * 
     * 1, 3, 5, 3, 6, 3, 2
     * 
     * output is "the possible path that the student can take from the entrance that gets into the exit following the rules
     * 
     * rules:
     * 
     * ● There are n vertices that represent stops numbered (0, 1, …. , n-1)
       ● Edges are directed and separate: if A->B is an edge, B->A can also be an edge, but for a
               given i and j there cannot be more than one edge connecting Vi -> Vj 
       ● No vertex can hold more than one key.
       ● Multiple keys might be needed to open a door
     *
     *  efficiency goal is reasonably as short as possible without having to brute force.
     *
     */
    
    /*
     * Runtime:
     * 
     * should request user input for a file
     * should then request a user for a key file
     * 
     * should construct graph, display path
     */
    
    public static void main(String[] args) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.FINE);
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            handlers[0].setLevel(Level.FINE);
        }
        
        System.out.print("\u001B[36m");
        
        DungeonMap dM = new DungeonMap("testGraph1.txt", "testKey1.txt", Level.ALL);
        
        
        dM.printAdjacencyMatrix();
        
        dM.printKeyLocations();
        
        dM.runFloydWarshall();
        
        rootLogger.info(dM.findOptimalPathUsingFloydWarshall(dM.startVertex, dM.endVertex).toString());
        
        dM.printFloydWarshallMap();
        
        dM.solveDungeon(dM.startVertex, dM.endVertex);
        
        
        /*
        dM = new DungeonMap("shortGraph1.txt", "shortKey1.txt");
        
        dM.printAdjacencyMatrix();
        
        dM.printKeyLocations();
       
        rootLogger.info("Checking where key is for room 2");
        
        rootLogger.info(dM.findRoomsWithKey(2).toString());
        
        dM.runFloydWarshall();
        
        rootLogger.info(dM.findLockedRoomsInPath(dM.findOptimalPathUsingFloydWarshall()).toString());
                
        dM.solveDungeon(dM.startVertex, dM.endVertex);
        
        */
        /* testing double take
        dM.grabKey(3);
        */
        
        
        /* testing removing key
        rootLogger.info(dM.isKey(3).toString());
        
        dM.removeRoomKey(3);
        
        rootLogger.info(dM.isKey(3).toString());
        */
        
    }
}
