package registers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import net.dougqh.jak.disassembler.JvmMethod;
import net.dougqh.jak.jvm.Category;
import net.dougqh.jak.jvm.JvmLocals;
import net.dougqh.jak.jvm.JvmLocalsHelper;
import net.dougqh.jak.jvm.JvmStack;
import net.dougqh.jak.jvm.JvmStackHelper;
import net.dougqh.jak.jvm.SimpleJvmLocalsTracker;
import net.dougqh.jak.jvm.SimpleJvmOperationProcessor;
import net.dougqh.jak.jvm.SimpleJvmStackTracker;
import net.dougqh.jak.jvm.operations.ConstantOperation;
import net.dougqh.jak.jvm.operations.JvmOperation;
import net.dougqh.jak.jvm.operations.iinc;
import static basicblocks.JvmOperationMatchers.*;

/**
 * A simple class that tracks how constants and results move the between
 * the stack and local variables.
 * @author dougqh
 */
public final class DataFlowAnalysis {
	public static void main(String[] args) throws IOException {
		JvmMethod method = JvmMethod.read(DataFlowAnalysis.class, "demo");
		
		method.process(new DataFlowProcessor());
	}
	
	public static final int demo() {
		int x = 0;
		x = x + 1;
		return x;
	}
	
	static final class DataFlowProcessor extends SimpleJvmOperationProcessor {
		public final JvmStackHelper<Record> stack = new JvmStackHelper<Record>();
		public final JvmLocalsHelper<Record> locals = new JvmLocalsHelper<Record>();
		
		private AtomicInteger nonceCounter = new AtomicInteger(0);
		private Record toStack = null;
		private Record[] fromStack = new Record[2];
		
		@Override
		public final JvmStack stack() {
			return new SimpleJvmStackTracker<Record>(this.stack) {
				@Override
				protected final void stack(final Type type, final Category category) {
					this.stack.push(DataFlowProcessor.this.toStack(), category);
				}
				
				@Override
				protected final void unstack(final Type type, final Category category) {
					DataFlowProcessor.this.fromStack(this.stack.pop(category));
				}
			};
		}
		
		@Override
		public final JvmLocals locals() {
			return new SimpleJvmLocalsTracker<Record>(this.locals) {
				@Override
				protected void load(final int slot, final Type type, final Category category) {
					//needs to produce a nonce if null for parameter handling
					Record record = this.locals.get(slot, category);
					System.out.printf("load %s%n", record);
					
					DataFlowProcessor.this.toStack(record);
				}
				
				@Override
				protected final void store(final int slot, final Type type, final Category category) {
					Record record = DataFlowProcessor.this.fromStack();
					System.out.printf("store %s%n", record);
					
					this.locals.set(slot, record, category);
					
					System.out.println();
				}
				
				@Override
				public final void inc(final int slot, final int amount) {
					Record record = this.locals.get(slot);
					Record newRecord = record.inc(amount);
					
					this.locals.set(slot, record.inc(amount));
					
					System.out.print("inc ");
					System.out.print(record);
					System.out.print("->");
					System.out.print(newRecord);
					System.out.println();
				}
			};
		}
		
		private final void toStack(final Record record) {
			System.out.printf("toStack %s%n", record);
			
			this.toStack = record;
		}
		
		private final Record toStack() {
			Record forStack = this.toStack;
			this.toStack = null;
			return forStack;
		}
		
		private final void fromStack(final Record record) {
			System.out.printf("fromStack %s%n", record);
			
			if ( this.fromStack[0] == null ) {
				this.fromStack[0] = record;
			} else {
				this.fromStack[1] = record;
			}
		}
		
		private final Record fromStack() {
			if ( this.fromStack[1] != null ) {
				Record result = this.fromStack[1];
				this.fromStack[1] = null;
				return result;
			} else {
				Record result = this.fromStack[0];
				this.fromStack[0] = null;
				return result;
			}
		}
		
		private final Record[] allFromStack() {
			Record[] result;
			if ( this.fromStack[1] != null ) {
				result = Arrays.copyOf(this.fromStack, 2);
			} else {
				result = Arrays.copyOf(this.fromStack, 1);
			}
			this.fromStack[0] = null;
			this.fromStack[1] = null;
			return result;
		}
		
		@Override
		public final boolean shouldProcess(
			final Integer pos,
			final Class<? extends JvmOperation> opClass )
		{
			if ( is(opClass, CONST) ) {
				// const is hydrated, so that the value can be captured
				return true;
			} else if ( is(opClass, LOAD) || is(opClass, STORE) || is(opClass, iinc.class ) ) {
				// load, stores, and iincs are handled via the locals tracker
				return false;
			} else {
				// everything else, create a nonce that represents the instruction being performed
				this.toStack(new ComputeRecord(opClass, this.allFromStack()));
				return false;
			}
		}
		
		@Override
		public final void process(final JvmOperation op) {
			if ( is(op, CONST) ) {
				ConstantOperation constOp = as(op, CONST);
				this.toStack(new ConstRecord(constOp.value()));
			}
		}
	}
	
	static abstract class Record {
		protected abstract Record inc(final int amount);
	}
	
	static final class ConstRecord extends Record {
		final Object value;
		
		ConstRecord(final Object value) {
			this.value = value;
		}
		
		@Override
		protected final Record inc(final int amount) {
			//Only valid for integers
			Integer value = (Integer)this.value;
			return new ConstRecord(value + amount);
		}
		
		@Override
		public final String toString() {
			return "<const " + this.value + ">";
		}
	}
	
	static final class ComputeRecord extends Record {
		final Class<? extends JvmOperation> opClass;
		final Record[] inputs;
		
		public ComputeRecord(
			final Class<? extends JvmOperation> opClass,
			final Record[] inputs)
		{
			this.opClass = opClass;
			this.inputs = inputs;
		}
		
		@Override
		protected final Record inc(final int amount) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public final String toString() {
			return Arrays.toString(this.inputs) + " " + this.opClass.getSimpleName();
		}
	}
}