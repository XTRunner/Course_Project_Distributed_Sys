#include <stdio.h>
#include <string.h>
#include <time.h>
#include <sys/types.h>
#include <rpc/rpc.h>
#include "p1.h"

#define MAX_LEN 1024

long get_response(void); /* Fetch keyboard input */

long get_response()
{
	long choice;
	
	printf("==========================================================\n");
	printf("                   Menu:\n");
	printf("-------------------------------------------\n");
	printf("                1. Date\n");
	printf("                2. Time\n");
	printf("              	3. Combination\n");
	printf("              	4. CPU usage\n");
	printf("              	5. Memory usage\n");
	printf("              	6. Swap usage\n");
	printf("              	7. List of user names\n");
	printf("              	8. Load procs per min\n");
	printf("              	9. Quit\n");
	printf("-------------------------------------------\n");
	printf("              	Choice (1-9):");
	scanf("%ld", &choice);
	printf("==========================================================\n");
	
	return(choice);
}


int main(int argc, char **argv)
{
	CLIENT *cl;       /* RPC handle */
	char *server;     /* Hostname of server */
	char **sresult;	  /* Return String of P1_1() on server machine */
	char s[MAX_LEN];  /* Array for holding output */
	long response;    /* Keyboard input*/
	long *lresult;	  /* Pointer to Long input */
	
	if(argc != 2){    /* Input format: client hostname */
		fprintf(stderr, "usage:%s hostname\n", argv[0]);
		exit(1);
	}

	server = argv[1];
	lresult = (&response);
	
	/* Create RPC handle on client side and specifes protocol as UDP */
	if((cl=clnt_create(server,p1_PROG,p1_VERS,"udp")) == NULL){
		clnt_pcreateerror(server);
		exit(2);
	}
	
	response = get_response();
	
	while(response != 9){ /* If input does not indicate Quit */
		if((sresult = p1_1(lresult,cl)) == NULL){ /* Call P1_1() on server machine & Pass argument by client handle */
			clnt_perror(cl,server);
			exit(3);
		}

		printf(" %s\n", *sresult);
		response = get_response();
	}

	clnt_destroy(cl); /* Destroy Client RPC handle */
	exit(0);
}
