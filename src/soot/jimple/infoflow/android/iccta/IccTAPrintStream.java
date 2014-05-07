package soot.jimple.infoflow.android.iccta;

import java.io.OutputStream;
import java.io.PrintStream;

public class IccTAPrintStream extends PrintStream 
{

	PrintStream os = null;
	
	public IccTAPrintStream(OutputStream out) 
	{
		super(out);
		
		this.os = (PrintStream) out;
	}

	@Override
	public void println(String x) 
	{
		if (x.startsWith("["))
		{
			os.println(x);
		}
		else
		{
			os.println("[IccTA] " + x);
		}
	}
}
