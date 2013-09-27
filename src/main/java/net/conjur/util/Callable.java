package net.conjur.util;

public interface Callable<T> {
	public T call() throws Exception;
}
