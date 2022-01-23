#!/usr/bin/env python3

import sys
from os import listdir
from os.path import isfile, join

files = [ f for f in listdir(sys.argv[1]) if isfile(join(sys.argv[1], f)) ]

participantsf = [ f for f in files if len(str(f)) == 1 ]
askerf = [ f for f in files if f.startswith("steps_") ][0]

def stepsFromFile(filep):
    instep = [ -1 ] * 8
    steptimes = [ [] for _ in range(8) ]
    # Get all steps as 2d array
    with open(filep, 'r') as f:
        for line in f:
            if "Begin Step" in line:
                line = line[line.index("Begin Step"):]
            if line.startswith("Begin Step "):
                step = int(line[11:12]) - 1
                time = int(line[line.rindex(" ")+1:])
                if instep[step] != -1:
                    print("Problem in " + filep)
                    print("Begin step before last end (" + str(step) + ")")
                instep[step] = time
            if line.startswith("End Step "):
                step = int(line[9:10]) - 1
                time = int(line[line.rindex(" ")+1:])
                if instep[step] == -1:
                    print("Problem in " + filep)
                    print("End step without start (" + str(step) + ")")
                steptimes[step].append(time - instep[step])
                instep[step] = -1
    # Average them
    result = [ 0 ] * 8
    for i, reps in enumerate(steptimes):
        if len(reps) == 0: continue
        avg = sum(reps) / len(reps)
        result[i] = avg
    return result

askersteps = stepsFromFile(join(sys.argv[1], askerf))
print("Asker steps: " + str(askersteps))
for partif in participantsf:
    psteps = stepsFromFile(join(sys.argv[1], partif))
    print("P: " + str(psteps))



