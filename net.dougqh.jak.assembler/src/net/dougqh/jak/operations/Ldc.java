package net.dougqh.jak.operations;

import net.dougqh.jak.assembler.ConstantEntry;
import net.dougqh.jak.assembler.JavaCoreCodeWriter;
import net.dougqh.jak.types.Category1;

public final class Ldc extends Operation {
	public static final String ID = "ldc";
	public static final byte CODE = LDC;
	
	public static final Ldc prototype() {
		return new Ldc( 0 );
	}
	
	private final int constantPoolRef;
	
	public Ldc( final int constantPoolRef ) {
		this.constantPoolRef = constantPoolRef;
	}
	
	@Override
	public final String getId() {
		return ID;
	}
	
	@Override
	public final int getCode() {
		return CODE;
	}
	
	@Override
	public final Class< ? >[] getCodeOperandTypes() {
		return new Class< ? >[] { int.class };
	}
	
	@Override
	public final Class< ? >[] getStackOperandTypes() {
		return NO_ARGS;
	}
	
	@Override
	public final Class< ? >[] getStackResultTypes() {
		return new Class< ? >[] { Category1.class };
	}
	
	@Override
	public final void write( final JavaCoreCodeWriter writer ) {
		writer.ldc( ConstantEntry.category1( this.constantPoolRef ) );
	}
}