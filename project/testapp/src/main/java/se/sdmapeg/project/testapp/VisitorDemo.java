package se.sdmapeg.project.testapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VisitorDemo {

    public static void main(String[] args) {
	Apa apa = new Apa();
	Bepa bepa = new Bepa();
	Cepa cepa = new Cepa();
	Depa depa = new Depa();
	List<Visitable> list = new ArrayList<>();
	list.add(apa);
	list.add(bepa);
	list.add(cepa);
	list.add(depa);
	Visitor<String> visitor = new HandlingVisitor();
	Visitor<Integer> anotherVisitor = new AlphabeticCalculatorVisitor();
	
	for (Visitable visitable : list) {
	    System.out.println(visitable.accept(visitor));
	}
	for (Visitable visitable : list) {
	    System.out.println(visitable.accept(anotherVisitor) + 2);
	}
    }
    

    private static class Apa implements Visitable {

	@Override
	public <T> T accept(Visitor<T> visitor) {
	   return visitor.visitApa(this);
	}
    }

    private static class Bepa implements Visitable {
	@Override
	public <T> T accept(Visitor<T> visitor) {
	    return visitor.visitBepa(this);
	}
    }

    private static class Cepa implements Visitable {
	@Override
	public <T> T accept(Visitor<T> visitor) {
	    return visitor.visitCepa(this);
	}
    }

    private static class Depa implements Visitable {
	@Override
	public <T> T accept(Visitor<T> visitor) {
	    return visitor.visitDepa(this);
	}
    }

    private interface Visitable {
	<T> T accept(Visitor<T> visitor);
    }
    
    private interface Visitor<T> {
	public T visitApa(Apa apa);

	public T visitBepa(Bepa bepa);
	
	public T visitCepa(Cepa cepa);
	
	public T visitDepa(Depa depa);	
    }
    
    private static class HandlingVisitor implements Visitor<String>{

	@Override
	public String visitApa(Apa apa) {
	   return "Apa!";
	}

	@Override
	public String visitBepa(Bepa bepa) {
	   return "Bepa!";
	}

	@Override
	public String visitCepa(Cepa cepa) {
	   return "Cepa!";
	}
	
	@Override
	public String visitDepa(Depa depa){
	    return "Depa!";
	}
    }
    
    private static class AlphabeticCalculatorVisitor implements Visitor<Integer>{

	@Override
	public Integer visitApa(Apa apa) {
	    return Integer.valueOf(1);
	}

	@Override
	public Integer visitBepa(Bepa bepa) {
	    return Integer.valueOf(2);
	}

	@Override
	public Integer visitCepa(Cepa cepa) {
	    return Integer.valueOf(3);
	}

	@Override
	public Integer visitDepa(Depa depa) {
	    return Integer.valueOf(4);
	}

    }
}
