package net.dougqh.jak.jvm.operations;

import java.lang.reflect.Type;

import net.dougqh.jak.jvm.JvmOperationProcessor;

public final class fstore_0 extends FixedStoreOperation {
	public static final String ID = "fstore_0";
	public static final byte CODE = FSTORE_0;
	
	public static final fstore_0 instance() {
		return new fstore_0();
	}
	
	private fstore_0() {}
	
	@Override
	public final String getId() {
		return ID;
	}
	
	@Override
	public final int getCode() {
		return CODE;
	}
	
	@Override
	public final int slot() {
		return 0;
	}
	
	@Override
	public final Type type() {
		return float.class;
	}
	
	@Override
	public final void process( final JvmOperationProcessor processor ) {
		processor.fstore_0();
	}
}