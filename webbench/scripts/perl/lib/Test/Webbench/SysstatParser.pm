package Test::Webbench::SysstatParser;

=head1 NAME

Test::Webbench::SysstatParser - Perl module to parse output files from sysstat command.

=head1 SYNOPSIS

 use Test::Webbench::SysstatParser;

 my $parser = new Test::Webbench::SysstatParser;
 $parser->parse($text);

=head1 DESCRIPTION

This module transforms sysstat command output into a hash that can be used to generate
XML.

=head1 FUNCTIONS

Also see L<Test::Parser> for functions available from the base class.

=cut

use strict;
use warnings;
use POSIX qw(ceil floor);
use Test::Parser;
use Test::Parser::Iostat;
use Test::Parser::Oprofile;
use Test::Parser::PgOptions;
use Test::Parser::Readprofile;
use Test::Parser::Sar;
use Test::Parser::Sysctl;
use Test::Parser::Vmstat;
use XML::Simple;

@Test::Webbench::SysstatParser::ISA = qw(Test::Parser);
use base 'Test::Parser';

use fields qw(
              data
              sample_length
              );

use vars qw( %FIELDS $AUTOLOAD $VERSION );
our $VERSION = '1.7';

=head2 new()

Creates a new Test::Webbench::SysstatParser instance.
Also calls the Test::Parser base class' new() routine.
Takes no arguments.

=cut

sub new {
    my $class = shift;
    my Test::Webbench::SysstatParser $self = fields::new($class);
    $self->SUPER::new();

    $self->name('sysstat');
    $self->type('standards');

    $self->{data} = {};
    $self->{sample_length} = 60; # Seconds.

    return $self;
}

=head3 data()

Returns a hash representation of the sysstat data.

=cut
sub data {
    my $self = shift;
    if (@_) {
        $self->{data} = @_;
    }
    return {dbt2 => $self->{data}};
}

sub duration {
    my $self = shift;
    return $self->{data}->{duration};
}

sub errors {
    my $self = shift;
    return $self->{data}->{errors};
}

sub metric {
    my $self = shift;
    return $self->{data}->{metric};
}

=head3

Override of Test::Parser's default parse() routine to make it able
to parse sysstat output.  Support only reading from a file until a better
parsing algorithm comes along.

=cut
sub parse {
    #
    # TODO
    # Make this handle GLOBS and stuff like the parent class.
    #
    my $self = shift;
    my $input = shift or return undef;
    my $output = shift or return undef;
    return undef unless (-d $input);
    my $filename;
    #
    # Put everything into a report directory under the specified output
    # directory.
    #
    $self->{outdir} = $output;
    my $report_dir = $output;
    system "mkdir -p $report_dir";
    #
    # Get general test information.
    #
    $filename = "$input/readme.txt";
    if (-f $filename) {
        $self->parse_readme($filename);
    }

    #
    # Get database data.  First determine what database was used.
    #
    $filename = "$input/db/readme.txt";
    if (-f $filename) {
        $self->parse_db($filename);
    }
    #
    # Get oprofile data.
    #
    my $oprofile = "$input/oprofile.txt";
    if (-f $oprofile) {
        my $oprofile = new Test::Parser::Oprofile;
        $oprofile->parse($oprofile);
        my $d = $oprofile->data();
        for my $k (keys %$d) {
            $self->{data}->{$k} = $d->{$k};
        }
    }
    #
    # Get readprofile data.
    #
    my $readprofile = "$input/readprofile.txt";
    if (-f $readprofile) {
        my $readprofile = new Test::Parser::Readprofile;
        $readprofile->parse($readprofile);
        my $d = $readprofile->data();
        for my $k (keys %$d) {
            $self->{data}->{$k} = $d->{$k};
        }
    }
    #
    # Get sysctl data.
    #
    my $sysctl = "$input/proc.out";
    if (-f $sysctl) {
        my $sysctl = new Test::Parser::Sysctl;
        $sysctl->parse($sysctl);
        my $d = $sysctl->data();
        for my $k (keys %$d) {
            $self->{data}->{os}->{$k} = [$d->{$k}];
        }
    }
    #
    # Put all the sar plots under a sar directory.
    #
    $self->parse_sar("$input/sar.out", "$report_dir/sar", 'driver');
    $self->parse_sar("$input/db/sar.out", "$report_dir/db/sar", 'db');
    #
    # Put all the vmstat plots under a vmstat directory.
    #
    $self->parse_vmstat("$input/vmstat.out", "$report_dir/vmstat",
            'driver');
    $self->parse_vmstat("$input/db/vmstat.out", "$report_dir/db/vmstat",
            'db');
    #
    # Put all the iostat plots under a iostat directory.
    #
    $self->parse_iostat("$input/iostatx.out", "$report_dir/iostat",
            'driver');
    $self->parse_iostat("$input/db/iostatx.out", "$report_dir/db/iostat",
            'db');

    return 1;
}

