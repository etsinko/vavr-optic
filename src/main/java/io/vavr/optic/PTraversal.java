package io.vavr.optic;


import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.function.Function;

/**
 * A {@link PTraversal} can be seen as a {@link POptional} generalised to 0 to n targets where n can be infinite.
 * <p>
 * {@link PTraversal} stands for Polymorphic Traversal as it set and modify methods change a type {@code A} to {@code B} and {@code S} to {@code T}.
 * {@link Traversal} is a {@link PTraversal} restricted to monomoprhic updates.</p>
 *
 * @param <S> the source of a {@link PTraversal}
 * @param <T> the modified source of a {@link PTraversal}
 * @param <A> the target of a {@link PTraversal}
 * @param <B> the modified target of a {@link PTraversal}
 */
public abstract class PTraversal<S, T, A, B> {

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract <C> Function<S, Function<C, T>> modifyFunctionF(Function<A, Function<C, B>> f);

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract <L> Function<S, Either<L, T>> modifyEitherF(Function<A, Either<L, B>> f);

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract Function<S, List<T>> modifyListF(Function<A, List<B>> f);

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract Function<S, Option<T>> modifyOptionF(Function<A, Option<B>> f);

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract Function<S, Stream<T>> modifyStreamF(Function<A, Stream<B>> f);

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract Function<S, Tuple1<T>> modifyP1F(Function<A, Tuple1<B>> f);

    /**
     * modify polymorphically the target of a {@link PTraversal} with an Applicative function
     */
    public abstract <E> Function<S, Validation<E, T>> modifyValidationF(Semigroup<E> s, Function<A, Validation<E, B>> f);

    /**
     * map each target to a {@link Monoid} and combine the results
     */
    public abstract <M> Function<S, M> foldMap(Monoid<M> monoid, Function<A, M> f);

    /**
     * combine all targets using a target's {@link Monoid}
     */
    public final Function<S, A> fold(final Monoid<A> m) {
        return foldMap(m, Function.identity());
    }

    /**
     * get all the targets of a {@link PTraversal}
     */
    public final List<A> getAll(final S s) {
        return foldMap(Monoid.listMonoid(), List::of).apply(s);
    }

    /**
     * find the first target of a {@link PTraversal} matching the predicate
     */
    public final Function<S, Option<A>> find(final Function<A, Boolean> p) {
        return foldMap(Monoid.firstOptionMonoid(), a -> p.apply(a) ? Option.some(a) : Option.none());
    }

    /**
     * get the first target of a {@link PTraversal}
     */
    public final Option<A> headOption(final S s) {
        return find(Function1.constant(Boolean.TRUE)).apply(s);
    }

    /**
     * check if at least one target satisfies the predicate
     */
    public final Function<S, Boolean> exist(final Function<A, Boolean> p) {
        return foldMap(Monoid.disjunctionMonoid, p);
    }

    /**
     * check if all targets satisfy the predicate
     */
    public final Function<S, Boolean> all(final Function<A, Boolean> p) {
        return foldMap(Monoid.conjunctionMonoid, p);
    }

    /**
     * modify polymorphically the target of a {@link PTraversal} with a function
     */
    public final Function<S, T> modify(final Function<A, B> f) {
        return s -> this.modifyP1F(a -> Tuple.of(f.apply(a))).apply(s)._1();
    }

    /**
     * set polymorphically the target of a {@link PTraversal} with a value
     */
    public final Function<S, T> set(final B b) {
        return modify(Function1.constant(b));
    }

