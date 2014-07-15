package TestingHarness;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.AbstractModule;

public class TestModule extends AbstractModule 
{
	@Override
	protected void configure()
	{
		bind(Map.class).to(HashMap.class);
	}
}
