package Ntse::NtseStatus;

=head1 NAME

Ntse::NtseStatus - Perl module to parse ntse status and generate html report

=head1 SYNOPSIS

=head1 DESCRIPTION

=head1 FUNCTIONS

=cut

use strict;
use warnings;
use CGI;
use CGI::Pretty;
use Sys::Hostname;
use Chart::Graph::Gnuplot qw(gnuplot);

use fields qw(
              caption
              format
              header
              outdir
              infile
);

my @varnames = (
	'Ntse_buf_size',
	'Ntse_buf_log_reads',
	'Ntse_buf_log_writes',
	'Ntse_buf_phy_reads',
	'Ntse_buf_pending_reads',
	'Ntse_buf_read_time',
	'Ntse_buf_phy_writes',
	'Ntse_buf_write_time',
	'Ntse_buf_scavenger_writes',
	'Ntse_buf_flush_writes',
	'Ntse_buf_prefetches',
	'Ntse_buf_batch_prefetches',
	'Ntse_buf_nonbatch_prefetches',
	'Ntse_buf_prefetch_pages',
	'Ntse_buf_page_creates',
	'Ntse_buf_alloc_block_fails',
	'Ntse_buf_replace_searches',
	'Ntse_buf_replace_search_len',
	'Ntse_buf_dirty_pages',
	'Ntse_buf_pinned_pages',
	'Ntse_buf_rlocked_pages',
	'Ntse_buf_wlocked_pages',
	'Ntse_buf_avg_hash_conflict',
	'Ntse_buf_max_hash_conflict',
	'Ntse_buf_unsafe_lock_fails',
	'Ntse_log_writes',
	'Ntse_log_write_size',
	'Ntse_log_flushes',
	'Ntse_log_flush_pages',
	#Ntse_log_tail_lsn,
	#Ntse_log_start_lsn,
	#Ntse_log_checkpoint_lsn,
	#Ntse_log_flushed_lsn,
	'Ntse_mms_size',
	'Ntse_mms_queries',
	'Ntse_mms_query_hits',
	'Ntse_mms_inserts',
	'Ntse_mms_deletes',
	'Ntse_mms_updates',
	'Ntse_mms_rec_replaces',
	'Ntse_mms_page_replaces',
	'Ntse_rows_reads',
	'Ntse_rows_inserts',
	'Ntse_rows_updates',
	'Ntse_rows_deletes',
	'Ntse_rowlock_rlocks',
	'Ntse_rowlock_wlocks',
	'Ntse_rowlock_spins',
	'Ntse_rowlock_waits',
	'Ntse_rowlock_wait_time',
	'Ntse_rowlock_active_readers',
	'Ntse_rowlock_active_writers',
	'Ntse_rowlock_avg_hash_conflict',
	'Ntse_rowlock_max_hash_conflict',
	'Ntse_handler_use_count',
	'Ntse_handler_use_time'
);

sub caption {
    my $self = shift;
    if (@_) {
        $self->{caption} = shift;
    }
    return $self->{caption};
}

sub format {
    my $self = shift;
    if (@_) {
        $self->{format} = shift;
    }
    return $self->{format};
}

sub infile {
    my $self = shift;
    if (@_) {
        $self->{infile} = shift;
    }
    return $self->{infile};
}

sub outdir {
    my $self = shift;
    if (@_) {
        $self->{outdir} = shift;
    }
    return $self->{outdir};
}

sub header {
    my $self = shift;
    if (@_) {
        $self->{header} = shift;
    }
    return $self->{header};
}

sub new {
    my $class = shift;
    my Ntse::NtseStatus $self = fields::new($class);
    $self->{infile} = shift;
    $self->{outdir} = shift;
	#
	# Building an XML hash in memory may not be exactly the same as reading it
	# back from a file.  Compensating...
	#
    $self->{caption} = '';
    $self->{format} = 'png';
    $self->{header} = 1;  
		
    return $self;
}

