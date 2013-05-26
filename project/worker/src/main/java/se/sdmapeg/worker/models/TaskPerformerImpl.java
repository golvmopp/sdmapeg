package se.sdmapeg.worker.models;

import java.util.List;
import java.util.concurrent.ExecutionException;

import se.sdmapeg.common.tasks.FindNextIntTask;
import se.sdmapeg.common.tasks.FindNextPrimeTask;
import se.sdmapeg.common.tasks.IdleTask;
import se.sdmapeg.common.tasks.PrimeFactorsTask;
import se.sdmapeg.common.tasks.PythonTask;
import se.sdmapeg.common.tasks.Result;
import se.sdmapeg.common.tasks.SimpleFailure;
import se.sdmapeg.common.tasks.SimpleListResult;
import se.sdmapeg.common.tasks.SimpleResult;
import se.sdmapeg.common.tasks.TaskPerformer;
import se.sdmapeg.worker.taskperformers.FindNextInteger;
import se.sdmapeg.worker.taskperformers.FindNextPrime;
import se.sdmapeg.worker.taskperformers.IdleTaskPerformer;
import se.sdmapeg.worker.taskperformers.PrimeFactors;
import se.sdmapeg.worker.taskperformers.PythonTaskPerformer;

/**
 * An implementation of a task performer.
 */
public final class TaskPerformerImpl implements TaskPerformer {

    @Override
    public Result<String> performPythonTask(PythonTask pythonCode) {
	    try {
		    return SimpleResult.newSimpleResult(PythonTaskPerformer.execute(pythonCode.getPythonCode()));
	    } catch (ExecutionException ex) {
		    return SimpleFailure.newSimpleFailure(ex);
	    }
    }

	@Override
	public Result<Integer> performFindNextIntTask(FindNextIntTask nextIntTask) {
		try {
			return SimpleResult.newSimpleResult(Integer.valueOf(FindNextInteger.findNextInteger(nextIntTask.getStart())));
		} catch (ExecutionException ex) {
			return SimpleFailure.newSimpleFailure(ex);
		}
	}

	@Override
	public Result<List<Long>> findPrimeFactors(PrimeFactorsTask primeFactorsTask) {
		try {
			return SimpleListResult.newSimpleListResult(PrimeFactors.findPrimeFactors(primeFactorsTask.getNumber()));
		} catch (ExecutionException ex) {
			return SimpleFailure.newSimpleFailure(ex);
		}
	}

	@Override
	public Result<Long> performFindNextPrimeTask(
			FindNextPrimeTask findNextPrimeTask) {
		try {
			return SimpleResult.newSimpleResult(FindNextPrime.findNextPrime(findNextPrimeTask.getFirstPrime()));
		} catch (ExecutionException ex) {
			return SimpleFailure.newSimpleFailure(ex);
		}
	}

	@Override
	public Result<String> performIdleTask(IdleTask idleTask) {
		try {
			return SimpleResult.newSimpleResult(IdleTaskPerformer.idle());
		} catch (ExecutionException ex) {
			return SimpleFailure.newSimpleFailure(ex);
		}
	}
}