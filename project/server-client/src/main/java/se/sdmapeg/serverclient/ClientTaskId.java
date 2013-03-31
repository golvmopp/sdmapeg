package se.sdmapeg.serverclient;

import se.sdmapeg.common.Id;

/**
 * Representation for the Server's Task ID.
 */
public final class ClientTaskId implements Id {
    private final long id;

    private ClientTaskId(long id) {
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
	if (this == obj)
	    return true;
	if (!(obj instanceof ClientTaskId))
	    return false;
	ClientTaskId other = (ClientTaskId) obj;
	if (id != other.id)
	    return false;
	return true;
    }
    
    public static ClientTaskId getId(long id) {
	return new ClientTaskId(id);
    }


}
