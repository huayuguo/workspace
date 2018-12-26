#!/bin/bash
if [ "$1" == "systemprop" ];then
    if [ -e "$2" ];then
        cp $2 "$2"_tmp
        echo "" >> "$2"_tmp
        cat ""$2"_tmp" | while read row;do
            if [ ! -z "$row" ];then
                KEY=`echo $row | awk -F"=" 'sub(/^[[:blank:]]*/,"",$2) {print $1}'`
                VALUE=`echo $row | awk -F"=" 'sub(/^[[:blank:]]*/,"",$2) {print $2}'`
                RET=`awk -F"=" '{if(/^'"$KEY"'/)print $1}' $3`
                if [ ! -z "$RET" ];then 
                    OLD_VALUE=`awk -F"=" 'sub(/^[[:blank:]]*/,"",$2) {if(/^'"$KEY"'/)print $2}' $3`
                    if [ "$VALUE" != "$OLD_VALUE" ];then
                        sed -i "/^$KEY/s/=.*/=$VALUE/g" $3; 
                    fi
                else
		    echo >> $3	
                    echo $row >> $3
                fi 
            fi
        done
        rm "$2"_tmp
    fi
elif [ "$1" == "logo" ];then
	if [ -e out/target/product/$2/obj/BOOTLOADER_OBJ/build-$2/dev/logo ];then
		rm -rf out/target/product/$2/obj/BOOTLOADER_OBJ/build-$2/dev/logo/*
	fi
elif [ "$1" == "defconfig" ];then
	if [ -e "$2" ];then
        cp $2 "$2"_tmp
        echo "" >> "$2"_tmp
        cat ""$2"_tmp" | while read row;do
            if [ ! -z "$row" ];then
                NUMBER=`echo $row | awk -F"=" '{if(!/^#/)print NF}'`
                if [[ $NUMBER -gt 1 ]];then
                    NEW=`echo $row | awk -F"=" '{if(!/^#/)print}'`
                    SET=`echo $NEW | awk -F"=" 'sub(/^[[:blank:]]*/,"",$1) {print $1}'`
                    OLD=`grep -s "$SET=" $3`
                    if [ ! -z "$OLD" ];then
                        if [ "$NEW" != "$OLD" ];then
                            sed -i "s/$OLD/$row/g" $3; 
                        fi
                    else
                        echo "do nothing!"
                    fi
                else
                    tmp=${row#*#}
                    tmp=${tmp%%=*}
                    KEY=`echo ${tmp%%is*} | sed 's/ //g'`
                    OLD=`grep -s "$KEY" $3| awk '{if(!/^#/)print}'`
                    MULTI=`echo $OLD | awk -F" " '{print NF}'`
                    if [[ $MULTI -gt 1 ]];then
                        for i in `echo $OLD | awk -F" " '{print}'`
                        do
                            tmp=`echo $i | awk -F"=" '{print $1}'`
                            if [ $tmp = $KEY ];then
                                OLD=$i
                            fi
                        done
                    fi
              
                    if [ ! -z "$OLD" ];then
                        sed -i "s/$OLD/$row/g" $3; 
                    else
                        echo
                    fi
                     
                fi
            fi
        done
        rm "$2"_tmp
    fi
elif [ "$1" == "log" ];then
    if [ -e "$2" ];then
        rm "$2"
    else
        mkdir -p ${2%/*}
    fi
    ALLPATH=${2%/*}
    OUTPATH=${ALLPATH%%system*}
    echo Build   Path: $PWD>> "$2"
    echo Build Branch: `git branch | awk '{if(match($1,"*")){print $2}}'`>> "$2"
    echo Build   Time: `date +%Y-%m-%d\ %H:%M` >>"$2"
    echo Build Author:`whoami`>> "$2"
    IP=`ifconfig eth0 |awk '/inet/ {split($2,x,":");print x[2]}'`
    if [ "$IP" = "" ];then
        IP=`ifconfig em0 |awk '/inet/ {split($2,x,":");print x[2]}'`
    fi
    if [ "$IP" = "" ];then
        IP=`ifconfig em1 |awk '/inet/ {split($2,x,":");print x[2]}'`
    fi
    echo Build Server:"$IP">> "$2"
    echo >>$2
    echo >>$2
    echo "$ALLPATH" >>$2
    echo "$OUTPATH" >>$2
    echo >>$2
    git log >>$2
    cp $OUTPATH*_Android_scatter.txt $ALLPATH
fi

