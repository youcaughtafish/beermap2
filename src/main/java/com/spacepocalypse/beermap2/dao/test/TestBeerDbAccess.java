package com.spacepocalypse.beermap2.dao.test;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.spacepocalypse.beermap2.dao.BeerDbAccess;
import com.spacepocalypse.beermap2.domain.MappedBeer;
import com.spacepocalypse.beermap2.domain.MappedBrewery;
import com.spacepocalypse.beermap2.service.Constants;
import com.spacepocalypse.util.Conca;


public class TestBeerDbAccess  {
	private BeerDbAccess dbAccess;
	private Logger log4jLogger;
	
	@Before
	public void setup() {
		log4jLogger = Logger.getLogger(getClass());
		log4jLogger.setLevel(Level.INFO);
		
		ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/dbaccess.xml");
        dbAccess = (BeerDbAccess)ctx.getBean("dbAccess");
	}
	
	public void copyValues() {
	    final Connection conn = dbAccess.getDbConnection();
	    
	    try {
            final PreparedStatement stmt = 
                    conn.prepareStatement(Conca.t("select id from beer_rating"));
            final ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                final int id = rs.getInt("id");
                
                final PreparedStatement ratingValQuery = conn.prepareStatement(
                        "select v.value " +
                		"from beer_rating_lv v join beer_rating r on (r.rating_fk = v.id) " +
                		"where r.id = ?");
                ratingValQuery.setInt(1, id);
                final ResultSet valueRS = ratingValQuery.executeQuery();
                
                if (valueRS.next()) {
                    final int value = valueRS.getInt(1);
                    final PreparedStatement insertStmt = conn.prepareStatement("update beer_rating " +
                    		"set rating_value = ? " +
                    		"where id = ?");
                    insertStmt.setInt(1, value);
                    insertStmt.setInt(2, id);
                    
                    insertStmt.execute();
                }
                
//                conn.prepareStatement("insert into beer_rating (rating_value) values ()
            }
	    
	    } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	@Ignore
	@Test
	public void testFindBrewery() throws InvalidParameterException, SQLException {
		Map<String, String> params = new HashMap<String, String>();
		params.put(Constants.QUERY_KEY_BREWERY_NAME, "%head%");
		
		List<MappedBeer> beers = dbAccess.findAllBeers(params);
		
		TestCase.assertNotNull(beers);
		TestCase.assertTrue(beers.size() > 0);
		
		log4jLogger.info(Conca.t("beers returned: {", beers.toString(), "}"));
	}
	
	@Ignore
	@Test
    public void testFindAllBeersForUser() {
        final List<MappedBeer> beers = dbAccess.findBeersForUserId(1);
        
        TestCase.assertNotNull(beers);
        TestCase.assertNotNull(beers.size() > 0);
        
        log4jLogger.info(Conca.t("beers returned from query: ", beers.size(), " result: [", beers, "]"));
    }
	
	@Ignore
	@Test
	public void testFindAllBreweries() {
	    final List<MappedBrewery> breweries = dbAccess.findAllBreweries();
	    
	    TestCase.assertNotNull(breweries);
	    TestCase.assertNotNull(breweries.size() > 0);
	    
	    log4jLogger.info(Conca.t("breweries returned from query: ", breweries.size()));
	}
	
	@Test
	public void testInsertBeer() throws SQLException {
	    final Connection conn1 = dbAccess.getDbConnection();
	    conn1.setAutoCommit(false);
	    
	    final Connection conn2 = dbAccess.getDbConnection();
	    conn2.setAutoCommit(false);
	    
	    conn1.prepareStatement("insert into beers (name) values ('test1')").executeUpdate();
	    PreparedStatement stmt1 = conn1.prepareStatement("select last_insert_id() as last_id_1");
        stmt1.execute();
        ResultSet resultSet1 = stmt1.getResultSet();
        resultSet1.next();
        int lastId1 = resultSet1.getInt("last_id_1");
	    
        PreparedStatement stmt2 = conn2.prepareStatement("select last_insert_id() as last_id_2");
        stmt2.execute();
        ResultSet resultSet2 = stmt2.getResultSet();
        resultSet2.next();
        int lastId2 = resultSet2.getInt("last_id_2");
	    
        TestCase.assertTrue(lastId1 != lastId2);
        
        conn1.rollback();
        conn2.rollback();
        conn1.close();
        conn2.close();
	}
}
