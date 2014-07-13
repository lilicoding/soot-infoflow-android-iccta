import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.android.iccta.db.DB;
import soot.jimple.infoflow.android.iccta.icc.ICCLink;
import soot.jimple.infoflow.android.iccta.icc.ICCLinksEpiccProvider;


public class ICCLinksEpiccProviderTest 
{
	@Test
	public void testFetchProviderLinks()
	{
		DB.setJdbcPath("res/jdbc.xml");
		
		String pkg = "lu.uni.serval.icc_query1";
		
		ICCLinksEpiccProvider provider = new ICCLinksEpiccProvider();
		
		List<ICCLink> links = provider.fetchProviderLinks(new String[] {pkg});
		
		for (ICCLink l : links)
		{
			System.out.println(l);
		}
	}
}
