package io.vavr.optic;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.util.function.Function;

/**
 * A {@link Getter} can be seen as a glorified get method between a type S and a type A.
 *
 * A {@link Getter} is also a valid {@link Fold}
 *
 * @param <S> the source of a {@link Getter}
 * @param <A> the target of a {@link Getter}
 */
public abstract class Getter<S, A> {

    Getter() {
        super();
    }

    /** get the target of a {@link Getter} */
    public abstract A get(S s);

    /** join two {@link Getter} with the same target */
    public final <S1> Getter<Either<S, S1>, A> sum(final Getter<S1, A> other) {
        return getter(e -> e.fold(this::get, other::get));
    }

    /** pair two disjoint {@link Getter} */
    public final <S1, A1> Getter<Tuple2<S, S1>, Tuple2<A, A1>> product(final Getter<S1, A1> other) {
        return getter(p2 -> Tuple.of(this.get(p2._1()), other.get(p2._2())));
    }

    public final <B> Getter<Tuple2<S, B>, Tuple2<A, B>> first() {
        return getter(p -> Tuple.of(this.get(p._1()), p._2()));
    }

    public final <B> Getter<Tuple2<B, S>, Tuple2<B, A>> second() {
        return getter(p -> Tuple.of(p._1(), this.get(p._2())));
    }

    /*************************************************************/
    /** Compose methods between a {@link Getter} and another Optics */
    /*************************************************************/

    /** compose a {@link Getter} with a {@link Fold} */
    public final <B> Fold<S, B> composeFold(final Fold<A, B> other) {
        return asFold().composeFold(other);
    }

    /** compose a {@link Getter} with a {@link Getter} */
    public final <B> Getter<S, B> composeGetter(final Getter<A, B> other) {
        return getter(s -> other.get(get(s)));
    }

    /** compose a {@link Getter} with a {@link POptional} */
    public final <B, C, D> Fold<S, C> composeOptional(final POptional<A, B, C, D> other) {
        return asFold().composeOptional(other);
    }

    /** compose a {@link Getter} with a {@link PPrism} */
    public final <B, C, D> Fold<S, C> composePrism(final PPrism<A, B, C, D> other) {
        return asFold().composePrism(other);
    }

    /** compose a {@link Getter} with a {@link PLens} */
    public final <B, C, D> Getter<S, C> composeLens(final PLens<A, B, C, D> other) {
        return composeGetter(other.asGetter());
    }

    /** compose a {@link Getter} with a {@link PIso} */
    public final <B, C, D> Getter<S, C> composeIso(final PIso<A, B, C, D> other) {
        return composeGetter(other.asGetter());
    }

    /******************************************************************/
    /** Transformation methods to view a {@link Getter} as another Optics */
    /******************************************************************/

    /** view a {@link Getter} with a {@link Fold} */
    public final Fold<S, A> asFold() {
        return new Fold<S, A>() {
            @Override
            public <B> Function<S, B> foldMap(final Monoid<B> m, final Function<A, B> f) {
                return s -> f.apply(get(s));
            }
        };
    }

    public static <A> Getter<A, A> id() {
        return PIso.<A, A> pId().asGetter();
    }

    public static <A> Getter<Either<A, A>, A> codiagonal() {
        return getter(e -> e.fold(Function.identity(), Function.identity()));
    }

    public static <S, A> Getter<S, A> getter(final Function<S, A> get) {
        return new Getter<S, A>() {

            @Override
            public A get(final S s) {
                return get.apply(s);
            }
        };
    }
}