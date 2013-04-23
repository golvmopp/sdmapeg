package se.sdmapeg.serverworker.communication;

public interface WorkStealingRequestMessage extends ServerToWorkerMessage {
    @Override
    <T> T accept(Handler<T> handler);

    int getDesired();
}
