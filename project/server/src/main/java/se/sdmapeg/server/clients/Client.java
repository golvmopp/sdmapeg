/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.sdmapeg.server.clients;

import java.net.InetAddress;
import java.util.Set;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverworker.TaskId;

/**
 *
 * @author niclas
 */
interface Client {

	void disconnect();

	Set<TaskId> getActiveTasks();

	InetAddress getAddress();

	void listen();

	void taskCompleted(TaskId taskId, Result<?> result);
}
