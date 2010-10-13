package net.dougqh.jak.repl;

import java.io.IOException;

final class ResetCommand extends FixedIdCommand {
	static final String ID = "reset";
	static final ResetCommand INSTANCE = new ResetCommand();
	
	private ResetCommand() {
		super( ID );
	}

	@Override
	final void run(
		final JakRepl repl,
		final String command,
		final String[] args,
		final boolean isSolitary )
		throws IOException
	{
		checkNoArguments( args );
		
		repl.resetProgram();
	}
}
