package com.dhenton9000.auctions.service.impl;


import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import com.ninja_squad.dbsetup.operation.Operation;
 
/**
 *
 * @author dhenton
 */
public class DBOperations {
    public static final Operation DELETE_ALL = 
        deleteAllFrom("AUCTION_BIDS", "AUCTION_ITEMS", "BIDDERS");
}
