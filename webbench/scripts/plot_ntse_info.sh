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
# @AUTHOR "LI WEIZHAO"
# @CONTACT "rickylee86#gmail.com"
# @DATE "2012-11-17"
#

INFILE="ntse.status.before"
OUTDIR="./tmp"
PLOTFILE="ntse_status_plot.data"

while getopts "i:o:" opt; do
	case $opt in
		i)
			INFILE=$OPTARG
			;;
		o)
			OUTDIR=$OPTARG
			;;
	esac
done
if [ ! -f "$INFILE" ]; then
	echo "$INFILE does not exist."
	exit 1
fi

make_directories()
{
	OUTDIR=${1}
	mkdir -p ${OUTDIR}
}

make_directories $OUTDIR

./transform_ntse_info.pl --if $INFILE --o ${OUTDIR}/

#
# Plot the ntse physical read information.
#
NAME="ntse_physical_reads"
INPUT_FILE="ntse_buf_phy_reads.data"
PNG_FILE="${NAME}.png"
echo "plot \"ntse_buf_phy_reads.data\" using 1:2 title \"NTSE buffer physical reads\" with lines" > ${OUTDIR}/${PLOTFILE}
echo "set title \"NTSE buffer physical reads\"" >> ${OUTDIR}/${PLOTFILE}
echo "set grid xtics ytics" >> ${OUTDIR}/${PLOTFILE}
echo "set xlabel \"Time\"" >> ${OUTDIR}/${PLOTFILE}
echo "set ylabel \"pages read\"" >> ${OUTDIR}/${PLOTFILE}
echo "set term png small" >> ${OUTDIR}/${PLOTFILE}
echo "set output \"${PNG_FILE}\"" >> ${OUTDIR}/${PLOTFILE}
#echo "set yrange [0:]" >> ${OUTDIR}/${PLOTFILE}
echo "replot" >> ${OUTDIR}/${PLOTFILE}
(cd ${OUTDIR}; gnuplot ${PLOTFILE} > /dev/null 2>&1)
rm ${OUTDIR}/${PLOTFILE} > /dev/null 2>&1
rm ${OUTDIR}/ntse_buf_phy_reads.data > /dev/null 2>&1	
#
# Plot the ntse physical write information.
#
NAME="ntse_physical_writes"
INPUT_FILE="ntse_buf_phy_reads.data"
PNG_FILE="${NAME}.png"
echo "plot \"ntse_buf_phy_writes.data\" using 1:2 title \"NTSE buffer physical writes\" with lines" > ${OUTDIR}/${PLOTFILE}
echo "set title \"NTSE buffer physical writes\"" >> ${OUTDIR}/${PLOTFILE}
echo "set grid xtics ytics" >> ${OUTDIR}/${PLOTFILE}
echo "set xlabel \"Time\"" >> ${OUTDIR}/${PLOTFILE}
echo "set ylabel \"pages write\"" >> ${OUTDIR}/${PLOTFILE}
echo "set term png small" >> ${OUTDIR}/${PLOTFILE}
echo "set output \"${PNG_FILE}\"" >> ${OUTDIR}/${PLOTFILE}
#echo "set yrange [0:]" >> ${OUTDIR}/${PLOTFILE}
echo "replot" >> ${OUTDIR}/${PLOTFILE}
(cd ${OUTDIR}; gnuplot ${PLOTFILE} > /dev/null 2>&1)
rm ${OUTDIR}/${PLOTFILE} > /dev/null 2>&1
rm ${OUTDIR}/ntse_buf_phy_writes.data	> /dev/null 2>&1
