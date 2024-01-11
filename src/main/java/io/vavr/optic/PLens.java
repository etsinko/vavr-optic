package io.vavr.optic;

import io.vavr.Tuple1;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.function.Function;

/**
 * A {@link PLens} can be seen as a pair of functions:<ul>
 * <li>{@code get: S => A} i.e. from an {@code S}, we can extract an {@code A}</li>
 * <li>{@code set: (B, S) => T} i.e. if we replace an {@code A} by a {@code B} in an {@code S}, we obtain a {@code T}</li>
 * </ul>
 * <p>
 * A {@link PLens} could also be defined as a weaker {@link PIso} where set requires an additional parameter than reverseGet.</p>
 * <p>
 * {@link PLens} stands for Polymorphic Lens as it set and modify methods change a type {@code A} to {@code B} and {@code S} to {@code T}. {@link Lens}
 * is a {@link PLens} restricted to monomoprhic updates.</p>
 * <p>
 * A {@link PLens} is also a valid {@link Getter}, {@link Fold}, {@link POptional}, {@link PTraversal} and {@link PSetter}</p>
 * <p>
 * Typically a {@link PLens} or {@link Lens} can be defined between a Product (e.g. case class, tuple, HList) and one of it is
 * component.</p>
 *
 * @param <S> the source of a {@link PLens}
 * @param <T> the modified source of a {@link PLens}
 * @param <A> the target of a {@link PLens}
 * @param <B> the modified target of a {@link PLens}
 */
public abstract class PLens<S, T, A, B> {

    PLens() {
        super();
    }

    /**
     * get the target of a {@link PLens}
     */
    public abstract A get(S s);

