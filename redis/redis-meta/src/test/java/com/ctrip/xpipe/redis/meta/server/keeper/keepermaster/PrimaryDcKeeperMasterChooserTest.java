package com.ctrip.xpipe.redis.meta.server.keeper.keepermaster;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unidal.tuple.Pair;

import com.ctrip.xpipe.api.server.Server.SERVER_ROLE;
import com.ctrip.xpipe.netty.ByteBufUtils;
import com.ctrip.xpipe.redis.core.entity.RedisMeta;
import com.ctrip.xpipe.redis.core.protocal.MASTER_STATE;
import com.ctrip.xpipe.redis.core.protocal.pojo.KeeperRole;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wenchao.meng
 *
 * Nov 13, 2016
 */
public class PrimaryDcKeeperMasterChooserTest extends AbstractDcKeeperMasterChooserTest{
	
	private PrimaryDcKeeperMasterChooser primaryDcKeeperMasterChooser;
	private List<RedisMeta> redises;
	
	@Before
	public void befoePrimaryDcKeeperMasterChooserTest(){
		
		primaryDcKeeperMasterChooser = new PrimaryDcKeeperMasterChooser(clusterId, shardId, dcMetaCache, currentMetaManager, scheduled);
		
		redises = new LinkedList<>();
		int port1 = randomPort();
		redises.add(new RedisMeta().setIp("localhost").setPort(port1));
		
		redises.add(new RedisMeta().setIp("localhost").setPort(randomPort(Arrays.asList(port1))));
		when(dcMetaCache.getShardRedises(clusterId, shardId)).thenReturn(redises);
		when(dcMetaCache.isCurrentDcPrimary(clusterId, shardId)).thenReturn(true);
	}
	
	@Test
	public void testNoneMaster(){
		
		Assert.assertNull(primaryDcKeeperMasterChooser.chooseKeeperMaster());
		
	}

	@Test
	public void testOneMaster() throws Exception{

		KeeperRole role = new KeeperRole(SERVER_ROLE.MASTER, "localhost", randomPort(), MASTER_STATE.REDIS_REPL_CONNECT, 0L);
		RedisMeta chosen = redises.get(0);
		startServer(chosen.getPort(), ByteBufUtils.readToString(role.format()));
		

		when(currentMetaManager.getKeeperMaster(clusterId, shardId)).thenReturn(null);
		Assert.assertEquals(new Pair<String, Integer>(chosen.getIp(), chosen.getPort()), primaryDcKeeperMasterChooser.chooseKeeperMaster());
		for(RedisMeta redisMeta : redises){
			
			when(currentMetaManager.getKeeperMaster(clusterId, shardId)).thenReturn(new Pair<String, Integer>(redisMeta.getIp(), redisMeta.getPort()));
			Assert.assertEquals(new Pair<String, Integer>(chosen.getIp(), chosen.getPort()), primaryDcKeeperMasterChooser.chooseKeeperMaster());
		}

	}

	@Test
	public void testMultiMaster() throws Exception{

		KeeperRole role = new KeeperRole(SERVER_ROLE.MASTER, "localhost", randomPort(), MASTER_STATE.REDIS_REPL_CONNECT, 0L);
		
		for(RedisMeta redisMeta : redises){
			startServer(redisMeta.getPort(), ByteBufUtils.readToString(role.format()));
		}
		
		when(currentMetaManager.getKeeperMaster(clusterId, shardId)).thenReturn(null);
		Assert.assertEquals(new Pair<String, Integer>(redises.get(0).getIp(), redises.get(0).getPort()), primaryDcKeeperMasterChooser.chooseKeeperMaster());
		
		for(RedisMeta redisMeta : redises){
			
			Pair<String, Integer> currentMaster = new Pair<String, Integer>(redisMeta.getIp(), redisMeta.getPort());
			when(currentMetaManager.getKeeperMaster(clusterId, shardId)).thenReturn(currentMaster);
			Assert.assertEquals(currentMaster, primaryDcKeeperMasterChooser.chooseKeeperMaster());
		}

	}
}
