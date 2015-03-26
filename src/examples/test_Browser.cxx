#include <iostream>
#include <dic.hxx>
using namespace std;

class ErrorHandler : public DimErrorHandler
{
	void errorHandler(int severity, int code, char *msg)
	{
		int index = 0;
		char **services;
		cout << severity << " " << msg << endl;
		services = DimClient::getServerServices();
		cout<< "from "<< DimClient::getServerName() << " services:" << endl;
		while(services[index])
		{
			cout << services[index] << endl;
			index++;
		}
	}
public:
	ErrorHandler() {DimClient::addErrorHandler(this);}
};

class StrService : public DimInfo
{

	void infoHandler()
	{
		int index = 0;
		char **services;
//		cout << "Dns Node = " << DimClient::getDnsNode() << endl;
		cout << "Received STRVAL : " << getString() << endl;
		services = DimClient::getServerServices();
		cout<< "from "<< DimClient::getServerName() << " services:" << endl;
		while(services[index])
		{
			cout << services[index] << endl;
			index++;
		}
	}
public :
	StrService() : DimInfo("TEST/STRVAL","not available") {};
};


void **AllServices;
int *AllServiceStates;
int NServices;
int NBytes;

class MyTimer: public DimTimer
{
  void timerHandler()
    {
      int i;
      int missing = 0;
      for(i = 0; i < NServices; i++)
      {
	if(AllServiceStates[i] == -2)
	  {
	    missing++;
	  }
      }
      dim_print_date_time();
      printf("Missing %d, NBytes = %d\n", missing, NBytes);
      if(missing)
	start(1);
    }
 public:
  MyTimer(): DimTimer(2){};
};

class TestGetService: public DimInfo
{
  void infoHandler()
  {
    int i, index, size, done = 1;
    char *dataptr;
    
    size = getSize();
    NBytes += size;
    dataptr = new char[size];
    memcpy(dataptr, getData(), size);
    for(i = 0; i < NServices; i++)
      {
	if(AllServices[i] == this)
	  {
	    index = i;
	    break;
	  }
      }
    AllServiceStates[index] = getInt();
    //    printf("Got %s, %d, %d\n", getName(), index, AllServiceStates[index]);
    for(i = 0; i < NServices; i++)
      {
	if(AllServiceStates[i] == -2)
	  {
	    done = 0;
	    break;
	  }
      }
    if(done)
      {
	dim_print_date_time();
	printf("All Services Received\n");
      }
  }
public :
  TestGetService(char *name): DimInfo(name, -1){};
};


int main(int argc, char **argv)
{
		
	ErrorHandler errHandler;
//	StrService servstr;
	char *server, *ptr, *ptr1;
	DimBrowser br;
	int type, n, index, i, ret;
	MyTimer *myTimer;
	char findStr[132];

	ret = 0;
	strcpy(findStr,"*");
	if(argc > 1)
	{
		if(!strcmp(argv[1],"-f"))
		{
		  if(argc > 2)
		    strcpy(findStr,argv[2]);
		  else 
		    ret = 1;
		}
		else
		  ret = 1;
		if(ret)
		{
			printf("Parameters: [-f <search string>]\n");
			exit(0);
		}
	}
//	DimClient::addErrorHandler(errHandler);
dim_print_date_time();
printf("Asking %s\n", findStr);
        n = br.getServices(findStr);
dim_print_date_time();
	cout << "found " << n << " services" << endl; 
	
	AllServices = (void **) new TestGetService*[n];
	AllServiceStates = new int[n];
	NServices = n;
	NBytes = 0;
	index = 0;
	for(i = 0; i < n; i++)
	  AllServiceStates[i] = 0;
	while((type = br.getNextService(ptr, ptr1))!= 0)
	{
	  
	  //		cout << "type = " << type << " - " << ptr << " " << ptr1 << endl;
	  AllServiceStates[index] = -2;
	  AllServices[index] = new TestGetService(ptr);
	  index++;
	  //	  if(index >= 1000)
	  //	    break;
	}
	myTimer = new MyTimer();
	dim_print_date_time();
	printf("Got Service Names\n");
	/*
	br.getServers();
	while(br.getNextServer(server, ptr1))
	{
		cout << server << " @ " << ptr1 << endl;
	}

	br.getServerClients("DIS_DNS");
	while(br.getNextServerClient(ptr, ptr1))
	{
		cout << ptr << " @ " << ptr1 << endl;
	}
	DimInfo servint("TEST/INTVAL",-1); 
	*/
	while(1)
	{
		sleep(10);
		/*
		cout << "Current INTVAL : " << servint.getInt() << endl;
		DimClient::sendCommand("TEST/CMND","UPDATE_STRVAL");
		*/
	}
	return 0;
}
