package io.vavr.optic;

import io.vavr.control.Either;

import java.util.function.Function;

/**
 * A {@link PSetter} is a generalisation of Functor map:<ul>
 * <li>{@code map: (A => B) => F[A] => F[B]}</li>
 * <li>{@code modify: (A => B) => S => T}</li>
 * </ul>
 * <p>
 * {@link PSetter} stands for Polymorphic Setter as it set and modify methods change a type {@code A} to {@code B} and {@code S} to {@code T}.</p>
 * <p>
 * {@link PTraversal}, {@link POptional}, {@link PPrism}, {@link PLens} and {@link PIso} are valid {@link PSetter}</p>
 *
 * @param <S> the source of a {@link PSetter}
 * @param <T> the modified source of a {@link PSetter}
 * @param <A> the target of a {@link PSetter}
 * @param <B> the modified target of a {@link PSetter}
 */
public abstract class PSetter<S, T, A, B> {

    PSetter() {
        super();
    }

    /**
     * modify polymorphically the target of a {@link PSetter} with a function
     */
    public abstract Function<S, T> modify(Function<A, B> f);

    /**
     * set polymorphically the target of a {@link PSetter} with a value
     */
    public abstract Function<S, T> set(final B b);

    /**
     * join two {@link PSetter} with the same target
     */
    public final <S1, T1> PSetter<Either<S, S1>, Either<T, T1>, A, B> sum(final PSetter<S1, T1, A, B> other) {
        return pSetter(f -> e -> e.bimap(modify(f), other.modify(f)));
    }

    /*************************************************************/
    /** Compose methods between a {@link PSetter} and another Optics */
    /*************************************************************/

    /**
     * compose a {@link PSetter} with a {@link PSetter}
     */
    public final <C, D> PSetter<S, T, C, D> composeSetter(final PSetter<A, B, C, D> other) {
        final PSetter<S, T, A, B> self = this;
        return new PSetter<S, T, C, D>() {

            @Override
            public Function<S, T> modify(final Function<C, D> f) {
                return self.modify(other.modify(f));
            }

            @Override
            public Function<S, T> set(final D d) {
                return self.modify(other.set(d));
            }
        };
    }

    /**
     * compose a {@link PSetter} with a {@link PTraversal}
     */
    public final <C, D> PSetter<S, T, C, D> composeTraversal(final PTraversal<A, B, C, D> other) {
        return composeSetter(other.asSetter());
    }

    /**
     * compose a {@link PSetter} with a {@link POptional}
     */
    public final <C, D> PSetter<S, T, C, D> composeOptional(final POptional<A, B, C, D> other) {
        return composeSetter(other.asSetter());
    }

    /**
     * compose a {@link PSetter} with a {@link PPrism}
     */
    public final <C, D> PSetter<S, T, C, D> composePrism(final PPrism<A, B, C, D> other) {
        return composeSetter(other.asSetter());
    }

    /**
     * compose a {@link PSetter} with a {@link PLens}
     */
    public final <C, D> PSetter<S, T, C, D> composeLens(final PLens<A, B, C, D> other) {
        return composeSetter(other.asSetter());
    }

    /**
     * compose a {@link PSetter} with a {@link PIso}
     */
    public final <C, D> PSetter<S, T, C, D> composeIso(final PIso<A, B, C, D> other) {
        return composeSetter(other.asSetter());
    }

    public static <S, T> PSetter<S, T, S, T> pId() {
        return PIso.<S, T>pId().asSetter();
    }

    public static <S, T> PSetter<Either<S, S>, Either<T, T>, S, T> pCodiagonal() {
        return pSetter(f -> e -> e.bimap(f, f));
    }

    public static <S, T, A, B> PSetter<S, T, A, B> pSetter(final Function<Function<A, B>, Function<S, T>> modify) {
        return new PSetter<S, T, A, B>() {
            @Override
            public Function<S, T> modify(final Function<A, B> f) {
                return modify.apply(f);
            }

            @Override
            public Function<S, T> set(final B b) {
                return modify.apply(__ -> b);
            }
        };
    }
}