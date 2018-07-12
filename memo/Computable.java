package io.lzz;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;


/**
 * 描述一个计算功能,输入类型是A,输出结果类型是V,输入A为对象时请重构equals和hashCode
 *
 * @author lzz
 *
 * @param <A>
 * @param <V>
 */
public abstract class Computable<A, V> {

	// A代表输入，Future<V>代表输出的结果
	private final Map<A, Future<V>> cache = new ConcurrentHashMap<A, Future<V>>();

	protected abstract V compute(A arg) throws Exception;

	protected V computeHandle(final A arg) throws Exception {
		Future<V> f = cache.get(arg);
		if (f == null) {
			Callable<V> eval = new Callable<V>() {
				@Override
				public V call() throws Exception {
					return compute(arg);
				}
			};
			FutureTask<V> ft = new FutureTask<V>(eval);
			f = ((ConcurrentHashMap<A, Future<V>>) cache).putIfAbsent(arg, ft); // 执行一个任务
			if (f == null) {
				f = ft;
				ft.run();
			}
		}
		try {
			return f.get();
		} catch (Exception e) {
			throw e;
		} finally {
			cache.remove(arg);
		}
	}
}
