package com.ctrip.xpipe.redis.core.protocal.cmd.manual;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ctrip.xpipe.api.command.CommandFuture;
import com.ctrip.xpipe.api.command.CommandFutureListener;
import com.ctrip.xpipe.netty.commands.NettyClient;
import com.ctrip.xpipe.pool.FixedObjectPool;
import com.ctrip.xpipe.redis.core.protocal.Psync;
import com.ctrip.xpipe.redis.core.protocal.cmd.InMemoryPsync;
import com.ctrip.xpipe.redis.core.protocal.cmd.Replconf;
import com.ctrip.xpipe.redis.core.protocal.cmd.Replconf.ReplConfType;

/**
 * @author wenchao.meng
 *
 *         Oct 19, 2016
 */
public class ReplconfTest extends AbstractCommandTest {

	private String host = "127.0.0.1";
	private int port = 6379;

	@Before
	public void beforeReplconfTest() throws Exception {

	}

	@Test
	public void test() throws Exception {

		for (int i = 0; i < 100; i++) {

			FixedObjectPool<NettyClient> clientPool = null;

			try {
				clientPool = createClientPool(host, port);

				Replconf replconf = new Replconf(clientPool, ReplConfType.LISTENING_PORT, String.valueOf(1234));
				replconf.execute().addListener(new CommandFutureListener<Object>() {

					@Override
					public void operationComplete(CommandFuture<Object> commandFuture) throws Exception {
						logger.info("{}", commandFuture.get());
					}
				});

				Psync psync = new InMemoryPsync(clientPool, "?", -1L);
				try {
					psync.execute().get(100, TimeUnit.MILLISECONDS);
					Assert.fail();
				} catch (TimeoutException e) {
				}
			} finally {
				if (clientPool != null) {
					clientPool.getObject().channel().close();
				}
			}
		}
	}
}
