package findep.utils.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;

import findep.utils.SystemOutLogger;

public class MyCache {

	private SystemOutLogger SYSOUTLOGGER = SystemOutLogger.getInstance();

	private static MyCache CACHE = new MyCache();

	private Cache<String, String> stringCache = null;

	private boolean enableCache = true;

	private MyCache() {
		String _enableCache = System.getenv("enable_cache");
		if (_enableCache == null) {
			_enableCache = "true";
		}
		enableCache = Boolean.parseBoolean(_enableCache);

		if (!enableCache) {
			SYSOUTLOGGER.sysout(-1, "Cache is not used.");
		}

		init();
	}

	public static MyCache getInstance() {
		return CACHE;
	}

	private void init() {
		if (!enableCache)
			return;

		// TODO: this as env variable
		int cacheSize = 100;
		SYSOUTLOGGER.sysout(-1, "Cache is used. Cache size: "+cacheSize+"MB.");

		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("stringCache",
						CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
								ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(cacheSize, MemoryUnit.MB)))
				.build();
		cacheManager.init();

		stringCache = cacheManager.getCache("stringCache", String.class, String.class);

	}

	// TODO: statistics

	public boolean isEnabled() {
		return enableCache;
	}

	public String get(String key) {
		if (!enableCache)
			return null;

		return stringCache.get(key);
	}

	public void put(String key, String value) {
		if (!enableCache)
			return;

		stringCache.put(key, value);
	}

}
