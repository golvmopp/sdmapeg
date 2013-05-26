package se.sdmapeg.common.tasks;

import java.util.List;

/**
 * Interface for the use of the visitor pattern with tasks.
 *
 */
public interface TaskPerformer {
    Result<String> performPythonTask(PythonTask pythonCode);
	Result<Integer> performFindNextIntTask(FindNextIntTask nextIntTask);
	Result<List<Long>> findPrimeFactors(PrimeFactorsTask primeFactorsTask);
	Result<Long> performFindNextPrimeTask(FindNextPrimeTask findNextPrimeTask);
	Result<String> performIdleTask(IdleTask idleTask);
}