sub parse_db {
    my $self = shift;
    my $filename = shift;

    open(FILE, "< $filename");
    my $line = <FILE>;
    close(FILE);
    #
    # Check to see if the parameter output file exists.
    #
    $filename = $self->{outdir} . "/db/param.out";
    if (-f $filename) {
        my $db;
        if ($line =~ /PostgreSQL/) {
            $db = new Test::Parser::PgOptions;
        }
        $db->parse($filename);
        my $d = $db->data();
        for my $k (keys %$d) {
            $self->{data}->{db}->{$k} = $d->{$k};
        }
    }
}


sub parse_readme {
    my $self = shift;
    my $filename = shift;

    open(FILE, "< $filename");
    my $line = <FILE>;
    chomp($line);
    $self->{data}->{date} = $line;

    $line = <FILE>;
    chomp($line);
    $self->{data}->{comment} = $line;

    $line = <FILE>;
    my @i = split / /, $line;
    $self->{data}->{os}{name} = $i[0];
    $self->{data}->{os}{version} = $i[2];

    $self->{data}->{cmdline} = <FILE>;
    chomp($self->{data}->{cmdline});

    $line = <FILE>;
    my @data = split /:/, $line;
    $data[1] =~ s/^\s+//;
    @data = split / /, $data[1];
    $self->{data}->{scale_factor} = $data[0];

    close(FILE);
}

sub parse_iostat {
    my $self = shift;
    my $file = shift;
    my $dir = shift;
    my $system = shift;

    if (-f $file) {
        system "mkdir -p $dir";
        my $iostat = new Test::Parser::Iostat;
        $iostat->outdir($dir);
        $iostat->parse($file);
        my $d = $iostat->data();
        for my $k (keys %$d) {
            $self->{data}->{system}->{$system}->{iostat}->{$k} = $d->{$k};
        }
    }
}

sub parse_sar {
    my $self = shift;
    my $file = shift;
    my $dir = shift;
    my $system = shift;

    my $sar = {};
    if (-f $file) {
        system "mkdir -p $dir";
        my $sar = new Test::Parser::Sar;
        $sar->outdir($dir);
        $sar->parse($file);
        my $d = $sar->data();
        for my $k (keys %$d) {
            $self->{data}->{system}->{$system}->{sar}->{$k} = $d->{$k};
        }
    }
}

sub parse_vmstat {
    my $self = shift;
    my $file = shift;
    my $dir = shift;
    my $system = shift;

    if (-f $file) {
        system "mkdir -p $dir";
        my $vmstat = new Test::Parser::Vmstat;
        $vmstat->outdir($dir);
        $vmstat->parse($file);
        my $d = $vmstat->data();
        for my $k (keys %$d) {
            $self->{data}->{system}->{$system}->{vmstat}->{$k} = $d->{$k};
        }
    }
}

=head3 to_xml()

Returns sar data transformed into XML.

=cut
sub to_xml {
    my $self = shift;
    return XMLout({%{$self->{data}}}, RootName => 'sysstat',
            OutputFile => "$self->{outdir}/result.xml");
}

sub rampup {
    my $self = shift;
    return $self->{data}->{rampup};
}

sub transactions {
    my $self = shift;
    return @{$self->{data}->{transactions}->{transaction}};
}

sub get_90th_per {
    my $self = shift;
    my $index = shift;
    my @data = @_;

    my $result;
    my $floor = floor($index);
    my $ceil = ceil($index);
    if ($floor == $ceil) {
        $result = $data[$index];
    } else {
        if ($data[$ceil]) {
            $result = ($data[$floor] + $data[$ceil]) / 2;
        } else {
            $result = $data[$floor];
        }
    }
    return $result;
}

1;
__END__

=head1 AUTHOR

Mark Wong <markwkm@gmail.com>
 
September 2006
- response time sort to use numeric sort not ascii
- 90th percentile sort to use numeric sort
Richard Kennedy EnterpriseDB

=head1 COPYRIGHT

Copyright (C) 2006-2008 Mark Wong & Open Source Development Labs, Inc.
All Rights Reserved.

This script is free software; you can redistribute it and/or modify it
under the same terms as Perl itself.

=head1 SEE ALSO

L<Test::Parser>

=end

