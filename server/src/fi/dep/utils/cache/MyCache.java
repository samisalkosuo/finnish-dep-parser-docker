package fi.dep.utils.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCache {

	private Logger logger = LoggerFactory.getLogger(MyCache.class);

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
			logger.info("Cache is not used.");
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
		logger.info("Cache is used. Cache size: {} MB.", cacheSize);

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
		if (!enableCache || key == null)
			return null;

		String value = stringCache.get(key);
		logger.trace("Get from cache. Key {}. Value: {}", key, value);
		return value;
	}

	public void put(String key, String value) {
		if (!enableCache)
			return;

		if (key == null || value == null) {
			logger.debug("Trying to put null to cache. Key {}. Value: {}", key, value);
			return;
		}

		logger.trace("Put to cache. Key {}. Value: {}", key, value);
		stringCache.put(key, value);
	}

}
