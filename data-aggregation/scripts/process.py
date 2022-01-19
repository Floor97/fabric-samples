#!/usr/bin/env python3

# x axis = cyles
# y axis = time
#
#
#


import sys
from os import listdir
from os.path import isfile, join


hide_info=False
def iprint(*args, **kwargs):
    if hide_info: return
    s = " ".join(map(str,args))
    print("[I]: " + s, **kwargs)


#
# Returns an array with the time that this
# participant waited in total for each cycle
#
# In file format:
# $id = $participants_${operators}test${cycle}
# Waiting, id time: $id $waiting_time
#
# 
def process_participant(p_file, total_cycles):
    result=[0] * total_cycles
    with open(p_file, 'r', encoding = 'utf-8') as f:
        for line in f:
            if not line.startswith("Waiting, id time: "):
                continue
            line = line[18:]
            iprint(line)
            qid, time = line.split(" ")
            cycle = int(qid.split("test")[1])
            if cycle >= total_cycles:
                print("Total cycles was wrong I guess (P)")
                sys.exit(1)
            result[cycle] += int(time)
    return result


def process_all_participants(peer_folder, total_cycles):
    p_files = [f for f in listdir(peer_folder)
            if f.isdigit() and isfile(join(peer_folder, f))]
    p_totals = [0] * total_cycles
    for p in p_files:
        pf = join(peer_folder, p)
        d = process_participant(pf, total_cycles)
        for i, v in enumerate(d):
            p_totals[i] += v
    return [ t / len(p_files) for t in p_totals ]


#
# Get the total time a cycle took
# as an array, for each cycle.
#
# Start Cycle 0: 1642605876650
# End ID: 1_1test0, 1642605886492
#
def process_asker(data_folder, total_cycles):
    c_start = [-1] * total_cycles
    c_end   = [-1] * total_cycles

    file_loc = data_folder + "/run_4_1.txt"
    with open(file_loc, 'r', encoding = 'utf-8') as f:
        for line in f:
            if line.startswith("Start Cycle "):
                rest = line[12:]
                c, time = list(map(int, rest.split(": ")))
                if c >= total_cycles:
                    print("Total cycles was wrong I guess (A)")
                    sys.exit(1)
                c_start[c] = time
            if line.startswith("End ID: "):
                qid, time = line[8:].split(", ")
                c = int(qid.split("test")[1])
                c_end[c] = int(time)

    result=[0] * total_cycles
    for i, times in enumerate(zip(c_start, c_end)):
        if times[1] <= 0 or times[0] <= 0:
            print("Asker did not have start/end for cycle " + i)
            sys.exit(1)
        result[i] = times[1] - times[0]
    return result



def process(data_folder, peer_folder, total_cycles):
    p_avg = process_all_participants(peer_folder, total_cycles)
    iprint(p_avg)
    asker = process_asker(data_folder, total_cycles)
    iprint(asker)
    

things="/mnt/f/CSE Bachelor/CSE Y4/performance analysis/Cycles/4_1"
process(things, things, 100)





