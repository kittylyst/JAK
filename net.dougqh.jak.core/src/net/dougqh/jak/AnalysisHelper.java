package net.dougqh.jak;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnalysisHelper<T> {
	private static final Null NULL = new Null();
	
	private final T codeElement;

	private final ConcurrentHashMap<Class<?>, Object> resultCache = 
		new ConcurrentHashMap<Class<?>, Object>();
	
	public AnalysisHelper(final T codeElement) {
		this.codeElement = codeElement;
	}
	
	protected abstract Class<? super T>[] getSearchTypes();
	
	public final <V> V get(final Class<V> analysisClass) {
		Object cachedResult = this.resultCache.get(analysisClass);
		if ( cachedResult != null ) {
			return AnalysisHelper.<V>fromCacheValue(cachedResult);
		}
		
		V result = this.calculate(analysisClass);
		this.resultCache.put(analysisClass, toCacheValue(result));
		return result;
	}
	
	private final <V> V calculate(final Class<V> analysisClass) {
		NoSuchMethodException noSuchMethodException = null;
		
		for ( Class<? super T> searchClass: this.getSearchTypes() ) {
			try {
				return this.calculate(analysisClass, searchClass);
			} catch ( NoSuchMethodException e ) {
				if ( noSuchMethodException == null ) {
					noSuchMethodException = e;
				}
			}
		}
		
		throw new IllegalStateException(noSuchMethodException);
	}
	
	private final <V> V calculate(
		final Class<V> analysisClass,
		final Class<? super T> searchClass)
		throws NoSuchMethodException
	{
		try {
			return analysisClass.getConstructor(searchClass).newInstance(this.codeElement);
		} catch ( SecurityException e ) {
			throw new IllegalStateException(e);
		}  catch ( IllegalAccessException e ) {
			throw new IllegalStateException(e);
		} catch ( InvocationTargetException e ) {
			throw new IllegalStateException(e);
		}  catch ( InstantiationException e ) {
			throw new IllegalStateException(e);
		}
	}
	
	private static final <V> V fromCacheValue(final Object value) {
		if ( value == NULL ) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			V result = (V)value;
			return result;
		}
	}
	
	private static final Object toCacheValue(final Object value) {
		if ( value == null ) {
			return NULL;
		} else {
			return value;
		}
	}
	
	private static class Null {}
}
