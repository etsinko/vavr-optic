package io.vavr.optic;

import io.vavr.control.Option;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PrismTest {
    @Test
    public void testPrismSome() {
        Prism<String, Integer> prism = Prism.prism(this::decode, Object::toString);
        assertThat(prism.getOption("18"), is(Option.some(18)));
    }

    @Test
    public void testPrismNone() {
        Prism<String, Integer> prism = Prism.prism(this::decode, Object::toString);
        assertThat(prism.getOption("Z"), is(Option.none()));
    }

    private Option<Integer> decode(String s) {
        try {
            return Option.some(Integer.decode(s));
        } catch (NumberFormatException nfe) {
            return Option.none();
        }
    }
}