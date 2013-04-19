package se.sdmapeg.serverworker.communication;

import java.util.Set;

import se.sdmapeg.serverworker.TaskId;

public class WorkStealingResponse implements WorkerToServerMessage {
    
    private final Set<TaskId> stolenIds;
    
    private WorkStealingResponse(Set<TaskId> stolenIds){
	this.stolenIds = stolenIds;
    }

    @Override
    public <T> T accept(Handler<T> handler) {
	return handler.handle(this);
    }
    
    public WorkStealingResponse newWorkStealingResponse(Set<TaskId> stolenIds){
	return new WorkStealingResponse(stolenIds);	
    }

}
