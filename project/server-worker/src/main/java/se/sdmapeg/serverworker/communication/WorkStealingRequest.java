package se.sdmapeg.serverworker.communication;

public interface WorkStealingRequest extends ServerToWorkerMessage {
    @Override
    <T> T accept(Handler<T> handler);

    int getDesired();
}
