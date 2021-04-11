package ateranimavis.launcher.util;

import java.util.Map;
import java.util.Objects;

public class Pair<A, B> implements Map.Entry<A, B>{

    protected final A a;
    protected final B b;

    protected Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public A getKey() {
        return a;
    }

    @Override
    public B getValue() {
        return b;
    }

    @Override
    public B setValue(B value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return getA().equals(pair.getA()) && getB().equals(pair.getB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getA(), getB());
    }

    @Override
    public String toString() {
        return "Pair{a=" + a + ", b=" + b + '}';
    }
}
