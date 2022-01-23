
if [[ -z $1 ]]; then
	echo "This thing needs an argument"
	exit 1
fi

TO=$1
mkdir $TO
mv ./data/proc/* $TO
mv ../asker-application/steps* $TO


