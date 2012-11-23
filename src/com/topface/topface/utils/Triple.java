package com.topface.topface.utils;

public class Triple<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @SuppressWarnings("unchecked")
	public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Triple)) return false;
        final Triple<F, S, T> other;
        try {
            other = (Triple<F, S, T>) o;
        } catch (ClassCastException e) {
            return false;
        }
        return first.equals(other.first) && second.equals(other.second) && third.equals(other.second);
    }

    public int hashCode() {
        int result = 17;
        result = 31 * result + first.hashCode();
        result = 31 * result + second.hashCode();
        result = 31 * result + third.hashCode();
        return result;
    }

    public static <A, B, C> Triple<A, B, C> create(A a, B b, C c) {
        return new Triple<A, B, C>(a, b, c);
    }
}
