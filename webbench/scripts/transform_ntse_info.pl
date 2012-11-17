#!/usr/bin/perl -w
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

use strict;
use Getopt::Long;
use Chart::Graph::Gnuplot qw(gnuplot);

my $outdir;
my $infile;

GetOptions(
    "if=s" => \$infile,
    "o=s" => \$outdir
);

unless ( $infile && $outdir ) {
    print "usage: transform_ntse_info.pl --if <ntse_status.txt> --o <ouput dir>\n";
    exit 1;
}

unless ( -f $infile ) {
    print "$infile doesn't exist\n";
    exit 1;
}

#####################################################
#
#####################################################
my $tmpfile=${outdir} . "ntse_buf_phy_writes.temp";
my $datafile=${outdir} . "ntse_buf_phy_writes.data";

system "cat $infile | grep '^Ntse_buf_phy_writes'| awk '{print \$2}' > $tmpfile";

my $line;
my $lastLine;
my $result;
my $period;

system "echo \"\" > $datafile";
open (FILE, $tmpfile) or die "Couldn't open: $!\n";
$lastLine=<FILE>;
$period=1;
while ( defined( $line=<FILE> ) ) {
	$result=($line - $lastLine);
	system "echo \"$period $result\" >> $datafile";
	$lastLine=$line;
	$period++;
}
close (FILE) or die "Couldn't close: $!\n";

system "rm $tmpfile";
#####################################################
#
#####################################################
$tmpfile=${outdir} . "ntse_buf_phy_reads.temp";
$datafile=${outdir} . "ntse_buf_phy_reads.data";

system "cat $infile | grep '^Ntse_buf_phy_reads'| awk '{print \$2}' > $tmpfile";

system "echo \"\" > $datafile";
open (FILE, $tmpfile) or die "Couldn't open: $!\n";
$lastLine=<FILE>;
$period=1;
while ( defined( $line=<FILE> ) ) {
	system "echo \"$period $result\" >> $datafile";
	$lastLine=$line;
	$period++;
}
close (FILE) or die "Couldn't close: $!\n";
system "rm $tmpfile";