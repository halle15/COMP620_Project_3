package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import src.DungeonMap;

class DungeonMapTest {

    @Test
    void testGraph1Key1() {
        DungeonMap dM = new DungeonMap("testGraph1.txt", "testKey1.txt", Level.ALL);

        
        dM.printAdjacencyMatrix();
        
        dM.printKeyLocations();
        
        dM.runFloydWarshall();
                
        dM.printFloydWarshallMap();
                
        assertEquals(dM.solveDungeon(dM.startVertex, dM.endVertex).toString(), "[0, 1, 2, 1, 3, 5, 9, 8, 10, 11, 12, 11, 10, 8, 9, 5, 6, 5, 3, 1, 2, 4, 7]");
    }
    
    @Test
    void testGraph2Key2() {
        DungeonMap dM = new DungeonMap("testGraph2.txt", "testKey2.txt", Level.ALL);

        
        dM.printAdjacencyMatrix();
        
        dM.printKeyLocations();
        
        dM.runFloydWarshall();
                
        dM.printFloydWarshallMap();
                
        assertEquals(dM.solveDungeon(dM.startVertex, dM.endVertex).toString(), "[0, 2, 1, 2, 3, 2, 4, 5, 6]");
    }
    
    @Test
    void testShortGraphShortKey1() {
        DungeonMap dM = new DungeonMap("shortGraph1.txt", "shortKey1.txt");
        
        dM.printAdjacencyMatrix();
        
        dM.printKeyLocations();
        
        assertEquals("[0, 1, 3, 1, 2, 4]", dM.solveDungeon(dM.startVertex, dM.endVertex).toString());
    }

}
