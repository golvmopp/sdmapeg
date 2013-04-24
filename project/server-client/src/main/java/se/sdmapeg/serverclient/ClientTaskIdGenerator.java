package se.sdmapeg.serverclient;

import se.sdmapeg.common.Id;
import se.sdmapeg.common.IdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of IdGenerator for ClientTaskIds.
 */
public final class ClientTaskIdGenerator implements IdGenerator<ClientTaskId> {
	private AtomicLong idCount = new AtomicLong(0L);

	@Override
	public ClientTaskId newId() {
		return ClientTaskIdImpl.getId(idCount.getAndIncrement());
	}

	private static final class ClientTaskIdImpl implements ClientTaskId {
		private final long id;

		private ClientTaskIdImpl(long id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ClientTaskIdImpl)) {
				return false;
			}
			ClientTaskIdImpl other = (ClientTaskIdImpl) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "ClientTaskId{" + id + '}';
		}

		/**
		 * Returns an ID matching the specified underlying long id.
		 */
		public static ClientTaskId getId(long id) {
			return new ClientTaskIdImpl(id);
		}
	}
}
