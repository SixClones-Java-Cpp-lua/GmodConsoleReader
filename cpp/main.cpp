#include <iostream>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <cstddef>
#include <string.h>
#include <thread>
#include <sys/wait.h>

using namespace std;

bool running = false;

void isRunning(int PID)
{
	while (running)
	{
		int status;
		pid_t result = waitpid(PID, &status, WNOHANG);
		if (result == 0)
		{ // Child still alive 
			running = true;
		}
		else
		{ // Child exited
			running = false;
		}
	}
}
	
void readGmod(int fd) {
	char buffer[1024];
	while (running)
	{
		memset(buffer, 0, sizeof buffer);
		int result = read(fd, buffer, sizeof buffer);
		if (result > 0)
		{
			cout << buffer;
		}
	}
}

int main(int argc, char** argv)
{
	int fdm, fds;
	char* slavename;
	
	fdm = posix_openpt(O_RDWR);
	if (fdm < 0)
	{
		cout << "posix_openpt error" << endl;
	}
	else
	{
		//cout << "fdm : " << fdm << endl;
		if (grantpt(fdm) != 0)
		{
			cout << "grantpt error" << endl;
		}
		else
		{
			if (unlockpt(fdm) != 0)
			{
				cout << "unlockpt error" << endl;
			}
			else
			{
				slavename = ptsname(fdm);
				if (slavename == NULL)
				{
					cout << "ptsname error" << endl;
				}
				else
				{
					//cout << "slavename : " << slavename << endl;
					int PID = fork();
					if (PID == 0)
					{
						//child
						close(0);
						close(1);
						close(2);
						close(fdm);
						int fds = open(slavename, O_RDWR);
						dup2(fds, 0);
						dup2(fds, 1);
						dup2(fds, 2);
						
						execvp("./srcds_run", argv);
					} else {
						running = true;
						std:thread checkRunning (isRunning, PID);
						thread scanGmod (readGmod, fdm);
					
						while (running)
						{
							string gmodCmd;
							getline(cin, gmodCmd);
							gmodCmd += "\n";
							char buffer[gmodCmd.length() + 1];
							strcpy (buffer, gmodCmd.c_str());
							write(fdm, buffer, gmodCmd.length() + 1);
						}
						scanGmod.join();
						checkRunning.join();
					}
				}
			}
		}
	}
				
	
	
    return 0;	
}
