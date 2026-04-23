#!/bin/bash
cat app/src/main/java/com/nuvio/tv/ui/screens/player/PlayerScreen.kt | grep -n -B 2 -A 5 "IconButton"
