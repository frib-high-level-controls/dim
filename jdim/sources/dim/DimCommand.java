package dim;

public class DimCommand extends MutableMemory implements DataDecoder, DimCommandHandler
{
    int service_id;

	public DimCommand(String theServiceName, String format)
	{
		service_id = Server.addCommand(theServiceName, format, this);
	}
	public void decodeData(Memory theData)
    {
		int size = theData.getDataSize();
		setSize(size);
		copyFromMemory(theData);
		commandHandler();
    }
    public void finalize()
    {
        removeCommand();
    }
	
    public void removeCommand()
    {
        Server.removeService(service_id);
    }
	public void commandHandler() {};
}

interface DimCommandHandler
{
	void commandHandler();
}