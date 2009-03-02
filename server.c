#include <stdio.h>
#include <stdlib.h>
#include <string.h>
 
#include "SDL_net.h"

static char reply[128];
static char move[2][128];
void handle(char *input) {
  char *s = input;
  static int id = 0;
  static int gotmove[2];
  static int sent[2];
  static int charm_gesture[2] = { -1, -1 };
  static int charm_hand[2] = { -1, -1 };
  static int para_hand[2][2] = {{ -1, -1 } , { -1, -1 }};
  int index = 0;
  void add_char(char c) {
    reply[index] = c;
    index++;
  }
  void add_nchar(char *cp, int n) {
    strncpy(reply + index, cp, n);
    index += n;
  }
  void fin() {
    reply[index] = '\0';
  }

  switch(*s) {
    case 'N':
      add_char('a' + id);
      fin();
      id++;
      if (id > 1) id = 0;
      break;
    case 'a':
    case 'b':
      {
      int j = *s - 'a';
      s++;
      switch(*s) {
	case 'C':  // Charm check-in.
	  charm_hand[j] = s[1] - '0';
	  charm_gesture[j] = s[2] - '0';
	  add_char('0');
	  fin();
	  break;
	case 'G':  // Charmed gesture query.
	  if (-1 == charm_gesture[1 - j]) {  // Should not happen!
	    add_char('-');
	  } else {
	    add_char('0' + charm_gesture[1 - j]);
	    charm_gesture[1 - j] = -1;
	  }
	  fin();
	  break;
	case 'H':  // Charmed hand query.
	  if (-1 == charm_hand[1 - j]) {
	    add_char('-');
	  } else {
	    add_char('0' + charm_hand[1 - j]);
	    charm_hand[1 - j] = -1;
	  }
	  fin();
	  break;
	case 'P':  // Paralyzed hand check-in.
	{
printf("d-: %d %d %d %d\n", para_hand[0][0], para_hand[0][1], para_hand[1][0], para_hand[1][1]);
	  int i = s[1] - '0';
	  if (i != 0 && i != 1) i = 0;  // TODO: Should not happen!
	  para_hand[j][i] = s[2] - '0';
	  add_char('0');
printf("d0: %d %d %d %d\n", para_hand[0][0], para_hand[0][1], para_hand[1][0], para_hand[1][1]);
	  fin();
	  break;
	}
	case 'Q':  // Paralyzed hand query.
	{
printf("d1: %d %d %d %d\n", para_hand[0][0], para_hand[0][1], para_hand[1][0], para_hand[1][1]);
	  int i = s[1] - '0';
	  if (i != 0 && i != 1) i = 0;  // TODO: Should not happen!
	  if (-1 == para_hand[1 - j][i]) {
	    add_char('-');
	  } else {
	    add_char('0' + para_hand[1 - j][i]);
	    para_hand[1 - j][i] = -1;
	  }
printf("d2: %d %d %d %d\n", para_hand[0][0], para_hand[0][1], para_hand[1][0], para_hand[1][1]);
	  fin();
	  break;
	}
	case 'M':  // Move check-in.
	  if (!gotmove[j]) {
	    gotmove[j] = 1;
	    strncpy(move[j], s + 1, 7);
	    int n = move[j][6] - '0';
	    if (n > 16) n = 16;
	    strncpy(move[j] + 7, s + 1 + 7, n * 2);
	  }
	  // *** FALL THROUGH ***
	case '-':  // Retry.
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
	  break;
	default:
	  add_char('?');
	  fin();
	  break;
      }
      }
      break;
    default:
      add_char('?');
      fin();
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
	  printf("recv '%s'\n", buffer);
	  handle(buffer);
	  printf("send '%s'\n", reply);
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
