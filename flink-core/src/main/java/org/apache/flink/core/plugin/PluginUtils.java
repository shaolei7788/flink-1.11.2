/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.core.plugin;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.FlinkRuntimeException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Utility functions for the plugin mechanism.
 */
public final class PluginUtils {

	private PluginUtils() {
		throw new AssertionError("Singleton class.");
	}

	public static PluginManager createPluginManagerFromRootFolder(Configuration configuration) {

		/*************************************************
		 * TODO_MA 马中华 https://blog.csdn.net/zhongqi2513
		 *  注释： 返回 DefaultPluginManager
		 */
		return createPluginManagerFromRootFolder(PluginConfig.fromConfiguration(configuration));
	}

	private static PluginManager createPluginManagerFromRootFolder(PluginConfig pluginConfig) {

		// TODO_MA 注释： 如果 plugins 文件夹存在
		if (pluginConfig.getPluginsPath().isPresent()) {
			try {

				// TODO_MA 注释：
				Collection<PluginDescriptor> pluginDescriptors =
					new DirectoryBasedPluginFinder(pluginConfig.getPluginsPath().get()).findPlugins();

				// TODO_MA 注释：
				return new DefaultPluginManager(pluginDescriptors, pluginConfig.getAlwaysParentFirstPatterns());
			} catch (IOException e) {
				throw new FlinkRuntimeException("Exception when trying to initialize plugin system.", e);
			}
		}

		// TODO_MA 注释： 如果 plugins 文件夹不存在
		else {
			return new DefaultPluginManager(Collections.emptyList(), pluginConfig.getAlwaysParentFirstPatterns());
		}
	}
}
