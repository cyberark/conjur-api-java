package net.conjur.api.utils;

public interface Callable<T> {
	public T call() throws Exception;
}
