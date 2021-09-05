/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.dispatcher.runner;

import org.apache.flink.runtime.dispatcher.DefaultDispatcherBootstrap;
import org.apache.flink.runtime.dispatcher.Dispatcher;
import org.apache.flink.runtime.dispatcher.DispatcherBootstrap;
import org.apache.flink.runtime.dispatcher.DispatcherFactory;
import org.apache.flink.runtime.dispatcher.DispatcherId;
import org.apache.flink.runtime.dispatcher.PartialDispatcherServices;
import org.apache.flink.runtime.dispatcher.PartialDispatcherServicesWithJobGraphStore;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobmanager.JobGraphWriter;
import org.apache.flink.runtime.rpc.RpcService;
import org.apache.flink.util.FlinkRuntimeException;

import java.util.Collection;

/**
 * Factory for the {@link DefaultDispatcherGatewayService}.
 */
class DefaultDispatcherGatewayServiceFactory implements AbstractDispatcherLeaderProcess.DispatcherGatewayServiceFactory {

	private final DispatcherFactory dispatcherFactory;

	private final RpcService rpcService;

	private final PartialDispatcherServices partialDispatcherServices;

	DefaultDispatcherGatewayServiceFactory(DispatcherFactory dispatcherFactory, RpcService rpcService,
		PartialDispatcherServices partialDispatcherServices) {
		this.dispatcherFactory = dispatcherFactory;
		this.rpcService = rpcService;
		this.partialDispatcherServices = partialDispatcherServices;
	}

	@Override
	public AbstractDispatcherLeaderProcess.DispatcherGatewayService create(DispatcherId fencingToken, Collection<JobGraph> recoveredJobs,
		JobGraphWriter jobGraphWriter) {

		// TODO_MA 注释： Dispatcher 的一个默认引导程序
		// TODO_MA 注释： 待恢复执行的 job 的集合
		final DispatcherBootstrap bootstrap = new DefaultDispatcherBootstrap(recoveredJobs);

		final Dispatcher dispatcher;
		try {

			/*************************************************
			 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
			 *  注释： 创建 Dispatcher
			 *  dispatcherFactory = SessionDispatcherFactory
			 */
			dispatcher = dispatcherFactory.createDispatcher(rpcService, fencingToken, bootstrap,

				// TODO_MA 注释： PartialDispatcherServicesWithJobGraphStore
				PartialDispatcherServicesWithJobGraphStore.from(partialDispatcherServices, jobGraphWriter));

		} catch(Exception e) {
			throw new FlinkRuntimeException("Could not create the Dispatcher rpc endpoint.", e);
		}

		/*************************************************
		 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
		 *  注释： Dispatcher 也是一个 RpcEndpoint 启动起来了之后，给自己发送一个 Hello 消息证明启动
		 */
		dispatcher.start();


		// TODO_MA 注释： 返回一个返回值
		return DefaultDispatcherGatewayService.from(dispatcher);
	}
}
