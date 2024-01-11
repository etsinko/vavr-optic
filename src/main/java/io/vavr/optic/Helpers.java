package io.vavr.optic;

import io.vavr.Tuple1;
import io.vavr.Value;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.function.Function;

class Helpers {
    private Helpers() {
    }

    /**
     * Function composition.
     *
     * @param f A function to compose with another.
     * @param g A function to compose with another.
     * @return A function that is the composition of the given arguments.
     */
    public static <A, B, C> Function<A, C> compose(final Function<B, C> f, final Function<A, B> g) {
        return g.andThen(f);
    }

    /**
     * Performs function application within a higher-order function (applicative functor pattern).
     *
     * @param cab The higher-order function to apply a function to.
     * @param ca  A function to apply within a higher-order function.
     * @return A new function after applying the given higher-order function to the given function.
     */
    public static <A, B, C> Function<C, B> apply(final Function<C, Function<A, B>> cab, final Function<C, A> ca) {
        return c -> {
            A a = ca.apply(c);
            return cab.apply(c).apply(a);
        };
    }

    /**
     * Function application on this projection's value.
     *
     * @param f The either of the function to apply on this projection's value.
     * @return The result of function application within either.
     */
    public static <A, B, X> Either<A, X> apply(Either<A, B> e, Either<A, Function<B, X>> f) {
        return f.flatMap(e::map);
    }

    /**
     * Performs function application within a list (applicative functor pattern).
     *
     * @param lf The list of functions to apply.
     * @return A new list after applying the given list of functions through this list.
     */
    public static <A, B> List<B> apply(final Value<A> l, final List<Function<A, B>> lf) {
        return lf.flatMap(l::map);
    }

    /**
     * Performs function application within an optional value (applicative functor pattern).
     *
     * @param of The optional value of functions to apply.
     * @return A new optional value after applying the given optional value of functions through this
     * optional value.
     */
    public static <A, B> Option<B> apply(final Option<A> l, final Option<Function<A, B>> of) {
        return of.flatMap(l::map);
    }

    /**
     * Performs function application within a list (applicative functor pattern).
     *
     * @param lf The list of functions to apply.
     * @return A new list after applying the given list of functions through this list.
     */
    public static <A, B> Stream<B> apply(final Stream<A> l, final Stream<Function<A, B>> lf) {
        return lf.flatMap(l::map);
    }

    /**
     * Performs function application within an optional value (applicative functor pattern).
     *
     * @param of The optional value of functions to apply.
     * @return A new optional value after applying the given optional value of functions through this
     * optional value.
     */
    public static <A, B> Tuple1<B> apply(final Tuple1<A> l, final Tuple1<Function<A, B>> of) {
        return of.map(f -> f.apply(l._1()));
    }

    /**
     * Function application on the successful side of this validation, or accumulating the errors on the failing side
     * using the given semigroup should one or more be encountered.
     *
     * @param s The semigroup to accumulate errors with if
     * @param v The validating function to apply.
     * @return A failing validation if this or the given validation failed (with errors accumulated if both) or a
     * succeeding validation if both succeeded.
     */
    public static <E, T, A> Validation<E, A> accumapply(final Semigroup<E> s, Validation<E, T> a, final Validation<E, Function<T, A>> v) {
        return a.isInvalid()
               ? Validation.invalid(v.isInvalid()
                                    ? s.sum(v.getError(), a.getError())
                                    : a.getError())
               : v.isInvalid()
                 ? Validation.invalid(v.getError())
                 : Validation.valid(v.get().apply(a.get()));
    }
}
