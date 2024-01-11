package io.vavr.optic;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.function.Function;

/**
 * A {@link POptional} can be seen as a pair of functions:<ul>
 * <li>{@code getOrModify: S => T \/ A}</li>
 * <li>{@code set : (B, S) => T}</li>
 * </ul>
 * <p>
 * A {@link POptional} could also be defined as a weaker {@link PLens} and weaker {@link PPrism}</p>
 * <p>
 * {@link POptional} stands for Polymorphic Optional as it set and modify methods change a type {@code A} to {@code B} and {@code S} to {@code T}.
 * {@link Optional} is a {@link POptional} restricted to monomoprhic updates: {@code type Optional[S, A] = POptional[S, S, A, A]}</p>
 *
 * @param <S> the source of a {@link POptional}
 * @param <T> the modified source of a {@link POptional}
 * @param <A> the target of a {@link POptional}
 * @param <B> the modified target of a {@link POptional}
 */
public abstract class POptional<S, T, A, B> {

    POptional() {
        super();
    }

    /**
     * get the target of a {@link POptional} or modify the source in case there is no target
     */
    public abstract Either<T, A> getOrModify(S s);

    /**
     * get the modified source of a {@link POptional}
     */
    public abstract Function<S, T> set(final B b);

    /**
     * get the target of a {@link POptional} or nothing if there is no target
     */
    public abstract Option<A> getOption(final S s);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract Function<S, List<T>> modifyListF(Function<A, List<B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract Function<S, Option<T>> modifyOptionF(Function<A, Option<B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract Function<S, Stream<T>> modifyStreamF(Function<A, Stream<B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract Function<S, Tuple1<T>> modifyP1F(Function<A, Tuple1<B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with an Applicative function
     */
    public abstract <E> Function<S, Validation<E, T>> modifyValidationF(Function<A, Validation<E, B>> f);

    /**
     * modify polymorphically the target of a {@link POptional} with a function
     */
    public abstract Function<S, T> modify(final Function<A, B> f);

    /**
     * modify polymorphically the target of a {@link POptional} with a function. return empty if the {@link POptional} is not
     * matching
     */
    public final Function<S, Option<T>> modifyOption(final Function<A, B> f) {
        return s -> getOption(s).map(Function1.constant(modify(f).apply(s)));
    }

    /**
     * set polymorphically the target of a {@link POptional} with a value. return empty if the {@link POptional} is not matching
     */
    public final Function<S, Option<T>> setOption(final B b) {
        return modifyOption(Function1.constant(b));
    }

    /**
     * check if a {@link POptional} has a target
     */
    public final boolean isMatching(final S s) {
        return getOption(s).isDefined();

    }

    /**
     * join two {@link POptional} with the same target
     */
    public final <S1, T1> POptional<Either<S, S1>, Either<T, T1>, A, B> sum(final POptional<S1, T1, A, B> other) {
        return pOptional(
                e -> e.fold(s -> getOrModify(s).mapLeft(Either::<T, T1>left), s1 -> other.getOrModify(s1)
                                                                                         .mapLeft(Either::<T, T1>right)),
                b -> e -> e.bimap(set(b), other.set(b)));
    }

    public <C> POptional<Tuple2<S, C>, Tuple2<T, C>, Tuple2<A, C>, Tuple2<B, C>> first() {
        return pOptional(
                sc -> getOrModify(sc._1()).bimap(t -> Tuple.of(t, sc._2()), a -> Tuple.of(a, sc._2())),
                bc -> s_ -> Tuple.of(set(bc._1()).apply(s_._1()), bc._2()));
    }

    public <C> POptional<Tuple2<C, S>, Tuple2<C, T>, Tuple2<C, A>, Tuple2<C, B>> second() {
        return pOptional(
                cs -> getOrModify(cs._2()).bimap(t -> Tuple.of(cs._1(), t), a -> Tuple.of(cs._1(), a)),
                cb -> _s -> Tuple.of(cb._1(), set(cb._2()).apply(_s._2())));
    }

    /***************************************************************/
    /** Compose methods between a {@link POptional} and another Optics */
    /***************************************************************/

    /**
     * compose a {@link POptional} with a {@link Fold}
     */
    public final <C> Fold<S, C> composeFold(final Fold<A, C> other) {
        return asFold().composeFold(other);
    }

    /**
     * compose a {@link POptional} with a {@link Getter}
     */
    public final <C> Fold<S, C> composeGetter(final Getter<A, C> other) {
        return asFold().composeGetter(other);
    }

    /**
     * compose a {@link POptional} with a {@link PSetter}
     */
    public final <C, D> PSetter<S, T, C, D> composeSetter(final PSetter<A, B, C, D> other) {
        return asSetter().composeSetter(other);
    }

    /**
     * compose a {@link POptional} with a {@link PTraversal}
     */
    public final <C, D> PTraversal<S, T, C, D> composeTraversal(final PTraversal<A, B, C, D> other) {
        return asTraversal().composeTraversal(other);
    }

    /**
     * compose a {@link POptional} with a {@link POptional}
     */
    public final <C, D> POptional<S, T, C, D> composeOptional(final POptional<A, B, C, D> other) {
        final POptional<S, T, A, B> self = this;
        return new POptional<S, T, C, D>() {

            @Override
            public Either<T, C> getOrModify(final S s) {
                return self.getOrModify(s)
                           .flatMap(a -> other.getOrModify(a)
                                              .bimap(b -> POptional.this.set(b).apply(s), Function.identity()));
            }

            @Override
            public Function<S, T> set(final D d) {
                return self.modify(other.set(d));
            }

            @Override
            public Option<C> getOption(final S s) {
                return self.getOption(s).flatMap(other::getOption);
            }

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
            public <E> Function<S, Validation<E, T>> modifyValidationF(final Function<C, Validation<E, D>> f) {
                return self.modifyValidationF(other.modifyValidationF(f));
            }

            @Override
            public Function<S, T> modify(final Function<C, D> f) {
                return self.modify(other.modify(f));
            }
        };
    }

    /**
     * compose a {@link POptional} with a {@link PPrism}
     */
    public final <C, D> POptional<S, T, C, D> composePrism(final PPrism<A, B, C, D> other) {
        return composeOptional(other.asOptional());
    }

    /**
     * compose a {@link POptional} with a {@link PLens}
     */
    public final <C, D> POptional<S, T, C, D> composeLens(final PLens<A, B, C, D> other) {
        return composeOptional(other.asOptional());
    }

    /**
     * compose a {@link POptional} with a {@link PIso}
     */
    public final <C, D> POptional<S, T, C, D> composeIso(final PIso<A, B, C, D> other) {
        return composeOptional(other.asOptional());
    }

    /*********************************************************************/
    /** Transformation methods to view a {@link POptional} as another Optics */
    /*********************************************************************/

    /**
     * view a {@link POptional} as a {@link Fold}
     */
    public final Fold<S, A> asFold() {
        return new Fold<S, A>() {
            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> m, final Function<A, M> f) {
                return s -> POptional.this.getOption(s).map(f).getOrElse(m::zero);
            }
        };
    }

    /**
     * view a {@link POptional} as a {@link PSetter}
     */
    public PSetter<S, T, A, B> asSetter() {
        return new PSetter<S, T, A, B>() {
            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return POptional.this.modify(f);
            }

            @Override
            public Function<S, T> set(final B b) {
                return POptional.this.set(b);
            }
        };
    }

    /**
     * view a {@link POptional} as a {@link PTraversal}
     */
    public PTraversal<S, T, A, B> asTraversal() {
        final POptional<S, T, A, B> self = this;
        return new PTraversal<S, T, A, B>() {

            @Override
            public <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f) {
                return self.modifyFunctionF(f);
            }

            @Override
            public <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f) {
                return self.modifyEitherF(f);
            }

            @Override
            public Function<S, List<T>> modifyListF(final Function<A, List<B>> f) {
                return self.modifyListF(f);
            }

            @Override
            public Function<S, Option<T>> modifyOptionF(final Function<A, Option<B>> f) {
                return self.modifyOptionF(f);
            }

            @Override
            public Function<S, Stream<T>> modifyStreamF(final Function<A, Stream<B>> f) {
                return self.modifyStreamF(f);
            }

            @Override
            public Function<S, Tuple1<T>> modifyP1F(final Function<A, Tuple1<B>> f) {
                return self.modifyP1F(f);
            }

            @Override
            public <E> Function<S, Validation<E, T>> modifyValidationF(Semigroup<E> s, final Function<A, Validation<E, B>> f) {
                return self.modifyValidationF(f);
            }

            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
                return s -> self.getOption(s).map(f).getOrElse(monoid::zero);
            }
        };
    }

    public static <S, T> POptional<S, T, S, T> pId() {
        return PIso.<S, T>pId().asOptional();
    }

    /**
     * create a {@link POptional} using the canonical functions: getOrModify and set
     */
    public static <S, T, A, B> POptional<S, T, A, B> pOptional(final Function<S, Either<T, A>> getOrModify, final Function<B, Function<S, T>> set) {
        return new POptional<S, T, A, B>() {
            @Override
            public Either<T, A> getOrModify(final S s) {
                return getOrModify.apply(s);
            }

            @Override
            public Function<S, T> set(final B b) {
                return set.apply(b);
            }

            @Override
            public Option<A> getOption(final S s) {
                return getOrModify.apply(s).right().toOption();
            }

            @Override
            public <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f) {
                return s -> getOrModify.apply(s).fold(
                        Function1::constant,
                        a -> f.apply(a).andThen(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f) {
                return s -> getOrModify.apply(s).fold(
                        Either::right,
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, List<T>> modifyListF(final Function<A, List<B>> f) {
                return s -> getOrModify.apply(s).fold(
                        List::of,
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, Option<T>> modifyOptionF(final Function<A, Option<B>> f) {
                return s -> getOrModify.apply(s).fold(
                        Option::some,
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, Stream<T>> modifyStreamF(final Function<A, Stream<B>> f) {
                return s -> getOrModify.apply(s).fold(
                        Stream::of,
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, Tuple1<T>> modifyP1F(final Function<A, Tuple1<B>> f) {
                return s -> getOrModify.apply(s).fold(
                        Tuple::of,
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public <E> Function<S, Validation<E, T>> modifyValidationF(final Function<A, Validation<E, B>> f) {
                return s -> getOrModify.apply(s).fold(
                        Validation::<E, T>valid,
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return s -> getOrModify.apply(s).fold(Function.identity(), a -> set.apply(f.apply(a)).apply(s));
            }
        };
    }

}