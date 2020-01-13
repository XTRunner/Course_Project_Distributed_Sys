#include <rpc/rpc.h>
#include <time.h>
#include <sys/types.h>
#include "p1.h"

#include <stdlib.h>
#include <stdio.h>
#include <sys/sysinfo.h>
#include <sys/resource.h>
#include <pwd.h>
#include <unistd.h>
#include <malloc.h>

#define MAX_LEN 1024
#define LINUX_SYSINFO_LOADS_SCALE 65536.0


char** p1_1(long *option){
	struct tm *timeptr;  /* Pointer to time structure */
	time_t clock;        /* time_t structure for localtime() */
    static char *ptr;    /* Pointer to returned String value */
	
	FILE* flopen;		 /* Pointer to checked file */
	long double cpu1[4], cpu2[4], totalUsage, totalIdle; /* Storage for CPU information */
	char user[MAX_LEN];  /* Storage for user list*/
	char meminfo[MAX_LEN]; /* Storage for memory info*/
	char memline[64]; /* Storage for each line of memory info*/
	
	static char err[] = "Invalid Response \0";
	static char s[MAX_LEN]; /* Return String value */
	
	struct passwd *pwd;  /* passwd structure for getpwent() */

    struct sysinfo statinfo; /* sysinfo structure for sysinfo() */
	
	double loadavg[3];  /* Storage for load info */
    
	/* Initial and get current time */
	clock = time(0);
    timeptr = localtime(&clock);

    switch(*option){
        case 1:
            strftime(s, MAX_LEN, "%A, %B %d, %Y",timeptr); /* day_of_week, month, day, year */
            ptr=s;
            break;
            
        case 2:
            strftime(s, MAX_LEN, "%T",timeptr); /* 24-hour notation timestamp*/
            ptr=s;
            break;
            
        case 3:
            strftime(s, MAX_LEN,"%A, %B %d, %Y - %T",timeptr); /* Combine 1 & 2 */
            ptr=s;
            break;
            
        case 4:
		/*
			1	user	Time spent with normal processing in user mode.
			2	nice	Time spent with niced processes in user mode.
			3	system	Time spent running in kernel mode.
			4	idle	Time spent in vacations twiddling thumbs.
		*/
		{
			flopen = fopen("/proc/stat", "r"); /* Open stat file */
			fscanf(flopen, "cpu %Lf %Lf %Lf %Lf", &cpu1[0], &cpu1[1], &cpu1[2], &cpu1[3]);
			fclose(flopen);
			
			sleep(1); /* Sleep for 1 sec because all values shown in /proc/stat are accumlative */
			
			flopen = fopen("/proc/stat", "r");
			fscanf(flopen, "cpu %Lf %Lf %Lf %Lf", &cpu2[0], &cpu2[1], &cpu2[2], &cpu2[3]);
			fclose(flopen);
			
            totalUsage = cpu2[0] + cpu2[1] + cpu2[2] + cpu2[3] - (cpu1[0] + cpu1[1] + cpu1[2] + cpu1[3]); /* Real CPU time within that 1 sec */
			totalIdle = cpu2[3] - cpu1[3]; /* CPU Idle time within that 1 sec */
			
			sprintf(s, "CPU usage: \n -%Lf%%\n", 100.0 * ((totalUsage-totalIdle)/totalUsage));
			ptr=s;
            break;
		}
		
		/*
			struct sysinfo {
               long uptime;             
               unsigned long loads[3];  
               unsigned long totalram;  
               unsigned long freeram;   
               unsigned long sharedram; 
               unsigned long bufferram; 
               unsigned long totalswap; 
               unsigned long freeswap;  
               unsigned short procs;    
               char _f[22];             
           }
		*/
        case 5:
		/*
            sysinfo(&statinfo);
            sprintf(s, "Memory - Total: %lu Bytes\n          Available: %lu Bytes\n          Share: %lu Bytes\n          Buffer: %lu Bytes\n", statinfo.totalram, statinfo.freeram, statinfo.sharedram, statinfo.bufferram);
        */
			flopen = fopen("/proc/meminfo", "r"); /* Open memory info file */
			stpcpy(meminfo, "Memory Usage: \n");
			int count = 0;
			while( fgets(memline, 64, flopen) != NULL && count < 4 ){
				strcat(meminfo, "  -");
				strcat(meminfo, memline);
				count = count + 1;
			}
			
			fclose(flopen);
			stpcpy(s,meminfo);
			ptr=s;
            break;
        
		case 6:
		{	
			if (sysinfo(&statinfo) != -1){
				sprintf(s, "Swap usage: \n -Total: %lu Bytes\n -Available: %lu Bytes\n", statinfo.totalswap, statinfo.freeswap);
			} else {
				sprintf(s, "Swap memory info was unobtainable...");
			}
			ptr=s;
            break; 
		}
		
		/*
		struct passwd {
               char   *pw_name;       
               char   *pw_passwd;     
               uid_t   pw_uid;        
               gid_t   pw_gid;        
               char   *pw_gecos;      
               char   *pw_dir;        
               char   *pw_shell;      
        }
		*/
		case 7:
		{
			while (pwd = getpwent()){ /* Each time return one entry */
				strcat(user, pwd->pw_name);
				strcat(user, "\n");
			}
			stpcpy(s,user);
			ptr=s;
			break;
		}
		
		
        case 8:
		/*
            sysinfo(&statinfo); 
       		sprintf(s, "Load procs per minute - 1 min: %lu\n                         5 mins: %lu\n                         15 mins: %lu\n", statinfo.loads[0]/LINUX_SYSINFO_LOADS_SCALE, statinfo.loads[1]/LINUX_SYSINFO_LOADS_SCALE, statinfo.loads[2]/LINUX_SYSINFO_LOADS_SCALE);
		*/
			if (getloadavg(loadavg, 3) != -1){ /* get load avg into loadavg*/
				sprintf(s, "Load procs per minute: \n - 1 min: %lf\n - 5 mins: %lf\n - 15 mins: %lf\n", loadavg[0], loadavg[1], loadavg[2]);
			} else {
				sprintf(s, "Load average info was unobtainable...");
			}
			ptr=s;
            break;    
		
        default: 
            ptr=err;
            break;
    }
    
    return(&ptr);
}