    /**
     * join two {@link PTraversal} with the same target
     */
    public final <S1, T1> PTraversal<Either<S, S1>, Either<T, T1>, A, B> sum(final PTraversal<S1, T1, A, B> other) {
        final PTraversal<S, T, A, B> self = this;
        return new PTraversal<Either<S, S1>, Either<T, T1>, A, B>() {

            @Override
            public <C> Function<Either<S, S1>, Function<C, Either<T, T1>>> modifyFunctionF(final Function<A, Function<C, B>> f) {
                return ss1 -> ss1.fold(
                        s -> Helpers.compose(Either::left, self.modifyFunctionF(f).apply(s)),
                        s1 -> Helpers.compose(Either::right, other.modifyFunctionF(f).apply(s1))
                );
            }

            @Override
            public <L> Function<Either<S, S1>, Either<L, Either<T, T1>>> modifyEitherF(final Function<A, Either<L, B>> f) {
                return ss1 -> ss1.fold(
                        s -> self.modifyEitherF(f).apply(s).map(Either::left),
                        s1 -> other.modifyEitherF(f).apply(s1).map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S1>, List<Either<T, T1>>> modifyListF(final Function<A, List<B>> f) {
                return ss1 -> ss1.fold(
                        s -> self.modifyListF(f).apply(s).map(Either::left),
                        s1 -> other.modifyListF(f).apply(s1).map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S1>, Option<Either<T, T1>>> modifyOptionF(final Function<A, Option<B>> f) {
                return ss1 -> ss1.fold(
                        s -> self.modifyOptionF(f).apply(s).map(Either::left),
                        s1 -> other.modifyOptionF(f).apply(s1).map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S1>, Stream<Either<T, T1>>> modifyStreamF(final Function<A, Stream<B>> f) {
                return ss1 -> ss1.fold(
                        s -> self.modifyStreamF(f).apply(s).map(Either::left),
                        s1 -> other.modifyStreamF(f).apply(s1).map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S1>, Tuple1<Either<T, T1>>> modifyP1F(final Function<A, Tuple1<B>> f) {
                return ss1 -> ss1.fold(
                        s -> self.modifyP1F(f).apply(s).map(Either::left),
                        s1 -> other.modifyP1F(f).apply(s1).map(Either::right)
                );
            }

            @Override
            public <E> Function<Either<S, S1>, Validation<E, Either<T, T1>>> modifyValidationF(Semigroup<E> se, final Function<A, Validation<E, B>> f) {
                return ss1 -> ss1.fold(
                        s -> self.modifyValidationF(se, f).apply(s).map(Either::left),
                        s1 -> other.modifyValidationF(se, f).apply(s1).map(Either::right)
                );
            }

            @Override
            public <M> Function<Either<S, S1>, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
                return ss1 -> ss1.fold(
                        self.foldMap(monoid, f),
                        other.foldMap(monoid, f)
                );
            }

        };
    }

    /****************************************************************/
    /** Compose methods between a {@link PTraversal} and another Optics */
    /****************************************************************/

    /**
     * compose a {@link PTraversal} with a {@link Fold}
     */
    public final <C> Fold<S, C> composeFold(final Fold<A, C> other) {
        return asFold().composeFold(other);
    }

    //

    /**
     * compose a {@link PTraversal} with a {@link Getter}
     */
    public final <C> Fold<S, C> composeFold(final Getter<A, C> other) {
        return asFold().composeGetter(other);
    }

    /**
     * compose a {@link PTraversal} with a {@link PSetter}
     */
    public final <C, D> PSetter<S, T, C, D> composeSetter(final PSetter<A, B, C, D> other) {
        return asSetter().composeSetter(other);
    }

    /**
     * compose a {@link PTraversal} with a {@link PTraversal}
     */
    public final <C, D> PTraversal<S, T, C, D> composeTraversal(final PTraversal<A, B, C, D> other) {
        final PTraversal<S, T, A, B> self = this;
        return new PTraversal<S, T, C, D>() {

            @Override
            public <G> Function<S, Function<G, T>> modifyFunctionF(final Function<C, Function<G, D>> f) {
                return self.modifyFunctionF(other.modifyFunctionF(f));
            }

            @Override
            public <L> Function<S, Either<L, T>> modifyEitherF(final Function<C, Either<L, D>> f) {
                return self.modifyEitherF(other.modifyEitherF(f));
            }

            @Override
            public Function<S, List<T>> modifyListF(final Function<C, List<D>> f) {
                return self.modifyListF(other.modifyListF(f));
            }

            @Override
            public Function<S, Option<T>> modifyOptionF(final Function<C, Option<D>> f) {
                return self.modifyOptionF(other.modifyOptionF(f));
            }

            @Override
            public Function<S, Stream<T>> modifyStreamF(final Function<C, Stream<D>> f) {
                return self.modifyStreamF(other.modifyStreamF(f));
            }

            @Override
            public Function<S, Tuple1<T>> modifyP1F(final Function<C, Tuple1<D>> f) {
                return self.modifyP1F(other.modifyP1F(f));
            }

            @Override
            public <E> Function<S, Validation<E, T>> modifyValidationF(Semigroup<E> s, final Function<C, Validation<E, D>> f) {
                return self.modifyValidationF(s, other.modifyValidationF(s, f));
            }

            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<C, M> f) {
                return self.foldMap(monoid, other.foldMap(monoid, f));
            }
        };
    }

    /**
     * compose a {@link PTraversal} with a {@link POptional}
     */
    public final <C, D> PTraversal<S, T, C, D> composeOptional(final POptional<A, B, C, D> other) {
        return composeTraversal(other.asTraversal());
    }

    /**
     * compose a {@link PTraversal} with a {@link PPrism}
     */
    public final <C, D> PTraversal<S, T, C, D> composePrism(final PPrism<A, B, C, D> other) {
        return composeTraversal(other.asTraversal());
    }

    /**
     * compose a {@link PTraversal} with a {@link PLens}
     */
    public final <C, D> PTraversal<S, T, C, D> composeLens(final PLens<A, B, C, D> other) {
        return composeTraversal(other.asTraversal());
    }

    /**
     * compose a {@link PTraversal} with a {@link PIso}
     */
    public final <C, D> PTraversal<S, T, C, D> composeIso(final PIso<A, B, C, D> other) {
        return composeTraversal(other.asTraversal());
    }

    /**********************************************************************/
    /** Transformation methods to view a {@link PTraversal} as another Optics */
    /**********************************************************************/

    /**
     * view a {@link PTraversal} as a {@link Fold}
     */
    public final Fold<S, A> asFold() {
        return new Fold<S, A>() {
            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
                return PTraversal.this.foldMap(monoid, f);
            }
        };
    }

    /**
     * view a {@link PTraversal} as a {@link PSetter}
     */
    public PSetter<S, T, A, B> asSetter() {
        return PSetter.pSetter(this::modify);
    }

    public static <S, T> PTraversal<S, T, S, T> pId() {
        return PIso.<S, T>pId().asTraversal();
    }

    public static <S, T> PTraversal<Either<S, S>, Either<T, T>, S, T> pCodiagonal() {
        return new PTraversal<Either<S, S>, Either<T, T>, S, T>() {

            @Override
            public <C> Function<Either<S, S>, Function<C, Either<T, T>>> modifyFunctionF(final Function<S, Function<C, T>> f) {
                return s -> s.bimap(f, f).fold(
                        f1 -> Helpers.compose(Either::left, f1),
                        f1 -> Helpers.compose(Either::right, f1)
                );
            }

            @Override
            public <L> Function<Either<S, S>, Either<L, Either<T, T>>> modifyEitherF(final Function<S, Either<L, T>> f) {
                return s -> s.bimap(f, f).fold(
                        e -> e.map(Either::left),
                        e -> e.map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S>, List<Either<T, T>>> modifyListF(final Function<S, List<T>> f) {
                return s -> s.bimap(f, f).fold(
                        l -> l.map(Either::left),
                        l -> l.map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S>, Option<Either<T, T>>> modifyOptionF(final Function<S, Option<T>> f) {
                return s -> s.bimap(f, f).fold(
                        o -> o.map(Either::left),
                        o -> o.map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S>, Stream<Either<T, T>>> modifyStreamF(final Function<S, Stream<T>> f) {
                return s -> s.bimap(f, f).fold(
                        stream -> stream.map(Either::left),
                        stream -> stream.map(Either::right)
                );
            }

            @Override
            public Function<Either<S, S>, Tuple1<Either<T, T>>> modifyP1F(final Function<S, Tuple1<T>> f) {
                return s -> s.bimap(f, f).fold(
                        p1 -> p1.map(Either::left),
                        p1 -> p1.map(Either::right)
                );
            }

            @Override
            public <E> Function<Either<S, S>, Validation<E, Either<T, T>>> modifyValidationF(Semigroup<E> se, final Function<S, Validation<E, T>> f) {
                return s -> s.bimap(f, f).fold(
                        v -> v.map(Either::left),
                        v -> v.map(Either::right)
                );
            }

            @Override
            public <M> Function<Either<S, S>, M> foldMap(final Monoid<M> monoid, final Function<S, M> f) {
                return s -> s.fold(f, f);
            }
        };
    }

    public static <S, T, A, B> PTraversal<S, T, A, B> pTraversal(final Function<S, A> get1, final Function<S, A> get2,
                                                                 final Function3<B, B, S, T> set) {
        return new PTraversal<S, T, A, B>() {

            @Override
            public <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f) {
                return s -> Helpers.apply(Helpers.compose(b1 -> b2 -> set.apply(b1, b2, s), f.apply(get1.apply(s))), f.apply(get2.apply(s)));
            }

            @Override
            public <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f) {
                return s -> Helpers.apply(f.apply(get2.apply(s)), f.apply(get1.apply(s))
                                                                   .<Function<B, T>>map(b1 -> b2 -> set.apply(b1, b2, s)));
            }

            @Override
            public Function<S, List<T>> modifyListF(final Function<A, List<B>> f) {
                return s -> Helpers.apply(f.apply(get2.apply(s)), f.apply(get1.apply(s))
                                                                   .<Function<B, T>>map(b1 -> b2 -> set.apply(b1, b2, s)));
            }

            @Override
            public Function<S, Option<T>> modifyOptionF(final Function<A, Option<B>> f) {
                return s -> Helpers.apply(f.apply(get2.apply(s)), f.apply(get1.apply(s))
                                                                   .<Function<B, T>>map(b1 -> b2 -> set.apply(b1, b2, s)));
            }

            @Override
            public Function<S, Stream<T>> modifyStreamF(final Function<A, Stream<B>> f) {
                return s -> Helpers.apply(f.apply(get2.apply(s)), f.apply(get1.apply(s))
                                                                   .<Function<B, T>>map(b1 -> b2 -> set.apply(b1, b2, s)));
            }

            @Override
            public Function<S, Tuple1<T>> modifyP1F(final Function<A, Tuple1<B>> f) {
                return s -> Helpers.apply(f.apply(get2.apply(s)), f.apply(get1.apply(s))
                                                                   .<Function<B, T>>map(b1 -> b2 -> set.apply(b1, b2, s)));
            }

            @Override
            public <E> Function<S, Validation<E, T>> modifyValidationF(Semigroup<E> se, final Function<A, Validation<E, B>> f) {
                return s -> Helpers.accumapply(se, f.apply(get2.apply(s)), f.apply(get1.apply(s))
                                                                            .<Function<B, T>>map(b1 -> b2 -> set.apply(b1, b2, s)));
            }

            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
                return s -> monoid.sum(f.apply(get1.apply(s)), f.apply(get2.apply(s)));
            }
        };
    }

    public static <S, T, A, B> PTraversal<S, T, A, B> pTraversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                                 final Function4<B, B, B, S, T> set) {
        return fromCurried(pTraversal(get1, get2, (b1, b2, s) -> b3 -> set.apply(b1, b2, b3, s)), get3);
    }

    public static <S, T, A, B> PTraversal<S, T, A, B> pTraversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                                 final Function<S, A> get4,
                                                                 final Function5<B, B, B, B, S, T> set) {
        return fromCurried(pTraversal(get1, get2, get3, (b1, b2, b3, s) -> b4 -> set.apply(b1, b2, b3, b4, s)), get4);
    }

    public static <S, T, A, B> PTraversal<S, T, A, B> pTraversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                                 final Function<S, A> get4, final Function<S, A> get5,
                                                                 final Function6<B, B, B, B, B, S, T> set) {
        return fromCurried(pTraversal(get1, get2, get3, get4, (b1, b2, b3, b4, s) -> b5 -> set.apply(b1, b2, b3, b4, b5, s)), get5);
    }

    public static <S, T, A, B> PTraversal<S, T, A, B> pTraversal(final Function<S, A> get1, final Function<S, A> get2, final Function<S, A> get3,
                                                                 final Function<S, A> get4, final Function<S, A> get5, final Function<S, A> get6,
                                                                 final Function7<B, B, B, B, B, B, S, T> set) {
        return fromCurried(
                pTraversal(get1, get2, get3, get4, get5, (b1, b2, b3, b4, b5, s) -> b6 -> set.apply(b1, b2, b3, b4, b5, b6, s)),
                get6);
    }

    private static <S, T, A, B> PTraversal<S, T, A, B> fromCurried(final PTraversal<S, Function<B, T>, A, B> curriedTraversal,
                                                                   final Function<S, A> lastGet) {
        return new PTraversal<S, T, A, B>() {

            @Override
            public <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f) {
                return s -> Helpers.apply(curriedTraversal.modifyFunctionF(f).apply(s), f.apply(lastGet.apply(s)));
            }

            @Override
            public <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f) {
                return s -> Helpers.apply(f.apply(lastGet.apply(s)), curriedTraversal.modifyEitherF(f).apply(s));
            }

            @Override
            public Function<S, List<T>> modifyListF(final Function<A, List<B>> f) {
                return s -> Helpers.apply(f.apply(lastGet.apply(s)), curriedTraversal.modifyListF(f).apply(s));
            }

            @Override
            public Function<S, Option<T>> modifyOptionF(final Function<A, Option<B>> f) {
                return s -> Helpers.apply(f.apply(lastGet.apply(s)), curriedTraversal.modifyOptionF(f).apply(s));
            }

            @Override
            public Function<S, Stream<T>> modifyStreamF(final Function<A, Stream<B>> f) {
                return s -> Helpers.apply(f.apply(lastGet.apply(s)), curriedTraversal.modifyStreamF(f).apply(s));
            }

            @Override
            public Function<S, Tuple1<T>> modifyP1F(final Function<A, Tuple1<B>> f) {
                return s -> Helpers.apply(f.apply(lastGet.apply(s)), curriedTraversal.modifyP1F(f).apply(s));
            }

            @Override
            public <E> Function<S, Validation<E, T>> modifyValidationF(Semigroup<E> se, final Function<A, Validation<E, B>> f) {
                return s -> Helpers.accumapply(se, f.apply(lastGet.apply(s)), curriedTraversal.modifyValidationF(se, f)
                                                                                              .apply(s));
            }

            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
                return s -> monoid.sum(curriedTraversal.foldMap(monoid, f).apply(s), f.apply(lastGet.apply(s)));
            }
        };
    }
}
