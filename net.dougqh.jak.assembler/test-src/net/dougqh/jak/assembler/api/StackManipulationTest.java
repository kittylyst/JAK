package net.dougqh.jak.assembler.api;

import static net.dougqh.jak.assembler.JavaAssembler.*;
import static org.junit.Assert.*;
import net.dougqh.jak.assembler.JavaClassWriter;
import net.dougqh.jak.assembler.JavaWriter;

import org.junit.Test;

public final class StackManipulationTest {
	public final @Test void swap() {
		JavaClassWriter classWriter = new JavaWriter().define( 
			public_().final_().class_( "Swap" ).implements_( IntCalculation.class ) );
		
		classWriter.defineDefaultConstructor();
		
		classWriter.define(
			public_().method( int.class, "exec" ).arg( int.class, "arg0" ).arg( int.class, "arg1" ) ).
			
			iload( "arg0" ).
			iload( "arg1" ).
			swap().
			ireturn();
		
		IntCalculation swap = classWriter.< IntCalculation >newInstance();
		
		assertEquals( 20, swap.exec( 20, 10 ) );
		assertEquals( 10, swap.exec( 10, 20 ) );
	}
	
	public interface IntCalculation {
		public abstract int exec( final int arg0, final int arg1 );
	}
}
