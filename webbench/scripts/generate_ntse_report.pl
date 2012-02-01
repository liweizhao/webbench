#!/usr/bin/perl -w

use lib './scripts';
use lib './scripts/perl/lib';

use strict;
use warnings;
use Getopt::Long;
use Ntse::NtseStatus;

my $format = "png";
my $infile;
my $outdir = "report";

GetOptions(
    'f=s' => \$format,
    'i=s' => \$infile,
    'o=s' => \$outdir,
);

unless( $infile ) {
        print "no input file specified!\n";
        exit 1;
}

unless( $outdir ) {
        print "no output dir specified!\n";
        exit 1;
}

my $presenter;

$presenter = new Ntse::NtseStatus($infile, $outdir);

#
# Start creating report.
#
$presenter->format($format);
$presenter->outdir($outdir);
$presenter->infile($infile);
$presenter->plot();

system "mkdir -p $outdir";
open(H, "> $outdir/ntse_status.html")
    or die "Couldn't open $outdir/index.html for writing: $!\n";
print H $presenter->to_html();

my $q = new CGI;

my $link = $q->a({href => "ntse_status/mms_shootings.png"}, $q->img(
        {src => "ntse_status/mms_shootings.png",
        height => 96, width => 128}));

my $mms_html = $q->Tr( $q->td("mms_shootings") .
                          $q->td({align => 'center'}, $link)
                     );
print H $mms_html;

close(H);
print "ntse status html report generated.\n";