sub to_html {
    my $self = shift;

    my $dir = $self->{outdir};
    $dir = '.' unless ($dir);

		my $q = new CGI;
		my $h;
		my $temp;
		$temp = $q->caption($self->{caption});		
		$h = $q->start_html('NTSE Status Collecting Report');
    $h .= $q->h1('NTSE Status Collecting Report');
		
		my $var;
		foreach $var (@varnames) {
			$temp .= $q->Tr(
                    	$q->td($var) .
                    	$q->td({align => 'center'},
                            	$self->image_link(
                                    	"ntse_status/$var.$self->{format}"))
            	 );		
   	}
	
		my $currentTime = getTime();
		my $host = hostname;
		my $sh = $q->table($q->Tr($q->td("host: $host")));
		$sh .= $q->p($q->Tr($currentTime));
		
    return $h . $sh . $q->table($temp);
}

sub image_link {
    my $self = shift;
    my $filename = shift;

    my $q = new CGI;
    return $q->a({href => $filename}, $q->img({src => $filename,
        height => 96, width => 128}));
}

sub plot {
  	my $self = shift;
  	
		if (! -e "$self->{outdir}/ntse_status") {
			print "Make directory: $self->{outdir}/ntse_status\n";
			if (! -e $self->{outdir}) {
				mkdir($self->{outdir},0777) || die "can't make directory $self->{outdir}";  
			}
			mkdir("$self->{outdir}/ntse_status",0777) || die "can't make directory $self->{outdir}/ntse_status";  
		}

		my $var;
		foreach $var (@varnames) {	
			my @x = ();
			my @y = ();
			my $period;
			my $line;
								
			open (FILE, $self->{infile}) or die "Couldn't open: $!\n";
			while ( defined( $line=<FILE> ) ) {
				chomp $line;
				$_= $line;
				if (m/$var/) {					
					push @y, (split(/\t/,$line))[1];
					$period++;
					push @x, $period;
				}
			}
			close (FILE) or die "Couldn't close: $!\n";
		
			my %gopts = (
  			'title' => "$var",
    		'yrange' => '[0:]',
    		'x-axis label' => "period",
    		'y-axis label' => "$var",
    		'extra_opts' => 'set grid xtics ytics',
    		'output type' => "png",
    		'output file' => "$self->{outdir}/ntse_status/${var}.png"		
			);
	
			my %ds_opts = (
				'title' => "$var",
  			'type' => 'columns',
  			'style' => 'linespoints'
			);
	
			my @ds = ();	
			push @ds, [\%ds_opts, \@x, \@y];
		
			gnuplot(\%gopts, @ds);
		}
}

sub getTime
{
    my $time = shift || time();
    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime($time);

    $year += 1900;
    $mon ++;

    $min  = '0'.$min  if length($min)  < 2;
    $sec  = '0'.$sec  if length($sec)  < 2;
    $mon  = '0'.$mon  if length($mon)  < 2;
    $mday = '0'.$mday if length($mday) < 2;
    $hour = '0'.$hour if length($hour) < 2;
   
    my $weekday = ('Sun','Mon','Tue','Wed','Thu','Fri','Sat')[$wday];
    
    return "$year-$mon-$mday $hour:$min:$sec $weekday"

    #return { 'second' => $sec,
    #         'minute' => $min,
    #         'hour'   => $hour,
    #         'day'    => $mday,
    #         'month'  => $mon,
    #         'year'   => $year,
    #         'weekNo' => $wday,
    #         'wday'   => $weekday,
    #         'yday'   => $yday,
    #         'date'   => "$year-$mon-$mday"
    #      };
}

1;
__END__

=head1 AUTHOR

Li Weizhao <liweizhao@corp.netease.com>

=head1 COPYRIGHT

Copyright (C) 2009  Netease Corporation.
All Rights Reserved.


=head1 SEE ALSO

L<Ntse::NtseStatus>

=end
