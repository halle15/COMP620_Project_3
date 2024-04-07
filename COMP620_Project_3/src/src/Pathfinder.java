package src;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.logging.Logger;

public class Pathfinder {
    
    private DungeonMap dM;
    private PriorityQueue<Integers> frontier;
    
    private Logger logger = Logger.getLogger(DungeonMap.class.getName());
    
    public  Pathfinder(DungeonMap dM) {
        this.dM = dM;
        this.frontier = new PriorityQueue<>(Comparator.comparingInt(node -> node.jumps));

    }
    /**
     * 
     * @return Returns ArrayList<Integer> of path of rooms to go to get to end
     */
    public ArrayList<Integer> findPath() {

        ArrayList<Integer> foundPath = new ArrayList<Integer>();

        int currentPosition = dM.startVertex;
        
        
        
        /*
         * Modified Djikstra's Params
         * 
         */
        
        int[] distances = new int[size];
        boolean[] visited = new boolean[size];
        int[] predecessors = new int[size];
        ArrayList<Integer> path = new ArrayList<>();

        logger.info("Starting pathfinding at " + dM.startVertex + " with target " + dM.endVertex);

        while (currentPosition != dM.endVertex) {
            
            
            /*
             * TODO: actually make a pathfinding algorithm
             */
            
            
            /*
             * FIRST IDEA
             * 
             * Possibly start by finding a path to all possible nodes, ignoring those that we can't hit just yet:
             *      So if there is a path with a weight above 0, we just stop searching beyond that and treat it
             *      as a max infinity in terms of our priority pathing, as to prevent us from trying to path far
             *      beyond doors that don't have anything behind them.
             * 
             * Find those that have keys, and place them in the priority queue, then after the keys the doors that those open
             * 
             * We can then hit the door, and when we do we would redo out paths, so we would have a repetition of:
             * 
             * We must keep track of a "real" cost of the locks, and a "jump" cost in our pathfinding algorithm as to limit
             * the numbers of jumps to grab keys then access doors.
             * 
             * Pseudocode:
             * 
             * Find all possible nodes we can travel to without using a key.
             *      If the exit is here, take and output the path to this.
             * If the exit is not in those, we take all the shortest paths to these keys.
             * As soon as we take a key, the paths are updated, so we reupdate our priorities.
             * 
             * 
             */
            
            
            /*
             * SECOND IDEA:
             * 
             * If we just start willy nilly ignoring what doors are behind what, we will just
             * prioritize finding all keys anyways and do some stupid breadth first search type,
             * when in reality we have a map and do not need to do this.
             * 
             * Instead, we can start by creating a shortest path and consider the weight through doors
             * 
             * We will have this preliminary path that we would go through IF we had the keys,
             * 
             * So, for each door we just need to have a priority to find those keys before the door
             * 
             * What if this key has a door that needs another key?
             * 
             * We just keep reiterating and refining the algorithm until we have no more modifications to make.
             * 
             * So the pseudocode is:
             * 
             * Run a regular pathfinding algorithm as normal to get the optimal path if we had the keys
             *
             * Put these in order for our priority queue
             * 
             * 
             *
             *
             * 
             */

            break;
        }

        return foundPath;
    }
}
