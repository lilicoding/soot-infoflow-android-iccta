package soot.jimple.infoflow.android.iccta.intentandfilter;

import org.junit.Assert;
import org.junit.Test;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * Test Results:
 * 
 * 1) For MimeType,
 	1.1) Intent, can specify its type as 'str' or '*' or 'str1/str2' or '* /str2' or 'str1/ *' or '* / *'
 * 	1.2) IntentFilter, the type must specify like 'type/subtype', the '/' is mandatory.
 * 	1.3) When Intent's type start with '*' but its subtype is not '*', then the first '*' only means a str
 * 
 * 
 * @author li.li
 *
 */

public class IntentMatchFilterTest 
{
	public Intent getIntent()
	{
		Intent i = new Intent();
		i.setAction("ACTION");
		i.addCategory("CATEGORY");
		
		return i;
	}
	
	public IntentFilter getIntentFilter()
	{
		IntentFilter f = new IntentFilter();
		f.addAction("ACTION");
		f.addCategory("CATEGORY");
		return f;
	}
	
	@Test
	public void testMimeType1()
	{
		try
		{
			Intent i = getIntent();
			i.setType("iccta");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("iccta/*");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v > 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testMimeType2()
	{
		try
		{
			Intent i = getIntent();
			i.setType("iccta/123");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("iccta/*");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v > 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testMimeType3()
	{
		try
		{
			Intent i = getIntent();
			i.setType("iccta/*");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("iccta/123");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v > 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testMimeType4()
	{
		try
		{
			Intent i = getIntent();
			i.setType("iccta/123");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("iccta/123");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v > 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testMimeType5()
	{
		try
		{
			Intent i = getIntent();
			i.setType("123/*");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("123/iccta");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v > 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
	
	
	//When Intent's type start with '*' but its subtype is not '*', then the first '*' only means a str
	@Test
	public void testMimeType6()
	{
		try
		{
			Intent i = getIntent();
			i.setType("*/123");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("1234/123");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v < 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testMimeType7()
	{
		try
		{
			Intent i = getIntent();
			i.setType("*");
			
			IntentFilter f = getIntentFilter();
			f.addDataType("abc/123");
			
			int v = f.match(i.getAction(), i.getType(), i.getScheme(), i.getData(), i.getCategories(), "IccTA");
			
			Assert.assertTrue(v < 0);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.fail();
		}
	}
}
