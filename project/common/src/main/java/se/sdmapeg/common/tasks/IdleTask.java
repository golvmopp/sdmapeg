package se.sdmapeg.common.tasks;

public class IdleTask implements Task<String> {

   
    private static final long serialVersionUID = -6219107583928645360L;
    private String name;
    
    private IdleTask(String name){
	this.name = name;
    }
    
    @Override
    public Result<String> perform(TaskPerformer taskPerformer) {
	return taskPerformer.performIdleTask(this);
    }

    @Override
    public Class<String> resultType() {
	return String.class;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public String getTypeName() {
	return "Idle Task";
    }

    public static IdleTask newIdleTask(String name) {
	return new IdleTask(name);
    }

}
