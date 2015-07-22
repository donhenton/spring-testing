/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.auctions.service.impl;

import com.dhenton9000.auctions.model.AuctionItem;
import com.dhenton9000.auctions.model.Bidders;
import com.dhenton9000.auctions.service.AuctionService;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.operation.Operation;
import javax.sql.DataSource;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
//http://dbsetup.ninja-squad.com/

/**
 * This test set rolls back the test changes. It has the problem of
 * not letting you see the data after the tests
 * 
 * @author dhenton
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testing-context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class})
@Transactional
public class TransactionAwareAuctionServiceImplTests {

    private static final Logger LOG
            = LoggerFactory.getLogger(TransactionAwareAuctionServiceImplTests.class);

    @Autowired
    private Destination destination;
    @Autowired
    private AuctionService auctionService;

    @Before
    public void prepare() throws Exception {
        
      Operation itemsOperations =   Operations.sequenceOf(
        
              insertInto("AUCTION_ITEMS")
                        .columns("ID","AUCTION_DESCRIPTION", "STARTING_BID")
                        .values(1,"alpha", "99.99")
                        .values(2,"beta", "88.90")
                        .values(3,"gamma", "677.90")
                        .values(4,"epsilon", "222.90")
                        .build()
        );
      
      Operation bidOperations =   Operations.sequenceOf(
        
              insertInto("AUCTION_BIDS")
                        .columns("ID","AUCTION_ITEM_ID", "BIDDER_ID")
                        .values(1,1, 2)
                        .values(2,2,2)
                        .values(3,3,2)
                        .build()
        );
        
        Operation operation
                = sequenceOf(
                        DBOperations.DELETE_ALL,
                        
                        
                        insertInto("BIDDERS")
                        .columns("ID","USER_NAME", "PASSWORD")
                        .values(1,"user 100", "Amazon")
                        .values(2,"user 200", "Snort")
                        .build(),
                        
                        itemsOperations ,
                         bidOperations
                        
                        
                );

        DbSetup dbSetup = new DbSetup(destination, operation);
        dbSetup.launch();
    }

    @Test
    public void testAssemble() {
        assertNotNull(auctionService);
        AuctionItem item = auctionService.getAuctionItem(2);
        assertNotNull(item);
        assertEquals("beta", item.getAuctionDescription());
    }

    @Test
    public void testGetBidders() {

        Bidders b = auctionService.getBidderByUserName("user 100");
        assertNotNull(b);
        int id = b.getId();
        assertTrue(id > 0);

        b = auctionService.getBidderById(id);
        assertEquals(id, b.getId().intValue());
        assertEquals("Amazon", b.getPassword());
    }

    @Test
    public void testGetBiddersWithItems() {

        Bidders b = auctionService.getBidderByUserName("user 100");
        assertNotNull(b);

        int id = b.getId();
        assertTrue(id > 0);

        b = auctionService.getBiddersWithItems(2);
        assertEquals(3, b.getAuctionItems().size());
        boolean gotIt = false;
        for (AuctionItem item : b.getAuctionItems()) {

            if (item.getAuctionDescription().equals("beta")) {
                gotIt = true;
                break;
            }

        }
        assertTrue(gotIt);
    }

    @Test
    public void testInserts() {
        Bidders b = new Bidders();
        b.setUserName("user 100");
        b.setPassword("frump");
        Integer res = auctionService.insertBidder(b);
        assertNotNull(res);

        AuctionItem ac = new AuctionItem();
        ac.setAuctionDescription("alpha");
        ac.setStartingBid(new Float(1));
        Integer res2 = auctionService.insertAuctionItem(ac);
        assertNotNull(res2);

        res2 = auctionService.insertBidForItem(1, 1);
        assertNotNull(res2);

    }

    @Test
    public void testUpdatesForBidders() {

        Bidders b = auctionService.getBidderByUserName("user 100");
        assertNotNull(b);
        int id = b.getId();
        assertTrue(id > 0);

        b.setUserName("bonzo");
        auctionService.updateBidders(b);
        b = auctionService.getBidderById(id);
        assertEquals("bonzo", b.getUserName());
    }

    @Test
    public void testUpdatesForAuctionItems() {
        AuctionItem item = auctionService.getAuctionItem(1);
        assertEquals("alpha", item.getAuctionDescription());
        item.setAuctionDescription("FRED");
        item.setStartingBid(new Float(3));
        auctionService.updateAuctionItem(item);
        item = auctionService.getAuctionItem(1);
        assertEquals("FRED", item.getAuctionDescription());
        assertEquals(new Float(3), item.getStartingBid());

    }

    @Test
    public void testDeleteForBidders() {

        auctionService.deleteBidders(1);
        Bidders b = auctionService.getBidderById(1);
        assertNull(b);

    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testDeleteForAuctionItemsFailsIfInUse() {

        auctionService.deleteAuctionItem(1);
        

    }
    
    @Test 
    public void testDeleteForAuctionItemsIfNotInUse() {

        AuctionItem item = auctionService.getAuctionItem(4);
        assertNotNull(item);
        
        auctionService.deleteAuctionItem(4);
        item = auctionService.getAuctionItem(4);
        assertNull(item);

    }

}
