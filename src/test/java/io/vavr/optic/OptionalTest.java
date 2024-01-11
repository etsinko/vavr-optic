package io.vavr.optic;

import io.vavr.control.Option;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class OptionalTest {
    @Test
    public void testOptionalSome() {
        Optional<String, Integer> o = Optional.optional(this::decode, i -> s -> s);
        assertThat(o.getOption("18"), is(Option.some(18)));
    }

    @Test
    public void testOptionalNone() {
        Optional<String, Integer> o = Optional.optional(this::decode, i -> s -> s);
        assertThat(o.getOption("Z"), is(Option.none()));
    }

    private Option<Integer> decode(String s) {
        try {
            return Option.some(Integer.decode(s));
        } catch (NumberFormatException nfe) {
            return Option.none();
        }
    }
}