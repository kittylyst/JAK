package net.dougqh.jak.repl;

import java.lang.reflect.Type;

import net.dougqh.jak.jvm.JvmLocalsTracker;

public final class ReplLocals implements JvmLocalsTracker {
	private final JakRepl repl;
	private final JvmLocalsTracker locals;
	
	ReplLocals(
		final JakRepl repl,
		final JvmLocalsTracker locals )
	{
		this.repl = repl;
		this.locals = locals;
	}
	
	@Override
	public int declare( final Type type ) {
		return this.locals.declare( type );
	}
	
	@Override
	public final void undeclare( final int slot ) {
		this.locals.undeclare( slot );
	}
	
	@Override
	public final Type typeOf( final int slot, final Type expectedType ) {
		return this.locals.typeOf( slot, expectedType );
	}
	
	@Override
	public final void inc( final int slot, final int amount ) {
		this.locals.inc( slot, amount );
	}
	
	@Override
	public final void load( final int slot, final Type type ) {
		this.locals.load( slot, type );
	}
	
	@Override
	public final void store( final int slot, final Type type ) {
		this.locals.store( slot, type );
		
		this.repl.stateCodeWriter().storeLocal( slot, type );
	}
	
	@Override
	public final int maxLocals() {
		return this.locals.maxLocals();
	}
}
