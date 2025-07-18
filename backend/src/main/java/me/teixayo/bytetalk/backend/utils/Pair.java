package me.teixayo.bytetalk.backend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Pair<A, B>
{

    private A first;
    private B second;

    public boolean equals(Pair<A, B> other)
    {
        return other.getFirst().equals(first) && other.getSecond().equals(second);
    }

    public String toString()
    {
        return "Pair{first=" + first + ", second=" + second + '}';
    }

    public static <A,B> Pair<A,B> of(A first, B second) {
        return new Pair<>(first,second);
    }
}