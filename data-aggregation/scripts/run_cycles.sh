#!/usr/bin/env bash

set -e

OUTPUT_DATA_FOLDER="$PWD/data"
PARTICIPANT_PROJECT="$PWD/../participant-application"
ASKER_PROJECT="$PWD/../asker-application"
SECONDS_PER_CYCLE=10
TOTAL_CYCLES=100



HERE="$PWD"
echo "Output directory: $OUTPUT_DATA_FOLDER"
echo "Building projects..."
cd "$PARTICIPANT_PROJECT"
./gradlew build
cd "$ASKER_PROJECT"
./gradlew build
echo "Projects ready!"
echo
mkdir -p "$OUTPUT_DATA_FOLDER"

ID_MID="test"
PROC_FILES="$OUTPUT_DATA_FOLDER/proc"
ASKER_FIFO="$PROC_FILES/asker.fifo"
TOKILL=""

# Make sure your IP is in this file set right:
# data-aggregation/data-aggregation-shared/src/main/java/datatypes/values/IPFSConnection.java

check_text() {
    local FILE=$1
    local TEXT=$2
    echo -n "${FILE##*/}? "
    while ! grep "$TEXT" "$FILE" >/dev/null; do
        if [[ ! -z "$3" ]]; then
            if ! ps -p $3; then
                echo "Process we are waiting for quit..."
                exit 1
            fi
        fi
        sleep 1
    done
    echo -n "$TEXT! "
}

start_participants() {
    local P=$1
    cd "$PARTICIPANT_PROJECT"

    # start up N participants
    echo "Starting $P participants..."
    for (( c=1; c<=$P; c++ )); do
        ./gradlew --console=plain -q run &> "$PROC_FILES/$c" &
        TOKILL="$TOKILL $!"
        echo -n "$! "
    done
    echo

    echo "In case of emergency: '  kill $TOKILL  '"

    # wait a bit so peers can start up
    sleep 10

    # check that all peers really did start up
    echo "Checking participants"
    for (( c=1; c<=$P; c++ )); do
        check_text "$PROC_FILES/$c" "exists"
    done
    echo
    echo "All participants are ready!"
}

start_asker() {
    cd "$ASKER_PROJECT"
    
    local P=$1 # amount of participants
    local O=$2 # amount of operators

    # Make a fifo for the asker to put stuff in
    echo "Starting asker"
    if [[ -p "$ASKER_FIFO" ]]; then
        echo "Fifo already exists! I hope you know what you're doing"
        rm "$ASKER_FIFO"
    fi
    mkfifo "$ASKER_FIFO"

    # Start up the asker
    ASKER_OUT="$OUTPUT_DATA_FOLDER/data_${P}p_${O}o.txt"
    ./gradlew --console=plain -q run < "$ASKER_FIFO" &> "$ASKER_OUT" &
    ASKER_PID="$!"

    exec 3>"$ASKER_FIFO"

    echo "${P}_${O}$ID_MID" >&3

    check_text "$ASKER_OUT" "username:" "$ASKER_PID"
    echo
    echo "Asker is ready!"
}

kill_them() {
    # force shutdown all that are left
    echo "Stopping processes ..."
    for pid in $TOKILL; do
        echo -n "$pid. "
        if kill "$pid"; then
            TOKILL=${TOKILL// $pid/}
        else
            echo "Failed to kill $pid"
        fi
    done
    # close pipe
    exec 3>&-
    rm "$ASKER_FIFO" || true
}

kill_and_exit() {
    kill_them
    exit 0
}

do_run() {
    local P=$1 # amount of participants
    local O=$2 # amount of operators

    printf " == Starting run %3sp %3so\n" $P $O
    echo "Creating temp folder $PROC_FILES"
    mkdir -p "$PROC_FILES"

    FINAL_DIR="$OUTPUT_DATA_FOLDER/${P}_${O}"
    if [[ -d "$FINAL_DIR" ]]; then
        echo "Can't use a final dir that already exists"
        echo "You might want to manually fix this"
        echo "For now it means I will just not run this"
        return 0
    fi


    start_participants $P

    start_asker $P $O

    # every N seconds, run a new query
    for (( c=0; c<$TOTAL_CYCLES; c+=1 )); do
        echo "Starting cycle $c"
        echo "start $O $P 600" >&3
        sleep $SECONDS_PER_CYCLE
    done
        
    # wait until the asker is done
    local last="End ID: ${P}_${O}$ID_MID$(( TOTAL_CYCLES - 1 ))"
    echo "Waiting for $last"
    check_text "$ASKER_OUT" "$last" "$ASKER_PID"
    echo
    echo "Got results of the last cycle"
    sleep 30 # Waiting for last to finish
    echo "exit" > "$ASKER_FIFO"

    kill_them

    # move the results to a different dir
    mkdir "$FINAL_DIR"
    mv "$ASKER_OUT" "$FINAL_DIR"
    mv "$PROC_FILES/"* "$FINAL_DIR"
    
    echo
    printf " == Finished run %3sp %3so\n" $P $O
    echo
}

trap 'kill_and_exit' EXIT

for o in 1 3 5; do
    for (( p=$o; p<=5; p+=2 )); do
        cd "$HERE"
        ./launch_prototype.sh start 3
        do_run $p $o
        cd "$HERE"
        ./launch_prototype.sh stop 3
    done
done
