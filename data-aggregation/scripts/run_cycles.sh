#!/usr/bin/env bash

set -e

HERE="$PWD"
OUTPUT_DATA_FOLDER="$PWD/data"
PARTICIPANT_PROJECT="$PWD/../participant-application"
ASKER_PROJECT="$PWD/../asker-application"
TOTAL_CYCLES=5

echo "Output directory: $OUTPUT_DATA_FOLDER"
echo "Building projects..."
cd "$PARTICIPANT_PROJECT"
#./gradlew build
cd "$ASKER_PROJECT"
#./gradlew build
echo
echo "Projects ready!"
echo
mkdir -p "$OUTPUT_DATA_FOLDER"

check_text() {
    local FILE=$1
    local TEXT=$2
    echo -n "${FILE##*/}? "
    while ! grep "$TEXT" "$FILE"; do
        sleep 1
    done
    echo -n "$TEXT! "
}

do_run() {
    local P=$1 # amount of participants

    printf " == Starting run %3sp %3so\n" $P $O
    PROC_FILES="$OUTPUT_DATA_FOLDER/proc"
    echo "Creating temp folder $PROC_FILES"
    mkdir -p "$PROC_FILES"

    cd "$PARTICIPANT_PROJECT"

    # start up N participants
    local S=""
    for (( c=1; c<=$P; c++ )); do
	      echo -n "Starting participant $c..."
	      ./gradlew --console=plain -q run > "$PROC_FILES/$c" 2>&1 &
        S="$S $!"
        echo "$!"
    done

    # wait a bit so peers can start up
    sleep 10

    # check that all peers really did start up
    echo "Checking participants"
    for (( c=1; c<=$P; c++ )); do
        check_text "$PROC_FILES/$c" "Ready"
    done
    echo
    echo "All participants are ready!"

    read -p "press enter to quit..."

    # force shutdown all peers that are left
    echo "Stopping processes ..."
    for pid in $S; do
        echo -n "$pid"
        kill "$pid"
        ps -p "$pid" >/dev/null && echo -n ". "
    done

    rm -r "$PROC_FILES"

    echo
    printf " == Finished run %3sp %3so\n" $P $O
    echo
}

do_run $1

#for o in 1 4 8; do
#    for (( p=$o; p<=20; p+=1 )); do
#        do_run $p $o
#    done
#done


