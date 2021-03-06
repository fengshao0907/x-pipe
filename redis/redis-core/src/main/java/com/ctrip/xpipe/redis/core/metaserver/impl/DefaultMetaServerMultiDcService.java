package com.ctrip.xpipe.redis.core.metaserver.impl;

import java.util.ArrayList;
import java.util.List;

import com.ctrip.xpipe.redis.core.metaserver.MetaServerConsoleService;
import com.ctrip.xpipe.redis.core.metaserver.MetaServerMultiDcService;

/**
 * @author wenchao.meng
 *
 * Nov 3, 2016
 */
public class DefaultMetaServerMultiDcService extends AbstractMetaService implements MetaServerMultiDcService{

	private String  upstreamchangePath;
	private String  metaServerAddress;
	
	public DefaultMetaServerMultiDcService(String metaServerAddress) {
		this(metaServerAddress, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL_MILLI);
	}
	
	public DefaultMetaServerMultiDcService(String metaServerAddress, int retryTimes, int retryIntervalMilli) {
		super(retryTimes, retryIntervalMilli);
		this.metaServerAddress = metaServerAddress;
		upstreamchangePath = String.format("%s/%s/%s", metaServerAddress, MetaServerConsoleService.PATH_PREFIX, PATH_UPSTREAM_CHANGE);
	}

	@Override
	public void upstreamChange(String clusterId, String shardId, String ip, int port) {
		
		restTemplate.put(upstreamchangePath, null, clusterId, shardId, ip, port);
	}


	@Override
	protected List<String> getMetaServerList() {
		
		List<String> result = new ArrayList<>();
		result.add(metaServerAddress);
		return result;
	}
	
	@Override
	public String toString() {
		
		return String.format("%s[%s]", getClass().getSimpleName(), metaServerAddress);
	}
	
}
