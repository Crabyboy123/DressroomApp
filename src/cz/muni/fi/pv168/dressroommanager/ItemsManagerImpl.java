/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.muni.fi.pv168.dressroommanager;

import cz.muni.fi.pv168.common.DBUtils;
import cz.muni.fi.pv168.common.IllegalEntityException;
import cz.muni.fi.pv168.common.ServiceFailureException;
import cz.muni.fi.pv168.common.ValidationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.*;
import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Anna
 */
public class ItemsManagerImpl implements ItemsManager{
       
    private DataSource dataSource;
    
    private static final Logger logger = Logger.getLogger(
            ItemsManagerImpl.class.getName());

    /*
    public ItemsManagerImpl( ) {}
    
    public ItemsManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    */
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    
    @Override
    public void createItem(Item item){
        //logger.log(Level.INFO, "Attempt to add an item: {0}", item.toString());
        checkDataSource();
        validate(item);
        if (item.getId() != null) {
            throw new IllegalEntityException("item id is already set");
        } 
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "INSERT INTO ITEM (type,add_date,gender,size,note) "
                    + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                st.setString(1, item.getType());
                st.setDate(2, item.getAdded());
                st.setString(3, item.getGender().name());
                st.setString(4, item.getSize());
                st.setString(5, item.getNote());
                //st.setLong(6, item.getCloset().getId());
            
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, item, true);
            
            Long id = DBUtils.getId(st.getGeneratedKeys());
            item.setId(id);
            conn.commit();
        } catch (SQLException ex){
            String msg = "Error when inserting item into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally{
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void deleteItem(Item item) {
        checkDataSource();
        if(item == null){
            throw new IllegalArgumentException("The item is null, can't be deleted");
        }
        if(item.getId() == null){
            throw new IllegalArgumentException("Id of the item is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM item WHERE id = ?");
                st.setLong(1, item.getId());
                
                int count = st.executeUpdate();
                DBUtils.checkUpdatesCount(count, item, false);
                conn.commit();
            
        } catch (SQLException ex) {
            String msg = "Error when deleting item from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }   
    
    @Override
    public Item getItemById(Long id) throws ServiceFailureException{
        
        checkDataSource();
        
        if(id == null){
            throw new IllegalArgumentException("id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try{
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT id, type, ADD_DATE, gender, size, note"
                    + " FROM item WHERE ID = ?");
            st.setLong(1, id);
            return executeQueryForSingleItem(st);
        } catch (SQLException ex){
            String msg = "Error when getting item with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    @Override
    public void updateItem(Item item) {
        checkDataSource();
        validate(item);
        
        if (item.getId() == null) {
            throw new IllegalEntityException("item id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);            
            st = conn.prepareStatement(
                    "UPDATE Item SET type = ?, gender = ?, size = ?, note = ?, closet = ? WHERE id = ?");
                st.setString(1, item.getType());
                st.setString(2, item.getGender().name());
                st.setString(3, item.getSize());
                st.setString(4, item.getNote());
                st.setString(5, item.getCloset().getName());
                st.setLong(6, item.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, item, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when updating item in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }
    
    static Item executeQueryForSingleItem(PreparedStatement st) throws SQLException, ServiceFailureException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Item result = resultSetToItem(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more items with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
    static private void validate(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("item is null");
        }
        if (item.getType().length() < 1) {
            throw new ValidationException("type of item is empty");
        }
        if (item.getType().length() < 1) {
            throw new ValidationException("item's closet name is empty");
        }
        /*
        if(closet.getOwner().matches(".*\\d.*")){
            throw new ValidationException("owner contains number");
        } 
        */
    }
    
    static private Item resultSetToItem(ResultSet rs) throws SQLException{
        Item item = new Item();
        item.setId(rs.getLong("id"));
        item.setType(rs.getString("type"));
        item.setAdded(rs.getDate("ADD_DATE"));
        //item.setGender(rs.getString("gender"));
        item.setSize(rs.getString("size"));
        item.setNote(rs.getString("note"));
        //item.setCloset(rs.getString("closet"));
        
        return item;
    }
}
