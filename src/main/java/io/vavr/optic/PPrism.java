package io.vavr.optic;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple1;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.function.Function;

/**
 * A {@link PPrism} can be seen as a pair of functions:<ul>
 * <li>{@code getOrModify: S => T \/ A}</li>
 * <li>{@code reverseGet : B => T}</li>
 * </ul>
 * <p>
 * A {@link PPrism} could also be defined as a weaker {@link PIso} where get can fail.</p>
 * <p>
 * Typically a {@link PPrism} or {@link Prism} encodes the relation between a Sum or CoProduct type (e.g. sealed trait) and one
 * of it is element.</p>
 * <p>
 * {@link PPrism} stands for Polymorphic Prism as it set and modify methods change a type {@code A} to {@code B} and {@code S} to {@code T}.
 * {@link Prism} is a {@link PPrism} where the type of target cannot be modified.
 * <p>
 * A {@link PPrism} is also a valid {@link Fold}, {@link POptional}, {@link PTraversal} and {@link PSetter}
 *
 * @param <S> the source of a {@link PPrism}
 * @param <T> the modified source of a {@link PPrism}
 * @param <A> the target of a {@link PPrism}
 * @param <B> the modified target of a {@link PPrism}
 */
public abstract class PPrism<S, T, A, B> {

    PPrism() {
        super();
    }

    /**
     * get the target of a {@link PPrism} or modify the source in case there is no target
     */

    public abstract Either<T, A> getOrModify(S s);

    /**
     * get the modified source of a {@link PPrism}
     */
    public abstract T reverseGet(B b);

