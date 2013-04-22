package se.sdmapeg.serverclient.communication;

import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.serverclient.ClientTaskId;

public final class ServerToClientMessageFactory {

	public static ServerToClientMessage newResultMessage(ClientTaskId clientTaskId, Result<?> result) {
		return new ResultMessageImpl(clientTaskId, result);
	}

	private static final class ResultMessageImpl implements ResultMessage {
		private final ClientTaskId id;
		private final Result<?> result;

		private ResultMessageImpl(ClientTaskId id, Result<?> result) {
			this.id = id;
			this.result = result;
		}

		@Override
		public <T> T accept(Handler<T> handler) {
			return handler.handle(this);
		}

		/**
		 * Returns the ClientTaskId of this ResultMessage.
		 * @return the ClientTaskId of this ResultMessage
		 */
		public ClientTaskId getId() {
			return id;
		}

		/**
		 * Returns the Result of this ResultMessage.
		 * @return the Result of this ResultMessage
		 */
		public Result<?> getResult() {
			return result;
		}
	}
}
