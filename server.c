#include <stdio.h>
#include <stdlib.h>
#include <string.h>
 
#include "SDL_net.h"

static char reply[128];
static char move[2][128];
void handle(char *s) {
  static int id = 0;
  static int gotmove[2];
  static int sent[2];
  int i = 0;
  void add_char(char c) {
    reply[i] = c;
    i++;
  }
  void add_nchar(char *cp, int n) {
    strncpy(reply + i, cp, n);
    i += n;
  }
  void fin() {
    reply[i] = '\n';
    reply[i + 1] = '\0';
  }

  switch(s[0]) {
    case 'N':
      add_char('a' + id);
      fin();
      id++;
      if (id > 1) id = 0;
      break;
    case 'a':
    case 'b':
      {
      int j = s[0] - 'a';
      if ('-' == s[1]) {  // Retry.
      } else if (!gotmove[j]) {  // Move check-in.
	gotmove[j] = 1;
	strncpy(move[j], s + 1, 7);
	int n = move[j][6] - '0';
	if (n > 16) n = 16;
	strncpy(move[j] + 7, s + 1 + 7, n * 2);
      }
      // Respond:
      if (gotmove[1 - j]) {
	add_nchar(move[1 - j], 7);
	int n = move[1 - j][6] - '0';
	if (n > 16) n = 16;
	add_nchar(move[1 - j] + 7, n * 2);
	sent[j] = 1;
      } else {
	add_char('-');
      }
      fin();
      }
      break;
  }
  if (sent[0] && sent[1]) {
    sent[0] = sent[1] = 0;
    gotmove[0] = gotmove[1] = 0;
  }
  return;
}
 
int main(int argc, char **argv) {
  TCPsocket sd, csd; // Server, client.
  IPaddress ip[1];
  int quit;
 
  if (SDLNet_Init() < 0) {
    fprintf(stderr, "SDLNet_Init: %s\n", SDLNet_GetError());
    exit(EXIT_FAILURE);
  }
  if (SDLNet_ResolveHost(ip, NULL, 3333) < 0) {
    fprintf(stderr, "SDLNet_ResolveHost: %s\n", SDLNet_GetError());
    exit(EXIT_FAILURE);
  }
  if (!(sd = SDLNet_TCP_Open(ip))) {
    fprintf(stderr, "SDLNet_TCP_Open: %s\n", SDLNet_GetError());
    exit(EXIT_FAILURE);
  }
 
  SDLNet_SocketSet serverset = SDLNet_AllocSocketSet(2);
  SDLNet_TCP_AddSocket(serverset, sd);
  quit = 0;
  while (!quit) {
    int n = SDLNet_CheckSockets(serverset, (Uint32) -1);
    if ((csd = SDLNet_TCP_Accept(sd))) {
      SDLNet_SocketSet set = SDLNet_AllocSocketSet(2);
      SDLNet_TCP_AddSocket(set, csd);
      n = SDLNet_CheckSockets(set, 3000);
      if (n == 0) {
	printf("too slow!\n");
      } else if (SDLNet_SocketReady(csd)) {
	int res;
	char buffer[128];
	if ((res = SDLNet_TCP_Recv(csd, buffer, 128 - 1)) > 0) {
	  buffer[res] = '\0';
	  printf("%d recv '%s'\n", res, buffer);
	  handle(buffer);
	  printf("sending '%s'\n", reply);
	  SDLNet_TCP_Send(csd, reply, strlen(reply));
	} else {
	  printf("error or bug: %d\n", res);
	}
      } else {
	printf("error or bug.\n");
      }
      SDLNet_FreeSocketSet(set);
      SDLNet_TCP_Close(csd);
    } else {
      fprintf(stderr, "SDLNet_TCP_Accept: %s\n", SDLNet_GetError());
    }
  }
  SDLNet_FreeSocketSet(serverset);
 
  SDLNet_TCP_Close(sd);
  SDLNet_Quit();
 
  return EXIT_SUCCESS;
}
