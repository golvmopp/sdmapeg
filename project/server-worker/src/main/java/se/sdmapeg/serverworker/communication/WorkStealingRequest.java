package se.sdmapeg.serverworker.communication;

public class WorkStealingRequest implements ServerToWorkerMessage {
       
    public int desired;
    
    private WorkStealingRequest(int desired){
	this.desired = desired;
    }

    @Override
    public <T> T accept(Handler<T> handler) {
	return handler.handle(this);
    }

    public int getDesired() {
	return desired;
    }
    
    public static WorkStealingRequest newWorkerStealingRequest(int desired){
	return new WorkStealingRequest(desired);
    } 
}
