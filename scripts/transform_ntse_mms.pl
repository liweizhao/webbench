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

use strict;
use Getopt::Long;
use Chart::Graph::Gnuplot qw(gnuplot);

my $outdir;
my $infile;

GetOptions(
    "i=s" => \$infile,
    "o=s" => \$outdir
);

unless ( $infile && $outdir ) {
    print "usage: transform_ntse_mms.pl --i <input file> --o <ouput dir>\n";
    exit 1;
}

#unless ( -f $indir ) {
#    print "$indir directory doesn't exist\n";
#    exit 1;
#}
###########################################################
#从information_schema数据库查询的输出文件中解析mms命中率
###########################################################
my $tmpfile=${outdir} . "/mms.temp";

if (! -e $outdir) {
        mkdir($outdir,0777) || die "can't make directory $outdir";
}

system "sed -n '/NTSE_MMS_STATS/,/NTSE_MUTEX_STATS/p' ${infile} | sed -e '/NTSE_MMS_STATS/d' -e '/NTSE_MUTEX_STATS/d' -e '/TABLE_ID/d' -e '/^\$/d' | awk '{print \$4, \$7, \$8}' > $tmpfile";

my $line;
my $result;
my $period1 = 1;
my $period2 = 1;
my @x1;
my @x2;
my @y1;
my @y2;

open (FILE, $tmpfile) or die "Couldn't open: $!\n";
while ( defined( $line=<FILE> ) ) {
        chomp $line;
        my @i = split / /, $line;
        if ($i[0] == 0) {#计算非大对象mms命中率
                $result = $i[2] * 100 / $i[1];
                push @y1, $result;
                push @x1, $period1;
                $period1++;
        } elsif ($i[0] == 1) {#计算大对象的mms命中率
                $result = $i[2] * 100 / $i[1];
                push @y2, $result;
                push @x2, $period2;
                $period2++;
        }
}
close (FILE) or die "Couldn't close: $!\n";

my %gopts = (
        'title' => "mms shootings",
        'yrange' => '[0:100]',
        'xtics' => "1",
        'x-axis label' => "period",
        'y-axis label' => "mms shootings",
        'extra_opts' => 'set grid xtics ytics',
        'output type' => "png",
        'output file' => "${outdir}/mms_shootings.png"
);

my %ds_opts_heap = (
   'title' => "mms shootings(heap)",
   'type' => 'columns',
   'style' => 'linespoints'
   );

my %ds_opts_slob = (
   'title' => "mms shootings(slob)",
   'type' => 'columns',
   'style' => 'linespoints'
   );

my @ds_heap = ();
my @ds_slob = ();

push @ds_heap, [\%ds_opts_heap, \@x1, \@y1];
push @ds_slob, [\%ds_opts_slob, \@x2, \@y2];
gnuplot(\%gopts, @ds_heap, @ds_slob);

unlink($tmpfile);
