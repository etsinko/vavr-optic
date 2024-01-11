package io.vavr.optic;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.Test;

import static org.junit.Assert.*;

public class IsoTest {
    @Test
    public void testIso() {
        final int oldNumber = 10;
        final String oldStreet = "Main St";
        final Address oldAddress = new Address(oldNumber, oldStreet);
        final Iso<Address, Tuple2<Integer, String>> addressIso = Iso.iso(p -> Tuple.of(p.number, p.street),
                p -> new Address(p._1(), p._2()));
        final Address a = addressIso.reverseGet(addressIso.get(oldAddress));
        assertEquals(a.number, oldAddress.number);
        assertEquals(a.street, oldAddress.street);
    }
}