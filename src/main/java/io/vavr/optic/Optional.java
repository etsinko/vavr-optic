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
 * {@link POptional} restricted to monomorphic update
 */
public final class Optional<S, A> extends POptional<S, S, A, A> {

    final POptional<S, S, A, A> pOptional;

    public Optional(final POptional<S, S, A, A> pOptional) {
        this.pOptional = pOptional;
    }

    @Override
    public Function<S, S> set(final A a) {
        return pOptional.set(a);
    }

    @Override
    public <E> Function<S, Validation<E, S>> modifyValidationF(final Function<A, Validation<E, A>> f) {
        return pOptional.modifyValidationF(f);
    }

    @Override
    public Function<S, Stream<S>> modifyStreamF(final Function<A, Stream<A>> f) {
        return pOptional.modifyStreamF(f);
    }

    @Override
    public Function<S, Tuple1<S>> modifyP1F(final Function<A, Tuple1<A>> f) {
        return pOptional.modifyP1F(f);
    }

    @Override
    public Function<S, Option<S>> modifyOptionF(final Function<A, Option<A>> f) {
        return pOptional.modifyOptionF(f);
    }

    @Override
    public Function<S, List<S>> modifyListF(final Function<A, List<A>> f) {
        return pOptional.modifyListF(f);
    }

    @Override
    public <C> Function<S, Function<C, S>> modifyFunctionF(final Function<A, Function<C, A>> f) {
        return pOptional.modifyFunctionF(f);
    }

    @Override
    public <L> Function<S, Either<L, S>> modifyEitherF(final Function<A, Either<L, A>> f) {
        return pOptional.modifyEitherF(f);
    }

    @Override
    public Function<S, S> modify(final Function<A, A> f) {
        return pOptional.modify(f);
    }

    @Override
    public Either<S, A> getOrModify(final S s) {
        return pOptional.getOrModify(s);
    }

    @Override
    public Option<A> getOption(final S s) {
        return pOptional.getOption(s);
    }

    /**
     * join two {@link Optional} with the same target
     */
    public <S1> Optional<Either<S, S1>, A> sum(final Optional<S1, A> other) {
        return new Optional<>(pOptional.sum(other.pOptional));
    }

    @Override
    public <C> Optional<Tuple2<S, C>, Tuple2<A, C>> first() {
        return new Optional<>(pOptional.first());
    }

    @Override
    public <C> Optional<Tuple2<C, S>, Tuple2<C, A>> second() {
        return new Optional<>(pOptional.second());
    }

    /**************************************************************/
    /** Compose methods between a {@link Optional} and another Optics */
    /**************************************************************/

    /**
     * compose a {@link Optional} with a {@link Setter}
     */
    public <C> Setter<S, C> composeSetter(final Setter<A, C> other) {
        return new Setter<>(pOptional.composeSetter(other.pSetter));
    }

    /**
     * compose a {@link Optional} with a {@link Traversal}
     */
    public <C> Traversal<S, C> composeTraversal(final Traversal<A, C> other) {
        return new Traversal<>(pOptional.composeTraversal(other.pTraversal));
    }

    /**
     * compose a {@link Optional} with a {@link Optional}
     */
    public <C> Optional<S, C> composeOptional(final Optional<A, C> other) {
        return new Optional<>(pOptional.composeOptional(other.pOptional));
    }

    /**
     * compose a {@link Optional} with a {@link Prism}
     */
    public <C> Optional<S, C> composePrism(final Prism<A, C> other) {
        return new Optional<>(pOptional.composePrism(other.pPrism));
    }

    /**
     * compose a {@link Optional} with a {@link Lens}
     */
    public <C> Optional<S, C> composeLens(final Lens<A, C> other) {
        return new Optional<>(pOptional.composeLens(other.pLens));
    }

    /**
     * compose a {@link Optional} with an {@link Iso}
     */
    public <C> Optional<S, C> composeIso(final Iso<A, C> other) {
        return new Optional<>(pOptional.composeIso(other.pIso));
    }

    /********************************************************************/
    /** Transformation methods to view a {@link Optional} as another Optics */
    /********************************************************************/

    /**
     * view a {@link Optional} as a {@link Setter}
     */
    @Override
    public Setter<S, A> asSetter() {
        return new Setter<>(pOptional.asSetter());
    }

    /**
     * view a {@link Optional} as a {@link Traversal}
     */
    @Override
    public Traversal<S, A> asTraversal() {
        return new Traversal<>(pOptional.asTraversal());
    }

    public static <S> Optional<S, S> id() {
        return new Optional<>(POptional.pId());
    }

    public static <S, A> Optional<S, A> ignored() {
        return optional(s -> Option.none(), a -> s -> s);
    }

    public static <S, A> Optional<S, A> optional(final Function<S, Option<A>> getOption, final Function<A, Function<S, S>> set) {
        return new Optional<>(new POptional<S, S, A, A>() {

            @Override
            public Either<S, A> getOrModify(final S s) {
                return getOption.apply(s).fold(() -> Either.left(s), Either::right);
            }

            @Override
            public Function<S, S> set(final A a) {
                return set.apply(a);
            }

            @Override
            public Option<A> getOption(final S s) {
                return getOption.apply(s);
            }

            @Override
            public <C> Function<S, Function<C, S>> modifyFunctionF(final Function<A, Function<C, A>> f) {
                return s -> getOption.apply(s).<Function<C, S>>fold(
                        () -> Function1.constant(s),
                        a -> f.apply(a).andThen(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public <L> Function<S, Either<L, S>> modifyEitherF(final Function<A, Either<L, A>> f) {
                return s -> getOption.apply(s).<Either<L, S>>fold(
                        () -> Either.right(s),
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, List<S>> modifyListF(final Function<A, List<A>> f) {
                return s -> getOption.apply(s).fold(
                        () -> List.of(s),
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, Option<S>> modifyOptionF(final Function<A, Option<A>> f) {
                return s -> getOption.apply(s).fold(
                        () -> Option.some(s),
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, Stream<S>> modifyStreamF(final Function<A, Stream<A>> f) {
                return s -> getOption.apply(s).fold(
                        () -> Stream.of(s),
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, Tuple1<S>> modifyP1F(final Function<A, Tuple1<A>> f) {
                return s -> getOption.apply(s).fold(
                        () -> Tuple.of(s),
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public <E> Function<S, Validation<E, S>> modifyValidationF(final Function<A, Validation<E, A>> f) {
                return s -> getOption.apply(s).<Validation<E, S>>fold(
                        () -> Validation.valid(s),
                        t -> f.apply(t).map(b -> set.apply(b).apply(s))
                );
            }

            @Override
            public Function<S, S> modify(final Function<A, A> f) {
                return s -> getOption.apply(s).fold(() -> s, a -> set.apply(f.apply(a)).apply(s));
            }

        });
    }

}