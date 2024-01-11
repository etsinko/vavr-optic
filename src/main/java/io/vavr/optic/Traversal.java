package io.vavr.optic;

import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.function.Function;

public final class Traversal<S, A> extends PTraversal<S, S, A, A> {

    final PTraversal<S, S, A, A> pTraversal;

    public Traversal(final PTraversal<S, S, A, A> pTraversal) {
        this.pTraversal = pTraversal;
    }

    @Override
    public <C> Function<S, Function<C, S>> modifyFunctionF(final Function<A, Function<C, A>> f) {
        return pTraversal.modifyFunctionF(f);
    }

    @Override
    public <L> Function<S, Either<L, S>> modifyEitherF(final Function<A, Either<L, A>> f) {
        return pTraversal.modifyEitherF(f);
    }

    @Override
    public Function<S, List<S>> modifyListF(final Function<A, List<A>> f) {
        return pTraversal.modifyListF(f);
    }

    @Override
    public Function<S, Option<S>> modifyOptionF(final Function<A, Option<A>> f) {
        return pTraversal.modifyOptionF(f);
    }

    @Override
    public Function<S, Stream<S>> modifyStreamF(final Function<A, Stream<A>> f) {
        return pTraversal.modifyStreamF(f);
    }

    @Override
    public Function<S, Tuple1<S>> modifyP1F(final Function<A, Tuple1<A>> f) {
        return pTraversal.modifyP1F(f);
    }

    @Override
    public <E> Function<S, Validation<E, S>> modifyValidationF(Semigroup<E> s, final Function<A, Validation<E, A>> f) {
        return pTraversal.modifyValidationF(s, f);
    }

    @Override
    public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
        return pTraversal.foldMap(monoid, f);
    }

    /** join two {@link Traversal} with the same target */
    public <S1> Traversal<Either<S, S1>, A> sum(final Traversal<S1, A> other) {
        return new Traversal<>(pTraversal.sum(other.pTraversal));
    }

    /***************************************************************/
    /** Compose methods between a {@link Traversal} and another Optics */
    /***************************************************************/

    /** compose a {@link Traversal} with a {@link Setter} */
    public <C> Setter<S, C> composeSetter(final Setter<A, C> other) {
        return new Setter<>(pTraversal.composeSetter(other.pSetter));
    }

    /** compose a {@link Traversal} with a {@link Traversal} */
    public <C> Traversal<S, C> composeTraversal(final Traversal<A, C> other) {
        return new Traversal<>(pTraversal.composeTraversal(other.pTraversal));
    }

    /*********************************************************************/
    /** Transformation methods to view a {@link Traversal} as another Optics */
    /*********************************************************************/

    /** view a {@link Traversal} as a {@link Setter} */
    @Override
    public Setter<S, A> asSetter() {
        return new Setter<>(pTraversal.asSetter());
    }

    public static <S> Traversal<S, S> id() {
        return new Traversal<>(PTraversal.pId());
    }

    public static <S> Traversal<Either<S, S>, S> codiagonal() {
        return new Traversal<>(PTraversal.pCodiagonal());
    }

    public static <S, A> Traversal<S, A> traversal(final Function<S, A> get1, final Function<S, A> get2, final Function3<A, A, S, S> set) {
        return new Traversal<>(PTraversal.pTraversal(get1, get2, set));
    }

    public static <S, A> Traversal<S, A> traversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                   final Function4<A, A, A, S, S> set) {
        return new Traversal<>(PTraversal.pTraversal(get1, get2, get3, set));
    }

    public static <S, A> Traversal<S, A> traversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                   final Function<S, A> get4, final Function5<A, A, A, A, S, S> set) {
        return new Traversal<>(PTraversal.pTraversal(get1, get2, get3, get4, set));
    }

    public static <S, A> Traversal<S, A> traversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                   final Function<S, A> get4, final Function<S, A> get5, final Function6<A, A, A, A, A, S, S> set) {
        return new Traversal<>(PTraversal.pTraversal(get1, get2, get3, get4, get5, set));
    }

    public static <S, A> Traversal<S, A> traversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                   final Function<S, A> get4, final Function<S, A> get5, final Function<S, A> get6, final Function7<A, A, A, A, A, A, S, S> set) {
        return new Traversal<>(PTraversal.pTraversal(get1, get2, get3, get4, get5, get6,
                set));
    }

}