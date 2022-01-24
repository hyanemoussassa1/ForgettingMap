package com.origamienergy.forgettingmap;

import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ForgettingMapTests {

    @Test
    public void testForgettingMapCanAddAnAssociation(){
        ForgettingMap map = new ForgettingMap(10);
        boolean success = map.add("key", "content");
        assertTrue(success);
    }

    @Test
    public void testForgettingMapCanFindAnExistingAssociation(){
        ForgettingMap map = new ForgettingMap(10);
        String key = "key";
        String content = "content";
        map.add(key, content);
        String contentFound = (String) map.find(key);
        assertEquals(contentFound, content);
    }

    @Test
    public void testForgettingMapCanAddAnAssociationOfIntegerKey(){
        ForgettingMap map = new ForgettingMap(10);
        boolean success = map.add(10, "content");
        assertTrue(success);
    }

    @Test
    public void testForgettingMapAddsNoMoreThanXAssociations(){
        ForgettingMap map = new ForgettingMap(3);
        map.add(10, "content_10");
        map.add(15, "content_15");
        map.add(20, "content_20");
        map.add(50, "content_30");
        assertEquals(3, map.size());
    }

    @Test
    public void testForgettingMapDoesNotContainTopmostUnusedAssociation(){
        ForgettingMap map = new ForgettingMap(3);
        map.add(10, "content_10");
        map.add(15, "content_15");
        map.add(20, "content_20");
        map.add(50, "content_50");
        assertNotNull(map.find(15));
        assertNotNull(map.find(20));
        assertNotNull(map.find(50));
        String notFound = (String) map.find(10);
        assertNull(notFound);
    }

    @Test
    public void testForgettingMapReturnsAccessOrder(){
        ForgettingMap map = new ForgettingMap(3);
        map.add(0, "content_0");
        map.add(15, "content_15");
        map.add(30, "content_30");
        Set<Integer> keys = map.keySet();
        assertEquals("[0, 15, 30]", keys.toString());
        map.find(15);
        assertEquals("[0, 30, 15]", keys.toString());
        map.find(0);
        assertEquals("[30, 15, 0]", keys.toString());
    }


    @Test
    public void testForgettingMapDoesNotContainLeastRetrievedByFind(){
        ForgettingMap map = new ForgettingMap(3);
        map.add(0, "content_0");
        map.add(15, "content_15");
        map.add(30, "content_30");
        Set<Integer> keys = map.keySet();
        map.find(15);
        map.find(15);
        map.find(15);
        map.find(0);
        map.find(30);
        map.find(30);
        map.add(40, "content_40");
        assertEquals(3, map.size());
        assertTrue(keys.contains(15));
        assertTrue(keys.contains(30));
        assertTrue(keys.contains(40));
        assertFalse(keys.contains(0));
    }

    @Test
    public void testForgettingMapDoesNotContainEntriesNotFound(){
        ForgettingMap map = new ForgettingMap(3);
        map.add(30, "content_0");
        map.add(15, "content_15");
        map.add(0, "content_30");
        Set<Integer> keys = map.keySet();
        map.find(15);
        map.find(0);
        map.add(40, "content_40");
        assertTrue(keys.contains(15));
        assertTrue(keys.contains(0));
        assertTrue(keys.contains(40));
        assertFalse(keys.contains(30));
    }

    @Test
    public void testForgettingMapWithMultipleLeastUsedEntries(){
        ForgettingMap map = new ForgettingMap(3);
        map.add(0, "content_0");
        map.add(15, "content_15");
        map.add(30, "content_30");
        Set<Integer> keys = map.keySet();
        map.find(15);
        map.find(15);
        map.find(15);
        map.find(0);
        map.find(30);
        map.add(40, "content_40");
        assertEquals(3, map.size());
        assertTrue(keys.contains(15));
        assertTrue(keys.contains(30));
        assertTrue(keys.contains(40));
        assertFalse(keys.contains(0));
    }

    @Test
    public void testForgettingMapWithMultipleAddsAndFind(){
        ForgettingMap map = new ForgettingMap(3);
        RandomString random = new RandomString();
        for (int i = 0; i < 500; i++) {
            map.add(i, random.nextString());
        }
        Set<Integer> keys = map.keySet();
        assertFalse(keys.contains(0));
    }

    @Test
    public void testForgettingMapWithAddConcurrency() throws InterruptedException {
        int numberOfThreads = 25;
        ForgettingMap map = new ForgettingMap(3);
        RandomString random = new RandomString();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                latch.countDown();
                map.add(random.nextString(), random.nextString());
            });
        }
        latch.await();
        assertEquals(3, map.keySet().size());
    }

    @Test
    public void testForgettingMapWithAddAndFindConcurrency() throws InterruptedException {
        int numberOfThreads = 25;
        ForgettingMap map = new ForgettingMap(3);
        RandomString random = new RandomString();
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService service = Executors.newFixedThreadPool(10);
        ConcurrentSkipListSet<String> addedKeys = new ConcurrentSkipListSet<>();
        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                latch.countDown();
                String key = random.nextString();
                addedKeys.add(key);
                map.add(key, random.nextString());
                for (String et: addedKeys) {
                    map.find(et);
                }
            });
        }
        latch.await();
        System.out.println(map.keySet());
        assertEquals(3, map.keySet().size());
    }

}
