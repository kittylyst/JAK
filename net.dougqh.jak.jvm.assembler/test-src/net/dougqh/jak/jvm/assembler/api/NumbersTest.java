package net.dougqh.jak.jvm.assembler.api;

import static net.dougqh.jak.Jak.*;
import static org.junit.Assert.*;
import net.dougqh.jak.jvm.assembler.JvmClassWriter;
import net.dougqh.jak.jvm.assembler.JvmWriter;

import org.junit.Test;

public final class NumbersTest {
	public final @Test void ldc() throws Exception {
		for ( int value = Integer.MIN_VALUE; 
			value < Integer.MAX_VALUE;
			value += 1000 )
		{
			JvmClassWriter writer = new JvmWriter().define(
				public_().final_().class_( "Test" ).implements_( AnInteger.class ) );
			
			writer.define( public_().init() ).
				this_().
				invokespecial( Object.class, init() ).
				return_();
			
			writer.define( public_().final_().method( int.class, "get" ) ).
				ldc( value ).
				ireturn();
			
			Class< ? > aClass = writer.load();
			AnInteger anInteger = (AnInteger)aClass.newInstance();
			
			assertEquals( value, anInteger.get() );
		}
	}
	
	public final @Test void iconst() throws Exception {
		for ( int value = Integer.MIN_VALUE; 
			value < Integer.MAX_VALUE;
			value += 1000 )
		{
			JvmClassWriter writer = new JvmWriter().define(
				public_().final_().class_( "Test" ).implements_( AnInteger.class ) );
			
			writer.define( public_().init() ).
				this_().
				invokespecial( Object.class, init() ).
				return_();
			
			writer.define( public_().final_().method( int.class, "get" ) ).
				iconst( value ).
				ireturn();
			
			Class< ? > aClass = writer.load();
			AnInteger anInteger = (AnInteger)aClass.newInstance();
			
			assertEquals( value, anInteger.get() );
		}
	}
	
	public static interface AnInteger {
		public int get();
	}
}
