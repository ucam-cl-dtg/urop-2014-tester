package privateinterfaces;

import java.util.List;

import testingharness.XMLTestSettings;
import exceptions.TestIDNotFoundException;

public class MongoTestsImpl implements MongoTestsInterface {

	@Override
	public void addNewTest(String tickId,
			List<XMLTestSettings> staticTestSettings) {
		// TODO Auto-generated method stub

	}

	@Override
	public void editExistingTest(String tickId) throws TestIDNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteTest(String tickId) throws TestIDNotFoundException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<XMLTestSettings> getTestSettings(String tickId)
			throws TestIDNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

}
