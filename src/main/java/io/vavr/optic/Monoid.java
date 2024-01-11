package io.vavr.optic;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.function.Supplier;

public interface Monoid<A> extends Semigroup<A> {
    A zero();

    static <T> Monoid<List<T>> listMonoid() {
        return seqMonoid(List::empty);
    }

    static <A, T extends Seq<A>> Monoid<T> seqMonoid(Supplier<T> empty) {
        return new Monoid<T>() {
            @Override
            public T zero() {
                return empty.get();
            }

            @Override
            public T sum(T a1, T a2) {
                return (T)a1.appendAll(a2);
            }
        };
    }

    static <T> Monoid<Option<T>> firstOptionMonoid() {
        return new Monoid<Option<T>>() {
            @Override
            public Option<T> zero() {
                return Option.none();
            }

            @Override
            public Option<T> sum(Option<T> a1, Option<T> a2) {
                return a1.orElse(a2);
            }
        };
    }

    Monoid<Boolean> disjunctionMonoid = new Monoid<Boolean>() {
        @Override
        public Boolean zero() {
            return false;
        }

        @Override
        public Boolean sum(Boolean a1, Boolean a2) {
            return a1 | a2;
        }
    };

    Monoid<Boolean> conjunctionMonoid = new Monoid<Boolean>() {
        @Override
        public Boolean zero() {
            return true;
        }

        @Override
        public Boolean sum(Boolean a1, Boolean a2) {
            return a1 & a2;
        }
    };

    Monoid<Integer> intMinMonoid = new Monoid<Integer>() {
        @Override
        public Integer zero() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Integer sum(Integer a1, Integer a2) {
            return Math.min(a1, a2);
        }
    };
}
