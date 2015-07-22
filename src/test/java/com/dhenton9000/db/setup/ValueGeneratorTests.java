/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.db.setup;

import com.ninja_squad.dbsetup.generator.SequenceValueGenerator;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author dhenton
 */
public class ValueGeneratorTests {
    
    @Test
    public void testValueGeneration()
    {
        SequenceValueGenerator v = 
                ValueGenerators.sequence().startingAt(1000L).incrementingBy(10);
        assertEquals(new Long(1000),v.nextValue());
        assertEquals(new Long(1010),v.nextValue());
    }
}