    /**
     * set polymorphically the target of a {@link PLens} using a function
     */
    public abstract Function<S, T> set(B b);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract <C> Function<S, Function<C, T>> modifyFunctionF(Function<A, Function<C, B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract <L> Function<S, Either<L, T>> modifyEitherF(Function<A, Either<L, B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract Function<S, List<T>> modifyListF(Function<A, List<B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract Function<S, Option<T>> modifyOptionF(Function<A, Option<B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract Function<S, Stream<T>> modifyStreamF(Function<A, Stream<B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract Function<S, Tuple1<T>> modifyP1F(Function<A, Tuple1<B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} with an Applicative function
     */
    public abstract <E> Function<S, Validation<E, T>> modifyValidationF(Function<A, Validation<E, B>> f);

    /**
     * modify polymorphically the target of a {@link PLens} using a function
     */
    public abstract Function<S, T> modify(final Function<A, B> f);

    /**
     * join two {@link PLens} with the same target
     */
    public final <S1, T1> PLens<Either<S, S1>, Either<T, T1>, A, B> sum(final PLens<S1, T1, A, B> other) {
        return pLens(
                e -> e.fold(this::get, other::get),
                b -> e -> e.bimap(PLens.this.set(b), other.set(b)));
    }

    /***********************************************************/
    /** Compose methods between a {@link PLens} and another Optics */
    /***********************************************************/

    /**
     * compose a {@link PLens} with a {@link Fold}
     */
    public final <C> Fold<S, C> composeFold(final Fold<A, C> other) {
        return asFold().composeFold(other);
    }

    /**
     * compose a {@link PLens} with a {@link Getter}
     */
    public final <C> Getter<S, C> composeGetter(final Getter<A, C> other) {
        return asGetter().composeGetter(other);
    }

    /**
     * compose a {@link PLens} with a {@link PSetter}
     */
    public final <C, D> PSetter<S, T, C, D> composeSetter(final PSetter<A, B, C, D> other) {
        return asSetter().composeSetter(other);
    }

    /**
     * compose a {@link PLens} with a {@link PTraversal}
     */
    public final <C, D> PTraversal<S, T, C, D> composeTraversal(final PTraversal<A, B, C, D> other) {
        return asTraversal().composeTraversal(other);
    }

    /**
     * compose a {@link PLens} with an {@link POptional}
     */
    public final <C, D> POptional<S, T, C, D> composeOptional(final POptional<A, B, C, D> other) {
        return asOptional().composeOptional(other);
    }

    /**
     * compose a {@link PLens} with a {@link PPrism}
     */
    public final <C, D> POptional<S, T, C, D> composePrism(final PPrism<A, B, C, D> other) {
        return asOptional().composeOptional(other.asOptional());
    }

    /**
     * compose a {@link PLens} with a {@link PLens}
     */
    public final <C, D> PLens<S, T, C, D> composeLens(final PLens<A, B, C, D> other) {
        final PLens<S, T, A, B> self = this;
        return new PLens<S, T, C, D>() {
            @Override
            public C get(final S s) {
                return other.get(self.get(s));
            }

            @Override
            public Function<S, T> set(final D d) {
                return self.modify(other.set(d));
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
     * compose a {@link PLens} with an {@link PIso}
     */
    public final <C, D> PLens<S, T, C, D> composeIso(final PIso<A, B, C, D> other) {
        return composeLens(other.asLens());
    }

    /************************************************************************************************/
    /** Transformation methods to view a {@link PLens} as another Optics */
    /************************************************************************************************/

    /**
     * view a {@link PLens} as a {@link Fold}
     */
    public final Fold<S, A> asFold() {
        return new Fold<S, A>() {
            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> m, final Function<A, M> f) {
                return s -> f.apply(get(s));
            }
        };
    }

    /**
     * view a {@link PLens} as a {@link Getter}
     */
    public final Getter<S, A> asGetter() {
        return new Getter<S, A>() {
            @Override
            public A get(final S s) {
                return PLens.this.get(s);
            }
        };
    }

    /**
     * view a {@link PLens} as a {@link PSetter}
     */
    public PSetter<S, T, A, B> asSetter() {
        return new PSetter<S, T, A, B>() {
            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return PLens.this.modify(f);
            }

            @Override
            public Function<S, T> set(final B b) {
                return PLens.this.set(b);
            }
        };
    }

    /**
     * view a {@link PLens} as a {@link PTraversal}
     */
    public PTraversal<S, T, A, B> asTraversal() {
        final PLens<S, T, A, B> self = this;
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
                return s -> f.apply(get(s));
            }

        };
    }

    /**
     * view a {@link PLens} as an {@link POptional}
     */
    public POptional<S, T, A, B> asOptional() {
        final PLens<S, T, A, B> self = this;
        return new POptional<S, T, A, B>() {
            @Override
            public Either<T, A> getOrModify(final S s) {
                return Either.right(self.get(s));
            }

            @Override
            public Function<S, T> set(final B b) {
                return self.set(b);
            }

            @Override
            public Option<A> getOption(final S s) {
                return Option.some(self.get(s));
            }

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
            public <E> Function<S, Validation<E, T>> modifyValidationF(final Function<A, Validation<E, B>> f) {
                return self.modifyValidationF(f);
            }

            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return self.modify(f);
            }
        };
    }

    public static <S, T> PLens<S, T, S, T> pId() {
        return PIso.<S, T>pId().asLens();
    }

    /**
     * create a {@link PLens} using a pair of functions: one to get the target, one to set the target.
     */
    public static <S, T, A, B> PLens<S, T, A, B> pLens(final Function<S, A> get, final Function<B, Function<S, T>> set) {
        return new PLens<S, T, A, B>() {

            @Override
            public A get(final S s) {
                return get.apply(s);
            }

            @Override
            public Function<S, T> set(final B b) {
                return set.apply(b);
            }

            @Override
            public <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f) {
                return s -> f.apply(get.apply(s)).andThen(b -> set.apply(b).apply(s));
            }

            @Override
            public <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f) {
                return s -> f.apply(get.apply(s)).map(a -> set.apply(a).apply(s));
            }

            @Override
            public Function<S, List<T>> modifyListF(final Function<A, List<B>> f) {
                return s -> f.apply(get.apply(s)).map(a -> set.apply(a).apply(s));
            }

            @Override
            public Function<S, Option<T>> modifyOptionF(final Function<A, Option<B>> f) {
                return s -> f.apply(get.apply(s)).map(a -> set.apply(a).apply(s));
            }

            @Override
            public Function<S, Stream<T>> modifyStreamF(final Function<A, Stream<B>> f) {
                return s -> f.apply(get.apply(s)).map(a -> set.apply(a).apply(s));
            }

            @Override
            public Function<S, Tuple1<T>> modifyP1F(final Function<A, Tuple1<B>> f) {
                return s -> f.apply(get.apply(s)).map(a -> set.apply(a).apply(s));
            }

            @Override
            public <E> Function<S, Validation<E, T>> modifyValidationF(final Function<A, Validation<E, B>> f) {
                return s -> f.apply(get.apply(s)).map(a -> set.apply(a).apply(s));
            }

            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return s -> set.apply(f.apply(get.apply(s))).apply(s);
            }
        };
    }
}