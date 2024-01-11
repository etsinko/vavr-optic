package io.vavr.optic;

import io.vavr.control.Either;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TraversalTest {
    @Test
    public void testTraversalLeft() {
        final Traversal<Either<Integer, Integer>, Integer> t = Traversal.codiagonal();
        assertThat(t.fold(Monoid.intMinMonoid).apply(Either.left(3)), is(3));
    }

    @Test
    public void testTraversalRight() {
        final Traversal<Either<Integer, Integer>, Integer> t = Traversal.codiagonal();
        assertThat(t.fold(Monoid.intMinMonoid).apply(Either.right(2)), is(2));
    }

}