    /**
     * get the target of a {@link PPrism} or nothing if there is no target
     */
    public abstract Option<A> getOption(final S s);

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final <C> Function<S, Function<C, T>> modifyFunctionF(final Function<A, Function<C, B>> f) {
        return s -> getOrModify(s).fold(
                Function1::constant,
                a -> f.apply(a).andThen(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final <L> Function<S, Either<L, T>> modifyEitherF(final Function<A, Either<L, B>> f) {
        return s -> getOrModify(s).fold(
                Either::right,
                t -> f.apply(t).map(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final Function<S, List<T>> modifyListF(final Function<A, List<B>> f) {
        return s -> getOrModify(s).fold(
                List::of,
                t -> f.apply(t).map(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final Function<S, Option<T>> modifyOptionF(final Function<A, Option<B>> f) {
        return s -> getOrModify(s).fold(
                Option::some,
                t -> f.apply(t).map(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final Function<S, Stream<T>> modifyStreamF(final Function<A, Stream<B>> f) {
        return s -> getOrModify(s).fold(
                Stream::of,
                t -> f.apply(t).map(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final Function<S, Tuple1<T>> modifyP1F(final Function<A, Tuple1<B>> f) {
        return s -> getOrModify(s).fold(
                Tuple::of,
                t -> f.apply(t).map(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with an Applicative function
     */
    public final <E> Function<S, Validation<E, T>> modifyValidationF(final Function<A, Validation<E, B>> f) {
        return s -> getOrModify(s).fold(
                Validation::<E, T>valid,
                t -> f.apply(t).map(this::reverseGet)
        );
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with a function
     */
    public final Function<S, T> modify(final Function<A, B> f) {
        return s -> getOrModify(s).fold(Function.identity(), a -> reverseGet(f.apply(a)));
    }

    /**
     * modify polymorphically the target of a {@link PPrism} with a function. return empty if the {@link PPrism} is not matching
     */
    public final Function<S, Option<T>> modifyOption(final Function<A, B> f) {
        return s -> getOption(s).map(a -> reverseGet(f.apply(a)));
    }

    /**
     * set polymorphically the target of a {@link PPrism} with a value
     */
    public final Function<S, T> set(final B b) {
        return modify(Function1.constant(b));
    }

    /**
     * set polymorphically the target of a {@link PPrism} with a value. return empty if the {@link PPrism} is not matching
     */
    public final Function<S, Option<T>> setOption(final B b) {
        return modifyOption(Function1.constant(b));
    }

    /**
     * check if a {@link PPrism} has a target
     */
    public final boolean isMatching(final S s) {
        return getOption(s).isDefined();
    }

    /**
     * create a {@link Getter} from the modified target to the modified source of a {@link PPrism}
     */
    public final Getter<B, T> re() {
        return Getter.getter(this::reverseGet);
    }

    /************************************************************/
    /** Compose methods between a {@link PPrism} and another Optics */
    /************************************************************/

    /**
     * compose a {@link PPrism} with a {@link Fold}
     */
    public final <C> Fold<S, C> composeFold(final Fold<A, C> other) {
        return asFold().composeFold(other);
    }

    /**
     * compose a {@link PPrism} with a {@link Getter}
     */
    public final <C> Fold<S, C> composeGetter(final Getter<A, C> other) {
        return asFold().composeGetter(other);
    }

    /**
     * compose a {@link PPrism} with a {@link PSetter}
     */
    public final <C, D> PSetter<S, T, C, D> composeSetter(final PSetter<A, B, C, D> other) {
        return asSetter().composeSetter(other);
    }

    /**
     * compose a {@link PPrism} with a {@link PTraversal}
     */
    public final <C, D> PTraversal<S, T, C, D> composeTraversal(final PTraversal<A, B, C, D> other) {
        return asTraversal().composeTraversal(other);
    }

    /**
     * compose a {@link PPrism} with a {@link POptional}
     */
    public final <C, D> POptional<S, T, C, D> composeOptional(final POptional<A, B, C, D> other) {
        return asOptional().composeOptional(other);
    }

    /**
     * compose a {@link PPrism} with a {@link PLens}
     */
    public final <C, D> POptional<S, T, C, D> composeLens(final PLens<A, B, C, D> other) {
        return asOptional().composeOptional(other.asOptional());
    }

    /**
     * compose a {@link PPrism} with a {@link PPrism}
     */
    public final <C, D> PPrism<S, T, C, D> composePrism(final PPrism<A, B, C, D> other) {
        return new PPrism<S, T, C, D>() {

            @Override
            public Either<T, C> getOrModify(final S s) {
                return PPrism.this.getOrModify(s)
                                  .flatMap(a -> other.getOrModify(a)
                                                  .bimap(b -> PPrism.this.set(b).apply(s), Function.identity()));
            }

            @Override
            public T reverseGet(final D d) {
                return PPrism.this.reverseGet(other.reverseGet(d));
            }

            @Override
            public Option<C> getOption(final S s) {
                return PPrism.this.getOption(s).flatMap(other::getOption);
            }
        };
    }

    /**
     * compose a {@link PPrism} with a {@link PIso}
     */
    public final <C, D> PPrism<S, T, C, D> composeIso(final PIso<A, B, C, D> other) {
        return composePrism(other.asPrism());
    }

    /******************************************************************/
    /** Transformation methods to view a {@link PPrism} as another Optics */
    /******************************************************************/

    /**
     * view a {@link PPrism} as a {@link Fold}
     */
    public final Fold<S, A> asFold() {
        return new Fold<S, A>() {
            @Override
            public <M> Function<S, M> foldMap(final Monoid<M> monoid, final Function<A, M> f) {
                return s -> getOption(s).map(f).getOrElse(monoid::zero);
            }
        };
    }

    /**
     * view a {@link PPrism} as a {@link Setter}
     */
    public PSetter<S, T, A, B> asSetter() {
        return new PSetter<S, T, A, B>() {
            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return PPrism.this.modify(f);
            }

            @Override
            public Function<S, T> set(final B b) {
                return PPrism.this.set(b);
            }
        };
    }

    /**
     * view a {@link PPrism} as a {@link PTraversal}
     */
    public PTraversal<S, T, A, B> asTraversal() {
        final PPrism<S, T, A, B> self = this;
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
                return s -> getOption(s).map(f).getOrElse(monoid::zero);
            }

        };
    }

    /**
     * view a {@link PPrism} as a {@link POptional}
     */
    public POptional<S, T, A, B> asOptional() {
        final PPrism<S, T, A, B> self = this;
        return new POptional<S, T, A, B>() {

            @Override
            public Either<T, A> getOrModify(final S s) {
                return self.getOrModify(s);
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
            public Function<S, T> set(final B b) {
                return self.set(b);
            }

            @Override
            public Option<A> getOption(final S s) {
                return self.getOption(s);
            }

            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return self.modify(f);
            }

        };
    }

    public static <S, T> PPrism<S, T, S, T> pId() {
        return PIso.<S, T>pId().asPrism();
    }

    /**
     * create a {@link PPrism} using the canonical functions: getOrModify and reverseGet
     */
    public static <S, T, A, B> PPrism<S, T, A, B> pPrism(final Function<S, Either<T, A>> getOrModify, final Function<B, T> reverseGet) {
        return new PPrism<S, T, A, B>() {

            @Override
            public Either<T, A> getOrModify(final S s) {
                return getOrModify.apply(s);
            }

            @Override
            public T reverseGet(final B b) {
                return reverseGet.apply(b);
            }

            @Override
            public Option<A> getOption(final S s) {
                return getOrModify.apply(s).toOption();
            }
        };
    }

}