/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.muni.fi.pv168.dressroommanager;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ItemsManagerImpl methods
 * @author Anna
 */
public class ItemsManagerImplTest {
    private ItemsManagerImpl manager;
    
    @Before
    public void setUp() {
        manager = new ItemsManagerImpl();
    }
    
    /**
     * Test of createItem method, of class ItemsManagerImpl.
     */
    @Test
    public void createItem(){
        //test if is in database
        System.out.println("test createItem");
        
        Item item = newItem("shirt", Gender.FEMALE, "XS", " ");
        manager.createItem(item);
        
        assertNotNull(item.getId());
        Item result = manager.getItemById(item.getId());
        assertEquals(item, result);
        assertNotSame(item, result);
    }
    
    /**
     * Another test of createItem. Testing wrong attributes
     */
    @Test
    public void createItemWithWrongAttributes(){
        try {
            manager.createItem(null);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        Item item = newItem("socks", Gender.MALE, "39", "");
        item.setId(2L);
        try {
            manager.createItem(item);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        item = newItem(null, null, null, null);
        try {
            manager.createItem(item);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        item = newItem(null, Gender.BOTH, "S", "nice");
        try {
            manager.createItem(item);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        item = newItem("", Gender.BOTH, "S", "nice");
        try {
            manager.createItem(item);
            fail();
        } catch (IllegalArgumentException ex) {
            //OK
        }
        
        // these variants should be ok
        item = newItem("socks", Gender.MALE, "39", "");
        manager.createItem(item);
        Item result = manager.getItemById(item.getId());
        assertNotNull(result);
        
        item = newItem("socks", Gender.MALE, "39", null);
        manager.createItem(item);
        result = manager.getItemById(item.getId());
        assertNotNull(result);
        assertNull(result.getNote());
        
        item = newItem("socks", Gender.FEMALE, null, "nice");
        manager.createItem(item);
        result = manager.getItemById(item.getId());
        assertNotNull(result);
        assertNull(result.getSize());
    }
    
    /**
     * Test of updateItem method of ItemsManagerImpl class
     */
    @Test
    public void updateItem(){
        System.out.println("test updateItem");
        
        Item item1 = newItem("shirt", Gender.FEMALE, "XS", " ");
        Item item2 = newItem("skirt", Gender.FEMALE, "S", "knee length, formal, black");
        manager.createItem(item1);
        manager.createItem(item2);
        Long item1Id = item1.getId();
        
        //change of gendre
        item1 = manager.getItemById(item1Id);
        item1.setGender(Gender.MALE);
        manager.updateItem(item1);
        assertEquals(item1.getGender(), Gender.MALE);
        
        //change type to null
        item1 = manager.getItemById(item1Id);
        item1.setType(null);
        manager.updateItem(item1);
        assertNull(item1.getType());   

        //change of type
        item1 = manager.getItemById(item1Id);
        item1.setType("jacket");
        manager.updateItem(item1);
        assertEquals("jacket", item1.getType());
        
        //change of note
        item1 = manager.getItemById(item1Id);
        item1.setNote("very nice shirt with Superman logo");
        manager.updateItem(item1);
        assertNotNull(item1.getNote());
        assertEquals("cool shirt with Superman logo", item1.getNote());
        
        //change of size
        item1 = manager.getItemById(item1Id);
        item1.setSize("L");
        manager.updateItem(item1);
        assertNotNull(item1);
        assertEquals("L", item1.getSize());
        
        // Check if updates didn't affected other records
        assertDeepEquals(item2, manager.getItemById(item2.getId()));
    }

    /**
     * Test of getItemById method of ItemsManagerImpl class
     */
    @Test
    public void getItemById(){
        System.out.println("test getItemById");
        
        Long id = null;
        ItemsManagerImpl instance = new ItemsManagerImpl();
        Item expResult = null;
        Item result = instance.getItemById(id);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");   
    }
    
    /**
     * Test of deleteItem method of ItemsManagerImpl class.
     */
    @Test
    public void deleteItem(){
        System.out.println("test deleteItem");
        
        Item item1 = newItem("shirt", Gender.FEMALE, "XS", " ");
        Item item2 = newItem("skirt", Gender.FEMALE, "S", "popis");
        manager.createItem(item1);
        manager.createItem(item2);
        
        assertNotNull(manager.getItemById(item1.getId()));
        assertNotNull(manager.getItemById(item2.getId()));
        
        manager.deleteItem(item1);
        
        assertNull(manager.getItemById(item1.getId()));
        assertNotNull(manager.getItemById(item2.getId()));
    }
        
    private static Item newItem(String type, Gender gender, String size, String note){
        Item item = new Item();
        item.setType(type);
        item.setGender(gender);
        item.setSize(size);
        item.setNote(note);
        return item; 
    }
    
    private void assertDeepEquals(Item item, Item result) {
        assertEquals(item.getId(), result.getId());
        assertEquals(item.getGender(), result.getGender());
        assertEquals(item.getType(), result.getType());
        assertEquals(item.getSize(), result.getSize());
        assertEquals(item.getNote(), result.getNote());
        
        //throw new UnsupportedOperationException("Not supported yet."); 
    }
}
