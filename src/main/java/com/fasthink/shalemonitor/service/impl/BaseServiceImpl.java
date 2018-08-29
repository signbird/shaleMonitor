package com.fasthink.shalemonitor.service.impl;

import com.fasthink.shalemonitor.cache.api.CacheManager;


public class BaseServiceImpl
{
	
	protected CacheManager cacheManager;
	
	public CacheManager getCacheManager()
	{
		return cacheManager;
	}

	public void setCacheManager(CacheManager cacheManager)
	{
		this.cacheManager = cacheManager;
	}

}
