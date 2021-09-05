/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.heartbeat;

import org.apache.flink.runtime.clusterframework.types.ResourceID;
import org.apache.flink.runtime.concurrent.ScheduledExecutor;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * {@link HeartbeatManager} implementation which regularly requests a heartbeat response from
 * its monitored {@link HeartbeatTarget}. The heartbeat period is configurable.
 *
 * @param <I> Type of the incoming heartbeat payload
 * @param <O> Type of the outgoing heartbeat payload
 */
public class HeartbeatManagerSenderImpl<I, O> extends HeartbeatManagerImpl<I, O> implements Runnable {

	private final long heartbeatPeriod;

	HeartbeatManagerSenderImpl(long heartbeatPeriod, long heartbeatTimeout, ResourceID ownResourceID, HeartbeatListener<I, O> heartbeatListener,
		ScheduledExecutor mainThreadExecutor, Logger log) {

		// TODO_MA 注释： 调用重载构造
		this(heartbeatPeriod, heartbeatTimeout, ownResourceID, heartbeatListener, mainThreadExecutor, log, new HeartbeatMonitorImpl.Factory<>());
	}

	HeartbeatManagerSenderImpl(long heartbeatPeriod, long heartbeatTimeout, ResourceID ownResourceID, HeartbeatListener<I, O> heartbeatListener,
		ScheduledExecutor mainThreadExecutor, Logger log, HeartbeatMonitor.Factory<O> heartbeatMonitorFactory) {
		super(heartbeatTimeout, ownResourceID, heartbeatListener, mainThreadExecutor, log, heartbeatMonitorFactory);
		this.heartbeatPeriod = heartbeatPeriod;

		/*************************************************
		 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
		 *  注释： 调度当前的类实例的 run() 方法的执行
		 *  执行的就是当前类的 run() 方法
		 */
		mainThreadExecutor.schedule(this, 0L, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {

		/*************************************************
		 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
		 *  注释： 在 Flink 的心跳机制中，跟其他的 集群不一样：
		 *  1、ResourceManager 发送心跳给 从节点 Taskmanager
		 *  2、从节点接收到心跳之后，返回响应
		 */

		// TODO_MA 注释： 实现循环执行
		if(!stopped) {
			log.debug("Trigger heartbeat request.");
			for(HeartbeatMonitor<O> heartbeatMonitor : getHeartbeatTargets().values()) {

				// TODO_MA 注释： ResourceManager 给 目标发送（TaskManager 或者 JobManager） 心跳
				requestHeartbeat(heartbeatMonitor);
			}

			/*************************************************
			 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
			 *  注释： 实现循环发送心跳的效果
			 */
			getMainThreadExecutor().schedule(this, heartbeatPeriod, TimeUnit.MILLISECONDS);
		}
	}

	/*************************************************
	 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
	 *  注释： HeartbeatMonitor 如果有  从节点返回心跳响应，则会被加入到 HeartbeatMonitor
	 *  管理了所有的心跳目标对象
	 */
	private void requestHeartbeat(HeartbeatMonitor<O> heartbeatMonitor) {
		O payload = getHeartbeatListener().retrievePayload(heartbeatMonitor.getHeartbeatTargetId());
		final HeartbeatTarget<O> heartbeatTarget = heartbeatMonitor.getHeartbeatTarget();

		/*************************************************
		 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
		 *  注释： 发送心跳
		 *  其实就是 集群中启动的从节点
		 */
		heartbeatTarget.requestHeartbeat(getOwnResourceID(), payload);
	}
}
