#!/bin/bash
#
# Copyright (c) <2011>, <NetEase Corporation>
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
# 3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

#
# Print ntse information_schema table and global status.
#
# This script will print all ntse information_schema tables and global status at start/end of the test plus the 15th minutes of test running.
# And partial inforamtion_schema tables (exclude _EX and other tables may affacting dbt2 test ) are printed once per minute.
#
# Required input:
# [1]: mysql configure file as $1. for example, ${HOME}/my.cnf
# [2]: iters as $2 represents number of minutes the test is running
#
# @author: niemingjun@163.org
# @date: 2009 August 5th
#

names=( NTSE_HEAP_STATS NTSE_MUTEX_STATS NTSE_MMS_STATS NTSE_LOB_STATS NTSE_INDEX_STATS NTSE_MMS_RIDHASH_CONFLICTS NTSE_CONNECTIONS NTSE_COMMAND_RETURN NTSE_TABLE_STATS NTSE_DBOBJ_STATS NTSE_RWLOCK_STATS)
namesAll=( NTSE_HEAP_STATS NTSE_MUTEX_STATS NTSE_INDEX_STATS_EX NTSE_MMS_STATS NTSE_LOB_STATS NTSE_INDEX_STATS NTSE_INDEX_DEF_EX NTSE_MMS_RIDHASH_CONFLICTS NTSE_CONNECTIONS NTSE_COMMAND_RETURN NTSE_TABLE_STATS NTSE_DBOBJ_STATS NTSE_BUF_DISTRIBUTION NTSE_COLUMN_DEF_EX  NTSE_RWLOCK_STATS  NTSE_LOB_STATS_EX  NTSE_HEAP_STATS_EX NTSE_TABLE_DEF_EX NTSE_MMS_RPCLS_STATS)
count=0
default_file=$1
iters=$2

# 约定的分隔字符。
# 用于将输出文件分割为3部分，开始初态，[分隔字符]中间运行状态数据， [分隔字符]结束时的终态。
# 分割命令 csplit -f cs_test test "/^${SPLIT_STRING}/" {1}
SPLIT_STRING="PRINT_ALL_TABLE_INFORMATION"

if [ $# -ne 2 ]; then
	echo "please specify 2 argumnents to run print_ntse_infor_schema.sh. Error: $# arguments provided only."
	echo "usage: print_ntse_infor_schema.sh <mysql_cnf_file> <iter_minutes> "
	exit
else
	echo "mysql configure file: ${default_file}"
	echo "test minutes: ${iters}"
fi
# should specify $1 as  mysqld config file $2 as loop interator counts. 

#print information_schema tables;
print_info_tables()
{
  for name in ${names[@]}
  do
    echo "-------------------------------------$name------------------------------------------"  
	#information_schema 表信息打印 用\G 以便查看：
    query_sql="select * from information_schema.$name \G" 
    mysql --defaults-file=$default_file -uroot -e "$query_sql" 
    echo
    echo
  done
}

#print all information_schema tables;
print_info_tables_all()
{
	echo "" 
	echo "Print all table information:" 
	for name in ${namesAll[@]}
	do
		echo "-------------------------------------$name------------------------------------------  " 
		query_sql="select * from information_schema.$name  \G" 
		mysql --defaults-file=$default_file -uroot -e "$query_sql" 
		echo
		echo
	done
}

#print ntse global status;
#show global status like 'ntse%'
print_ntse_global_status()
{
	
	mysql --defaults-file=$default_file -uroot -e "show global status like 'ntse%'" 
}

for (( i = 0 ; i < $iters ; i++ ))
do
	echo  ====================Test ${i} ========================
	print_ntse_global_status
	if [ $i -eq  0 -o $i -eq 14 ] ; then
		print_info_tables_all
		#在start时状态打印后，打印分隔符号
		if [ $i -eq 0 ]; then
			echo $SPLIT_STRING
		fi
	else
		print_info_tables
	fi	
	sleep 60
done

echo "" 
echo $SPLIT_STRING
echo "Stop status:"  
print_ntse_global_status
print_info_tables_all
