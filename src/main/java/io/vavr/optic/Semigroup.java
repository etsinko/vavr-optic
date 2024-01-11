package io.vavr.optic;

public interface Semigroup<A> {
    A sum(A a1, A a2);
}
