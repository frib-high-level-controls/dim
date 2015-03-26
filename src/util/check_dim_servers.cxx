#include <iostream>
#include <dic.hxx>
using namespace std;

#define MAX_SERVERS 5000
char server_names[MAX_SERVERS][256];
char server_nodes[MAX_SERVERS][256];
int server_versions[MAX_SERVERS];
int N_servers = 0;

class DimVersion : public DimInfo
{
  int myIndex;
	void infoHandler()
	{
      	  server_versions[myIndex] = getInt();
	  //	  cout << server_names[myIndex] << " version " << server_versions[myIndex] << endl; 
	}
public :
	DimVersion(char *service, int index) : 
	  DimInfo(service,-1), myIndex(index) {};
};

int main()
{
  //	int version = 0;
	int n;
	char *server, *node;
	/*
	DimCurrentInfo dns("DIS_DNS/VERSION_NUMBER",10,-1);
	
	version = dns.getInt();
	if(version == -1)
		cout << "DNS not running" << endl;
	else
		cout << "DNS running" << endl;
	return(0);
	*/
	DimBrowser br;
	DimVersion *srvptr;
	char serviceName[256];
	int index;
	int i;

	for(i = 0; i < MAX_SERVERS; i++)
	{
	  server_names[i][0] = '\0';
	  server_versions[i] = 0;
	}  

	n = br.getServers();
	index = 0;
	while(br.getNextServer(server, node))
	{
	  strcpy(server_names[index],server);
	  strcpy(server_nodes[index],node);
	  strcpy(serviceName,server);
	  strcat(serviceName,"/VERSION_NUMBER");
	  srvptr = new DimVersion(serviceName, index);
	  if(srvptr){}
	  //	  cout << "found " << server << " " << node << endl;
	  index++;
	}
	cout << "found " << n << " servers" << endl;
	N_servers = n;
	while(1)
	{
	  int found = 0;
	  sleep(1);
	  for(i = 0; i < N_servers; i++)
	  {
	    if(server_versions[i] == 0)
	    {
	      found = 1;
	    }
	  }
	  if(!found)
	    break;
	}
	int max = 0;
	for(i = 0; i < N_servers; i++)
	{
	    if(server_versions[i] > max)
	    {
	      max = server_versions[i];
	    }
	}
	n = 0;
	for(i = 0; i < N_servers; i++)
	{
	    if(server_versions[i] < max)
	    {
	      cout << server_names[i] <<"@" << server_nodes[i] << " version " << server_versions[i] << endl; 
	    }
	    else
	      n++;
	}	
	cout << n << " Servers with version " << max << endl; 
}
