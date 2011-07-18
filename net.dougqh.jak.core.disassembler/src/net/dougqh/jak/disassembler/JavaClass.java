package net.dougqh.jak.disassembler;

import java.util.List;

public interface JavaClass {
	//public abstract List< JavaField > getFields();
	
	public abstract JavaMethod getClassInitializer();
	
	public abstract List< JavaMethod > getConstructors();
	
	public abstract List< JavaMethod > getMethods();
}