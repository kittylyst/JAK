package net.dougqh.jak.jvm.operations;

import java.lang.reflect.Type;

import net.dougqh.jak.jvm.JvmOperationProcessor;

public final class i2c extends CastOperation {
	public static final String ID = "i2c";
	public static final byte CODE = I2C;
	
	public static final i2c instance() {
		return new i2c();
	}
	
	private i2c() {}
	
	@Override
	public final String getId() {
		return ID;
	}
	
	@Override
	public final int getCode() {
		return CODE;
	}
	
	@Override
	public final Type fromType() {
		return int.class;
	}
	
	@Override
	public final Type toType() {
		return char.class;
	}
	
	@Override
	public final void process( final JvmOperationProcessor processor ) {
		processor.i2c();
	}
}